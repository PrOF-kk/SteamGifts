package net.mabako.steam.store;

import android.os.AsyncTask;
import android.util.Log;

import net.mabako.steamgifts.http.OkHttp;

import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

abstract class LoadStoreTask extends AsyncTask<Void, Void, JSONObject> {
    @Override
    protected JSONObject doInBackground(Void... params) {
        try (Response response = getClient().newCall(getRequest()).execute()) {
            if (response.isSuccessful())
                return new JSONObject(response.body().string());

            return null;
        } catch (Exception e) {
            Log.e(LoadStoreTask.class.getSimpleName(), "Error loading Url", e);
            return null;
        }
    }

    protected OkHttpClient getClient() {
        return OkHttp.client();
    }

    protected abstract Request getRequest();
}
