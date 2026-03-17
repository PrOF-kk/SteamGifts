package net.mabako.steamgifts.data;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import net.mabako.Constants;
import net.mabako.common.OkHttpFutureCallback;
import net.mabako.steamgifts.ApplicationTemplate;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.Collections;
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
    private static final String TAG = GameFeaturesRepository.class.getSimpleName();
    private static final CompletableFuture<Map<Integer, GameFeatures>> GET_EMPTY_MAP = CompletableFuture.completedFuture(Collections.emptyMap());
    private static final CompletableFuture<GameFeatures> GET_EMPTY_FEATURES = CompletableFuture.completedFuture(new GameFeatures());

    // Do not use static initialization, it hangs OkHttp when waiting on the futures
    private static GameFeaturesRepository instance;

    private final OkHttpClient client;
    private final Map<Integer, GameFeatures> appGameFeatures = new ConcurrentHashMap<>();

    private boolean loadGameFeatures;
    private CompletableFuture<Map<Integer, GameFeatures>> downloadAppGameFeatures;

    /// Load initial state from shared preferences, on user preference change call setLoadGameFeatures directly
    /// to avoid needing to pass the Context around or keep a reference
    public static void firstInit(Context context) {
        instance = new GameFeaturesRepository(context);
        Log.d(TAG, "Initialized GameFeaturesRepository");
    }

    public static GameFeaturesRepository getInstance() {
        if (instance == null) {
            throw new IllegalStateException("GameFeaturesRepository must be initialized before use");
        }
        return instance;
    }

    private GameFeaturesRepository(Context context) {
        loadGameFeatures = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("preference_giveaway_show_game_features", true);
        client = new OkHttpClient.Builder()
                .connectTimeout(Constants.HTTP_TIMEOUT, TimeUnit.MILLISECONDS)
                .cache(new Cache(
                        new File(context.getCacheDir(), "http_cache"),
                        10 * 1024 * 1024
                ))
                .build();

        // Start downloading immediately if enabled
        // Don't for chocolate: the unhandled exception handler causes 2 processes to start, avoid double fetch
        if (loadGameFeatures && !((ApplicationTemplate) context.getApplicationContext()).getFlavor().equals("chocolate")) {
            fetchAppGameFeaturesAsync();
        }
    }

    public void setLoadGameFeatures(boolean load) {
        loadGameFeatures = load;
    }

    public @NonNull CompletableFuture<GameFeatures> getGameFeaturesAsync(Game game) {
        if (game.getType() == Game.Type.APP) {
            return fetchAppGameFeaturesAsync().thenApply(map -> map.getOrDefault(game.getId(), new GameFeatures()));
        }
        return GET_EMPTY_FEATURES;
    }

    private @NonNull CompletableFuture<Map<Integer, GameFeatures>> fetchAppGameFeaturesAsync() {
        if (!loadGameFeatures) {
            return GET_EMPTY_MAP;
        }
        if (downloadAppGameFeatures == null) {
            downloadAppGameFeatures = CompletableFuture.supplyAsync(this::fetchAppFeatures);
        }
        return downloadAppGameFeatures;
    }

    private Map<Integer, GameFeatures> fetchAppFeatures() {
        Log.d(TAG, "Fetching APP game features");

        // Data sources are the same used by SteamWebIntegration

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
                    String appIdStr = it.next();
                    JSONObject obj = json.getJSONObject(appIdStr);
                    if (obj.getBoolean("marketable")) {
                        int appId = Integer.parseInt(appIdStr);
                        int cardCount = obj.getInt("cards");
                        appGameFeatures.putIfAbsent(appId, new GameFeatures());
                        appGameFeatures.get(appId).setCards(cardCount);
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
                    String appIdStr = it.next();
                    int appId = Integer.parseInt(appIdStr);
                    appGameFeatures.putIfAbsent(appId, new GameFeatures());
                    appGameFeatures.get(appId).setDlc(true);
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
                    String appIdStr = it.next();
                    int appId = Integer.parseInt(appIdStr);
                    appGameFeatures.putIfAbsent(appId, new GameFeatures());
                    appGameFeatures.get(appId).setLimited(true);
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
                    int appId = json.getJSONObject(i).getInt("appid");
                    appGameFeatures.putIfAbsent(appId, new GameFeatures());
                    appGameFeatures.get(appId).setDelisted(true);
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

        Log.d(TAG, "Loaded " + appGameFeatures.size() + " game features");
        return appGameFeatures;
    }
}
