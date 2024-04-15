package net.mabako.steamgifts.fragments;

import android.app.Activity;
import android.os.Parcelable;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import net.mabako.steamgifts.core.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Simple fragment adapter that basically just holds a list of... fragments, without any fancy schmuck.
 */
// TODO restoring the instance state here leads to a situation where none of this.fragments equals the currently selected item, neither does any directly neighboring fragment. Works if all fragments are recreated though(?)
public abstract class FragmentAdapter extends FragmentStatePagerAdapter implements ViewPager.OnPageChangeListener {
    private final Activity activity;
    private final ViewPager viewPager;

    private List<Fragment> fragments = new ArrayList<>();
    private Fragment transientFragment;

    public FragmentAdapter(AppCompatActivity activity, ViewPager viewPager, Fragment... fragments) {
        super(activity.getSupportFragmentManager());
        this.activity = activity;
        this.viewPager = viewPager;
        this.fragments.addAll(Arrays.asList(fragments));
    }

    public FragmentAdapter(FragmentManager fragmentManager, AppCompatActivity activity, ViewPager viewPager, Fragment... fragments) {
        super(fragmentManager);
        this.activity = activity;
        this.viewPager = viewPager;
        this.fragments.addAll(Arrays.asList(fragments));
    }

    /**
     * This returns a list of all non-transient items.
     *
     * @return list of non-transient items
     */
    public List<Fragment> getItems() {
        return fragments;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        if (position < fragments.size())
            return fragments.get(position);
        if (position == fragments.size() && transientFragment != null)
            return transientFragment;

        throw new IllegalArgumentException("Invalid position: " + position);
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        Fragment fragment = (Fragment) object;
        if (fragments.contains(fragment))
            return fragments.indexOf(fragment);

        if (transientFragment != null && fragment == transientFragment)
            return fragments.size();

        return POSITION_NONE;
    }

    @Override
    public int getCount() {
        int size = fragments.size();
        if (transientFragment != null)
            ++size;

        return size;
    }

    public void add(Fragment fragment) {
        fragments.add(fragment);
        notifyDataSetChanged();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        FloatingActionButton scrollToTopButton = activity.findViewById(R.id.scroll_to_top_button);
        if (scrollToTopButton != null && positionOffsetPixels != 0) {
            scrollToTopButton.hide();
            scrollToTopButton.setTag(null);
        }
    }

    @Override
    public void onPageSelected(int position) {
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        if (state == ViewPager.SCROLL_STATE_IDLE) {
            int position = viewPager.getCurrentItem();

            // Remove the fragment as soon as it is swiped away.
            if (position < fragments.size() && transientFragment != null) {
                transientFragment = null;
                notifyDataSetChanged();
            }

            FloatingActionButton scrollToTopButton = activity.findViewById(R.id.scroll_to_top_button);
            if (scrollToTopButton != null) {
                Fragment fragment = getItem(position);
                if (fragment instanceof ListFragment listFragment) {
                    listFragment.setupScrollToTopButton();
                } else {
                    scrollToTopButton.setOnClickListener(v -> Toast.makeText(activity, "Got no scroll listener, can't scroll to top.", Toast.LENGTH_SHORT).show());
                }
            }
        }
    }

    public void setTransientFragment(Fragment transientFragment) {
        this.transientFragment = transientFragment;
        notifyDataSetChanged();
    }

    @Override
    public Parcelable saveState() {
        if (transientFragment != null)
            setTransientFragment(null);

        return super.saveState();
    }
}
