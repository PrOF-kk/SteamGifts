package net.mabako.steamgifts.data;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.Nullable;

import java.io.Serial;
import java.io.Serializable;

/**
 * A giveaway that can easily be passed around. Keep in mind this isn't optimal for displaying a
 * giveaway per se, but loading giveaways from
 * {@link net.mabako.steamgifts.activities.UrlHandlingActivity#getIntentForUri(Context, Uri)} gives
 * us no real content outside of the Giveaway Id.
 */
public class BasicGiveaway implements Serializable {
    @Serial
    private static final long serialVersionUID = 8330168808371401692L;
    private @Nullable String giveawayId;

    public BasicGiveaway(@Nullable String giveawayId) {
        this.giveawayId = giveawayId;
    }

    @Nullable
    public String getGiveawayId() {
        return giveawayId;
    }

    @Override
    public int hashCode() {
        if (giveawayId == null)
            return 0;
        return giveawayId.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof BasicGiveaway basicGiveaway && giveawayId != null) {
            return giveawayId.equals(basicGiveaway.giveawayId);
        }
        return false;

    }
}
