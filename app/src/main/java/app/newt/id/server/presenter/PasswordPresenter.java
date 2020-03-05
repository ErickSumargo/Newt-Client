package app.newt.id.server.presenter;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;

import app.newt.id.helper.API;
import app.newt.id.helper.Constant;
import app.newt.id.server.interfaces.Response;
import app.newt.id.server.response.RegistrationResponse;
import app.newt.id.server.response.UserResponse;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;

public class PasswordPresenter {
    private Context context;
    private Response response;

    public CompositeDisposable disposables;

    public PasswordPresenter(Context context, Response response) {
        this.context = context;
        this.response = response;
        disposables = new CompositeDisposable();
    }

    public void validatePhone(String phone) {
        Map<String, Object> data = new HashMap<>();
        data.put(Constant.REQ_PHONE, phone);

        API.with(context).getRest().validatePhone(data)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<retrofit2.Response<RegistrationResponse>>() {
                               @Override
                               public void onSubscribe(Disposable d) {
                                   disposables.add(d);
                               }

                               @Override
                               public void onSuccess(retrofit2.Response<RegistrationResponse> value) {
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

    public void resetPassword(String phone, String password) {
        Map<String, Object> data = new HashMap<>();
        data.put(Constant.REQ_PHONE, phone);
        data.put(Constant.REQ_PASSWORD, password);

        API.with(context).getRest().resetPassword(data)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<retrofit2.Response<RegistrationResponse>>() {
                               @Override
                               public void onSubscribe(Disposable d) {
                                   disposables.add(d);
                               }

                               @Override
                               public void onSuccess(retrofit2.Response<RegistrationResponse> value) {
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