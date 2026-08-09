package net.mabako.steamgifts.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;

import net.mabako.steamgifts.ApplicationTemplate;
import net.mabako.steamgifts.core.R;
import net.mabako.steamgifts.fragments.DiscussionListFragment;
import net.mabako.steamgifts.fragments.GiveawayListFragment;
import net.mabako.steamgifts.fragments.SavedFragment;
import net.mabako.steamgifts.fragments.interfaces.IActivityTitle;
import net.mabako.steamgifts.intro.IntroActivity;
import net.mabako.steamgifts.persistentdata.IPointUpdateNotification;
import net.mabako.steamgifts.persistentdata.SteamGiftsUserData;
import net.mabako.steamgifts.tasks.LogoutTask;

import java.io.Serializable;

public class MainActivity extends CommonActivity implements IPointUpdateNotification {
    public static final String ARG_TYPE = "type";
    public static final String ARG_QUERY = "query";
    public static final String ARG_NO_DRAWER = "no-drawer";

    private Navbar navbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean noDrawer = getIntent().getBooleanExtra(ARG_NO_DRAWER, false);
        setContentView(noDrawer ? R.layout.activity_one_fragment : R.layout.activity_main);

        setSupportActionBar(findViewById(R.id.toolbar));

        SteamGiftsUserData.addUpdateHandler(this);
        onUpdatePoints(SteamGiftsUserData.getCurrent(this).getPoints());

        if (!noDrawer)
            navbar = new Navbar(this);

        // savedInstanceState is non-null if a fragment state is saved from a previous configuration.
        if (savedInstanceState == null) {
            ((ApplicationTemplate) getApplication()).showBetaNotification(this, true);

            // Load a default fragment to show all giveaways
            Serializable type = getIntent().getSerializableExtra(ARG_TYPE);
            if (type == null)
                type = GiveawayListFragment.Type.ALL;

            String query = getIntent().getStringExtra(ARG_QUERY);


            if (type instanceof GiveawayListFragment.Type giveawayListFragmentType) {
                loadFragment(GiveawayListFragment.newInstance(giveawayListFragmentType, query, navbar == null));

                if (navbar != null)
                    navbar.setSelection(giveawayListFragmentType.getNavbarResource());
            } else if (type instanceof DiscussionListFragment.Type discussionListFragmentType) {
                loadFragment(DiscussionListFragment.newInstance(discussionListFragmentType, null));

                if (navbar != null)
                    navbar.setSelection(discussionListFragmentType.getNavbarResource());
            }
        } else {
            Fragment fragment = getCurrentFragment();
            if (fragment instanceof IActivityTitle) {
                updateTitle(fragment);
            }
        }

        IntroActivity.showIntroIfNecessary(this, IntroActivity.INTRO_MAIN, IntroActivity.INTRO_MAIN_VERSION);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SteamGiftsUserData.removeUpdateHandler(this);
    }

    /// Triggered upon the user logging in or logging out.
    @Override
    public void onAccountChange() {
        // Reconfigure our navigation bar items.
        navbar.reconfigure();

        super.onAccountChange();

        loadFragment(GiveawayListFragment.newInstance(GiveawayListFragment.Type.ALL, null, navbar == null));

        if (navbar != null)
            navbar.setSelection(R.string.navigation_giveaways_all);
    }

    @Override
    public void loadFragment(Fragment fragment) {
        super.loadFragment(fragment);
        onUpdatePoints(SteamGiftsUserData.getCurrent(this).getPoints());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_LOGIN -> {
                if (resultCode == RESPONSE_LOGIN_SUCCESSFUL) {
                    onAccountChange();
                    Snackbar.make(findViewById(R.id.swipeContainer), "Welcome, " + SteamGiftsUserData.getCurrent(this).getName() + "!", Snackbar.LENGTH_LONG).show();
                } else {
                    Snackbar.make(findViewById(R.id.swipeContainer), "Login failed", Snackbar.LENGTH_LONG).show();
                }
            }
            case REQUEST_SETTINGS -> {
                if (resultCode == RESPONSE_LOGOUT) {
                    new LogoutTask(MainActivity.this, SteamGiftsUserData.getCurrent(this).getSessionId()).execute();

                    SteamGiftsUserData.clear();
                    onAccountChange();
                } else {
                    Fragment fragment = getCurrentFragment();

                    if (navbar != null)
                        navbar.reconfigure();

                    // force an entire fragment reload if this is something giveaway reloaded
                    if (fragment instanceof GiveawayListFragment giveawayListFragment) {
                        loadFragment(GiveawayListFragment.newInstance(giveawayListFragment.getType(), null, false));

                        if (navbar != null)
                            navbar.setSelection(giveawayListFragment.getType().getNavbarResource());
                    } else if (fragment instanceof SavedFragment) {
                        loadFragment(new SavedFragment());
                    }
                }
            }
            default -> super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onUpdatePoints(final int newPoints) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            Fragment currentFragment = getCurrentFragment();
            if (SteamGiftsUserData.getCurrent(this).isLoggedIn() && (currentFragment instanceof GiveawayListFragment || currentFragment instanceof SavedFragment)) {
                actionBar.setSubtitle(newPoints + "P");
            } else {
                actionBar.setSubtitle(null);
            }
        }
    }
}
