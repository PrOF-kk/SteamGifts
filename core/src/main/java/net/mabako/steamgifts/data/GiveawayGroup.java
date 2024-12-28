package net.mabako.steamgifts.data;

import net.mabako.steamgifts.adapters.IEndlessAdaptable;
import net.mabako.steamgifts.core.R;

import java.io.Serial;
import java.io.Serializable;

public class GiveawayGroup implements IEndlessAdaptable, Serializable {
    public static final int VIEW_LAYOUT = R.layout.giveaway_group_item;
    @Serial
    private static final long serialVersionUID = 6889558816716859611L;

    private final String id;
    private final String title;
    private final String avatar;

    public GiveawayGroup(String id, String title, String avatar) {
        this.id = id;
        this.title = title;
        this.avatar = avatar;
    }

    public String getTitle() {
        return title;
    }

    public String getAvatar() {
        return avatar;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof GiveawayGroup group) {
            return group.id.equals(id);
        }
        return false;
    }

    @Override
    public int getLayout() {
        return VIEW_LAYOUT;
    }
}
