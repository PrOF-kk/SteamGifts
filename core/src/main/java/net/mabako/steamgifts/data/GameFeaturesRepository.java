package net.mabako.steamgifts.data;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import net.mabako.common.OkHttpFutureCallback;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;

public final class GameFeaturesRepository {
    public static final String TAG = GameFeaturesRepository.class.getSimpleName();

    // Do not use static initialization, it hangs OkHttp when waiting on the futures
    private static GameFeaturesRepository instance;

    private static File cacheDir;
    private static boolean loadGameFeatures;
    private static boolean firstInitDone = false;

    private static CompletableFuture<GameFeaturesRepository> downloadGameFeatures;

    private final Map<Integer, GameFeatures> data;

    /// Load initial state from shared preferences, on user preference change call setLoadGameFeatures directly
    /// to avoid needing to pass the Context around or keep a reference
    public static void firstInit(Context context) {
        cacheDir = context.getCacheDir();
        loadGameFeatures = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("preference_giveaway_show_game_features", true);
        firstInitDone = true;
    }

    public static void setLoadGameFeatures(boolean load) {
        loadGameFeatures = load;
        instance = null;
        downloadGameFeatures = null;
    }

    public static @NonNull CompletableFuture<GameFeatures> getGameFeaturesAsync(int gameId) {
        return downloadGameFeaturesAsync().thenApply(gameFeaturesRepository -> gameFeaturesRepository.data.getOrDefault(gameId, new GameFeatures()));
    }

    /// This future and futures chained to it shouldn't be saved to a variable outside of this class:
    /// CompletableFutures run at most once and after that just return the completion value,
    /// so it would return null once the user changes the preference.
    private static CompletableFuture<GameFeaturesRepository> downloadGameFeaturesAsync() {
        if (downloadGameFeatures == null) {
            downloadGameFeatures = CompletableFuture.supplyAsync(() -> {
                instance = new GameFeaturesRepository();
                return instance;
            });
        }
        return downloadGameFeatures;
    }


    private GameFeaturesRepository() {
        if (!firstInitDone) {
            throw new IllegalStateException("GameFeaturesRepository must be initialized before use");
        }
        data = new ConcurrentHashMap<>();
        if (loadGameFeatures) {
            downloadAppFeatures();
        }
    }

    private void downloadAppFeatures() {
        Log.d(TAG, "Initializing GameFeaturesRepository");

        // Data sources are the same used by SteamWebIntegration
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .cache(new Cache(
                        new File(cacheDir, "http_cache"),
                        10 * 1024 * 1024
                ))
                .build();

        var cardsFuture = new OkHttpFutureCallback<Void>((call, response) -> {
            try (ResponseBody body = response.body()) {
                /*
                {
                  "246420": {
                    "cards": 5,
                    "bundles": 9,
                    "marketable": true
                  },
                  ...
                }
                 */
                Log.d(TAG, "Cards: " + response + (response.cacheResponse() != null ? " (from cache)" : ""));
                JSONObject json = new JSONObject(body.string());
                for (Iterator<String> it = json.keys(); it.hasNext(); ) {
                    String gameIdStr = it.next();
                    JSONObject obj = json.getJSONObject(gameIdStr);
                    if (obj.getBoolean("marketable")) {
                        int gameId = Integer.parseInt(gameIdStr);
                        int cardCount = obj.getInt("cards");
                        data.putIfAbsent(gameId, new GameFeatures());
                        data.get(gameId).setCards(cardCount);
                    }
                }
                Log.d(TAG, "Loaded Cards");
            } catch (Exception e) {
                // ignore
                Log.e(TAG, "Failed loading card appids", e);
            }
            return null;
        });
        client.newCall(new Request.Builder().url("https://bartervg.com/browse/cards/json/").build())
                .enqueue(cardsFuture);

        var dlcFuture = new OkHttpFutureCallback<Void>((call, response) -> {
            try (ResponseBody body = response.body()) {
                /*
                {
                  "239550": {
                    "base_appID": 221380,
                    "base_item_id": 125
                  },
                  ...
                }
                 */
                Log.d(TAG, "DLC: " + response + (response.cacheResponse() != null ? " (from cache)" : ""));
                JSONObject json = new JSONObject(body.string());
                for (Iterator<String> it = json.keys(); it.hasNext(); ) {
                    String gameIdStr = it.next();
                    int gameId = Integer.parseInt(gameIdStr);
                    data.putIfAbsent(gameId, new GameFeatures());
                    data.get(gameId).setDlc(true);
                }
                Log.d(TAG, "Loaded DLC");
            } catch (Exception e) {
                // ignore
                Log.e(TAG, "Failed loading DLC appids", e);
            }
            return null;
        });
        client.newCall(new Request.Builder().url("https://bartervg.com/browse/dlc/json/").build())
                .enqueue(dlcFuture);

        var limitedFuture = new OkHttpFutureCallback<Void>((call, response) -> {
            try (ResponseBody body = response.body()) {
                /*
                {
                  "640260": {
                    "item_id": 35428
                },
                ...
                 */
                Log.d(TAG, "Limited: " + response + (response.cacheResponse() != null ? " (from cache)" : ""));
                JSONObject json = new JSONObject(body.string());
                for (Iterator<String> it = json.keys(); it.hasNext(); ) {
                    String gameIdStr = it.next();
                    int gameId = Integer.parseInt(gameIdStr);
                    data.putIfAbsent(gameId, new GameFeatures());
                    data.get(gameId).setLimited(true);
                }
                Log.d(TAG, "Loaded Limited");
            } catch (Exception e) {
                // ignore
                Log.e(TAG, "Failed loading limited appids", e);
            }
            return null;
        });
        client.newCall(new Request.Builder().url("https://bartervg.com/browse/tag/481/json").build())
                .enqueue(limitedFuture);

        var delistedFuture = new OkHttpFutureCallback<Void>((call, response) -> {
            try (ResponseBody body = response.body()) {
                /*
                {
                  "success": true,
                  "removed_apps": [
                    {
                      "appid": 648490,
                      "name": null,
                      "category": "Series episode",
                      "category_id": 28,
                      "type": "video",
                      "count": 13,
                      "changed_at": "2022-12-08"
                    },
                    ...
                  ]
                }
                 */
                Log.d(TAG, "Delisted: " + response + (response.cacheResponse() != null ? " (from cache)" : ""));
                JSONArray json = new JSONObject(body.string()).getJSONArray("removed_apps");
                for (int i = 0; i < json.length(); i++) {
                    int gameId = json.getJSONObject(i).getInt("appid");
                    data.putIfAbsent(gameId, new GameFeatures());
                    data.get(gameId).setDelisted(true);
                }
                Log.d(TAG, "Loaded Delisted");
            } catch (Exception e) {
                // ignore
                Log.e(TAG, "Failed loading delisted appids", e);
            }
            return null;
        });
        client.newCall(new Request.Builder().url("https://steam-tracker.com/api?action=GetAppList").build())
                .enqueue(delistedFuture);

        CompletableFuture.allOf(cardsFuture, dlcFuture, limitedFuture, delistedFuture).join();

        Log.d(TAG, "Loaded " + data.size() + " game features");
    }
}

