package com.example.apiconsumer.Util;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class HttpInterceptor implements Interceptor{
    @NotNull
    @Override
    public Response intercept(@NotNull Interceptor.Chain chain) throws IOException {
        Request request = chain.request();

        request = request.newBuilder()
                //.addHeader("Authorization", "Bearer " + StorageHandler.LoggedInUser.token)
                .build();

        Response response = chain.proceed(request);

        return response;
    }
}
