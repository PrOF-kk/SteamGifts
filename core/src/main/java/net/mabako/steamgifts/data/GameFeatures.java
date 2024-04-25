package net.mabako.steamgifts.data;

import java.io.Serializable;

public class GameFeatures implements Serializable {
    private int cards;
    private boolean dlc;
    private boolean limited;
    private boolean delisted;

    public int getCards() {
        return cards;
    }

    public void setCards(int cards) {
        this.cards = cards;
    }

    public boolean isLimited() {
        return limited;
    }

    public void setLimited(boolean limited) {
        this.limited = limited;
    }

    public boolean isDlc() {
        return dlc;
    }

    public void setDlc(boolean dlc) {
        this.dlc = dlc;
    }

    public boolean isDelisted() {
        return delisted;
    }

    public void setDelisted(boolean delisted) {
        this.delisted = delisted;
    }
}
