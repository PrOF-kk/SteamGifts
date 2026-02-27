package net.mabako.steamgifts.tasks;

import android.os.AsyncTask;
import android.util.Log;

import net.mabako.Constants;
import net.mabako.steamgifts.data.Giveaway;
import net.mabako.steamgifts.data.User;
import net.mabako.steamgifts.fragments.UserDetailFragment;
import net.mabako.steamgifts.persistentdata.SteamGiftsUserData;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LoadUserDetailsTask extends AsyncTask<Void, Void, List<Giveaway>> {
    private static final String TAG = LoadUserDetailsTask.class.getSimpleName();

    private final UserDetailFragment.UserGiveawayListFragment fragment;
    private final String path;
    private final int page;
    private final User user;
    private String foundXsrfToken;

    public LoadUserDetailsTask(UserDetailFragment.UserGiveawayListFragment fragment, String path, int page, User user) {
        this.fragment = fragment;
        this.path = path;
        this.page = page;
        this.user = user;
    }

    @Override
    protected List<Giveaway> doInBackground(Void... params) {
        Log.d(TAG, "Fetching giveaways for user " + path + " on page " + page);

        try {
            // Fetch the Giveaway page

            OkHttpClient.Builder client = new OkHttpClient.Builder()
                    .callTimeout(Constants.JSOUP_TIMEOUT, TimeUnit.MILLISECONDS);
            Request.Builder request = new Request.Builder();
            HttpUrl.Builder url = new HttpUrl.Builder()
                    .scheme("https")
                    .host("www.steamgifts.com")
                    .addPathSegment("user")
                    .addPathSegments(path)
                    .addPathSegment("search")
                    .addQueryParameter("page", Integer.toString(page));

            if (SteamGiftsUserData.getCurrent(fragment.getContext()).isLoggedIn()) {
                request.header("Cookie", "PHPSESSID=" + SteamGiftsUserData.getCurrent(fragment.getContext()).getSessionId());
                client.followRedirects(false);
            }

            try (Response response = client.build().newCall(request.url(url.build()).build()).execute()) {
                Document document = Jsoup.parse(response.body().string());

                if (response.code() == 200) {

                    SteamGiftsUserData.extract(fragment.getContext(), document);

                    if (!user.isLoaded())
                        foundXsrfToken = Utils.loadUserProfile(user, document);

                    // Parse all rows of giveaways
                    return Utils.loadGiveawaysFromList(document);
                } else {
                    Log.w(TAG, "Got status code " + response.code());
                    return null;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error fetching URL", e);
            return null;
        }
    }

    @Override
    protected void onPostExecute(List<Giveaway> result) {
        super.onPostExecute(result);

        if (!user.isLoaded() && result != null) {
            user.setLoaded(true);
            fragment.onUserUpdated(user);
        }

        fragment.addItems(result, page == 1, foundXsrfToken);
    }
}
