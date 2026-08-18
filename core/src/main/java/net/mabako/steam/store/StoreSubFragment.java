package net.mabako.steam.store;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import net.mabako.steam.store.data.Text;
import net.mabako.steamgifts.activities.DetailActivity;
import net.mabako.steamgifts.core.R;
import net.mabako.steamgifts.data.Game;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.HttpUrl;
import okhttp3.Request;

public class StoreSubFragment extends StoreFragment {
    private static final String TAG = StoreSubFragment.class.getSimpleName();

    public static StoreSubFragment newInstance(int subId) {
        StoreSubFragment fragment = new StoreSubFragment();

        Bundle args = new Bundle();
        args.putString("sub", String.valueOf(subId));
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    protected AsyncTask<Void, Void, ?> getFetchItemsTask(int page) {
        return new LoadSubTask();
    }

    public void showDetails(int appId) {
        DetailActivity activity = (DetailActivity) getActivity();
        activity.setTransientFragment(StoreAppFragment.newInstance(appId, true));
    }

    private class LoadSubTask extends LoadStoreTask {
        @Override
        protected Request getRequest() {
            return new Request.Builder().url(
                    new HttpUrl.Builder().scheme("https")
                            .host("store.steampowered.com")
                            .encodedPath("/api/packagedetails")
                            .addQueryParameter("packageids", requireArguments().getString("sub"))
                            .addQueryParameter("l", "en")
                            .build())
                    .build();
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            if (jsonObject != null) {
                try {
                    String subId = requireArguments().getString("sub");
                    JSONObject sub = jsonObject.getJSONObject(subId);

                    // Were we successful in fetching the details?
                    if (sub.getBoolean("success")) {
                        JSONObject data = sub.getJSONObject("data");
                        JSONArray apps = data.getJSONArray("apps");

                        List<Game> games = new ArrayList<>(apps.length());

                        for (int i = 0; i < apps.length(); ++i) {
                            JSONObject app = apps.getJSONObject(i);

                            Game game = new Game();
                            game.setType(Game.Type.APP);
                            game.setId(app.getInt("id"));
                            game.setName(app.getString("name"));

                            games.add(game);
                        }

                        addItems(games, true);
                    } else {
                        // Sub delisted
                        addItems(List.of(
                                new Text("The store page for this bundle is not available.\n", false),
                                new Text("You can <a href='https://steamdb.info/sub/" + subId + "/'>visit its SteamDB page instead \uD83D\uDD17</a>", true)),
                                true
                        );
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Exception during loading store sub", e);
                    Toast.makeText(getContext(), "Unable to load Store Sub", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(getContext(), "Unable to load Store Sub", Toast.LENGTH_LONG).show();
            }

            requireView().findViewById(R.id.progressBar).setVisibility(View.GONE);
        }
    }
}
