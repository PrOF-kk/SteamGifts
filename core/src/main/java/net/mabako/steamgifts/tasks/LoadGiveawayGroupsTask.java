package net.mabako.steamgifts.tasks;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import net.mabako.Constants;
import net.mabako.steamgifts.data.GiveawayGroup;
import net.mabako.steamgifts.fragments.GiveawayGroupListFragment;
import net.mabako.steamgifts.persistentdata.SteamGiftsUserData;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class LoadGiveawayGroupsTask extends AsyncTask<Void, Void, List<GiveawayGroup>> {
    private static final String TAG = LoadGiveawayGroupsTask.class.getSimpleName();

    private final GiveawayGroupListFragment fragment;
    private final int page;
    private final String path;

    public LoadGiveawayGroupsTask(GiveawayGroupListFragment fragment, int page, String path) {
        this.fragment = fragment;
        this.page = page;
        this.path = path;
    }

    @Override
    protected List<GiveawayGroup> doInBackground(Void... params) {
        Log.d(TAG, "Fetching giveaway groups for page " + page);

        try {
            // Fetch the Giveaway page

            OkHttpClient.Builder client = new OkHttpClient.Builder()
                    .callTimeout(Constants.JSOUP_TIMEOUT, TimeUnit.MILLISECONDS);
            Request.Builder request = new Request.Builder();
            HttpUrl.Builder url = new HttpUrl.Builder()
                    .scheme("https")
                    .host("www.steamgifts.com")
                    .addPathSegment("giveaway")
                    .addPathSegments(path)
                    .addPathSegments("groups/search")
                    .addQueryParameter("page", Integer.toString(page));

            if (SteamGiftsUserData.getCurrent(fragment.getContext()).isLoggedIn()) {
                request.header("Cookie", "PHPSESSID=" + SteamGiftsUserData.getCurrent(fragment.getContext()).getSessionId());
            }

            Document document;
            try (Response response = client.build().newCall(request.url(url.build()).build()).execute()) {
                document = Jsoup.parse(response.body().string());
            }

            SteamGiftsUserData.extract(fragment.getContext(), document);

            // Parse all rows of groups
            Elements groups = document.select(".table__row-inner-wrap");
            Log.d(TAG, "Found inner " + groups.size() + " elements");

            List<GiveawayGroup> groupList = new ArrayList<>(groups.size());
            for (Element element : groups) {
                Element link = element.expectFirst(".table__column__heading");

                // Basic information
                Uri href = Uri.parse(link.attr("href"));
                String title = link.text();
                String id = href.getPathSegments().get(href.getPathSegments().size() - 2);
                if (title.isEmpty())
                    title = href.getLastPathSegment();

                String avatar = null;
                Element avatarNode = element.getElementsByClass("table_image_avatar").first();
                if (avatarNode != null)
                    avatar = Utils.extractAvatar(avatarNode.attr("style"));

                GiveawayGroup group = new GiveawayGroup(id, title, avatar);
                groupList.add(group);
            }

            return groupList;
        } catch (IOException e) {
            Log.e(TAG, "Error fetching URL", e);
            return null;
        }
    }

    @Override
    protected void onPostExecute(List<GiveawayGroup> result) {
        super.onPostExecute(result);
        fragment.addItems(result, page == 1);
    }
}
