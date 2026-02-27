package net.mabako.steamgifts.tasks;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import net.mabako.Constants;
import net.mabako.steamgifts.data.Giveaway;
import net.mabako.steamgifts.fragments.GiveawayListFragment;
import net.mabako.steamgifts.persistentdata.FilterData;
import net.mabako.steamgifts.persistentdata.SteamGiftsUserData;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LoadGiveawayListTask extends AsyncTask<Void, Void, List<Giveaway>> {
    private static final String TAG = LoadGiveawayListTask.class.getSimpleName();

    private final GiveawayListFragment fragment;
    private final int page;
    private final GiveawayListFragment.Type type;
    private final @Nullable String searchQuery;
    private final boolean showPinnedGiveaways;

    private String foundXsrfToken = null;

    public LoadGiveawayListTask(GiveawayListFragment activity, int page, GiveawayListFragment.Type type, @Nullable String searchQuery, boolean showPinnedGiveaways) {
        this.fragment = activity;
        this.page = page;
        this.type = type;
        this.searchQuery = searchQuery;
        this.showPinnedGiveaways = showPinnedGiveaways && type == GiveawayListFragment.Type.ALL && TextUtils.isEmpty(searchQuery);
    }

    @Override
    protected List<Giveaway> doInBackground(Void... params) {
        Log.d(TAG, "Fetching giveaways for page " + page);

        try {
            // Fetch the Giveaway page

            OkHttpClient.Builder client = new OkHttpClient.Builder()
                    .callTimeout(Constants.JSOUP_TIMEOUT, TimeUnit.MILLISECONDS);
            Request.Builder request = new Request.Builder();
            HttpUrl.Builder url = new HttpUrl.Builder()
                    .scheme("https")
                    .host("www.steamgifts.com")
                    .addPathSegment("giveaways")
                    .addPathSegment("search")
                    .addQueryParameter("page", Integer.toString(page));

            if (searchQuery != null) {
                url.addQueryParameter("q", searchQuery);
            }

            FilterData filterData = FilterData.getCurrent(fragment.getContext());
            if (!filterData.isEntriesPerCopy()) {
                addFilterParameter(url, "entry_max", filterData.getMaxEntries());
                addFilterParameter(url, "entry_min", filterData.getMinEntries());
            }
            if (!filterData.isRestrictLevelOnlyOnPublicGiveaways()) {
                addFilterParameter(url, "level_min", filterData.getMinLevel());
                addFilterParameter(url, "level_max", filterData.getMaxLevel());
            }
            addFilterParameter(url, "region_restricted", filterData.isRegionRestrictedOnly());
            addFilterParameter(url, "copy_min", filterData.getMinCopies());
            addFilterParameter(url, "copy_max", filterData.getMaxCopies());
            addFilterParameter(url, "point_min", filterData.getMinPoints());
            addFilterParameter(url, "point_max", filterData.getMaxPoints());

            if (type != GiveawayListFragment.Type.ALL) {
                url.addQueryParameter("type", type.name().toLowerCase(Locale.ENGLISH));
            }

            // Add PHPSESSID cookie
            if (SteamGiftsUserData.getCurrent(fragment.getContext()).isLoggedIn()) {
                request.header("Cookie", "PHPSESSID=" + SteamGiftsUserData.getCurrent(fragment.getContext()).getSessionId());
            }

            Document document;
            try (Response response = client.build().newCall(request.url(url.build()).build()).execute()) {
                document = Jsoup.parse(response.body().string());
            }

            SteamGiftsUserData.extract(fragment.getContext(), document);

            // Fetch the xsrf token
            Element xsrfToken = document.selectFirst("input[name=xsrf_token]");
            if (xsrfToken != null)
                foundXsrfToken = xsrfToken.attr("value");

            // Do away with pinned giveaways.
            if (!showPinnedGiveaways)
                document.select(".pinned-giveaways__outer-wrap").html("");

            // Parse all rows of giveaways
            return Utils.loadGiveawaysFromList(document);
        } catch (Exception e) {
            Log.e(TAG, "Error fetching URL", e);
            return null;
        }
    }

    @Override
    protected void onPostExecute(List<Giveaway> result) {
        super.onPostExecute(result);
        fragment.addItems(result, page == 1, foundXsrfToken);
    }

    private void addFilterParameter(HttpUrl.Builder url, String parameterName, int value) {
        if (value >= 0) {
            url.addQueryParameter(parameterName, String.valueOf(value));
        }
    }

    private void addFilterParameter(HttpUrl.Builder url, String parameterName, boolean value) {
        if (value) {
            url.addQueryParameter(parameterName, "true");
        }
    }
}
