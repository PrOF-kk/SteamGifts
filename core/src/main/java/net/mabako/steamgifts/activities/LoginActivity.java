package net.mabako.steamgifts.activities;

import android.os.Bundle;
import android.webkit.JavascriptInterface;

import net.mabako.common.SteamLoginActivity;
import net.mabako.steamgifts.persistentdata.SteamGiftsUserData;

import org.jsoup.Jsoup;

public class LoginActivity extends SteamLoginActivity {
    // SG used to redirect to the Steam login page from https://www.steamgifts.com/?login
    private static final String LOGIN_URL = "https://steamcommunity.com/openid/login?openid.identity=http%3A%2F%2Fspecs.openid.net%2Fauth%2F2.0%2Fidentifier_select&openid.claimed_id=http%3A%2F%2Fspecs.openid.net%2Fauth%2F2.0%2Fidentifier_select&openid.ns=http%3A%2F%2Fspecs.openid.net%2Fauth%2F2.0&openid.mode=checkid_setup&openid.return_to=https%3A%2F%2Fwww.steamgifts.com%2F%3Flogin%3D";
    private static final String REDIRECTED_URL = "https://www.steamgifts.com/?login=";

    public LoginActivity() {
        super(REDIRECTED_URL, REDIRECTED_URL);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        webView.addJavascriptInterface(new JavaScriptContentHandler(), "contenthandler");
        webView.loadUrl(LOGIN_URL);
    }

    @Override
    protected void onLoginSuccessful(String phpSessionId) {
        SteamGiftsUserData.clear();
        SteamGiftsUserData.getCurrent(this).setSessionId(phpSessionId);

        webView.loadUrl("javascript:contenthandler.processHTML(document.documentElement.outerHTML);");
    }

    @Override
    protected void onLoginCancelled() {
        setResult(RESULT_CANCELED);
        finish();
    }

    private class JavaScriptContentHandler {
        @JavascriptInterface
        @SuppressWarnings("unused")
        public void processHTML(String html) {
            SteamGiftsUserData.extract(LoginActivity.this, Jsoup.parse(html));

            setResult(CommonActivity.RESPONSE_LOGIN_SUCCESSFUL);
            finish();
        }
    }
}
