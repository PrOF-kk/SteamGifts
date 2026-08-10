package net.mabako.steamgifts.http;

import android.content.Context;

import net.mabako.Constants;

import org.jspecify.annotations.NullMarked;

import java.io.File;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.Response;

@NullMarked
public final class OkHttp {

    @SuppressWarnings("NotNullFieldNotInitialized")
    private static OkHttpClient instance;

    private OkHttp() {}

    public static void init(Context context) {
        instance = new OkHttpClient.Builder()
                .followRedirects(false)
                .callTimeout(Constants.HTTP_TIMEOUT, TimeUnit.MILLISECONDS)
                .cache(new Cache(
                        new File(context.getCacheDir(), "http_cache"),
                        50 * 1024L * 1024
                ))
                .build();
    }

    /// Common http client, doesn't follow redirects.
    /// To customize properly, [OkHttpClient#newBuilder()]
    public static OkHttpClient client() {
        return instance;
    }

    public static boolean wasRedirectedHome(Response response) {
        return response.request().url().encodedPath().equals("/");
    }
}
