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
import net.mabako.steamgifts.data.GameFeatures;
import net.mabako.steamgifts.data.GameFeaturesRepository;

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
        int appId = Integer.parseInt(getArguments().getString("app"));
        GameFeatures gameFeatures = GameFeaturesRepository.waitForGameFeaturesDownload().join().getGameFeatures(appId);

        return gameFeatures.isDelisted() ? new ShowDelistedAppTask() : new LoadAppTask();
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
                        .followRedirects(true)
                        .execute();

                responseCode = response.statusCode();

                if (responseCode == 200) {
                    Document document = response.parse();

                    // Game description
                    Element description = document.getElementById("game_area_description");
                    if (description != null)
                        addDescription(description);

                    // Space!
                    items.add(new Space());

                    // All reviews
                    Element allReviews = document.getElementsByClass("game_review_summary").first();
                    if (allReviews != null)
                        items.add(new Text("<strong>All Reviews:</strong> " + allReviews.ownText(), true));

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
            if (responseCode != 200) {
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

    private class ShowDelistedAppTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) { return null; }

        @Override
        protected void onPostExecute(Void unused) {
            getView().findViewById(R.id.progressBar).setVisibility(View.GONE);
            addItems(List.of(
                    new Text("This app has been retired and is no longer available on the Steam store.", false),
                    new Text("Click <a href='https://steamdb.info/app/" + getArguments().getString("app") + "/'>HERE</a> to visit its SteamDB page", true)
                    ), true);
        }
    }
}
