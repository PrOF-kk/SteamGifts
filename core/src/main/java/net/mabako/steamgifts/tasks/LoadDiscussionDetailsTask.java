package net.mabako.steamgifts.tasks;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import net.mabako.Constants;
import net.mabako.steamgifts.adapters.EndlessAdapter;
import net.mabako.steamgifts.data.Comment;
import net.mabako.steamgifts.data.Discussion;
import net.mabako.steamgifts.data.DiscussionExtras;
import net.mabako.steamgifts.data.Poll;
import net.mabako.steamgifts.fragments.DiscussionDetailFragment;
import net.mabako.steamgifts.persistentdata.SteamGiftsUserData;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LoadDiscussionDetailsTask extends AsyncTask<Void, Void, DiscussionExtras> {
    private static final String TAG = LoadDiscussionDetailsTask.class.getSimpleName();

    private final DiscussionDetailFragment fragment;
    private String discussionId;
    private int page;
    private final boolean loadDetails;

    private Discussion loadedDetails = null;
    private boolean lastPage = false;

    public LoadDiscussionDetailsTask(DiscussionDetailFragment fragment, String discussionId, int page, boolean loadDetails) {
        this.fragment = fragment;
        this.discussionId = discussionId;
        this.page = page;
        this.loadDetails = loadDetails;
    }

    @Override
    protected DiscussionExtras doInBackground(Void... params) {
        // This can be retried: handle closing the request manually
        try {
            Response response;
            Response initialResponse = connect();
            if (initialResponse.code() == 200) {
                response = initialResponse;
                Uri uri = Uri.parse(response.request().url().toString());
                Log.v(TAG, "Current URI -> " + uri);
                if (uri.getPathSegments().size() < 2)
                    throw new Exception("Could actually not find the discussion, we're at URI " + uri.toString());

                // are we expecting to be on this page? this can be most easily figured out if we check for the last path segment to be "search"
                if (!"search".equals(uri.getLastPathSegment())) {
                    // Let's just try again.
                    discussionId = uri.getPathSegments().get(1) + "/" + uri.getPathSegments().get(2);

                    initialResponse.close();
                    response = connect();
                }


                Document document = Jsoup.parse(response.body().string());
                response.close();

                // Update user details
                SteamGiftsUserData.extract(fragment.getContext(), document);

                DiscussionExtras extras = loadExtras(document);
                if (loadDetails) {
                    loadedDetails = loadDiscussion(document, uri);
                }

                // Do we have a page?
                Element pagination = document.select(".pagination__navigation a").last();
                if (pagination != null) {
                    lastPage = !"Last".equalsIgnoreCase(pagination.text());
                    if (lastPage)
                        page = Integer.parseInt(pagination.attr("data-page-number"));

                } else {
                    // no pagination
                    lastPage = true;
                    page = 1;
                }

                return extras;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error fetching URL", e);
        }
        return null;
    }

    private Response connect() throws IOException {

        // As of 2023-12, pages after the last one return no comments instead of redirecting to the actual last page
        String url = (page != EndlessAdapter.LAST_PAGE)
                ? "https://www.steamgifts.com/discussion/" + discussionId + "/search?page=" + page
                : "https://www.steamgifts.com/discussion/" + discussionId + "/search?page=last";

        Log.v(TAG, "Fetching discussion details for " + url);
        OkHttpClient client = new OkHttpClient.Builder()
                .callTimeout(Constants.JSOUP_TIMEOUT, TimeUnit.MILLISECONDS)
                .followRedirects(true)
                .build();

        Request.Builder request = new Request.Builder().url(url);
        if (SteamGiftsUserData.getCurrent(fragment.getContext()).isLoggedIn())
            request.header("Cookie", "PHPSESSID=" + SteamGiftsUserData.getCurrent(fragment.getContext()).getSessionId());

        return client.newCall(request.build()).execute();
    }

    private Discussion loadDiscussion(Document document, Uri linkUri) {
        Element element = document.expectFirst(".comments");

        // Basic information
        String discussionLink = linkUri.getPathSegments().get(1);
        String discussionName = linkUri.getPathSegments().get(2);

        Discussion discussion = new Discussion(discussionLink);
        discussion.setName(discussionName);
        discussion.setTitle(Utils.getPageTitle(document));

        discussion.setCreator(element.expectFirst(".comment__username a").text());
        discussion.setCreatedTime(Integer.parseInt(element.expectFirst(".comment__actions > div span").attr("data-timestamp")));

        Element headerButton = document.selectFirst(".page__heading__button");
        if (headerButton != null) {
            // remove the dropdown menu.
            headerButton.select(".page__heading__relative-dropdown").html("");

            // Is this button saying 'Closed'?
            discussion.setLocked("Closed".equals(headerButton.text().trim()));
        }

        return discussion;
    }

    @NonNull
    private DiscussionExtras loadExtras(Document document) {
        DiscussionExtras extras = new DiscussionExtras();

        // Load the description
        Element description = document.selectFirst(".comment__display-state .markdown");
        if (description != null) {
            // This will be null if no description is given.
            description.select("blockquote").tagName("custom_quote");
            extras.setDescription(Utils.loadAttachedImages(extras, description));
        }

        // Can we send a comment?
        Element xsrf = document.selectFirst(".comment--submit form input[name=xsrf_token]");
        if (xsrf != null)
            extras.setXsrfToken(xsrf.attr("value"));


        // Load comments
        Elements commentsNode = document.select(".comments");
        if (commentsNode.size() > 1) {
            Element rootCommentNode = commentsNode.last();
            if (rootCommentNode != null)
                Utils.loadComments(rootCommentNode, extras, 0, fragment.getAdapter().isViewInReverse(), false, Comment.Type.COMMENT);
        }

        // Do we have a poll?
        Element pollElement = document.selectFirst(".poll");
        if (pollElement != null) {
            try {
                extras.setPoll(loadPoll(pollElement));
            } catch (Exception e) {
                Log.w(TAG, "unable to load poll", e);
            }
        }

        return extras;
    }

    private Poll loadPoll(Element pollElement) {
        Poll poll = new Poll();

        // Question and Description are actually both within the same element, which makes it a tad confusing.
        // Fetch the question and description
        Elements pollHeader = pollElement.select(".table__heading .table__column--width-fill p");

        // Set the description only, and remove that from the question element
        poll.setDescription(pollHeader.select("span.poll__description").text());
        pollHeader.select("span.poll__description").html("");

        // the remaining text is the question.
        poll.setQuestion(pollHeader.text());

        poll.setClosed(pollElement.selectFirst("form") == null);

        Elements answerElements = pollElement.select(".table__rows div[data-id]");
        for (Element thisAnswer : answerElements) {
            Poll.Answer answer = new Poll.Answer();

            answer.setId(Integer.parseInt(thisAnswer.attr("data-id")));
            answer.setVoteCount(Integer.parseInt(thisAnswer.attr("data-votes")));
            answer.setText(thisAnswer.select(".table__column__heading").text());

            poll.addAnswer(answer, thisAnswer.hasClass("is-selected"));
        }

        Log.d(TAG, poll.toString());

        return poll;
    }

    @Override
    protected void onPostExecute(DiscussionExtras discussionExtras) {
        super.onPostExecute(discussionExtras);

        if (discussionExtras != null || !loadDetails) {
            if (loadDetails)
                fragment.onPostDiscussionLoaded(loadedDetails);

            fragment.addItems(discussionExtras, page, lastPage);
        } else {
            Toast.makeText(fragment.getContext(), "Discussion does not exist or could not be loaded", Toast.LENGTH_LONG).show();
            fragment.getActivity().finish();
        }
    }
}
