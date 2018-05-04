package com.example.crypsis.customkeyboard.retrofit;

import com.example.crypsis.customkeyboard.presenter.OnSearchCompleted;

import java.util.List;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;


public class SearchInteractorImpl implements SearchInteractor {
    SearchApi mSearchApi;

    public SearchInteractorImpl(SearchApi searchApi){
        mSearchApi = searchApi;
    }

    @Override
    public void getSearchResult(CompositeSubscription compositeSubscription, OnSearchCompleted onSearchCompleted, String searchText) {


        onSearchCompleted.onShowProgressBar();

        compositeSubscription.add( mSearchApi.search(searchText)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<SearchModel>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        onSearchCompleted.onHideProgressBar();
                      if(e.getMessage()!=null){
                          onSearchCompleted.onSearchError(e.getMessage());
                      }
                    }

                    @Override
                    public void onNext(List<SearchModel> searchModelList) {
                        onSearchCompleted.onHideProgressBar();
                        onSearchCompleted.onSearchSuccess(searchModelList);
                    }
                })
        );

    }
}
