package net.mabako.steamgifts.fragments.util;

import androidx.fragment.app.Fragment;

import net.mabako.steamgifts.fragments.GiveawayListFragment;
import net.mabako.steamgifts.fragments.SavedGiveawaysFragment;
import net.mabako.steamgifts.fragments.interfaces.IHasEnterableGiveaways;
import net.mabako.steamgifts.fragments.interfaces.IHasHideableGiveaways;

import java.util.ArrayList;
import java.util.List;

public final class GiveawayListFragmentStack {
    private GiveawayListFragmentStack() {}
    private static final List<Fragment> fragments = new ArrayList<>();

    public static void addFragment(Fragment fragment) {
        if (!fragments.contains(fragment))
            fragments.add(fragment);
    }

    public static void removeFragment(Fragment fragment) {
        fragments.remove(fragment);
    }

    public static void onHideGame(long internalGameId) {
        for (Fragment fragment : fragments)
            if (fragment instanceof IHasHideableGiveaways iHasHideableGiveaways)
                iHasHideableGiveaways.onHideGame(internalGameId, false, null);
    }

    public static void onShowGame(long internalGameId) {
        for (Fragment fragment : fragments)
            if (fragment instanceof GiveawayListFragment giveawayListFragment)
                giveawayListFragment.onShowGame(internalGameId, false);
    }

    public static void onEnterLeaveResult(String giveawayId, String what, Boolean success) {
        for (Fragment fragment : fragments)
            if (fragment instanceof IHasEnterableGiveaways iHasEnterableGiveaways)
                iHasEnterableGiveaways.onEnterLeaveResult(giveawayId, what, success, false);
    }

    public static void onRemoveSavedGiveaway(String giveawayId) {
        for (Fragment fragment : fragments)
            if (fragment instanceof SavedGiveawaysFragment savedGiveawaysFragment)
                savedGiveawaysFragment.onRemoveSavedGiveaway(giveawayId);
    }
}
