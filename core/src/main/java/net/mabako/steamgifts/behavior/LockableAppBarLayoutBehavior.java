package net.mabako.steamgifts.behavior;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;

/// Behavior that can lock [CollapsingToolbarLayout] status
public class LockableAppBarLayoutBehavior extends AppBarLayout.Behavior {

    boolean locked;
    private final DragCallback dragCallBack = new DragCallback() {
        @Override
        public boolean canDrag(@NonNull AppBarLayout appBarLayout) {
            return !locked;
        }
    };

    public LockableAppBarLayoutBehavior() {
        setDragCallback(dragCallBack);
    }

    public LockableAppBarLayoutBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDragCallback(dragCallBack);
    }

    @Override
    public boolean onStartNestedScroll(@NonNull CoordinatorLayout parent, @NonNull AppBarLayout child, @NonNull View directTargetChild, View target, int nestedScrollAxes, int type) {
        if (locked) {
            return false;
        }

        return super.onStartNestedScroll(parent, child, directTargetChild, target, nestedScrollAxes, type);
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }
}
