package net.mabako.steam.store;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import net.mabako.Constants;
import net.mabako.steam.store.data.Picture;
import net.mabako.steam.store.data.Space;
import net.mabako.steam.store.data.Text;
import net.mabako.steamgifts.adapters.IEndlessAdaptable;
import net.mabako.steamgifts.core.R;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class StoreAppFragment extends StoreFragment {
    private static final String TAG = StoreAppFragment.class.getSimpleName();

    public static StoreAppFragment newInstance(int appId, boolean refreshOnCreate) {
        StoreAppFragment fragment = new StoreAppFragment();

        Bundle args = new Bundle();
        args.putString("app", String.valueOf(appId));
        args.putBoolean("refresh", refreshOnCreate);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getArguments().getBoolean("refresh", false))
            refresh();
    }

    @Override
    protected AsyncTask<Void, Void, ?> getFetchItemsTask(int page) {
        return new LoadAppTask();
    }

    private class LoadAppTask extends AsyncTask<Void, Void, Void> {
        int responseCode;
        List<IEndlessAdaptable> items = new ArrayList<>();
        @Override
        protected Void doInBackground(Void... params) {
            try {
                Connection.Response response = Jsoup
                        .connect("https://store.steampowered.com/app/" + getArguments().getString("app"))
                        .userAgent(Constants.JSOUP_USER_AGENT)
                        .timeout(Constants.JSOUP_TIMEOUT)
                        // Bypass age check
                        .cookie("birthtime", "0")
                        .followRedirects(false)
                        .execute();

                responseCode = response.statusCode();

                if (responseCode != 200) {
                    // Store page unavailable
                    items.add(new Text("The Steam store page for this app is not available.", false));
                    items.add(new Text("Click <a href='https://steamdb.info/app/" + getArguments().getString("app") + "/'>HERE</a> to visit its SteamDB page instead.", true));
                } else {
                    Document document = response.parse();

                    // Game description
                    Element description = document.getElementById("game_area_description");
                    if (description != null)
                        addDescription(description);

                    // Space!
                    items.add(new Space());

                    // All reviews
                    Element allReviewsDesc = document.select("[itemprop=aggregateRating] [itemprop=description]").first();
                    if (allReviewsDesc != null) {
                        // "All Reviews: (Negative/Positive/X reviews)"
                        String line = "<strong>All Reviews:</strong> " + allReviewsDesc.ownText();

                        Element allReviewsScore = allReviewsDesc.siblingElements().select(".responsive_reviewdesc").first();
                        if (allReviewsScore != null) {
                            String allReviewsScoreText = allReviewsScore.ownText();
                            if (allReviewsScoreText.indexOf('%') != -1) {
                                // "- X% of the Y user reviews for this game are positive."
                                String score = allReviewsScoreText.substring(2, allReviewsScoreText.indexOf('%') + 1);
                                // -> " (X% positive)"
                                line += " (" + score + " positive)";
                            }
                            // else "- Need more user reviews to generate a score"
                        }
                        items.add(new Text(line, true));
                    }

                    // Release date
                    Element releaseDate = document.getElementsByClass("date").first();
                    if (releaseDate != null)
                        items.add(new Text("<strong>Release Date:</strong> " + releaseDate.ownText(), true));

                    // Developer
                    Element developers = document.getElementById("developers_list");
                    if (developers != null)
                        items.add(new Text("<strong>Developer:</strong> " + developers.child(0).ownText(), true));

                    // Tags
                    Elements tags = document.getElementsByClass("app_tag");
                    String tagString = tags.stream()
                            .limit(5)
                            .map(Element::ownText)
                            .collect(Collectors.joining(", "));
                    items.add(new Text("<strong>Tags:</strong> " + tagString, true));

                    // Space!
                    items.add(new Space());

                    // Screenshots
                    Elements screenshots = document.getElementsByClass("highlight_screenshot_link");
                    screenshots.forEach(a -> items.add(new Picture(a.attr("href").replace("1920x1080", "800x600"), false)));
                }

                return null;
            } catch (Exception e) {
                Log.e(TAG, "Exception during loading store app", e);
                responseCode = 0;
                return null;
            }
        }

        @Override
        protected void onPostExecute(Void result) {
            getView().findViewById(R.id.progressBar).setVisibility(View.GONE);
            if (responseCode / 100 == 5) {
                // Error 5XX
                Toast.makeText(getContext(), "Unable to load Store App", Toast.LENGTH_LONG).show();
                return;
            }
            addItems(items, true);
        }

        private void addDescription(@NonNull Element description) {

            // Avoid creating many Texts, combine them if possible
            StringBuilder currentText = new StringBuilder();

            for (Node node : description.childNodes()) {
                if (!(node instanceof Element element)) {
                    // Text node
                    currentText.append(node.outerHtml());
                    continue;
                }

                if (element.tagName().equals("img")) {
                    // Normal image
                    flushText(currentText);
                    items.add(new Picture(element.attr("src"), true));
                } else {
                    if (!element.getElementsByTag("img").isEmpty()) {
                        // Image inside <a>, most likely
                        flushText(currentText);
                        items.add(new Text(element.outerHtml(), true));
                    } else {
                        // Other element
                        currentText.append(element.outerHtml());
                    }
                }
            }

            // Remaining text
            if (currentText.length() > 0)
                items.add(new Text(currentText.toString(), true));
        }

        private void flushText(StringBuilder sb) {
            if (sb.length() > 0) {
                items.add(new Text(sb.toString(), true));
                // Clear StringBuilder contents
                sb.setLength(0);
            }
        }
    }
}
