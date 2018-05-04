package com.example.crypsis.customkeyboard.presenter;

import com.example.crypsis.customkeyboard.retrofit.SearchModel;

import java.util.List;

/**
 * Created by crypsis on 21/11/16.
 */

public interface OnSearchCompleted {
    void onShowProgressBar();
    void onHideProgressBar();
    void onSearchError(String error);
    void onSearchSuccess(List<SearchModel> searchModelList);
}
