package net.mabako.steamgifts.intro;

import net.mabako.steamgifts.core.R;

/**
 * Created by mabako on 14.01.2016.
 */
public enum SubView {
    WELCOME(R.layout.intro_0_welcome),
    CONTEXT_MENUS(R.layout.intro_1_context_menus),
    INDICATORS(R.layout.intro_2_indicators),
    STORE(R.layout.intro_3_store);

    private final int layout;

    SubView(int layout) {
        this.layout = layout;
    }

    public int getLayout() {
        return layout;
    }
}
