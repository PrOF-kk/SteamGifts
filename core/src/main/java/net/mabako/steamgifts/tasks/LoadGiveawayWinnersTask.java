package net.mabako.steamgifts.tasks;

import android.os.AsyncTask;
import android.util.Log;

import net.mabako.Constants;
import net.mabako.steamgifts.data.Winner;
import net.mabako.steamgifts.fragments.GiveawayWinnerListFragment;
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


public class LoadGiveawayWinnersTask extends AsyncTask<Void, Void, List<Winner>> {
    private static final String TAG = LoadGiveawayGroupsTask.class.getSimpleName();

    private final GiveawayWinnerListFragment fragment;
    private final int page;
    private final String path;

    public LoadGiveawayWinnersTask(GiveawayWinnerListFragment fragment, int page, String path) {
        this.fragment = fragment;
        this.page = page;
        this.path = path;
    }

    @Override
    protected List<Winner> doInBackground(Void... params) {
        Log.d(TAG, "Fetching giveaways for page " + page);

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
                    .addPathSegment("winners")
                    .addPathSegment("search")
                    .addQueryParameter("page", Integer.toString(page));

            if (SteamGiftsUserData.getCurrent(fragment.getContext()).isLoggedIn())
                request.header("Cookie", "PHPSESSID=" + SteamGiftsUserData.getCurrent(fragment.getContext()).getSessionId());

            Document document;
            try (Response response = client.build().newCall(request.url(url.build()).build()).execute()) {
                document = Jsoup.parse(response.body().string());
            }

            SteamGiftsUserData.extract(fragment.getContext(), document);

            return loadAll(document);
        } catch (IOException e) {
            Log.e(TAG, "Error fetching URL", e);
            return null;
        }
    }

    @Override
    protected void onPostExecute(List<Winner> result) {
        super.onPostExecute(result);
        fragment.addItems(result, page == 1);
    }

    private List<Winner> loadAll(Document document) {
        Elements users = document.select(".table__row-inner-wrap");
        List<Winner> userList = new ArrayList<>(users.size());

        for (Element element : users) {
            userList.add(load(element));
        }
        return userList;
    }

    private Winner load(Element element) {
        Winner user = new Winner();

        user.setName(element.select(".table__column__heading").text());
        user.setAvatar(Utils.extractAvatar(element.select(".table_image_avatar").attr("style")));
        user.setStatus(element.select(".table__column--width-small.text-center").last().text());

        return user;
    }
}
