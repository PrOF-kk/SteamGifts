package net.mabako.steamgifts.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import net.mabako.steamgifts.http.OkHttp;
import net.mabako.steamgifts.persistentdata.SteamGiftsUserData;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.Response;

public abstract class AjaxTask<FragmentType> extends AsyncTask<Void, Void, Response> {
    private static final String TAG = AjaxTask.class.getSimpleName();

    private String url = "https://www.steamgifts.com/ajax.php";

    private final String xsrfToken;
    private final String what;

    private final Context context;
    private final FragmentType fragment;

    protected AjaxTask(FragmentType fragment, Context context, String xsrfToken, String what) {
        this.fragment = fragment;
        this.context = context;
        this.xsrfToken = xsrfToken;
        this.what = what;

        if (TextUtils.isEmpty(this.xsrfToken))
            Log.w(TAG, "no xsrf token for ajax call");

        if (TextUtils.isEmpty(this.what))
            Log.w(TAG, "no what for ajax call");
    }

    @Override
    protected Response doInBackground(Void... params) {
        try {
            Log.v(TAG, "Connecting to " + url);
            var client = OkHttp.client();
            Request.Builder request = new Request.Builder()
                    .url(url)
                    .header("Cookie", "PHPSESSID=" + SteamGiftsUserData.getCurrent(context).getSessionId());

            FormBody.Builder body = new FormBody.Builder()
                    .add("xsrf_token", xsrfToken)
                    .add("do", what);

            addExtraParameters(body);

            Response response = client.newCall(request.post(body.build()).build()).execute();

            Log.v(TAG, url + " returned Status Code " + response.code() + " (" + response.message() + ")");

            return response;
        } catch (IOException e) {
            Log.e(TAG, "Error fetching URL", e);
            return null;
        }
    }

    protected void addExtraParameters(FormBody.Builder body) { }

    protected FragmentType getFragment() {
        return fragment;
    }

    String getWhat() {
        return what;
    }

    void setUrl(String url) {
        this.url = url;
    }

    public Context getContext() {
        return context;
    }
}
