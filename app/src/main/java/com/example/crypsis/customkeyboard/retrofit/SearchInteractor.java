package com.example.crypsis.customkeyboard.retrofit;

import com.example.crypsis.customkeyboard.presenter.OnSearchCompleted;

import rx.subscriptions.CompositeSubscription;



public interface SearchInteractor {
    void getSearchResult(CompositeSubscription compositeSubscription, OnSearchCompleted onSearchCompleted, String searchText);
}
