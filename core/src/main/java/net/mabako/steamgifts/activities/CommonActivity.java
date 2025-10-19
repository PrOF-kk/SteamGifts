package net.mabako.steamgifts.activities;

import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import net.mabako.common.AbstractTextWatcher;
import net.mabako.steamgifts.core.R;
import net.mabako.steamgifts.data.BasicDiscussion;
import net.mabako.steamgifts.data.BasicGiveaway;
import net.mabako.steamgifts.fragments.DiscussionDetailFragment;
import net.mabako.steamgifts.fragments.GiveawayDetailFragment;
import net.mabako.steamgifts.fragments.UserDetailFragment;
import net.mabako.steamgifts.fragments.interfaces.IActivityTitle;
import net.mabako.steamgifts.persistentdata.SteamGiftsUserData;

public class CommonActivity extends BaseActivity {
    private static final String TAG = CommonActivity.class.getSimpleName();
    public static final String FRAGMENT_TAG = "Fragment Root";

    public static final int REQUEST_LOGIN = 3;
    public static final int REQUEST_LOGIN_PASSIVE = 4;

    public static final int RESPONSE_LOGIN_SUCCESSFUL = 6;

    public static final int REQUEST_SYNC = 8;
    public static final int RESPONSE_SYNC_SUCCESSFUL = 9;

    public static final int REQUEST_SETTINGS = 10;
    public static final int RESPONSE_LOGOUT = 11;

    public void requestLogin() {
        startActivityForResult(new Intent(this, LoginActivity.class), REQUEST_LOGIN);
    }

    public void loadFragment(Fragment fragment) {
        super.loadFragment(R.id.fragment_container, fragment, FRAGMENT_TAG);
        updateTitle(fragment);
    }

    protected void updateTitle(Fragment fragment) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            String fragmentTitle = getFragmentTitle(fragment);
            actionBar.setTitle(fragmentTitle);

            if (fragment instanceof IActivityTitle fWithActivityTitle) {
                String extraTitle = fWithActivityTitle.getExtraTitle();
                if (extraTitle != null && !extraTitle.equals(fragmentTitle)) {
                    actionBar.setSubtitle(extraTitle);
                }
            }
        }
    }

    @NonNull
    protected String getFragmentTitle(Fragment fragment) {
        String title = getString(R.string.app_name);
        if (fragment instanceof IActivityTitle fWithActivityTitle) {
            int resource = fWithActivityTitle.getTitleResource();
            String extraTitle = fWithActivityTitle.getExtraTitle();

            if (resource != 0) {
                title = getString(resource);
            } else if (extraTitle != null) {
                title = extraTitle;
            }
        }
        return title;
    }

    public Fragment getCurrentFragment() {
        return getCurrentFragment(FRAGMENT_TAG);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_LOGIN, REQUEST_LOGIN_PASSIVE:
                // Do not show an explicit notification.
                if (resultCode == RESPONSE_LOGIN_SUCCESSFUL && SteamGiftsUserData.getCurrent(this).isLoggedIn())
                    onAccountChange();

                // Pass on the result.
                setResult(resultCode);

                break;

            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            event.startTracking();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * Always-available "Go to ..." menu by long-pressing back.
     */
    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        // TODO allow this to be changed to normal overflow menus in the settings.
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            final CharSequence[] strings = new CharSequence[]{getString(R.string.go_to_giveaway), getString(R.string.go_to_discussion), getString(R.string.go_to_user)};
            final int[] hints = new int[]{R.string.go_to_giveaway_hint, R.string.go_to_discussion_hint, R.string.go_to_user_hint};

            AlertDialog.Builder gotoButtonsBuilder = new AlertDialog.Builder(this);
            gotoButtonsBuilder.setTitle(R.string.go_to);
            gotoButtonsBuilder.setItems(strings, (dialogInterface, dialogSelected) -> {

                final View view = getLayoutInflater().inflate(R.layout.go_to_dialog, null);
                EditText inputField = view.findViewById(R.id.edit_text);
                inputField.setHint(hints[dialogSelected]);

                AlertDialog.Builder idInputBuilder = new AlertDialog.Builder(CommonActivity.this);
                idInputBuilder.setTitle(R.string.go_to);
                idInputBuilder.setMessage(strings[dialogSelected]);
                idInputBuilder.setView(view);
                idInputBuilder.setPositiveButton(android.R.string.ok, (dialog, which) -> { /* do nothing */ });
                final AlertDialog idInputDialog = idInputBuilder.show();

                // Force opening the keyboard. This is a hack.
                inputField.requestFocus();
                inputField.postDelayed(() -> {
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.showSoftInput(inputField, InputMethodManager.SHOW_IMPLICIT);
                }, 500);

                Button okButton = idInputDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                okButton.setOnClickListener(v -> {
                    String target = inputField.getText().toString();
                    Intent intent = new Intent(CommonActivity.this, DetailActivity.class);
                    switch (dialogSelected) {
                        case 0 -> intent.putExtra(GiveawayDetailFragment.ARG_GIVEAWAY, new BasicGiveaway(target));
                        case 1 -> intent.putExtra(DiscussionDetailFragment.ARG_DISCUSSION, new BasicDiscussion(target));
                        case 2 -> intent.putExtra(UserDetailFragment.ARG_USER, target);
                    }
                    startActivity(intent);

                    idInputDialog.dismiss();
                });
                // Handle the enter key
                inputField.setOnEditorActionListener((v, actionId, enterEvent) -> {
                    if (actionId == EditorInfo.IME_ACTION_GO || enterEvent != null) {
                        okButton.performClick();
                        return true;
                    }
                    return false;
                });

                // Giveaway and discussion ids can only be 5 chars long, allow entering longer text and then editing it down to 5
                boolean isGiveawayOrDiscussionDialog = (dialogSelected == 0 || dialogSelected == 1);
                if (isGiveawayOrDiscussionDialog) {
                    okButton.setEnabled(false);
                    inputField.addTextChangedListener(new AbstractTextWatcher() {
                        @Override
                        public void afterTextChanged(Editable s) {
                            okButton.setEnabled(s.length() == 5);
                        }
                    });
                }

            });
            gotoButtonsBuilder.show();
            return true;
        }
        return super.onKeyLongPress(keyCode, event);
    }
}
