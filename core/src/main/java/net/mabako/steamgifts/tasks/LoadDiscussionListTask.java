package net.mabako.steamgifts.tasks;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import net.mabako.Constants;
import net.mabako.steamgifts.data.Discussion;
import net.mabako.steamgifts.fragments.DiscussionListFragment;
import net.mabako.steamgifts.persistentdata.SteamGiftsUserData;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Fetch a list of all discussions.
 */
public class LoadDiscussionListTask extends AsyncTask<Void, Void, List<Discussion>> {
    private static final String TAG = LoadGiveawayListTask.class.getSimpleName();

    private final DiscussionListFragment fragment;
    private final int page;
    private final DiscussionListFragment.Type type;
    private final DiscussionListFragment.Sort sort;
    private final String searchQuery;

    public LoadDiscussionListTask(DiscussionListFragment fragment, int page, DiscussionListFragment.Type type, DiscussionListFragment.Sort sort, String searchQuery) {
        this.fragment = fragment;
        this.page = page;
        this.type = type;
        this.sort = sort;
        this.searchQuery = searchQuery;
    }

    @Override
    protected List<Discussion> doInBackground(Void... params) {
        try {
            // Fetch the Discussions page
            String segment = "";
            if (type != DiscussionListFragment.Type.ALL)
                segment = type.name().replace("_", "-").toLowerCase(Locale.ENGLISH) + "/";
            String url = "https://www.steamgifts.com/discussions/" + segment + "search";

            Log.d(TAG, "Fetching discussions for page " + page + " and URL " + url);

            Connection jsoup = Jsoup.connect(url)
                    .userAgent(Constants.JSOUP_USER_AGENT)
                    .timeout(Constants.JSOUP_TIMEOUT);
            jsoup.data("page", Integer.toString(page));

            if (sort == DiscussionListFragment.Sort.NEW)
                jsoup.data("sort", "new");
            if (searchQuery != null)
                jsoup.data("q", searchQuery);

            // We do not want to follow redirects here, because SteamGifts redirects to the main (giveaways) page if we're not logged in.
            // For all other pages however, if we're not logged in, we're redirected once as well?
            if (type == DiscussionListFragment.Type.CREATED)
                jsoup.followRedirects(false);

            if (SteamGiftsUserData.getCurrent(fragment.getContext()).isLoggedIn())
                jsoup.cookie("PHPSESSID", SteamGiftsUserData.getCurrent(fragment.getContext()).getSessionId());
            Document document = jsoup.get();

            SteamGiftsUserData.extract(fragment.getContext(), document);

            // Parse all rows of discussions
            Elements discussions = document.select(".table__row-inner-wrap");
            Log.d(TAG, "Found inner " + discussions.size() + " elements");

            List<Discussion> discussionList = new ArrayList<>(discussions.size());
            for (Element element : discussions) {
                Element link = element.expectFirst("h3 a");

                // Basic information
                Uri uri = Uri.parse(link.attr("href"));
                String discussionId = uri.getPathSegments().get(1);
                String discussionName = uri.getPathSegments().get(2);

                Discussion discussion = new Discussion(discussionId);
                discussion.setTitle(link.text());
                discussion.setName(discussionName);

                Element p = element.expectFirst(".table__column--width-fill p");
                discussion.setCreatedTime(Integer.parseInt(p.expectFirst("span").attr("data-timestamp")));
                discussion.setCreator(p.select("a").last().text());

                // The creator's avatar
                Element avatarNode = element.selectFirst(".table_image_avatar");
                if (avatarNode != null)
                    discussion.setCreatorAvatar(Utils.extractAvatar(avatarNode.attr("style")));

                discussion.setLocked(element.hasClass("is-faded"));
                // Discussion title has the poll or pinned icon
                discussion.setPoll(element.selectFirst("h3 i.fa-align-left") != null);
                discussion.setPinned(element.selectFirst("h3 i.fa-long-arrow-right") != null);
                discussionList.add(discussion);
            }
            return discussionList;
        } catch (Exception e) {
            Log.e(TAG, "Error fetching URL", e);
            return null;
        }
    }

    @Override
    protected void onPostExecute(List<Discussion> result) {
        super.onPostExecute(result);
        fragment.addItems(result, page == 1);
    }
}
