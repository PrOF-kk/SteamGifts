package net.mabako.steamgifts.tasks;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import net.mabako.Constants;
import net.mabako.steamgifts.data.GiveawayGroup;
import net.mabako.steamgifts.fragments.GiveawayGroupListFragment;
import net.mabako.steamgifts.persistentdata.SteamGiftsUserData;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


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

            Connection jsoup = Jsoup.connect("https://www.steamgifts.com/giveaway/" + path + "/groups/search")
                    .userAgent(Constants.JSOUP_USER_AGENT)
                    .timeout(Constants.JSOUP_TIMEOUT);
            jsoup.data("page", Integer.toString(page));

            if (SteamGiftsUserData.getCurrent(fragment.getContext()).isLoggedIn())
                jsoup.cookie("PHPSESSID", SteamGiftsUserData.getCurrent(fragment.getContext()).getSessionId());
            Document document = jsoup.get();

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
