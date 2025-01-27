package net.mabako.steamgifts.adapters;

import android.app.Activity;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import net.mabako.steamgifts.adapters.viewholder.DiscussionListItemViewHolder;
import net.mabako.steamgifts.data.Discussion;

import java.io.Serial;
import java.util.List;

public class DiscussionAdapter extends EndlessAdapter {
    @Serial
    private static final long serialVersionUID = 8932865507723121067L;

    /**
     * Discussions that are shown per page.
     */
    private static final int ITEMS_PER_PAGE = 100;

    /**
     * Context of this adapter.
     */
    private transient Activity context;

    public void setFragmentValues(OnLoadListener listener, @NonNull Activity context) {
        setLoadListener(listener);
        this.context = context;
    }

    @Override
    protected RecyclerView.ViewHolder onCreateActualViewHolder(View view, int viewType) {
        if (context == null)
            throw new IllegalStateException("Got no context");

        return new DiscussionListItemViewHolder(view, context, this);
    }

    @Override
    protected void onBindActualViewHolder(RecyclerView.ViewHolder h, int position) {
        if (h instanceof DiscussionListItemViewHolder holder) {
            Discussion discussion = (Discussion) getItem(position);

            holder.setFrom(discussion);
        }
    }

    @Override
    protected boolean hasEnoughItems(List<IEndlessAdaptable> items) {
        return items.size() == ITEMS_PER_PAGE;
    }
}
