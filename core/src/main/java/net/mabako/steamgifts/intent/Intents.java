package net.mabako.steamgifts.intent;

import android.content.Intent;

public abstract class Intents {
    private Intents() {}

    public static Intent shareUrl(String url) {
        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, url);
        sendIntent.setType("text/plain");

        return Intent.createChooser(sendIntent, null);
    }
}
