package com.example.crypsis.customkeyboard;

import com.example.crypsis.customkeyboard.retrofit.SearchModel;

import java.util.List;

/**
 * Created by crypsis on 21/11/16.
 */

public interface SearchView {
    void showProgressBar();
    void hideProgressBar();
    void searchError(String error);
    void searchSuccess(List<SearchModel> searchModelList);
}
