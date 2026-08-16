package net.mabako.steam.store.data;

import net.mabako.steamgifts.adapters.IEndlessAdaptable;
import net.mabako.steamgifts.core.R;

import java.io.Serializable;

public record Text(
        String text,
        boolean html
) implements IEndlessAdaptable, Serializable {
    public static final int VIEW_LAYOUT = R.layout.text_item;

    @Override
    public int getLayout() {
        return VIEW_LAYOUT;
    }
}
