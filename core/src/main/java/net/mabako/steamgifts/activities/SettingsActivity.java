package net.mabako.steamgifts.activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.ActionBar;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;

import net.mabako.steamgifts.ApplicationTemplate;
import net.mabako.steamgifts.core.R;
import net.mabako.steamgifts.fragments.WhitelistBlacklistFragment;
import net.mabako.steamgifts.persistentdata.SteamGiftsUserData;

public class SettingsActivity extends BaseActivity {
    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String s) {

            addPreferencesFromResource(R.xml.preferences_app);

            if (SteamGiftsUserData.getCurrent(getActivity()).isLoggedIn()) {
                addPreferencesFromResource(R.xml.preferences_sg);

                findPreference("preference_sg_sync").setOnPreferenceClickListener(preference -> {
                    getActivity().startActivity(new Intent(getActivity(), SyncActivity.class));
                    return true;
                });

                findPreference("preference_sg_whitelist").setOnPreferenceClickListener(preference -> {
                    Intent intent = new Intent(getActivity(), DetailActivity.class);
                    intent.putExtra(WhitelistBlacklistFragment.ARG_TYPE, WhitelistBlacklistFragment.What.WHITELIST);
                    getActivity().startActivity(intent);
                    return true;
                });

                findPreference("preference_sg_blacklist").setOnPreferenceClickListener(preference -> {
                    Intent intent = new Intent(getActivity(), DetailActivity.class);
                    intent.putExtra(WhitelistBlacklistFragment.ARG_TYPE, WhitelistBlacklistFragment.What.BLACKLIST);
                    getActivity().startActivity(intent);
                    return true;
                });

                findPreference("preference_sg_hidden_games").setOnPreferenceClickListener(preference -> {
                    Intent intent = new Intent(getActivity(), DetailActivity.class);
                    intent.putExtra(DetailActivity.ARG_HIDDEN_GAMES, true);
                    getActivity().startActivity(intent);
                    return true;
                });

                findPreference("preference_sg_logout").setOnPreferenceClickListener(preference -> {
                    getActivity().setResult(CommonActivity.RESPONSE_LOGOUT);
                    getActivity().finish();
                    return true;
                });
            }

            addPreferencesFromResource(R.xml.preferences_other);

            ListPreference browserPreferences = (ListPreference) findPreference("preference_external_browser");

            boolean tabsSupported = ChromeTabsDelegate.isCustomTabsSupported(getActivity());
            if (tabsSupported) {
                // We have some chrome version installed which supports custom tabs.
                browserPreferences.setEntries(R.array.preference_browser_entries_with_tabs);
                browserPreferences.setEntryValues(R.array.preference_browser_entry_values_with_tabs);
            } else {
                // No chrome, no tabs. This probably is also the case for API levels < Jellybean, at least custom tabs are not officially supported before that.
                browserPreferences.setEntries(R.array.preference_browser_entries);
                browserPreferences.setEntryValues(R.array.preference_browser_entry_values);

            }

            if (!tabsSupported || isDefaultApp()) {
                // Notification for default app isn't displayed if we -are- the default app.
                // TODO if we're not the default app, maybe offer a selection to set this the default at least?
                Preference chromeTabsInfo = findPreference("tools_preference_default_app");
                ((PreferenceCategory) findPreference("preferences_other")).removePreference(chromeTabsInfo);
            }

            if (!((ApplicationTemplate) getActivity().getApplication()).allowGameImages()) {
                Preference images = findPreference("preference_giveaway_load_images");
                ((PreferenceCategory) findPreference("preferences_giveaways")).removePreference(images);
            }
        }

        private boolean isDefaultApp() {
            PackageManager pm = getActivity().getPackageManager();
            ResolveInfo resolveInfo = pm.resolveActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.steamgifts.com/giveaway/xxxxx/")), PackageManager.MATCH_DEFAULT_ONLY);
            ActivityInfo activityInfo = resolveInfo.activityInfo;
            if (activityInfo == null || !activityInfo.exported)
                return false;

            Log.d(SettingsActivity.class.getSimpleName(), "Current default handler for SteamGifts URLs: " + activityInfo.name);
            return UrlHandlingActivity.class.getName().equals(activityInfo.name);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);

        setSupportActionBar(findViewById(R.id.toolbar));
        ActionBar actionbar = getSupportActionBar();
        if (actionbar != null) {
            actionbar.setDisplayHomeAsUpEnabled(true);
        }

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new SettingsFragment()).commit();
    }
}
