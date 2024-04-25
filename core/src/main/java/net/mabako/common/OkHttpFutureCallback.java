package net.mabako.common;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * An OkHttp Callback that can be waited on and return a value
 */
public class OkHttpFutureCallback<T> extends CompletableFuture<T> implements Callback {

    private final BiFunction<Call, Response, T> onResponse;
    private final BiFunction<Call, IOException, T> onFailure;

    public OkHttpFutureCallback(BiFunction<Call, Response, T> onResponse) {
        super();
        this.onResponse = onResponse;
        this.onFailure = (request, e) -> {
            complete(null);
            return null;
        };
    }

    public OkHttpFutureCallback(BiFunction<Call, Response, T> onResponse, BiFunction<Call, IOException, T> onFailure) {
        super();
        this.onResponse = onResponse;
        this.onFailure = onFailure;
    }

    @Override
    public void onFailure(@NonNull Call call, @NonNull IOException e) {
        var ret = this.onFailure.apply(call, e);
        this.complete(ret);
    }

    @Override
    public void onResponse(@NonNull Call call, @NonNull Response response) {
        var ret = this.onResponse.apply(call, response);
        this.complete(ret);
    }
}
