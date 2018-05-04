package com.example.crypsis.customkeyboard.presenter;

import rx.subscriptions.CompositeSubscription;

/**
 * Created by crypsis on 21/11/16.
 */

public interface SearchPresenter {
    void getSearchResult(CompositeSubscription compositeSubscription, String searchText);
}
