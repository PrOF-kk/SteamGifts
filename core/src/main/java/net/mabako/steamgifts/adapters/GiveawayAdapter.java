package net.mabako.steamgifts.adapters;

import android.app.Activity;
import android.content.SharedPreferences;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import net.mabako.steamgifts.adapters.viewholder.GiveawayListItemViewHolder;
import net.mabako.steamgifts.data.Game;
import net.mabako.steamgifts.data.Giveaway;
import net.mabako.steamgifts.fragments.ListFragment;
import net.mabako.steamgifts.persistentdata.FilterData;
import net.mabako.steamgifts.persistentdata.SavedGiveaways;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

public class GiveawayAdapter extends EndlessAdapter {
    @Serial
    private static final long serialVersionUID = 4291118458389025091L;

    /**
     * Giveaways that are shown per page.
     */
    private final int itemsPerPage;

    /**
     * Context of this adapter.
     */
    private transient Activity context;

    /**
     * Fragment this is shown in.
     */
    private transient Fragment fragment;

    /**
     * Should we filter the item for any criteria?
     */
    private final boolean filterItems;

    /**
     * Instance of SavedGiveaways to save/unsave giveaways.
     */
    private transient SavedGiveaways savedGiveaways;

    /**
     * Should we load images for this list?
     */
    private final boolean loadImages;

    public GiveawayAdapter(int itemsPerPage, SharedPreferences sharedPreferences) {
        this(itemsPerPage, false, sharedPreferences);
    }

    public GiveawayAdapter(int itemsPerPage, boolean filterItems, SharedPreferences sharedPreferences) {
        this.itemsPerPage = itemsPerPage;
        this.filterItems = filterItems;
        this.loadImages = sharedPreferences.getString("preference_giveaway_load_images", "details;list").contains("list");
        setHasStableIds(true);
    }

    public void setFragmentValues(@NonNull Activity activity, @NonNull ListFragment fragment, SavedGiveaways savedGiveaways) {
        setLoadListener(fragment);
        this.context = activity;
        this.fragment = fragment;
        this.savedGiveaways = savedGiveaways;
    }

    @Override
    protected RecyclerView.ViewHolder onCreateActualViewHolder(View view, int viewType) {
        return new GiveawayListItemViewHolder(view, context, this, fragment, savedGiveaways);
    }

    @Override
    public void onBindActualViewHolder(RecyclerView.ViewHolder h, int position) {
        if (h instanceof GiveawayListItemViewHolder holder) {
            Giveaway giveaway = (Giveaway) getItem(position);

            holder.setFrom(giveaway, loadImages);
        }
    }

    @Override
    protected boolean hasEnoughItems(List<IEndlessAdaptable> items) {
        return items.size() >= itemsPerPage;
    }

    public @Nullable Giveaway findItem(@NonNull String giveawayId) {
        for (IEndlessAdaptable adaptable : getItems()) {
            Giveaway giveaway = (Giveaway) adaptable;
            if (giveaway != null && giveawayId.equals(giveaway.getGiveawayId()))
                return giveaway;
        }
        return null;
    }

    public void removeGiveaway(String giveawayId) {
        for (int position = getItems().size() - 1; position >= 0; --position) {
            Giveaway giveaway = (Giveaway) getItem(position);

            if (giveaway != null && giveawayId.equals(giveaway.getGiveawayId())) {
                removeItem(position);
            }
        }
    }

    public List<RemovedElement> removeHiddenGame(long internalGameId) {
        if (internalGameId == Game.NO_APP_ID)
            throw new IllegalStateException();

        List<EndlessAdapter.RemovedElement> removedElements = new ArrayList<>();
        for (int position = getItems().size() - 1; position >= 0; --position) {
            Giveaway giveaway = (Giveaway) getItem(position);

            if (giveaway != null && giveaway.getInternalGameId() == internalGameId) {
                removedElements.add(removeItem(position));
            }
        }

        // At this point, the first element in removedElements is actually the last element of the original adapter, since we went through it in reverse; so we ... reverse it.
        // Since we store the element and the one before it, we can reasonably get rid of a series of successive giveaways and restore them in the original order.
        Collections.reverse(removedElements);
        return removedElements;
    }

    public EndlessAdapter.RemovedElement removeSwipedGiveaway(int position) {
        return removeItem(position);
    }

    @Override
    protected int addFiltered(List<IEndlessAdaptable> items) {
        if (filterItems && fragment != null) {
            FilterData fd = FilterData.getCurrent(fragment.getContext());

            boolean hideEntered = fd.isHideEntered();

            boolean checkLevelOnlyOnPublicGiveaway = fd.isRestrictLevelOnlyOnPublicGiveaways();
            int minLevel = fd.getMinLevel();
            int maxLevel = fd.getMaxLevel();

            boolean entriesPerCopy = fd.isEntriesPerCopy();
            int minEntries = fd.getMinEntries();
            int maxEntries = fd.getMaxEntries();

            if (hideEntered
                    || (checkLevelOnlyOnPublicGiveaway && (minLevel >= 0 || maxLevel >= 0))
                    || (entriesPerCopy && (minEntries >= 0 || maxEntries >= 0))) {
                // Let's actually perform filtering if we have any options set.
                for (ListIterator<IEndlessAdaptable> iter = items.listIterator(items.size()); iter.hasPrevious(); ) {
                    Giveaway giveaway = (Giveaway) iter.previous();
                    int level = giveaway.getLevel();
                    int entriesPerCopyValue = giveaway.getEntries() / giveaway.getCopies();

                    if (hideEntered && giveaway.isEntered()) {
                        iter.remove();
                    } else if (checkLevelOnlyOnPublicGiveaway && !giveaway.isGroup() && !giveaway.isWhitelist() && ((minLevel >= 0 && level < minLevel) || (maxLevel >= 0 && level > maxLevel))) {
                        iter.remove();
                    } else if (entriesPerCopy && (minEntries >= 0 && entriesPerCopyValue < minEntries) || (maxEntries >= 0 && entriesPerCopyValue > maxEntries)) {
                        iter.remove();
                    }
                }
            }
        }
        return super.addFiltered(items);
    }

    @Override
    public long getItemId(int position) {
        IEndlessAdaptable item = getItem(position);
        return item != null
                ? item.hashCode()
                // Loading item
                : Integer.MAX_VALUE;
    }
}
