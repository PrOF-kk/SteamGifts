package net.mabako.steamgifts.data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.mabako.steamgifts.adapters.IEndlessAdaptable;
import net.mabako.steamgifts.core.R;
import net.mabako.steamgifts.persistentdata.SteamGiftsUserData;

import java.io.Serial;
import java.util.Calendar;

public class Giveaway extends BasicGiveaway implements IEndlessAdaptable {
    @Serial
    private static final long serialVersionUID = 1356878822345232771L;
    public static final int VIEW_LAYOUT = R.layout.giveaway_item;

    /**
     * Note: SteamGifts truncates the giveaway title on the giveaway list, thus this may not be
     * accurate prior to opening the giveaway page. {@link GiveawayExtras#setTitle(String)} is
     * used on the details page to fix the title being cut off.
     */
    private String title;

    private String name;

    private @NonNull Game game;

    /**
     * Who created this giveaway?
     */
    private String creator;

    private int entries;
    private int copies;
    private int points;

    /**
     * When was this giveaway created?
     */
    private CustomDateTime createdTime;

    /**
     * When will this giveaway end?
     */
    private CustomDateTime endTime;

    /**
     * Have we entered this giveaway?
     */
    private boolean entered;

    private boolean whitelist;
    private boolean group;
    private boolean isPrivate;
    private boolean regionRestricted;

    /**
     * Level required to enter this giveaway.
     */
    private int level;

    /**
     * Id used (exclusively?) for filtering games.
     */
    private long internalGameId;

    public Giveaway() {
        this(null);
    }

    public Giveaway(@Nullable String giveawayId) {
        super(giveawayId);
        game = new Game();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public int getEntries() {
        return entries;
    }

    public void setEntries(int entries) {
        this.entries = entries;
    }

    public int getCopies() {
        return copies;
    }

    public void setCopies(int copies) {
        this.copies = copies;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public String getRelativeEndTime(Context context) {
        return endTime != null ? endTime.toString(context) : null;
    }

    public String getRelativeCreatedTime(Context context) {
        return createdTime != null ? createdTime.toString(context) : null;
    }

    public boolean isOpen() {
        // FIXME
        return endTime == null || endTime.isInTheFuture();
    }

    /**
     * Check if the current user can enter this giveaway.
     */
    public boolean userCanEnter() {
        SteamGiftsUserData currentUser = SteamGiftsUserData.getCurrent(null);

        return
                // The user has enough points
                currentUser.getPoints() >= this.getPoints()
                        // The user's level is high enough
                        && currentUser.getLevel() >= this.getLevel()
                        // The user is not the giveaway's creator
                        && !currentUser.getName().equals(this.getCreator());
    }

    public boolean isEntered() {
        return entered;
    }

    public void setEntered(boolean entered) {
        this.entered = entered;
    }

    public boolean isWhitelist() {
        return whitelist;
    }

    public void setWhitelist(boolean whitelist) {
        this.whitelist = whitelist;
    }

    public boolean isGroup() {
        return group;
    }

    public void setGroup(boolean group) {
        this.group = group;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public boolean isLevelPositive() {
        return level > 0 && SteamGiftsUserData.getCurrent(null).getLevel() >= level;
    }

    public boolean isLevelNegative() {
        return level > 0 && !isLevelPositive();
    }

    public boolean isRegionRestricted() {
        return regionRestricted;
    }

    public void setRegionRestricted(boolean regionRestricted) {
        this.regionRestricted = regionRestricted;
    }

    public Calendar getEndTime() {
        return endTime != null ? endTime.getCalendar() : null;
    }

    public void setEndTime(int endTimestamp, @NonNull String relativeEndTime) {
        this.endTime = new CustomDateTime(endTimestamp, relativeEndTime.startsWith("Begins in "));
    }

    public Calendar getCreatedTime() {
        return createdTime != null ? createdTime.getCalendar() : null;
    }

    public void setCreatedTime(int createdTimestamp) {
        this.createdTime = new CustomDateTime(createdTimestamp, false);
    }

    @NonNull
    @Override
    public String toString() {
        return "[GA " + getGiveawayId() + ", " + getGame().getId() + "]";
    }

    public long getInternalGameId() {
        return internalGameId;
    }

    public void setInternalGameId(long internalGameId) {
        this.internalGameId = internalGameId;
    }

    @Override
    public int getLayout() {
        return VIEW_LAYOUT;
    }

    @Override
    public boolean equals(Object o) {
        if (getGiveawayId() == null && o instanceof Giveaway g) {
            // Compare some random attributes
            return title.equals(g.title) && endTime.equals(g.endTime) && createdTime.equals(g.createdTime);
        } else {
            return super.equals(o);
        }
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public void setPrivate(boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    public void setGame(@NonNull Game game) {
        this.game = game;
    }

    public @NonNull Game getGame() {
        return game;
    }
}
