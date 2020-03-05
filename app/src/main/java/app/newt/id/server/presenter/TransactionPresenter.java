package app.newt.id.server.presenter;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;

import app.newt.id.helper.API;
import app.newt.id.helper.Constant;
import app.newt.id.server.interfaces.Response;
import app.newt.id.server.response.RegistrationResponse;
import app.newt.id.server.response.TransactionResponse;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class TransactionPresenter {
    private Context context;
    private Response response;

    public CompositeDisposable disposables;

    public TransactionPresenter(Context context, Response response) {
        this.context = context;
        this.response = response;
        disposables = new CompositeDisposable();
    }

    public void getTransactionDatas() {
        API.with(context).getRest().getTransactionDatas()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<retrofit2.Response<TransactionResponse>>() {
                               @Override
                               public void onSubscribe(Disposable d) {
                                   disposables.add(d);
                               }

                               @Override
                               public void onSuccess(retrofit2.Response<TransactionResponse> value) {
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