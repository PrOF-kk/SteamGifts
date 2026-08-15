package net.mabako.steamgifts.http;

import android.content.Context;

import net.mabako.Constants;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jspecify.annotations.NullMarked;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.MediaType;
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
                .addNetworkInterceptor(new ConditionallyForceCache())
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

    /// Parse a Response using jsoup. [Response#body()]'s stream will get closed.
    public static Document parseJsoup(Response response) throws IOException {
        MediaType contentType = response.body().contentType();
        Charset charset = contentType == null ? null : contentType.charset();
        String charsetName = charset == null ? null : charset.name();
        return Jsoup.parse(response.body().byteStream(), charsetName, "");
    }

    public static class CacheFor {
        int seconds;
        public CacheFor(int seconds) {
            this.seconds = seconds;
        }
        public static CacheFor days(int days) {
            return new CacheFor(days * 24 * 60 * 60);
        }
    }

    private static class ConditionallyForceCache implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            CacheFor cacheFor = chain.request().tag(CacheFor.class);
            Response originalResponse = chain.proceed(chain.request());
            if (cacheFor == null || !originalResponse.isSuccessful()) {
                return originalResponse;
            }
            return originalResponse.newBuilder()
                    .removeHeader("Pragma") // Remove Pragma: no-cache
                    .header("Cache-Control", "public, immutable, max-age=" + cacheFor.seconds)
                    .build();
        }
    }
}
