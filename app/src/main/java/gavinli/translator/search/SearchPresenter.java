package gavinli.translator.search;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.Spanned;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by GavinLi
 * on 16-11-15.
 */

public class SearchPresenter implements SearchContract.Presenter {
    private SearchContract.View mView;
    private SearchContract.Model mModel;
    private Context mContext;
    private Subscription mAutoComplete;

    public SearchPresenter(SearchContract.View view, SearchContract.Model model,
                           Context context) {
        mView = view;
        mModel = model;
        mContext = context;
        mView.setPresenter(this);
    }

    @Override
    public void loadExplain(String word) {
        if (mAutoComplete != null) {
            mAutoComplete.unsubscribe();
            mAutoComplete = null;
        }
        Observable<ArrayList<Spanned>> observable = Observable.create(new Observable.OnSubscribe<ArrayList<Spanned>>() {
            @Override
            public void call(Subscriber<? super ArrayList<Spanned>> subscriber) {
                try {
                    subscriber.onNext(mModel.getExplain(word.replace(" ", "-"),
                            word -> new SaveWordTask().execute(word),
                            url -> onSpeaked(url)));
                } catch (IOException | IndexOutOfBoundsException e) {
                    e.printStackTrace();
                    subscriber.onError(e);
                }
            }
        }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread());
        observable.subscribe(new Subscriber<ArrayList<Spanned>>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
                if(e instanceof IOException) {
                    mView.showNetworkError();
                } else if (e instanceof IndexOutOfBoundsException) {
                    mView.showNotFoundWordError();
                }
            }

            @Override
            public void onNext(ArrayList<Spanned> spanneds) {
                mView.hideBackground();
                mView.showExplain(spanneds);
            }
        });
    }

    private void onSpeaked(String url) {
        MediaPlayer mediaPlayer = MediaPlayer.create(mContext, Uri.parse(url));
        mediaPlayer.setLooping(false);
        mediaPlayer.start();
    }

    @Override
    public void loadAutoComplete(String key) {
        if (mAutoComplete != null) {
            mAutoComplete.unsubscribe();
            mAutoComplete = null;
        }
        Observable<ArrayList<String>> observable = Observable.create(new Observable.OnSubscribe<ArrayList<String>>() {
            @Override
            public void call(Subscriber<? super ArrayList<String>> subscriber) {
                try {
                    subscriber.onNext(mModel.getComplete(key));
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                    subscriber.onError(e);
                }
            }
        }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread());
        mAutoComplete = observable.subscribe(new Subscriber<ArrayList<String>>() {
            @Override
            public void onCompleted() {
                mAutoComplete = null;
            }

            @Override
            public void onError(Throwable e) {
                if(e instanceof IOException) {
                    mView.showNetworkError();
                } else if(e instanceof JSONException) {
                    //不应该出现JSONException
                    throw new RuntimeException(e);
                }
                mAutoComplete = null;
            }

            @Override
            public void onNext(ArrayList<String> words) {
                mView.showSuggestion(words);
            }
        });
    }

    class SaveWordTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            if(!mModel.wordExisted(strings[0])) {
                mModel.saveWord(strings[0]);
                return "单词保存至Wordbook";
            } else {
                return "单词已存在";
            }
        }

        @Override
        protected void onPostExecute(String info) {
            mView.showWordInfo(info);
        }
    }
}