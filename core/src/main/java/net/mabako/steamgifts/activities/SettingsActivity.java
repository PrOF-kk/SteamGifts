package net.mabako.steamgifts.activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import androidx.appcompat.app.ActionBar;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;

import net.mabako.steamgifts.ApplicationTemplate;
import net.mabako.steamgifts.core.R;
import net.mabako.steamgifts.data.GameFeaturesRepository;
import net.mabako.steamgifts.fragments.WhitelistBlacklistFragment;
import net.mabako.steamgifts.persistentdata.SteamGiftsUserData;

public class SettingsActivity extends BaseActivity {
    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String s) {

            addPreferencesFromResource(R.xml.preferences_app);

            findPreference("preference_giveaway_show_game_features").setOnPreferenceChangeListener((preference, newValue) -> {
                GameFeaturesRepository.setLoadGameFeatures((boolean) newValue);
                return true;
            });

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

            ListPreference browserPreferences = findPreference("preference_external_browser");

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

            Preference chromeTabsInfo = findPreference("tools_preference_default_app");
            if (!tabsSupported || isDefaultApp()) {
                // Notification for default app isn't displayed if we -are- the default app.
                ((PreferenceCategory) findPreference("preferences_other")).removePreference(chromeTabsInfo);
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    // Offer to take the user to the settings page directly
                    chromeTabsInfo.setEnabled(true); // Clickable
                    chromeTabsInfo.setOnPreferenceClickListener(preference -> {
                        Intent intent = new Intent(Settings.ACTION_APP_OPEN_BY_DEFAULT_SETTINGS);
                        intent.setData(Uri.parse("package:" + getActivity().getPackageName()));
                        startActivity(intent);
                        return true;
                    });
                    chromeTabsInfo.setSummary(chromeTabsInfo.getSummary() + "\n\nTap here to go to the Android SG url settings page.");
                } else {
                    chromeTabsInfo.setSummary(chromeTabsInfo.getSummary() + "\n\nGo to SteamGifts in your browser and open any giveaway or discussion. When asked, select this app as default.");
                }
            }

            if (!((ApplicationTemplate) getActivity().getApplication()).allowGameImages()) {
                Preference images = findPreference("preference_giveaway_load_images");
                ((PreferenceCategory) findPreference("preferences_giveaways")).removePreference(images);
            }
        }

        private boolean isDefaultApp() {
            PackageManager pm = getActivity().getPackageManager();
            ResolveInfo resolveInfo = pm.resolveActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.steamgifts.com/giveaway/xxxxx/")), PackageManager.MATCH_DEFAULT_ONLY);

            // No app set as default for any urls
            if (resolveInfo == null)
                return false;

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
