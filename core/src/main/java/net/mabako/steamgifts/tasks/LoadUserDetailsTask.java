package net.mabako.steamgifts.tasks;

import android.os.AsyncTask;
import android.util.Log;

import net.mabako.steamgifts.data.Giveaway;
import net.mabako.steamgifts.data.User;
import net.mabako.steamgifts.fragments.UserDetailFragment;
import net.mabako.steamgifts.http.OkHttp;
import net.mabako.steamgifts.persistentdata.SteamGiftsUserData;

import org.jsoup.nodes.Document;

import java.util.List;

import okhttp3.HttpUrl;
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
            var client = OkHttp.client();
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
            }

            try (Response response = client.newCall(request.url(url.build()).build()).execute()) {
                Document document = OkHttp.parseJsoup(response);

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
