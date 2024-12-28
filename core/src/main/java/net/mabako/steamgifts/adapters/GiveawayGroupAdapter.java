package net.mabako.steamgifts.adapters;

import android.content.Context;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import net.mabako.steamgifts.adapters.viewholder.GiveawayGroupViewHolder;
import net.mabako.steamgifts.data.GiveawayGroup;

import java.io.Serial;
import java.util.List;

public class GiveawayGroupAdapter extends EndlessAdapter {
    @Serial
    private static final long serialVersionUID = -7332179308183390985L;

    /**
     * Groups that are shown per page.
     */
    private static final int ITEMS_PER_PAGE = 25; // TODO actual number?

    /**
     * Context of this adapter.
     */
    private transient Context context;

    public GiveawayGroupAdapter() {
        this.alternativeEnd = true;
    }

    public void setFragmentValues(OnLoadListener listener, Context context) {
        setLoadListener(listener);
        this.context = context;
    }

    @Override
    protected RecyclerView.ViewHolder onCreateActualViewHolder(View view, int viewType) {
        if (context == null)
            throw new IllegalStateException("no context");

        return new GiveawayGroupViewHolder(view, context);
    }

    @Override
    protected void onBindActualViewHolder(RecyclerView.ViewHolder h, int position) {
        if (h instanceof GiveawayGroupViewHolder holder) {
            GiveawayGroup group = (GiveawayGroup) getItem(position);
            holder.setFrom(group);
        }
    }

    @Override
    protected boolean hasEnoughItems(List<IEndlessAdaptable> items) {
        return items.size() == ITEMS_PER_PAGE;
    }
}
