package net.mabako.steamgifts.tasks;

import androidx.fragment.app.Fragment;

import net.mabako.steamgifts.fragments.GiveawayListFragment;
import net.mabako.steamgifts.fragments.HiddenGamesFragment;
import net.mabako.steamgifts.fragments.interfaces.IHasHideableGiveaways;

import okhttp3.FormBody;
import okhttp3.Response;

public class UpdateGiveawayFilterTask<FragmentType extends Fragment> extends AjaxTask<FragmentType> {
    public static final String HIDE = "hide_giveaways_by_game_id";

    /**
     * Show a game on the giveaway list again.
     * <p>Consistency ftw?</p>
     */
    public static final String UNHIDE = "remove_filter";

    private final long internalGameId;
    private final String gameTitle;

    public UpdateGiveawayFilterTask(FragmentType fragment, String xsrfToken, String what, long internalGameId, String gameTitle) {
        super(fragment, fragment.getContext(), xsrfToken, what);

        this.internalGameId = internalGameId;
        this.gameTitle = gameTitle;
    }

    @Override
    protected void addExtraParameters(FormBody.Builder body) {
        body.add("game_id", String.valueOf(internalGameId));
    }

    @Override
    protected void onPostExecute(Response response) {
        try (response) {
            if (response == null || response.code() != 200) {
                // TODO Socket timed out or some stupid shit like that.
                return;
            }

            FragmentType fragment = getFragment();
            if (fragment instanceof IHasHideableGiveaways iHasHideableGiveaways && HIDE.equals(getWhat())) {
                iHasHideableGiveaways.onHideGame(internalGameId, true, gameTitle);
            }
            else if (fragment instanceof GiveawayListFragment giveawayListFragment && UNHIDE.equals(getWhat())) {
                giveawayListFragment.onShowGame(internalGameId, true);
            }
            else if (fragment instanceof HiddenGamesFragment hiddenGamesFragment && UNHIDE.equals(getWhat())) {
                hiddenGamesFragment.onShowGame(internalGameId);
            }
        }
    }
}
