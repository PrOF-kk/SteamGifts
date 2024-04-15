package net.mabako.steamgifts.behavior;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import net.mabako.steamgifts.core.R;
import net.mabako.steamgifts.fragments.FragmentAdapter;

/**
 * Floating Action Button that is hiding if you're scrolling down a {@link RecyclerView}, and is
 * visible if you're beyond the first element and scroll up again.
 */
public class ScrollAwareFABBehavior extends FloatingActionButton.Behavior {
    public ScrollAwareFABBehavior(Context context, AttributeSet attrs) {
        super();
    }

    @Override
    public boolean onStartNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull FloatingActionButton child, @NonNull View directTargetChild, @NonNull View target, int nestedScrollAxes, int type) {
        return (nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL) || super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target, nestedScrollAxes, type);
    }

    @Override
    public void onNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull FloatingActionButton child, @NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type);

        if (child.getTag() == null) {
            return;
        }

        // try to get the current page, if there's any.
        View view = coordinatorLayout;
        ViewPager viewPager = coordinatorLayout.findViewById(R.id.viewPager);
        if (viewPager != null && viewPager.getAdapter() instanceof FragmentAdapter pagerAdapter) {
            int currentPage = viewPager.getCurrentItem();

            Fragment currentItem = pagerAdapter.getItem(currentPage);

            view = currentItem.getView();
        }

        if (view == null) {
            // We do not have a view we can immediately find.
            child.hide();
            return;
        }

        // Hide if we're over the first item
        RecyclerView recyclerView = view.findViewById(R.id.list);
        if (recyclerView != null && recyclerView.getLayoutManager() instanceof LinearLayoutManager layoutManager) {
            if (layoutManager.findFirstVisibleItemPosition() == 0) {
                child.hide();
            } else if (dyConsumed > 1 && child.getVisibility() == View.VISIBLE) {
                child.hide();
            } else if (dyConsumed < 1 && child.getVisibility() != View.VISIBLE) {
                child.show();
            }
        } else {
            // no recyclerview to attach to?
            child.hide();
        }
    }
}
