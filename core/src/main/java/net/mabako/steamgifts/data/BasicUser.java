package net.mabako.steamgifts.data;

import androidx.annotation.LayoutRes;

import net.mabako.steamgifts.adapters.IEndlessAdaptable;
import net.mabako.steamgifts.core.R;

import java.io.Serial;
import java.io.Serializable;

public class BasicUser implements IEndlessAdaptable, Serializable {
    public static final int VIEW_LAYOUT = R.layout.basic_user_item;
    @Serial
    private static final long serialVersionUID = 2288877588005277781L;

    private int id;
    private String name;
    private String avatar;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    @LayoutRes
    @Override
    public int getLayout() {
        return VIEW_LAYOUT;
    }
}
