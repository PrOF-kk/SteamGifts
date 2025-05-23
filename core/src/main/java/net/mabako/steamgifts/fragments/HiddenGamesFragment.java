package net.mabako.steamgifts.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;

import net.mabako.steamgifts.adapters.EndlessAdapter;
import net.mabako.steamgifts.adapters.HiddenGamesAdapter;
import net.mabako.steamgifts.core.R;
import net.mabako.steamgifts.data.Game;
import net.mabako.steamgifts.fragments.interfaces.IActivityTitle;
import net.mabako.steamgifts.fragments.interfaces.IHasHideableGiveaways;
import net.mabako.steamgifts.fragments.interfaces.ILoadItemsListener;
import net.mabako.steamgifts.tasks.LoadGameListTask;
import net.mabako.steamgifts.tasks.UpdateGiveawayFilterTask;

import org.jsoup.nodes.Element;

import java.io.Serializable;
import java.util.List;

public class HiddenGamesFragment extends SearchableListFragment<HiddenGamesAdapter> implements IActivityTitle, IHasHideableGiveaways {
    private static final String SAVED_LAST_REMOVED = "last-removed-game";

    private EndlessAdapter.RemovedElement lastRemovedGame;

    public static HiddenGamesFragment newInstance(String query) {
        HiddenGamesFragment fragment = new HiddenGamesFragment();

        Bundle args = new Bundle();
        args.putString(SAVED_QUERY, query);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            lastRemovedGame = null;
        } else {
            lastRemovedGame = (EndlessAdapter.RemovedElement) savedInstanceState.getSerializable(SAVED_LAST_REMOVED);
        }

        adapter.setFragmentValues(this);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(SAVED_LAST_REMOVED, lastRemovedGame);
    }

    @NonNull
    @Override
    protected HiddenGamesAdapter createAdapter() {
        return new HiddenGamesAdapter();
    }

    @Override
    protected AsyncTask<Void, Void, ?> getFetchItemsTask(int page) {
        return new LoadHiddenGamesTask(this, getContext(), page, getSearchQuery());
    }

    @Override
    public Fragment newSearchingInstance(String query) {
        return newInstance(query);
    }

    public void requestShowGame(long internalGameId, String title) {
        new UpdateGiveawayFilterTask<>(this, adapter.getXsrfToken(), UpdateGiveawayFilterTask.UNHIDE, internalGameId, title).execute();
    }

    public void onShowGame(long internalGameId) {
        lastRemovedGame = adapter.removeShownGame(internalGameId);

        if (lastRemovedGame != null) {
            final Game game = (Game) lastRemovedGame.getElement();
            Snackbar.make(swipeContainer, getString(R.string.hidden_game_removed, game.getName()), Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.undo, v -> new UpdateGiveawayFilterTask<>(HiddenGamesFragment.this, adapter.getXsrfToken(), UpdateGiveawayFilterTask.HIDE, game.getInternalGameId(), game.getName()).execute()).show();
        }
    }

    @Override
    public void onHideGame(long internalGameId, boolean propagate, String gameTitle) {
        if (lastRemovedGame != null && lastRemovedGame.getElement() instanceof Game && ((Game) lastRemovedGame.getElement()).getInternalGameId() == internalGameId) {
            adapter.restore(lastRemovedGame);
            lastRemovedGame = null;
        }
    }

    @Override
    protected Serializable getType() {
        return null;
    }

    @Override
    public int getTitleResource() {
        return R.string.preference_sg_hidden_games;
    }

    @Override
    public String getExtraTitle() {
        return null;
    }

    private static class LoadHiddenGamesTask extends LoadGameListTask {

        public LoadHiddenGamesTask(ILoadItemsListener listener, Context context, int page, String query) {
            super(listener, context, "account/settings/giveaways/filters", page, query);
        }
        @Override
        protected Game load(Element element) {
            Game game = new Game();
            game.setName(element.select(".table__column__heading").text());
            game.setInternalGameId(Long.parseLong(element.expectFirst("input[name=game_id]").attr("value")));

            Element link = element.selectFirst(".table__column--width-fill .table__column__secondary-link");
            if (link != null) {
                Uri steamUri = Uri.parse(link.attr("href"));

                // Steam link
                if (steamUri != null) {
                    List<String> pathSegments = steamUri.getPathSegments();
                    if (pathSegments.size() >= 2)
                        game.setId(Integer.parseInt(pathSegments.get(1)));
                    game.setType("app".equals(pathSegments.get(0)) ? Game.Type.APP : Game.Type.SUB);
                }
            }

            return game;
        }
    }
}
