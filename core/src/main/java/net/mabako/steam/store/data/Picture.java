package net.mabako.steam.store.data;

import net.mabako.steamgifts.adapters.IEndlessAdaptable;
import net.mabako.steamgifts.core.R;

import java.io.Serializable;

public record Picture(
        String url,
        boolean inline
) implements IEndlessAdaptable, Serializable {
    public static final int VIEW_LAYOUT_WIDE = R.layout.store_picture;
    public static final int VIEW_LAYOUT_INLINE = R.layout.store_picture_inline;

    @Override
    public int getLayout() {
        return inline ? VIEW_LAYOUT_INLINE : VIEW_LAYOUT_WIDE;
    }
}
