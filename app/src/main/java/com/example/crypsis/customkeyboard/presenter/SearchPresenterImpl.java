package com.example.crypsis.customkeyboard.presenter;

import com.example.crypsis.customkeyboard.SearchView;
import com.example.crypsis.customkeyboard.retrofit.SearchInteractor;
import com.example.crypsis.customkeyboard.retrofit.SearchModel;

import java.util.List;

import rx.subscriptions.CompositeSubscription;

/**
 * Created by crypsis on 21/11/16.
 */

public class SearchPresenterImpl implements SearchPresenter, OnSearchCompleted {
    SearchView mSearchView;
    SearchInteractor mSearchInteractor;

    public SearchPresenterImpl(SearchView searchView, SearchInteractor searchInteractor){
        mSearchView = searchView;
        mSearchInteractor = searchInteractor;
    }


    @Override
    public void onShowProgressBar() {
        mSearchView.showProgressBar();
    }

    @Override
    public void onHideProgressBar() {
        mSearchView.hideProgressBar();
    }

    @Override
    public void onSearchError(String error) {
        mSearchView.searchError(error);
    }

    @Override
    public void onSearchSuccess(List<SearchModel> searchModelList) {
       mSearchView.searchSuccess(searchModelList);
    }


    @Override
    public void getSearchResult(CompositeSubscription compositeSubscription, String searchText) {
        mSearchInteractor.getSearchResult(compositeSubscription,this,searchText);
    }
}
