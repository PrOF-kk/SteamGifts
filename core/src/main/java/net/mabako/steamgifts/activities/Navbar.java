package net.mabako.steamgifts.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.StringRes;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome;
import com.mikepenz.materialdrawer.holder.ImageHolder;
import com.mikepenz.materialdrawer.holder.StringHolder;
import com.mikepenz.materialdrawer.iconics.IconicsImageHolder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader;
import com.mikepenz.materialdrawer.util.DrawerImageLoader;
import com.mikepenz.materialdrawer.widget.AccountHeaderView;
import com.mikepenz.materialdrawer.widget.MaterialDrawerSliderView;
import com.squareup.picasso.Picasso;

import net.mabako.steamgifts.core.R;
import net.mabako.steamgifts.fragments.DiscussionListFragment;
import net.mabako.steamgifts.fragments.GiveawayListFragment;
import net.mabako.steamgifts.fragments.SavedFragment;
import net.mabako.steamgifts.fragments.SearchableListFragment;
import net.mabako.steamgifts.fragments.UserDetailFragment;
import net.mabako.steamgifts.intro.IntroActivity;
import net.mabako.steamgifts.persistentdata.SteamGiftsUserData;
import net.mabako.steamgifts.receivers.AbstractNotificationCheckReceiver;

import org.jspecify.annotations.NonNull;

import java.util.StringJoiner;

public class Navbar {
    private final CommonActivity activity;
    private final AccountHeaderView accountHeader;
    private final MaterialDrawerSliderView drawer;

    private CustomProfileDrawerItem profile;

    public Navbar(final CommonActivity activity) {
        this.activity = activity;

        // Drawer toggle button
        drawer = activity.findViewById(R.id.slider);
        drawer.recyclerView.setVerticalScrollBarEnabled(false);
        DrawerLayout drawerLayout = activity.findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(activity, drawerLayout, activity.findViewById(R.id.toolbar), R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Header account image loader
        DrawerImageLoader.Companion.init(new AbstractDrawerImageLoader() {
            @Override
            public void set(@NonNull ImageView imageView, @NonNull Uri uri, @NonNull Drawable placeholder, String tag) {
                Picasso.get().load(uri).placeholder(R.drawable.default_avatar).into(imageView);
            }

            @Override
            public void cancel(@NonNull ImageView imageView) {
                Picasso.get().cancelRequest(imageView);
            }
        });

        // Header account view
        accountHeader = new AccountHeaderView(activity, null, 0, true);
        try (TypedArray ta = activity.getTheme().obtainStyledAttributes(new int[]{R.attr.colorAccountHeader})) {
            accountHeader.setHeaderBackground(new ImageHolder(ta.getDrawable(0)));
        }
        accountHeader.attachToSliderView(drawer);
        accountHeader.setSelectionListEnabledForSingleProfile(false);
        accountHeader.setOnAccountHeaderProfileImageListener((view, profile, current) -> {
            if (SteamGiftsUserData.getCurrent(activity).isLoggedIn()) {
                Intent intent = new Intent(activity, DetailActivity.class);
                intent.putExtra(UserDetailFragment.ARG_USER, SteamGiftsUserData.getCurrent(activity).getName());
                activity.startActivity(intent);
                return true;
            }
            return false;
        });

        // Drawer item behavior
        drawer.setOnDrawerItemClickListener((view, drawerItem, position) -> {
            // Stop searching, if any is done
            Fragment fragment = activity.getCurrentFragment();
            if (fragment instanceof SearchableListFragment)
                ((SearchableListFragment) fragment).stopSearch();

            long identifier = drawerItem.getIdentifier();
            if (identifier == R.string.login) {
                activity.requestLogin();

            } else if (identifier == R.string.navigation_help) {
                IntroActivity.showIntro(activity, IntroActivity.INTRO_MAIN);

            } else if (identifier == R.string.navigation_about) {
                activity.startActivity(new Intent(activity, AboutActivity.class));

            } else if (identifier == R.string.preferences) {
                activity.startActivityForResult(new Intent(activity, SettingsActivity.class), CommonActivity.REQUEST_SETTINGS);

            } else if (identifier == R.string.navigation_saved_elements) {
                activity.loadFragment(new SavedFragment());
                ActionBar actionBar = activity.getSupportActionBar();
                if (actionBar != null)
                    actionBar.setSubtitle(null);
            } else {
                for (GiveawayListFragment.Type type : GiveawayListFragment.Type.values()) {
                    if (type.getNavbarResource() == identifier) {
                        activity.loadFragment(GiveawayListFragment.newInstance(type, null, false));
                        break;
                    }
                }

                for (DiscussionListFragment.Type type : DiscussionListFragment.Type.values()) {
                    if (type.getNavbarResource() == identifier) {
                        activity.loadFragment(DiscussionListFragment.newInstance(type, null));
                        ActionBar actionBar = activity.getSupportActionBar();
                        if (actionBar != null)
                            actionBar.setSubtitle(null);
                        break;
                    }
                }

                return false;
            }

            drawerLayout.closeDrawer(drawer);
            return true;
        });

        // Refresh header when opened/closed
        drawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
                if (drawerView != drawer) return;

                TextView notifications = accountHeader.findViewById(R.id.material_drawer_account_header_notifications);

                // Are we even logged in?
                SteamGiftsUserData user = SteamGiftsUserData.getCurrent(activity);
                if (user.isLoggedIn() && profile != null) {
                    // Format the string
                    String newInfo = activity.getString(R.string.drawer_profile_info, user.getLevel(), user.getPoints());

                    // Is this still up-to-date?
                    if (profile.getDescription() == null || !newInfo.equals(profile.getDescription().toString())) {
                        profile.setDescription(new StringHolder(newInfo));
                    }

                    if (user.hasNotifications()) {
                        StringJoiner sj = new StringJoiner(" ");

                        if (user.getCreatedNotification() > 0)
                            sj.add("{faw-gift}").add(String.valueOf(user.getCreatedNotification()));

                        if (user.getWonNotification() > 0)
                            sj.add("{faw-trophy}").add(String.valueOf(user.getWonNotification()));

                        if (user.getMessageNotification() > 0)
                            sj.add("{faw-envelope}").add(String.valueOf(user.getMessageNotification()));

                        profile.setNotifications(new StringHolder(sj.toString()));
                    } else {
                        profile.setNotifications(new StringHolder("{faw-envelope}"));
                    }
                } else if (profile != null) {
                    profile.setNotifications(null);
                }
                if (profile != null) {
                    accountHeader.updateProfile(profile);
                    updateNotificationText(notifications, profile);
                }
            }
        });

        reconfigure();
    }

    private void updateNotificationText(TextView notificationText, CustomProfileDrawerItem profile) {
        if (notificationText == null) return;
        if (profile == null || profile.getNotifications() == null) {
            notificationText.setText("");
            notificationText.setOnClickListener(null);
        } else {
            profile.getNotifications().applyTo(notificationText);
            notificationText.setOnClickListener(v -> {
                if (SteamGiftsUserData.getCurrent(activity).isLoggedIn()) {
                    Intent intent = new Intent(activity, DetailActivity.class);
                    intent.putExtra(DetailActivity.ARG_NOTIFICATIONS, AbstractNotificationCheckReceiver.NotificationId.NO_TYPE);
                    activity.startActivity(intent);
                }
            });
        }
    }

    public void reconfigure() {
        // Rebuild the header.
        accountHeader.clear();

        // Update the account header.
        SteamGiftsUserData account = SteamGiftsUserData.getCurrent(activity);
        if (account.isLoggedIn()) {
            profile = new CustomProfileDrawerItem();
            profile.setName(new StringHolder(account.getName()));
            profile.setDescription(new StringHolder("..."));
            profile.setIdentifier(1L);

            if (account.getImageUrl() != null && !account.getImageUrl().isEmpty())
                profile.setIcon(new ImageHolder(account.getImageUrl()));

            accountHeader.addProfiles(profile);
        } else {
            profile = new CustomProfileDrawerItem();
            profile.setName(new StringHolder(activity.getString(R.string.guest)));
            profile.setDescription(new StringHolder("Not logged in"));
            profile.setIcon(new ImageHolder(R.drawable.default_avatar));
            profile.setIdentifier(1L);
            accountHeader.addProfiles(profile);
        }

        // Rebuild all items
        drawer.getItemAdapter().clear();

        // If we're not logged in, log in is the top.
        if (!account.isLoggedIn()) {
            PrimaryDrawerItem loginItem = new PrimaryDrawerItem();
            loginItem.setName(new StringHolder(R.string.login));
            loginItem.setIdentifier(R.string.login);
            loginItem.setSelectable(false);
            loginItem.setIcon(new IconicsImageHolder(FontAwesome.Icon.faw_sign_in_alt));
            drawer.getItemAdapter().add(loginItem);
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
        String mode = sharedPreferences.getString("preference_sidebar_discussion_list", "full");

        addGiveawayItems(account);

        if ("compact".equals(mode))
            drawer.getItemAdapter().add(new DividerDrawerItem());
        addDiscussionItems(account, mode);

        drawer.getItemAdapter().add(new DividerDrawerItem());

        PrimaryDrawerItem savedItem = new PrimaryDrawerItem();
        savedItem.setName(new StringHolder(R.string.navigation_saved_elements));
        savedItem.setIdentifier(R.string.navigation_saved_elements);
        savedItem.setIcon(new IconicsImageHolder(FontAwesome.Icon.faw_star));
        drawer.getItemAdapter().add(savedItem);

        PrimaryDrawerItem prefItem = new PrimaryDrawerItem();
        prefItem.setName(new StringHolder(R.string.preferences));
        prefItem.setIdentifier(R.string.preferences);
        prefItem.setSelectable(false);
        prefItem.setIcon(new IconicsImageHolder(FontAwesome.Icon.faw_cog));
        drawer.getItemAdapter().add(prefItem);

        PrimaryDrawerItem helpItem = new PrimaryDrawerItem();
        helpItem.setName(new StringHolder(R.string.navigation_help));
        helpItem.setIdentifier(R.string.navigation_help);
        helpItem.setSelectable(false);
        helpItem.setIcon(new IconicsImageHolder(FontAwesome.Icon.faw_question));
        drawer.getItemAdapter().add(helpItem);

        PrimaryDrawerItem aboutItem = new PrimaryDrawerItem();
        aboutItem.setName(new StringHolder(R.string.navigation_about));
        aboutItem.setIdentifier(R.string.navigation_about);
        aboutItem.setSelectable(false);
        aboutItem.setIcon(new IconicsImageHolder(FontAwesome.Icon.faw_info));
        drawer.getItemAdapter().add(aboutItem);
    }

    private void addGiveawayItems(SteamGiftsUserData account) {
        SectionDrawerItem section = new SectionDrawerItem();
        section.setName(new StringHolder(R.string.navigation_giveaways));
        section.setDivider(!account.isLoggedIn());
        drawer.getItemAdapter().add(section);

        PrimaryDrawerItem all = new PrimaryDrawerItem();
        all.setName(new StringHolder(R.string.navigation_giveaways_all));
        all.setIdentifier(R.string.navigation_giveaways_all);
        all.setIcon(new IconicsImageHolder(FontAwesome.Icon.faw_gift));
        drawer.getItemAdapter().add(all);

        PrimaryDrawerItem recommended = new PrimaryDrawerItem();
        recommended.setName(new StringHolder(R.string.navigation_giveaways_recommended));
        recommended.setIdentifier(R.string.navigation_giveaways_recommended);
        recommended.setIcon(new IconicsImageHolder(FontAwesome.Icon.faw_thumbs_up));
        drawer.getItemAdapter().add(recommended);

        PrimaryDrawerItem newItem = new PrimaryDrawerItem();
        newItem.setName(new StringHolder(R.string.navigation_giveaways_new));
        newItem.setIdentifier(R.string.navigation_giveaways_new);
        newItem.setIcon(new IconicsImageHolder(FontAwesome.Icon.faw_sync_alt));
        drawer.getItemAdapter().add(newItem);

        if (account.isLoggedIn()) {
            PrimaryDrawerItem wishlist = new PrimaryDrawerItem();
            wishlist.setName(new StringHolder(R.string.navigation_giveaways_wishlist));
            wishlist.setIdentifier(R.string.navigation_giveaways_wishlist);
            wishlist.setIcon(new IconicsImageHolder(FontAwesome.Icon.faw_heart));
            drawer.getItemAdapter().add(3, wishlist);

            PrimaryDrawerItem group = new PrimaryDrawerItem();
            group.setName(new StringHolder(R.string.navigation_giveaways_group));
            group.setIdentifier(R.string.navigation_giveaways_group);
            group.setIcon(new IconicsImageHolder(FontAwesome.Icon.faw_users));
            drawer.getItemAdapter().add(5, group);
        }
    }

    private void addDiscussionItems(SteamGiftsUserData account, String mode) {
        if ("full".equals(mode)) {
            SectionDrawerItem section = new SectionDrawerItem();
            section.setName(new StringHolder(R.string.navigation_discussions));
            section.setDivider(true);
            drawer.getItemAdapter().add(section);

            for (DiscussionListFragment.Type type : DiscussionListFragment.Type.values()) {
                if (type == DiscussionListFragment.Type.CREATED && !account.isLoggedIn())
                    continue;

                PrimaryDrawerItem item = new PrimaryDrawerItem();
                item.setName(new StringHolder(type.getNavbarResource()));
                item.setIdentifier(type.getNavbarResource());
                item.setIcon(new IconicsImageHolder(type.getIcon()));
                drawer.getItemAdapter().add(item);
            }
        } else if ("compact".equals(mode)) {
            PrimaryDrawerItem item = new PrimaryDrawerItem();
            item.setName(new StringHolder(R.string.navigation_discussions));
            item.setIdentifier(DiscussionListFragment.Type.ALL.getNavbarResource());
            item.setIcon(new IconicsImageHolder(DiscussionListFragment.Type.ALL.getIcon()));
            drawer.getItemAdapter().add(item);
        }
    }

    public void setSelection(@StringRes int resourceId) {
        drawer.setSelection(resourceId, false);
    }

    public static class CustomProfileDrawerItem extends ProfileDrawerItem {
        private StringHolder notifications;

        public void setNotifications(StringHolder notifications) {
            this.notifications = notifications;
        }

        public StringHolder getNotifications() {
            return notifications;
        }
    }
}
