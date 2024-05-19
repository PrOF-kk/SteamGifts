package net.mabako.steam.store.data;

import net.mabako.steamgifts.adapters.IEndlessAdaptable;
import net.mabako.steamgifts.core.R;

import java.io.Serializable;

public class Picture implements IEndlessAdaptable, Serializable {
    private static final long serialVersionUID = 1373131985788155321L;
    public static final int VIEW_LAYOUT_WIDE = R.layout.store_picture;
    public static final int VIEW_LAYOUT_INLINE = R.layout.store_picture_inline;

    private final String url;
    private final boolean inline;

    public Picture(String url, boolean inline) {
        this.url = url;
        this.inline = inline;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public int getLayout() {
        return  inline ? VIEW_LAYOUT_INLINE : VIEW_LAYOUT_WIDE;
    }
}
