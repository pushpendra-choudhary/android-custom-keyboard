package com.example.crypsis.customkeyboard.retrofit;

import java.util.List;

import retrofit2.http.GET;
import retrofit2.http.Path;
import rx.Observable;


public interface SearchApi {

    /**
     * See https://developer.github.com/v3/repos/#list-contributors
     */
    @GET("/{searchText}")
    Observable<List<SearchModel>> search(@Path("searchText") String searchText);

}
