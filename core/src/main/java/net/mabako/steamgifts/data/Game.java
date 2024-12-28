package net.mabako.steamgifts.data;

import net.mabako.steamgifts.adapters.IEndlessAdaptable;
import net.mabako.steamgifts.core.R;

import java.io.Serial;
import java.io.Serializable;

public class Game implements Serializable, IEndlessAdaptable {
    @Serial
    private static final long serialVersionUID = -4047245968975766647L;
    public static final int NO_APP_ID = 0;
    public static final int VIEW_LAYOUT = R.layout.game_item;

    private String name;
    private Type type;
    private int id;

    /**
     * Id used (exclusively?) for filtering games.
     */
    private long internalGameId;

    public Game() {
        this(Type.APP, NO_APP_ID);
    }

    public Game(Type type, int id) {
        this.type = type;
        this.id = id;
    }

    @Override
    public int getLayout() {
        return VIEW_LAYOUT;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getInternalGameId() {
        return internalGameId;
    }

    public void setInternalGameId(long internalGameId) {
        this.internalGameId = internalGameId;
    }

    @Override
    public int hashCode() {
        return (int) internalGameId;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Game g) {
            return g.internalGameId == internalGameId;
        }
        return false;

    }

    public enum Type {
        APP, SUB
    }
}
