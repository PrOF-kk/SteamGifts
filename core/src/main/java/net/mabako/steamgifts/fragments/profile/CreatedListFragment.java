package net.mabako.steamgifts.fragments.profile;

import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import net.mabako.steamgifts.adapters.GiveawayAdapter;
import net.mabako.steamgifts.core.R;
import net.mabako.steamgifts.fragments.ListFragment;
import net.mabako.steamgifts.fragments.interfaces.IActivityTitle;
import net.mabako.steamgifts.tasks.LoadCreatedGiveawaysTask;

import java.io.Serializable;

public class CreatedListFragment extends ListFragment<GiveawayAdapter> implements IActivityTitle {
    @Override
    public int getTitleResource() {
        return R.string.user_tab_created;
    }

    @Override
    public String getExtraTitle() {
        return null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter.setFragmentValues(getActivity(), this, null);
    }

    @NonNull
    @Override
    protected GiveawayAdapter createAdapter() {
        return new GiveawayAdapter(50, PreferenceManager.getDefaultSharedPreferences(getContext()));
    }

    @Override
    protected AsyncTask<Void, Void, ?> getFetchItemsTask(int page) {
        return new LoadCreatedGiveawaysTask(this, page);
    }

    @Override
    protected Serializable getType() {
        return null;
    }
}
