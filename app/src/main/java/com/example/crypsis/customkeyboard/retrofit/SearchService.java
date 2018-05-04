package com.example.crypsis.customkeyboard.retrofit;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;


public class SearchService {

    private SearchService() {
    }

    public static SearchApi createSearchService() {
        Retrofit.Builder builder = new Retrofit.Builder().addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("https://api.crypsis.com");

        /*if (!TextUtils.isEmpty(githubToken)) {

            OkHttpClient client = new OkHttpClient.Builder().addInterceptor(chain -> {
                Request request = chain.request();
                Request newReq = request.newBuilder()
                        *//*.addHeader("Authorization", format("token %s", githubToken))*//*
                        .build();
                return chain.proceed(newReq);
            }).build();

            builder.client(client);
        }*/

        return builder.build().create(SearchApi.class);
    }
}
