package app.newt.id.server.presenter;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import app.newt.id.R;
import app.newt.id.helper.AES;
import app.newt.id.helper.API;
import app.newt.id.helper.Constant;
import app.newt.id.helper.Session;
import app.newt.id.helper.Utils;
import app.newt.id.server.interfaces.Response;
import app.newt.id.server.model.Chat;
import app.newt.id.server.response.BaseResponse;
import app.newt.id.server.response.ChallengeResponse;
import app.newt.id.server.response.ChatResponse;
import app.newt.id.server.response.DialogsResponse;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

public class ChallengePresenter {
    private Context context;
    private Response response;

    public CompositeDisposable disposables;

    public ChallengePresenter(Context context, Response response) {
        this.context = context;
        this.response = response;
        disposables = new CompositeDisposable();
    }

    public void registerChallenger() {
        API.with(context).getRest().registerChallenger()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<retrofit2.Response<BaseResponse>>() {
                               @Override
                               public void onSubscribe(Disposable d) {
                                   disposables.add(d);
                               }

                               @Override
                               public void onSuccess(retrofit2.Response<BaseResponse> value) {
                                   if (value.code() == 200) {
                                       response.onSuccess(value.body());
                                   } else {
                                       response.onFailure(value.message());
                                   }
                               }

                               @Override
                               public void onError(Throwable e) {
                                   response.onFailure(e.getMessage());
                               }
                           }
                );
    }

    public void loadQuestions(int lesson) {
        API.with(context).getRest().loadQuestions(lesson)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<retrofit2.Response<ChallengeResponse>>() {
                               @Override
                               public void onSubscribe(Disposable d) {
                                   disposables.add(d);
                               }

                               @Override
                               public void onSuccess(retrofit2.Response<ChallengeResponse> value) {
                                   if (value.code() == 200) {
                                       response.onSuccess(value.body());
                                   } else {
                                       response.onFailure(value.message());
                                   }
                               }

                               @Override
                               public void onError(Throwable e) {
                                   response.onFailure(e.getMessage());
                               }
                           }
                );
    }

    public void loadQuestionDetail(int id) {
        API.with(context).getRest().loadQuestionDetail(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<retrofit2.Response<ChallengeResponse>>() {
                               @Override
                               public void onSubscribe(Disposable d) {
                                   disposables.add(d);
                               }

                               @Override
                               public void onSuccess(retrofit2.Response<ChallengeResponse> value) {
                                   if (value.code() == 200) {
                                       response.onSuccess(value.body());
                                   } else {
                                       response.onFailure(value.message());
                                   }
                               }

                               @Override
                               public void onError(Throwable e) {
                                   response.onFailure(e.getMessage());
                               }
                           }
                );
    }

    public void loadRanks(int lessonId) {
        API.with(context).getRest().loadRanks(lessonId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<retrofit2.Response<ChallengeResponse>>() {
                               @Override
                               public void onSubscribe(Disposable d) {
                                   disposables.add(d);
                               }

                               @Override
                               public void onSuccess(retrofit2.Response<ChallengeResponse> value) {
                                   if (value.code() == 200) {
                                       response.onSuccess(value.body());
                                   } else {
                                       response.onFailure(value.message());
                                   }
                               }

                               @Override
                               public void onError(Throwable e) {
                                   response.onFailure(e.getMessage());
                               }
                           }
                );
    }

    public void loadRecords(int challengerId, int lessonId) {
        API.with(context).getRest().loadRecords(challengerId, lessonId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<retrofit2.Response<ChallengeResponse>>() {
                               @Override
                               public void onSubscribe(Disposable d) {
                                   disposables.add(d);
                               }

                               @Override
                               public void onSuccess(retrofit2.Response<ChallengeResponse> value) {
                                   if (value.code() == 200) {
                                       response.onSuccess(value.body());
                                   } else {
                                       response.onFailure(value.message());
                                   }
                               }

                               @Override
                               public void onError(Throwable e) {
                                   response.onFailure(e.getMessage());
                               }
                           }
                );
    }

    public void submitAnswer(int id, String answer) {
        API.with(context).getRest().submitAnswer(id, Integer.valueOf(answer))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<retrofit2.Response<ChallengeResponse>>() {
                               @Override
                               public void onSubscribe(Disposable d) {
                                   disposables.add(d);
                               }

                               @Override
                               public void onSuccess(retrofit2.Response<ChallengeResponse> value) {
                                   if (value.code() == 200) {
                                       response.onSuccess(value.body());
                                   } else {
                                       response.onFailure(value.message());
                                   }
                               }

                               @Override
                               public void onError(Throwable e) {
                                   response.onFailure(e.getMessage());
                               }
                           }
                );
    }

    public void loadHistories(int lessonId, int lastId) {
        API.with(context).getRest().loadHistories(lessonId, lastId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<retrofit2.Response<ChallengeResponse>>() {
                               @Override
                               public void onSubscribe(Disposable d) {
                                   disposables.add(d);
                               }

                               @Override
                               public void onSuccess(retrofit2.Response<ChallengeResponse> value) {
                                   if (value.code() == 200) {
                                       response.onSuccess(value.body());
                                   } else {
                                       response.onFailure(value.message());
                                   }
                               }

                               @Override
                               public void onError(Throwable e) {
                                   response.onFailure(e.getMessage());
                               }
                           }
                );
    }
}