package net.mabako.steamgifts.activities;

import android.content.Intent;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import net.mabako.steamgifts.persistentdata.SteamGiftsUserData;

public class BaseActivity extends AppCompatActivity {
    public static final String CLOSE_NESTED = "close-nested";

    protected void loadFragment(int id, Fragment fragment, String tag) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        ft.replace(id, fragment, tag);

        ft.commitAllowingStateLoss();
    }

    protected Fragment getCurrentFragment(String fragmentTag) {
        return getSupportFragmentManager().findFragmentByTag(fragmentTag);
    }

    protected void onAccountChange() {
        // Persist all relevant data.
        SteamGiftsUserData.getCurrent(this).save(this);
    }

    /**
     * Handle "up" navigation
     *
     * @param item the used menu item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent data = new Intent();
            data.putExtra(CLOSE_NESTED, getNestingStringForHomePressed());

            setResult(0, data);
            finish();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * What happens when the user presses home?
     */
    public String getNestingStringForHomePressed() {
        return "";
    }
}
