package com.example.crypsis.customkeyboard;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.example.crypsis.customkeyboard.presenter.SearchPresenter;
import com.example.crypsis.customkeyboard.presenter.SearchPresenterImpl;
import com.example.crypsis.customkeyboard.retrofit.SearchInteractorImpl;
import com.example.crypsis.customkeyboard.retrofit.SearchModel;
import com.example.crypsis.customkeyboard.retrofit.SearchService;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.jakewharton.rxbinding.widget.TextViewTextChangeEvent;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Created by crypsis on 16/11/16.
 */
public class SimpleIME extends InputMethodService
        implements KeyboardView.OnKeyboardActionListener, RecyclerAdapter.RecyclerAdapterListener, SearchView {


    @Bind(R.id.search_layout) LinearLayout searchLayout;
    @Bind(R.id.searchProgressBar) ProgressBar mProgressBar;
    @Bind(R.id.search) EditText searchText;
    @Bind(R.id.keyboard) KeyboardView kv;
    private Keyboard keyboard;
    @Bind(R.id.searchResultLayout) RelativeLayout mSearchResultLayout;
    @Bind(R.id.recyclerview) RecyclerView myList;
    SearchPresenter mSearchPresenter;
    List<SearchModel> mSearchModelList;
    CompositeSubscription mCompositeSubscription;

    private boolean search = false;
    private boolean caps = false;
    View view;


    RecyclerAdapter recyclerAdapter;
    private Subscription _subscription;



    @Override
    public View onCreateInputView() {
        view = getLayoutInflater().inflate(R.layout.search_layout,null);
        ButterKnife.bind(this,view);

        myList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mSearchModelList = new ArrayList<>();
        recyclerAdapter = new RecyclerAdapter(this);
        myList.setAdapter(recyclerAdapter);

        /*searchText.addTextChangedListener(this);*/
        mSearchPresenter = new SearchPresenterImpl(this, new SearchInteractorImpl(SearchService.createSearchService()));
        subscribeDebounce();



        kv = (KeyboardView) view.findViewById(R.id.keyboard);
        keyboard = new Keyboard(this, R.xml.qwerty);
        kv.setKeyboard(keyboard);
        kv.setOnKeyboardActionListener(this);
        return view;
    }

    boolean isNotNullOrEmpty(String string){
        if(string!=null && string.length()>0) {
            return  true;
        }
        else return false;

    }

    void subscribeDebounce(){
        mCompositeSubscription = new CompositeSubscription();

        _subscription = RxTextView.textChangeEvents(searchText)
                .debounce(1000, TimeUnit.MILLISECONDS)// default Scheduler is Computation
                .filter(changes -> isNotNullOrEmpty(searchText.getText().toString()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(_getSearchObserver());
        mCompositeSubscription.add(_subscription); // adding debounce subscription
    }


    @Override
    public void onKey(int primaryCode, int[] keyCodes) {
        InputConnection ic = getCurrentInputConnection();
        playClick(primaryCode);

        if(search){
            switch(primaryCode){
                case Keyboard.KEYCODE_DELETE :
                    int length = searchText.getText().length();
                    if (length > 0) {
                        searchText.getText().delete(length - 1, length);
                    }
                    break;
                case Keyboard.KEYCODE_SHIFT:
                    caps = !caps;
                    keyboard.setShifted(caps);
                    kv.invalidateAllKeys();
                    break;
                case Keyboard.KEYCODE_DONE:
                    /*ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));*/
                    break;
                case Keyboard.KEYCODE_MODE_CHANGE:
                    InputMethodManager mgr =
                            (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (mgr != null) {
                        mgr.showInputMethodPicker();
                    }
                    break;
                default:
                    if(primaryCode==-3){
                        search = !search;
                        searchLayout.setVisibility(View.GONE);
                        mSearchResultLayout.setVisibility(View.GONE);
                        searchText.setText(null);
                        myList.setVisibility(View.GONE);

                        mCompositeSubscription.unsubscribe();

                        List<Keyboard.Key> keys = keyboard.getKeys();
                        for (Keyboard.Key key : keys) {
                            if (key.codes[0] == -3) {
                                Log.e("hello", String.valueOf(key.codes[0]));
                                key.icon = getResources().getDrawable(android.R.drawable.ic_menu_search);
                            }
                        }
                        kv.invalidateAllKeys();

                    }else{
                        char code = (char)primaryCode;
                        if(Character.isLetter(code) && caps){
                            code = Character.toUpperCase(code);
                        }
                       /* ic.commitText(String.valueOf(code),1);*/
                        searchText.append(String.valueOf(code));
                    }
            }
        }else{
            switch(primaryCode){
                case Keyboard.KEYCODE_DELETE :
                    ic.deleteSurroundingText(1, 0);
                    break;
                case Keyboard.KEYCODE_SHIFT:
                    caps = !caps;
                    keyboard.setShifted(caps);
                    kv.invalidateAllKeys();
                    break;
                case Keyboard.KEYCODE_DONE:
                    ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
                    break;
                case Keyboard.KEYCODE_MODE_CHANGE:
                    InputMethodManager mgr =
                            (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (mgr != null) {
                        mgr.showInputMethodPicker();
                    }
                    break;
                default:
                    if(primaryCode==-3){
                        search = !search;
                        searchLayout.setVisibility(View.VISIBLE);

                        List<Keyboard.Key> keys = keyboard.getKeys();
                        for (Keyboard.Key key : keys) {
                            if (key.codes[0] == -3) {
                                Log.e("hello", String.valueOf(key.codes[0]));
                                key.icon = getResources().getDrawable(android.R.drawable.ic_menu_close_clear_cancel);
                            }
                        }
                        kv.invalidateAllKeys();

                        if(_subscription.isUnsubscribed()){
                            subscribeDebounce();
                        }


                    }else{
                        char code = (char)primaryCode;
                        if(Character.isLetter(code) && caps){
                            code = Character.toUpperCase(code);
                        }
                        ic.commitText(String.valueOf(code),1);
                    }
            }
        }

    }

    @Override
    public void onPress(int primaryCode) {
    }

    @Override
    public void onRelease(int primaryCode) {
    }

    @Override
    public void onText(CharSequence text) {
    }

    @Override
    public void swipeDown() {
    }

    @Override
    public void swipeLeft() {
    }

    @Override
    public void swipeRight() {
    }

    @Override
    public void swipeUp() {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mCompositeSubscription.unsubscribe();
    }

    private void playClick(int keyCode){
        AudioManager am = (AudioManager)getSystemService(AUDIO_SERVICE);
        switch(keyCode){
            case 32:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_SPACEBAR);
                break;
            case Keyboard.KEYCODE_DONE:
            case 10:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_RETURN);
                break;
            case Keyboard.KEYCODE_DELETE:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_DELETE);
                break;
            default: am.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD);
        }
    }

    boolean im=false;

    @Override
    public void ola(String url) {
        /*InputConnection ic = getCurrentInputConnection();
        ic.commitText(url,1);*/

        Log.e("ola", "ola");

        /*getCurrentApplicationName();*/

        String url1 = "http://fyf.tac-cdn.net/images/products/large/BF216-11KM.jpg";
        String url2 = "http://static.prvd.com/siteimages/PFC_C_TIL_233x380_OTH16_SIT_03A_PIR699.jpg";

        shareItem(url1,url);

        /*if(!im){
            shareItem(url1,url);
            im=true;
        }else{
            shareItem(url2,url);
            im=false;
        }*/
    }

    /*@Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        recyclerAdapter.setAdapter(String.valueOf(charSequence));
        myList.setVisibility(View.VISIBLE);
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }*/


    private Observer<TextViewTextChangeEvent> _getSearchObserver() {
        return new Observer<TextViewTextChangeEvent>() {
            @Override
            public void onCompleted() {
                Timber.d("--------- onComplete");
            }

            @Override
            public void onError(Throwable e) {
                Timber.e(e, "--------- Woops on error!");
                Log.e("Dang error", "check your logs");
            }

            @Override
            public void onNext(TextViewTextChangeEvent onTextChangeEvent) {
                /*_log(format("Searching for %s", onTextChangeEvent.text().toString()));*/
                searchValue(onTextChangeEvent.text().toString());
            }
        };
    }

    private void searchValue(String text) {

        if (_isCurrentlyOnMainThread()) {
            /*_logs.add(0, logMsg + " (main thread) ");
            _adapter.clear();
            _adapter.addAll(_logs);*/
            Log.e("Searching for", text);

            // calling api
            mSearchPresenter.getSearchResult(mCompositeSubscription,text);
            //clear previous result
            mSearchModelList = new ArrayList<>();


            mSearchResultLayout.setVisibility(View.VISIBLE);



        } else {
            /*_logs.add(0, logMsg + " (NOT main thread) ");*/

            // You can only do below stuff on main thread.
            new Handler(Looper.getMainLooper()).post(() -> {
               /* _adapter.clear();
                _adapter.addAll(_logs);*/
            });
        }
    }

    private boolean _isCurrentlyOnMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }


    @Override
    public void showProgressBar() {
        mProgressBar.setVisibility(View.VISIBLE);
        myList.setVisibility(View.GONE);
    }

    @Override
    public void hideProgressBar() {
        mProgressBar.setVisibility(View.GONE);
        myList.setVisibility(View.VISIBLE);
    }

    @Override
    public void searchError(String error) {
        mProgressBar.setVisibility(View.GONE);
        recyclerAdapter.setAdapter(searchText.getText().toString());
        myList.setVisibility(View.VISIBLE);
        Log.e("error",error);
    }

    @Override
    public void searchSuccess(List<SearchModel> searchModelList) {
        Log.e("end_result","result");
    }


    public void shareItem(String url, String detail) {
        /*Picasso.with(getApplicationContext()).load(url).into(new com.squareup.picasso.Target() {
            @Override public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("image*//*");
                i.putExtra(Intent.EXTRA_STREAM, getLocalBitmapUri(bitmap));
                i.createChooser(i,"Share");
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }
            @Override public void onBitmapFailed(Drawable errorDrawable) { }
            @Override public void onPrepareLoad(Drawable placeHolderDrawable) { }
        });*/

        if(!isAirplaneModeOn(this)){
            //Picasso Code
            Picasso.with(getApplicationContext()).load(url).into(new com.squareup.picasso.Target() {
                @Override public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    Intent i = new Intent(Intent.ACTION_SEND);
                    i.setType("*/*");
                    i.putExtra(Intent.EXTRA_STREAM, getLocalBitmapUri(bitmap));
                    i.putExtra(Intent.EXTRA_TEXT, "Hey look this product you will like it: "+ detail);
                    i.createChooser(i,"Share with");
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                }
                @Override public void onBitmapFailed(Drawable errorDrawable) { }
                @Override public void onPrepareLoad(Drawable placeHolderDrawable) { }
            });

        }else{
            //do something else?
            Log.e("Airoplane", "mode on");
        }

    }

    public Uri getLocalBitmapUri(Bitmap bmp) {
        Uri bmpUri = null;
        try {
            File file =  new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "share_image_" + System.currentTimeMillis() + ".jpeg");
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.close();
            bmpUri = Uri.fromFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmpUri;
    }

    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static boolean isAirplaneModeOn(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return Settings.System.getInt(context.getContentResolver(),
                    Settings.System.AIRPLANE_MODE_ON, 0) != 0;
        } else {
            return Settings.Global.getInt(context.getContentResolver(),
                    Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
        }
    }


    void getCurrentApplicationName(){
        ActivityManager activityManager = (ActivityManager) SimpleIME.this.getSystemService( Context.ACTIVITY_SERVICE );
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        for(ActivityManager.RunningAppProcessInfo appProcess : appProcesses){
            if(appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND){
                Log.i("Foreground App", appProcess.processName);
                Log.i("Foreground app name", String.valueOf(appProcess.processName.contains("whatsapp")));
            }
        }


    }

}
