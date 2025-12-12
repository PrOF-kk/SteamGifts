package net.mabako.steamgifts.fragments;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.typeface.IIcon;
import com.mikepenz.iconics.utils.IconicsMenuInflaterUtil;

import net.mabako.steamgifts.activities.CommonActivity;
import net.mabako.steamgifts.adapters.DiscussionAdapter;
import net.mabako.steamgifts.core.R;
import net.mabako.steamgifts.fragments.interfaces.IActivityTitle;
import net.mabako.steamgifts.persistentdata.SteamGiftsUserData;
import net.mabako.steamgifts.tasks.LoadDiscussionListTask;

/**
 * List of all discussions.
 */
public class DiscussionListFragment extends SearchableListFragment<DiscussionAdapter> implements IActivityTitle {
    private static final String SAVED_TYPE = "type";
    private static final String SAVED_SORT = "sort";
    private static final String PREFERENCE_SORT = "preference_sort_discussions";

    /**
     * Type of items to show.
     */
    @NonNull
    private Type type = Type.ALL;

    @NonNull
    private Sort sort = Sort.LAST_POST;

    public static Fragment newInstance(Type type, String query) {
        DiscussionListFragment f = new DiscussionListFragment();

        Bundle args = new Bundle();
        args.putSerializable(SAVED_TYPE, type);
        args.putSerializable(SAVED_QUERY, query);
        f.setArguments(args);

        f.type = type;

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            type = (Type) getArguments().getSerializable(SAVED_TYPE);
            sort = Sort.valueOf(PreferenceManager.getDefaultSharedPreferences(requireContext())
                    .getString(PREFERENCE_SORT, Sort.LAST_POST.name()));
        } else {
            type = (Type) savedInstanceState.getSerializable(SAVED_TYPE);
            sort = (Sort) savedInstanceState.getSerializable(SAVED_SORT);
        }

        adapter.setFragmentValues(this, requireActivity());
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(SAVED_TYPE, type);
        outState.putSerializable(SAVED_SORT, sort);
    }

    @NonNull
    @Override
    protected DiscussionAdapter createAdapter() {
        return new DiscussionAdapter();
    }

    @Override
    protected AsyncTask<Void, Void, ?> getFetchItemsTask(int page) {
        return new LoadDiscussionListTask(this, page, type, sort, getSearchQuery());
    }

    @Override
    public int getTitleResource() {
        return type.getTitleResource();
    }

    @Override
    public String getExtraTitle() {
        return null;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public Fragment newSearchingInstance(String query) {
        return newInstance(type, query);
    }


    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        IconicsMenuInflaterUtil.parseXmlAndSetIconicsDrawables(requireContext(), R.menu.main_menu, menu);

        MenuItem sortMenu = menu.findItem(R.id.sort);
        sortMenu.setVisible(true);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
        if (!"full".equals(sharedPreferences.getString("preference_sidebar_discussion_list", "full"))) {
            MenuItem categoryMenu = menu.findItem(R.id.category);
            categoryMenu.setVisible(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.category) {
            View menuItemView = getActivity().findViewById(R.id.category);
            final PopupMenu popupMenu = new PopupMenu(getContext(), menuItemView);
            final Menu menu = popupMenu.getMenu();

            SteamGiftsUserData account = SteamGiftsUserData.getCurrent(getContext());
            for (final DiscussionListFragment.Type type : DiscussionListFragment.Type.values()) {
                // We only want to have 'Created Discussions' if we're actually logged in.
                if (type == DiscussionListFragment.Type.CREATED && !account.isLoggedIn())
                    continue;

                menu.add(type.getNavbarResource()).setOnMenuItemClickListener(menuItem -> {
                    popupMenu.dismiss();

                    ((CommonActivity) getActivity()).loadFragment(DiscussionListFragment.newInstance(type, null));
                    return true;
                });
            }

            popupMenu.show();

            return true;
        }
        if (item.getItemId() == R.id.sort) {
            new AlertDialog.Builder(requireContext())
                    .setTitle(R.string.sort_by)
                    .setSingleChoiceItems(R.array.sort_choice, this.sort.ordinal(), (dialog, which) -> {
                        Sort newSort = Sort.values()[which];
                        if (this.sort != newSort) {
                            this.sort = newSort;
                            PreferenceManager.getDefaultSharedPreferences(requireContext())
                                    .edit()
                                    .putString(PREFERENCE_SORT, newSort.name())
                                    .apply();
                            this.refresh();
                        }
                        dialog.dismiss();
                    })
                    .show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Different types of discussion lists.
     */
    public enum Type {
        ALL(R.string.navigation_discussions_all, R.string.navigation_discussions_all_title, FontAwesome.Icon.faw_comments),
        ANNOUNCEMENTS(R.string.navigation_discussions_announcements, R.string.navigation_discussions_announcements_title, FontAwesome.Icon.faw_bullhorn),
        // BUGS_SUGGESTIONS(R.string.navigation_discussions_bugs_suggestions, R.string.navigation_discussions_bugs_suggestions_title),
        DEALS(R.string.navigation_discussions_deals, R.string.navigation_discussions_deals_title, FontAwesome.Icon.faw_usd),
        GENERAL(R.string.navigation_discussions_general, R.string.navigation_discussions_general_title, FontAwesome.Icon.faw_comments),
        GROUP_RECRUITMENT(R.string.navigation_discussions_group_recruitment, R.string.navigation_discussions_group_recruitment_title, FontAwesome.Icon.faw_users),
        LETS_PLAY_TOGETHER(R.string.navigation_discussions_lets_play_together, R.string.navigation_discussions_lets_play_together_title, FontAwesome.Icon.faw_play_circle),
        OFF_TOPIC(R.string.navigation_discussions_off_topic, R.string.navigation_discussions_off_topic_title, FontAwesome.Icon.faw_comments),
        PUZZLES_EVENTS(R.string.navigation_discussions_puzzles_events, R.string.navigation_discussions_puzzles_events_title, FontAwesome.Icon.faw_puzzle_piece),
        CREATED(R.string.navigation_discussions_created, R.string.navigation_discussions_created_title, FontAwesome.Icon.faw_plus_circle);

        private final int titleResource;
        private final int navbarResource;
        private final IIcon icon;

        Type(int navbarResource, int titleResource, FontAwesome.Icon icon) {
            this.navbarResource = navbarResource;
            this.titleResource = titleResource;
            this.icon = icon;
        }

        public int getTitleResource() {
            return titleResource;
        }

        public int getNavbarResource() {
            return navbarResource;
        }

        public IIcon getIcon() {
            return icon;
        }
    }

    // Order needs to match @array/sort_choice
    public enum Sort {
        LAST_POST,
        NEW
    }
}
