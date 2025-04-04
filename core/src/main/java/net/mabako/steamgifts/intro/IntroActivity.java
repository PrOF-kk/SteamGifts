package net.mabako.steamgifts.intro;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import com.github.appintro.AppIntro2;

import net.mabako.steamgifts.core.R;

public class IntroActivity extends AppIntro2 {
    public static final String INTRO_MAIN = "main";
    public static final int INTRO_MAIN_VERSION = 3;

    public static void showIntroIfNecessary(Activity parentActivity, final String type, final int version) {
        SharedPreferences sp = parentActivity.getSharedPreferences("intro", MODE_PRIVATE);
        int lastSeenVersion = sp.getInt(type, 0);
        if (lastSeenVersion < version) {
            // Show the activity
            Intent intent = new Intent(parentActivity, IntroActivity.class);
            intent.putExtra("type", type);
            parentActivity.startActivity(intent);

            SharedPreferences.Editor spe = sp.edit();
            spe.putInt(type, version);
            spe.apply();
        }
    }

    public static void showIntro(Activity parentActivity, final String type) {
        Intent intent = new Intent(parentActivity, IntroActivity.class);
        intent.putExtra("type", type);
        parentActivity.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        switch (getIntent().getStringExtra("type")) {
            case INTRO_MAIN:
                setIndicatorColor(getResources().getColor(R.color.colorAccent), getResources().getColor(android.R.color.darker_gray));
                setSkipButtonEnabled(false);
                setWizardMode(true);

                addSlide(Slide.newInstance(SubView.MAIN_WELCOME));
                addSlide(Slide.newInstance(SubView.MAIN_GIVEAWAY_1));
                addSlide(Slide.newInstance(SubView.MAIN_GIVEAWAY_2));
                addSlide(Slide.newInstance(SubView.MAIN_GIVEAWAY_3));
                break;
        }
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        finish();
    }
}
