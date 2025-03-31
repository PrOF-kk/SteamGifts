package net.mabako.steamgifts.adapters.viewholder;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import net.mabako.steamgifts.activities.CommonActivity;
import net.mabako.steamgifts.activities.DetailActivity;
import net.mabako.steamgifts.activities.SyncActivity;
import net.mabako.steamgifts.core.R;
import net.mabako.steamgifts.data.GameFeatures;
import net.mabako.steamgifts.data.GameFeaturesRepository;
import net.mabako.steamgifts.data.Giveaway;
import net.mabako.steamgifts.data.GiveawayExtras;
import net.mabako.steamgifts.fragments.GiveawayDetailFragment;
import net.mabako.steamgifts.fragments.util.GiveawayDetailsCard;
import net.mabako.steamgifts.persistentdata.SteamGiftsUserData;

import java.util.StringJoiner;

public class GiveawayCardViewHolder extends RecyclerView.ViewHolder {
    private final GiveawayDetailFragment fragment;

    private final View progressBar;
    private final TextView title;
    private final View gameHiddenIndicator;
    private final TextView user;
    private final TextView timeRemaining;
    private final TextView timeCreated;
    private final TextView description;
    private final TextView entries, copies;

    private final Button enterGiveaway;
    private final Button leaveGiveaway;
    private final Button viewWinners;
    private final Button commentGiveaway;
    private final Button loginButton;
    private final Button errorMessage;
    private final Button indicator;
    private final View separator, actionSeparator;

    public GiveawayCardViewHolder(View v, final GiveawayDetailFragment fragment) {
        super(v);
        this.fragment = fragment;

        progressBar = v.findViewById(R.id.progressBar);
        title = v.findViewById(R.id.giveaway_name);
        gameHiddenIndicator = v.findViewById(R.id.game_hidden_indicator);
        user = v.findViewById(R.id.user);
        timeRemaining = v.findViewById(R.id.remaining);
        timeCreated = v.findViewById(R.id.created);
        description = v.findViewById(R.id.description);
        description.setMovementMethod(LinkMovementMethod.getInstance());
        entries = v.findViewById(R.id.entries);
        copies = v.findViewById(R.id.copies);
        separator = v.findViewById(R.id.separator);
        actionSeparator = v.findViewById(R.id.action_separator);

        enterGiveaway = v.findViewById(R.id.enter);
        leaveGiveaway = v.findViewById(R.id.leave);
        viewWinners = v.findViewById(R.id.winners);
        commentGiveaway = v.findViewById(R.id.comment);
        errorMessage = v.findViewById(R.id.error);
        loginButton = v.findViewById(R.id.login);
        loginButton.setOnClickListener(v2 -> ((CommonActivity) fragment.getActivity()).requestLogin());
        indicator = v.findViewById(R.id.indicator);
    }

    @SuppressLint("SetTextI18n")
    public void setFrom(final GiveawayDetailsCard card) {
        final Giveaway giveaway = card.getGiveaway();
        final GiveawayExtras extras = card.getExtras();

        for (View view : new View[]{enterGiveaway, leaveGiveaway, viewWinners, commentGiveaway, loginButton, errorMessage, description, indicator, user, title, timeRemaining, timeCreated, entries, copies, separator, actionSeparator})
            view.setVisibility(View.GONE);

        if (giveaway == null) {
            progressBar.setVisibility(View.VISIBLE);
        } else {
            user.setText("{faw-user} " + giveaway.getCreator());
            user.setOnClickListener(v -> fragment.showProfile(giveaway.getCreator()));

            for (View view : new View[]{user, title, timeRemaining, timeCreated, separator})
                view.setVisibility(View.VISIBLE);

            title.setText(giveaway.getTitle());

            gameHiddenIndicator.setVisibility(extras.isGameHidden() ? View.VISIBLE : View.GONE);

            if (giveaway.getEndTime() != null) {
                timeRemaining.setText("{faw-clock-o} " + giveaway.getRelativeEndTime(fragment.getContext()));

                if (giveaway.getCreatedTime() != null)
                    timeCreated.setText("{faw-calendar-o} " + giveaway.getRelativeCreatedTime(fragment.getContext()));
                else
                    timeCreated.setVisibility(View.GONE);
            } else {
                timeRemaining.setVisibility(View.GONE);
                timeCreated.setVisibility(View.GONE);
            }

            enterGiveaway.setText(itemView.getContext().getString(R.string.enter_giveaway_with_points, giveaway.getPoints()));
            leaveGiveaway.setText(itemView.getContext().getString(R.string.leave_giveaway_with_points, giveaway.getPoints()));

            if (giveaway.getEntries() >= 0) {
                entries.setText("{faw-users} " + fragment.getContext().getResources().getQuantityString(R.plurals.entries, giveaway.getEntries(), giveaway.getEntries()));
                entries.setVisibility(View.VISIBLE);
            }

            if (giveaway.getCopies() > 1) {
                copies.setText("{faw-clone} " + fragment.getContext().getResources().getQuantityString(R.plurals.copies, giveaway.getCopies(), giveaway.getCopies()));
                copies.setVisibility(View.VISIBLE);
            }

            if (extras == null) {
                // Still loading...
                progressBar.setVisibility(View.VISIBLE);
            } else {
                progressBar.setVisibility(View.GONE);

                if (extras.getDescription() != null) {
                    description.setText(StringUtils.fromHtml(fragment.getActivity(), extras.getDescription()));
                    description.setVisibility(View.VISIBLE);
                    actionSeparator.setVisibility(View.VISIBLE);
                }

                if (extras.getXsrfToken() != null && extras.getErrorMessage() == null && extras.isEnterable()) {
                    if (!extras.isEntered())
                        enterGiveaway.setVisibility(View.VISIBLE);
                    else
                        leaveGiveaway.setVisibility(View.VISIBLE);
                } else if (extras.getErrorMessage() != null) {
                    errorMessage.setText(extras.getErrorMessage());
                    errorMessage.setVisibility(View.VISIBLE);

                    if ("Sync Required".equals(extras.getErrorMessage())) {
                        errorMessage.setEnabled(true);
                        errorMessage.setOnClickListener(v -> fragment.getActivity().startActivityForResult(new Intent(fragment.getContext(), SyncActivity.class), SyncActivity.REQUEST_SYNC));
                    }
                } else if (extras.getWinners() != null) {
                    viewWinners.setText("{faw-trophy} " + extras.getWinners());
                    viewWinners.setVisibility(View.VISIBLE);
                } else if (!SteamGiftsUserData.getCurrent(null).isLoggedIn()) {
                    loginButton.setVisibility(View.VISIBLE);
                }

                if (extras.getXsrfToken() != null)
                    commentGiveaway.setVisibility(View.VISIBLE);

                enterGiveaway.setEnabled(true);
                leaveGiveaway.setEnabled(true);

                setupIndicators(giveaway);
            }

            enterGiveaway.setOnClickListener(v -> {
                if (extras != null) {
                    enterGiveaway.setEnabled(false);
                    fragment.requestEnterLeave(giveaway.getGiveawayId(), GiveawayDetailFragment.ENTRY_INSERT, extras.getXsrfToken());
                }
            });

            leaveGiveaway.setOnClickListener(v -> {
                if (extras != null) {
                    leaveGiveaway.setEnabled(false);
                    fragment.requestEnterLeave(giveaway.getGiveawayId(), GiveawayDetailFragment.ENTRY_DELETE, extras.getXsrfToken());
                }
            });

            viewWinners.setOnClickListener(v -> {
                Intent intent = new Intent(fragment.getContext(), DetailActivity.class);
                intent.putExtra(DetailActivity.ARG_GIVEAWAY_DETAILS, new DetailActivity.GiveawayDetails(DetailActivity.GiveawayDetails.Type.WINNERS, giveaway.getGiveawayId() + "/" + giveaway.getName(), giveaway.getTitle()));

                fragment.getActivity().startActivityForResult(intent, CommonActivity.REQUEST_LOGIN_PASSIVE);
            });

            commentGiveaway.setOnClickListener(v -> fragment.requestComment(null));
        }

        AttachedImageUtils.setFrom(itemView, extras, (CommonActivity) (fragment.getActivity()));
    }

    private void setupIndicators(final Giveaway giveaway) {
        StringJoiner sj = new StringJoiner(" ");

        if (giveaway.isPrivate())
            sj.add("{faw-lock}");
        if (giveaway.isWhitelist())
            sj.add("{faw-heart}");
        if (giveaway.isGroup())
            sj.add("{faw-users}");
        if (giveaway.isRegionRestricted())
            sj.add("{faw-globe}");

        if (giveaway.getLevel() > 0)
            sj.add("L" + giveaway.getLevel());

        GameFeatures gameFeatures = GameFeaturesRepository.waitForGameFeaturesDownload().join().getGameFeatures(giveaway.getGame().getId());
        if (gameFeatures.getCards() > 0)
            sj.add("{faw-ticket}");
        if (gameFeatures.isDlc())
            sj.add("{faw-download}");
        if (gameFeatures.isLimited())
            sj.add("{faw-asterisk}");
        if (gameFeatures.isDelisted())
            sj.add("{faw-trash}");

        if (sj.length() > 0) {
            indicator.setVisibility(View.VISIBLE);

            indicator.setText(sj.toString());

            if (giveaway.isGroup()) {
                indicator.setOnClickListener(v -> {
                    Intent intent = new Intent(fragment.getContext(), DetailActivity.class);
                    intent.putExtra(DetailActivity.ARG_GIVEAWAY_DETAILS, new DetailActivity.GiveawayDetails(DetailActivity.GiveawayDetails.Type.GROUPS, giveaway.getGiveawayId() + "/" + giveaway.getName(), giveaway.getTitle()));

                    fragment.getActivity().startActivityForResult(intent, CommonActivity.REQUEST_LOGIN_PASSIVE);
                });
            } else {
                indicator.setOnClickListener(null);
            }
        }
    }
}
