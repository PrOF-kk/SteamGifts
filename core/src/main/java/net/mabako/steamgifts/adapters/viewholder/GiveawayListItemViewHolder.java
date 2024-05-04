package net.mabako.steamgifts.adapters.viewholder;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import net.mabako.steamgifts.ApplicationTemplate;
import net.mabako.steamgifts.activities.DetailActivity;
import net.mabako.steamgifts.activities.MainActivity;
import net.mabako.steamgifts.adapters.EndlessAdapter;
import net.mabako.steamgifts.core.R;
import net.mabako.steamgifts.data.BasicGiveaway;
import net.mabako.steamgifts.data.Game;
import net.mabako.steamgifts.data.Giveaway;
import net.mabako.steamgifts.fragments.GiveawayDetailFragment;
import net.mabako.steamgifts.fragments.GiveawayListFragment;
import net.mabako.steamgifts.fragments.SavedGiveawaysFragment;
import net.mabako.steamgifts.fragments.interfaces.IHasEnterableGiveaways;
import net.mabako.steamgifts.persistentdata.SavedGiveaways;
import net.mabako.steamgifts.persistentdata.SteamGiftsUserData;

import java.util.Locale;
import java.util.StringJoiner;

public class GiveawayListItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnCreateContextMenuListener, MenuItem.OnMenuItemClickListener {
    private static final String TAG = GiveawayListItemViewHolder.class.getSimpleName();

    private final View itemContainer;
    private final TextView giveawayDetails;
    private final TextView giveawayName;
    private final TextView giveawayTime;
    private final ImageView giveawayImage;
    private final Button giveawayEnterButton;

    private final EndlessAdapter adapter;
    private final Activity activity;
    private final Fragment fragment;
    private SavedGiveaways savedGiveaways;

    private final View indicatorWhitelist, indicatorGroup, indicatorLevelPositive, indicatorLevelNegative, indicatorPrivate, indicatorRegionRestricted;

    private final View indicatorCards, indicatorDLC, indicatorLimited, indicatorDelisted;

    private static int measuredHeight = 0;

    public GiveawayListItemViewHolder(View v, Activity activity, EndlessAdapter adapter, Fragment fragment, SavedGiveaways savedGiveaways) {
        super(v);
        itemContainer = v.findViewById(R.id.list_item);
        giveawayName = v.findViewById(R.id.giveaway_name);
        giveawayDetails = v.findViewById(R.id.giveaway_details);
        giveawayTime = v.findViewById(R.id.time);
        giveawayImage = v.findViewById(R.id.giveaway_image);
        giveawayEnterButton = v.findViewById(R.id.giveaway_enter_button);

        indicatorWhitelist = v.findViewById(R.id.giveaway_list_indicator_whitelist);
        indicatorGroup = v.findViewById(R.id.giveaway_list_indicator_group);
        indicatorLevelPositive = v.findViewById(R.id.giveaway_list_indicator_level_positive);
        indicatorLevelNegative = v.findViewById(R.id.giveaway_list_indicator_level_negative);
        indicatorPrivate = v.findViewById(R.id.giveaway_list_indicator_private);
        indicatorRegionRestricted = v.findViewById(R.id.giveaway_list_indicator_region_restricted);

        indicatorCards = v.findViewById(R.id.giveaway_list_indicator_cards);
        indicatorDLC = v.findViewById(R.id.giveaway_list_indicator_dlc);
        indicatorLimited = v.findViewById(R.id.giveaway_list_indicator_limited);
        indicatorDelisted = v.findViewById(R.id.giveaway_list_indicator_delisted);

        this.activity = activity;
        this.fragment = fragment;
        this.adapter = adapter;
        this.savedGiveaways = savedGiveaways;

        v.setOnClickListener(this);
        v.setOnCreateContextMenuListener(this);
    }

    public void setFrom(Giveaway giveaway, boolean showImage) {
        giveawayName.setText(giveaway.getTitle());

        if (giveaway.getEndTime() != null) {
            giveawayTime.setText(giveaway.getRelativeEndTime(activity));
        } else {
            giveawayTime.setVisibility(View.GONE);
        }

        // Unicode "Bullet"
        StringJoiner sj = new StringJoiner(" • ");
        if (giveaway.getCopies() > 1)
            sj.add(activity.getResources().getQuantityString(R.plurals.copies, giveaway.getCopies(), giveaway.getCopies()));

        if (giveaway.getPoints() >= 0)
            sj.add(giveaway.getPoints() + "P");

        if (giveaway.getLevel() > 0)
            sj.add("L" + giveaway.getLevel());

        if (giveaway.getEntries() >= 0)
            sj.add(activity.getResources().getQuantityString(R.plurals.entries, giveaway.getEntries(), giveaway.getEntries()));

        giveawayDetails.setText(sj.toString());

        // giveaway_image
        if (giveaway.getGame().getId() != Game.NO_APP_ID && showImage && ((ApplicationTemplate) activity.getApplication()).allowGameImages()) {
            Picasso.get().load("https://cdn.akamai.steamstatic.com/steam/" + giveaway.getGame().getType().name().toLowerCase(Locale.ENGLISH) + "s/" + giveaway.getGame().getId() + "/capsule_184x69.jpg").into(giveawayImage, new Callback() {
                /**
                 * We manually set the height of this image to fit the container.
                 */
                @Override
                public void onSuccess() {
                    if (measuredHeight <= 0)
                        measuredHeight = itemContainer.getMeasuredHeight();

                    ViewGroup.LayoutParams params = giveawayImage.getLayoutParams();
                    params.height = measuredHeight;
                }

                @Override
                public void onError(Exception e) {
                    Log.e(TAG, "Failed to load capsule image for giveaway " + giveaway.getName() + " (game " + giveaway.getGame().getId() + ")", e);
                    ViewGroup.LayoutParams params = giveawayImage.getLayoutParams();
                    params.height = 0;
                }
            });
        } else {
            giveawayImage.setImageResource(android.R.color.transparent);
            ViewGroup.LayoutParams params = giveawayImage.getLayoutParams();
            params.height = 0;
        }

        StringUtils.setBackgroundDrawable(activity, itemContainer, giveaway.isEntered());

        // Check all the indicators
        indicatorWhitelist.setVisibility(giveaway.isWhitelist() ? View.VISIBLE : View.GONE);
        indicatorGroup.setVisibility(giveaway.isGroup() ? View.VISIBLE : View.GONE);
        indicatorLevelPositive.setVisibility(giveaway.isLevelPositive() ? View.VISIBLE : View.GONE);
        indicatorLevelNegative.setVisibility(giveaway.isLevelNegative() ? View.VISIBLE : View.GONE);
        indicatorPrivate.setVisibility(giveaway.isPrivate() ? View.VISIBLE : View.GONE);
        indicatorRegionRestricted.setVisibility(giveaway.isRegionRestricted() ? View.VISIBLE : View.GONE);

        indicatorCards.setVisibility(giveaway.getGame().getGameFeatures().getCards() > 0 ? View.VISIBLE : View.GONE);
        indicatorDLC.setVisibility(giveaway.getGame().getGameFeatures().isDlc() ? View.VISIBLE : View.GONE);
        indicatorLimited.setVisibility(giveaway.getGame().getGameFeatures().isLimited() ? View.VISIBLE : View.GONE);
        indicatorDelisted.setVisibility(giveaway.getGame().getGameFeatures().isDelisted() ? View.VISIBLE : View.GONE);

        // Initialize the enter button
        // Check if logged or the quick enter button setting is enabled
        boolean loggedIn = SteamGiftsUserData.getCurrent(null).isLoggedIn();
        if (!loggedIn || !giveaway.isOpen() || !(fragment instanceof IHasEnterableGiveaways) || !PreferenceManager.getDefaultSharedPreferences(fragment.getContext()).getBoolean("preference_giveaway_show_quick_enter", true)) {
            giveawayEnterButton.setVisibility(View.GONE);
        } else {
            giveawayEnterButton.setVisibility(View.VISIBLE);

            // Set the correct text based on the giveaway is already entered or not
            if (giveaway.isEntered()) {
                giveawayEnterButton.setText("{faw-times}");
                giveawayEnterButton.setEnabled(true);
            } else {
                giveawayEnterButton.setText("{faw-sign-in}");
                // Check if the user have enough points, that the giveaway is not from him and that he have the required level
                giveawayEnterButton.setEnabled(giveaway.userCanEnter());
            }

            // Set the event
            giveawayEnterButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Giveaway giveaway = (Giveaway) adapter.getItem(getAdapterPosition());

                    // When clicking this too quickly after another, you can essentially click on a giveaway that is null.
                    // There's probably no need to show a notification or anything, since there is visual feedback for whether or not you entered a giveaway.
                    if (giveaway != null)
                        ((IHasEnterableGiveaways) fragment).requestEnterLeave(giveaway.getGiveawayId(), giveaway.isEntered() ? GiveawayDetailFragment.ENTRY_DELETE : GiveawayDetailFragment.ENTRY_INSERT, adapter.getXsrfToken());
                }
            });
        }
    }

    @Override
    public void onClick(View v) {
        Giveaway giveaway = (Giveaway) adapter.getItem(getAdapterPosition());
        if (giveaway.getGiveawayId() != null && giveaway.getName() != null) {
            Intent intent = new Intent(activity, DetailActivity.class);

            if (giveaway.getInternalGameId() != Game.NO_APP_ID) {
                intent.putExtra(GiveawayDetailFragment.ARG_GIVEAWAY, giveaway);
            } else {
                intent.putExtra(GiveawayDetailFragment.ARG_GIVEAWAY, new BasicGiveaway(giveaway.getGiveawayId()));
            }

            activity.startActivityForResult(intent, MainActivity.REQUEST_LOGIN_PASSIVE);
        } else {
            Toast.makeText(activity, R.string.private_giveaway, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        // Are we logged in & do we have a token to submit with our "form"?
        if (fragment != null && (adapter.getXsrfToken() != null || savedGiveaways != null)) {

            // Which giveaway is this even for?
            final Giveaway giveaway = (Giveaway) adapter.getItem(getAdapterPosition());

            // We only know this giveaway exists, not the link to it.
            if (giveaway.getGiveawayId() == null || giveaway.getName() == null)
                return;

            boolean xsrfEvents = adapter.getXsrfToken() != null && giveaway.isOpen();
            boolean loggedIn = SteamGiftsUserData.getCurrent(null).isLoggedIn();

            // Header
            menu.setHeaderTitle(giveaway.getTitle());

            if (loggedIn && xsrfEvents && fragment instanceof IHasEnterableGiveaways) {
                // Text for Entering or Leaving the giveaway
                String enterText = activity.getString(R.string.enter_giveaway);
                String leaveText = activity.getString(R.string.leave_giveaway);

                // Include the points if we know
                if (giveaway.getPoints() >= 0) {
                    enterText = String.format(activity.getString(R.string.enter_giveaway_with_points), giveaway.getPoints());
                    leaveText = String.format(activity.getString(R.string.leave_giveaway_with_points), giveaway.getPoints());
                }

                // Show the relevant menu item.
                if (giveaway.isEntered()) {
                    menu.add(Menu.NONE, 1, Menu.NONE, leaveText).setOnMenuItemClickListener(this);
                } else {
                    menu.add(Menu.NONE, 2, Menu.NONE, enterText).setOnMenuItemClickListener(this).setEnabled(giveaway.userCanEnter());
                }
            }

            // Save/Un-save a game
            if (savedGiveaways != null && giveaway.getEndTime() != null) {
                if (!savedGiveaways.exists(giveaway.getGiveawayId())) {
                    menu.add(Menu.NONE, 4, Menu.NONE, R.string.add_saved_giveaway).setOnMenuItemClickListener(this);
                } else {
                    menu.add(Menu.NONE, 5, Menu.NONE, R.string.remove_saved_giveaway).setOnMenuItemClickListener(this);
                }
            }

            // Hide a game... forever
            if (loggedIn && xsrfEvents && giveaway.getInternalGameId() > 0 && fragment instanceof GiveawayListFragment) {
                menu.add(Menu.NONE, 3, Menu.NONE, R.string.hide_game).setOnMenuItemClickListener(this);
            }
        } else {
            Log.d(TAG, "Not showing context menu for giveaway. (xsrf-token: " + adapter.getXsrfToken() + ")");
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        Giveaway giveaway = (Giveaway) adapter.getItem(getAdapterPosition());
        if (giveaway == null) {
            Toast.makeText(fragment.getContext(), "Error, please try again.", Toast.LENGTH_SHORT).show();
            return false;
        }

        Log.d(TAG, "onMenuItemClick(" + item.getItemId() + ")");
        switch (item.getItemId()) {
            case 1:
                ((IHasEnterableGiveaways) fragment).requestEnterLeave(giveaway.getGiveawayId(), GiveawayDetailFragment.ENTRY_DELETE, adapter.getXsrfToken());
                return true;
            case 2:
                ((IHasEnterableGiveaways) fragment).requestEnterLeave(giveaway.getGiveawayId(), GiveawayDetailFragment.ENTRY_INSERT, adapter.getXsrfToken());
                return true;
            case 3:
                ((GiveawayListFragment) fragment).requestHideGame(giveaway.getInternalGameId(), giveaway.getTitle());
                return true;
            case 4:
                if (savedGiveaways.add(giveaway, giveaway.getGiveawayId())) {
                    Toast.makeText(fragment.getContext(), R.string.added_saved_giveaway, Toast.LENGTH_SHORT).show();
                }
                return true;
            case 5:
                if (savedGiveaways.remove(giveaway.getGiveawayId())) {
                    Toast.makeText(fragment.getContext(), R.string.removed_saved_giveaway, Toast.LENGTH_SHORT).show();
                    if (fragment instanceof SavedGiveawaysFragment)
                        ((SavedGiveawaysFragment) fragment).onRemoveSavedGiveaway(giveaway.getGiveawayId());
                }
                return true;
        }
        return false;
    }
}
