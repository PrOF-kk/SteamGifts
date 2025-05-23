package net.mabako.steamgifts.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.PreferenceManager;

import com.google.android.material.snackbar.Snackbar;

import net.mabako.steamgifts.adapters.GiveawayAdapter;
import net.mabako.steamgifts.adapters.IEndlessAdaptable;
import net.mabako.steamgifts.core.R;
import net.mabako.steamgifts.data.Giveaway;
import net.mabako.steamgifts.fragments.interfaces.IActivityTitle;
import net.mabako.steamgifts.fragments.interfaces.IHasEnterableGiveaways;
import net.mabako.steamgifts.fragments.profile.LoadEnteredGameListTask;
import net.mabako.steamgifts.fragments.profile.ProfileGiveaway;
import net.mabako.steamgifts.fragments.util.GiveawayListFragmentStack;
import net.mabako.steamgifts.persistentdata.SavedGiveaways;
import net.mabako.steamgifts.persistentdata.SteamGiftsUserData;
import net.mabako.steamgifts.tasks.EnterLeaveGiveawayTask;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Show a list of saved giveaways.
 */
public class SavedGiveawaysFragment extends ListFragment<SavedGiveawaysFragment.SavedGiveawaysAdapter> implements IActivityTitle, IHasEnterableGiveaways {
    private static final String TAG = SavedGiveawaysFragment.class.getSimpleName();

    private SavedGiveaways savedGiveaways;

    private LoadEnteredGameListTask enteredGameListTask;
    private EnterLeaveGiveawayTask enterLeaveTask;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter.setFragmentValues(getActivity(), this, savedGiveaways);

        GiveawayListFragmentStack.addFragment(this);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        savedGiveaways = new SavedGiveaways(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        setHasOptionsMenu(true);

        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();


        if (savedGiveaways != null) {
            savedGiveaways.close();
            savedGiveaways = null;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        GiveawayListFragmentStack.removeFragment(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (enteredGameListTask != null) {
            enteredGameListTask.cancel(true);
            enteredGameListTask = null;
        }

        if (enterLeaveTask != null) {
            enterLeaveTask.cancel(true);
            enterLeaveTask = null;
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.saved_giveaways_menu, menu);
        menu.findItem(R.id.remove_all_entered_saved).setVisible(adapter.getEnteredItemCount() > 0);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.remove_all_entered_saved) {
            new AlertDialog.Builder(requireContext())
                    .setMessage("Confirm removing all entered giveaways?\nThis cannot be undone")
                    .setCancelable(true)
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                        for (Giveaway enteredGiveaway : adapter.getEnteredItems()) {
                            savedGiveaways.remove(enteredGiveaway.getGiveawayId());
                            adapter.removeGiveaway(enteredGiveaway.getGiveawayId());
                        }

                        if (getActivity() != null)
                            getActivity().invalidateOptionsMenu();
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @NonNull
    @Override
    protected SavedGiveawaysAdapter createAdapter() {
        return new SavedGiveawaysAdapter(-1, false, PreferenceManager.getDefaultSharedPreferences(getContext()));
    }

    @Override
    protected AsyncTask<Void, Void, ?> getFetchItemsTask(int page) {
        return null;
    }

    @Override
    protected Serializable getType() {
        return null;
    }

    @Override
    protected void fetchItems(int page) {
        if (page != 1)
            return;

        super.addItems(savedGiveaways.all(), true);
        adapter.reachedTheEnd();

        // Load all entered giveaways to update the saved giveaways db
        if (enteredGameListTask != null)
            enteredGameListTask.cancel(true);

        if (SteamGiftsUserData.getCurrent(getContext()).isLoggedIn()) {
            enteredGameListTask = new LoadEnteredGameListTask(this, 1);
            enteredGameListTask.execute();
        }
    }

    @Override
    public int getTitleResource() {
        return R.string.saved_giveaways_title;
    }

    @Override
    public String getExtraTitle() {
        return null;
    }

    public void onRemoveSavedGiveaway(String giveawayId) {
        adapter.removeGiveaway(giveawayId);
    }

    /**
     * Callback for {@link #enteredGameListTask}
     * <p>Note: do NOT call this from within this class.</p>
     */
    @Override
    public void addItems(@Nullable List<? extends IEndlessAdaptable> items, boolean clearExistingItems) {
        if (items == null) {
            showSnack("Failed to update entered giveaways", Snackbar.LENGTH_LONG);
            return;
        }

        // do nothing much except update the status of saved giveaways.
        for (IEndlessAdaptable endlessAdaptable : items) {
            ProfileGiveaway giveaway = (ProfileGiveaway) endlessAdaptable;

            Giveaway existingGiveaway = adapter.findItem(giveaway.getGiveawayId());
            if (existingGiveaway != null) {
                existingGiveaway.setEntries(giveaway.getEntries());
                existingGiveaway.setEntered(giveaway.isEntered());
                adapter.notifyItemChanged(existingGiveaway);
            }
        }

        FragmentActivity activity = getActivity();
        if (activity != null)
            activity.invalidateOptionsMenu();

        // Load next page until we loaded all entered giveaways
        if (items.size() >= LoadEnteredGameListTask.ENTRIES_PER_PAGE) {
            enteredGameListTask = new LoadEnteredGameListTask(this, enteredGameListTask.getPage() + 1);
            enteredGameListTask.execute();
        }
    }

    @Override
    public void requestEnterLeave(String giveawayId, String enterOrDelete, String xsrfToken) {
        if (!SteamGiftsUserData.getCurrent(getContext()).isLoggedIn()) {
            Log.w(TAG, "Could not request enter/leave giveaway, since we're not logged in");
            return;
        }

        if (enterLeaveTask != null)
            enterLeaveTask.cancel(true);

        enterLeaveTask = new EnterLeaveGiveawayTask(this, getContext(), giveawayId, xsrfToken, enterOrDelete);
        enterLeaveTask.execute();
    }

    @Override
    public void onEnterLeaveResult(String giveawayId, String what, boolean success, boolean propagate) {
        if (success) {
            Giveaway giveaway = adapter.findItem(giveawayId);
            if (giveaway != null) {
                boolean currentlyEnteredAny = adapter.getEnteredItemCount() > 0;

                giveaway.setEntered(GiveawayDetailFragment.ENTRY_INSERT.equals(what));
                adapter.notifyItemChanged(giveaway);

                boolean nowEnteredAny = adapter.getEnteredItemCount() > 0;
                if (currentlyEnteredAny != nowEnteredAny && getActivity() != null)
                    getActivity().invalidateOptionsMenu();
            }
        } else {
            Log.e(TAG, "Probably an error catching the result...");
        }

        if (propagate)
            GiveawayListFragmentStack.onEnterLeaveResult(giveawayId, what, success);
    }

    /**
     * Adapter with some useful functions for saved items.
     */
    public static class SavedGiveawaysAdapter extends GiveawayAdapter {
        @Serial
        private static final long serialVersionUID = -6841859269105451683L;

        private SavedGiveawaysAdapter(int itemsPerPage, boolean filterItems, SharedPreferences sharedPreferences) {
            super(itemsPerPage, filterItems, sharedPreferences);
        }

        public int getEnteredItemCount() {
            int entered = 0;

            for (IEndlessAdaptable item : getItems()) {
                if (item instanceof Giveaway giveaway && giveaway.isEntered()) {
                    ++entered;
                }
            }

            return entered;
        }

        public List<Giveaway> getEnteredItems() {
            List<Giveaway> entered = new ArrayList<>(getItems().size());
            for (IEndlessAdaptable item : getItems()) {
                if (item instanceof Giveaway giveaway && giveaway.isEntered()) {
                    entered.add(giveaway);
                }
            }

            return entered;
        }
    }
}
