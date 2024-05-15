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

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;

public final class GameFeaturesRepository {
    public static final String TAG = GameFeaturesRepository.class.getSimpleName();

    // NB: do not use static initialization, it hangs OkHttp when waiting on the futures
    private static GameFeaturesRepository instance;

    private static File cacheDir;
    private static boolean loadGameFeatures;
    private static boolean initDone = false;

    private static CompletableFuture<GameFeaturesRepository> downloadGameFeatures;

    private final Map<Integer, GameFeatures> data;

    private GameFeaturesRepository() {
        if (!initDone) {
            throw new IllegalStateException("GameFeaturesRepository must be initialized before use");
        }
        data = new ConcurrentHashMap<>();
        if (loadGameFeatures) {
            initMapFromNetwork();
        }
    }

    public static void init(Context context) {
        cacheDir = context.getCacheDir();
        loadGameFeatures = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("preference_giveaway_show_game_features", true);
        initDone = true;
    }

    private void initMapFromNetwork() {
        Log.d(TAG, "Initializing GameFeaturesRepository");

        // Data sources are the same used by SteamWebIntegration
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
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
                Log.d(TAG, "Cards: " + response);
                JSONObject json = new JSONObject(body.string());
                for (Iterator<String> it = json.keys(); it.hasNext(); ) {
                    String gameIdStr = it.next();
                    int gameId = Integer.parseInt(gameIdStr);
                    int cardCount = json.getJSONObject(gameIdStr).getInt("cards");
                    data.putIfAbsent(gameId, new GameFeatures());
                    data.get(gameId).setCards(cardCount);
                }
                Log.d(TAG, "Loaded Cards");
            } catch (Exception e) {
                // ignore
                Log.e(TAG, e.getMessage());
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
                Log.d(TAG, "DLC: " + response);
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
                Log.e(TAG, e.getMessage());
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
                Log.d(TAG, "Limited: " + response);
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
                Log.e(TAG, e.getMessage());
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
                Log.d(TAG, "Delisted: " + response.toString());
                JSONArray json = new JSONObject(body.string()).getJSONArray("removed_apps");
                for (int i = 0; i < json.length(); i++) {
                    int gameId = json.getJSONObject(i).getInt("appid");
                    data.putIfAbsent(gameId, new GameFeatures());
                    data.get(gameId).setDelisted(true);
                }
                Log.d(TAG, "Loaded Delisted");
            } catch (Exception e) {
                // ignore
                Log.e(TAG, e.getMessage());
            }
            return null;
        });
        client.newCall(new Request.Builder().url("https://steam-tracker.com/api?action=GetAppListV3").build())
                .enqueue(delistedFuture);

        CompletableFuture.allOf(cardsFuture, dlcFuture, limitedFuture, delistedFuture).join();

        Log.d(TAG, "Loaded " + data.size() + " game features");
    }

    private static GameFeaturesRepository getInstance() {
        if (instance == null) {
            instance = new GameFeaturesRepository();
        }
        return instance;
    }

    public @NonNull GameFeatures getGameFeatures(int gameId) {
        return data.getOrDefault(gameId, new GameFeatures());
    }

    public static void setLoadGameFeatures(boolean load) {
        loadGameFeatures = load;
        instance = null;
    }

    public static CompletableFuture<GameFeaturesRepository> waitForGameFeaturesDownload() {
        if (downloadGameFeatures == null) {
            downloadGameFeatures = CompletableFuture.supplyAsync(GameFeaturesRepository::getInstance);
        }
        return downloadGameFeatures;
    }
}

