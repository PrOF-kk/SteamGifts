package net.mabako.steamgifts.activities;

import android.os.Bundle;

import net.mabako.steamgifts.core.R;
import net.mabako.steamgifts.fragments.AboutFragment;

public class AboutActivity extends CommonActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_one_fragment);

        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.navigation_about);
        loadFragment(R.id.fragment_container, new AboutFragment(), "about");
    }
}
