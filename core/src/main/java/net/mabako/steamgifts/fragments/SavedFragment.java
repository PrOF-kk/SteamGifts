package net.mabako.steamgifts.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import net.mabako.steamgifts.core.R;
import net.mabako.steamgifts.fragments.interfaces.IActivityTitle;

public class SavedFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_saved, container, false);

        ActionBar toolbar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (toolbar != null)
            toolbar.setTitle(R.string.navigation_saved_elements);

        ViewPager viewPager = layout.findViewById(R.id.viewPager);
        TitledPagerAdapter viewPagerAdapter = new TitledPagerAdapter((AppCompatActivity) getActivity(), viewPager, new SavedGiveawaysFragment(), new SavedDiscussionsFragment());
        viewPager.setAdapter(viewPagerAdapter);

        TabLayout tabLayout = layout.findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);

        return layout;
    }

    private class TitledPagerAdapter extends FragmentAdapter {
        public TitledPagerAdapter(AppCompatActivity activity, ViewPager viewPager, Fragment... fragments) {
            super(getChildFragmentManager(), activity, viewPager, fragments);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Fragment fragment = getItem(position);
            if (fragment instanceof IActivityTitle)
                return getString(((IActivityTitle) fragment).getTitleResource());
            return null;
        }
    }
}
