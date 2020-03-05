package app.newt.id.server.presenter;

import android.content.Context;

import java.util.List;

import app.newt.id.helper.API;
import app.newt.id.helper.Constant;
import app.newt.id.helper.Session;
import app.newt.id.server.interfaces.Response;
import app.newt.id.server.model.User;
import app.newt.id.server.response.BaseResponse;
import app.newt.id.server.response.DialogsResponse;
import app.newt.id.server.response.FeatureResponse;
import app.newt.id.server.response.LessonsResponse;
import app.newt.id.server.response.PrivateTeacherResponse;
import app.newt.id.server.response.TeachersResponse;
import app.newt.id.server.response.UpdateResponse;
import app.newt.id.server.response.UserResponse;
import app.newt.id.server.response.wrapper.BaseWrapper;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.schedulers.Schedulers;

public class BasePresenter {
    private Context context;
    private Response response;

    public CompositeDisposable disposables;

    public BasePresenter(Context context, Response response) {
        this.context = context;
        this.response = response;
        disposables = new CompositeDisposable();
    }

    public void estConnection() {
        API.with(context).getRest().test()
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

    public void updateApp() {
        API.with(context).getRest().updateApp()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<retrofit2.Response<UpdateResponse>>() {
                               @Override
                               public void onSubscribe(Disposable d) {
                                   disposables.add(d);
                               }

                               @Override
                               public void onSuccess(retrofit2.Response<UpdateResponse> value) {
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

    public void loadFeatures() {
        API.with(context).getRest().loadFeatures()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<retrofit2.Response<FeatureResponse>>() {
                               @Override
                               public void onSubscribe(Disposable d) {
                                   disposables.add(d);
                               }

                               @Override
                               public void onSuccess(retrofit2.Response<FeatureResponse> value) {
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

    public void loadBase() {
        if (Session.with(context).getUserType().equals(Constant.STUDENT)) {
            Single.zip(API.with(context).getRest().loadDialogs()
                            .subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread()),
                    API.with(context).getRest().loadTeachers()
                            .subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread()),
                    new BiFunction<retrofit2.Response<DialogsResponse>, retrofit2.Response<TeachersResponse>, BaseWrapper>() {
                        @Override
                        public BaseWrapper apply(retrofit2.Response<DialogsResponse> value1, retrofit2.Response<TeachersResponse> value2) throws Exception {
                            return new BaseWrapper(value1.body(), value2.body());
                        }
                    }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<BaseWrapper>() {
                        @Override
                        public void onSubscribe(Disposable d) {
                            disposables.add(d);
                        }

                        @Override
                        public void onSuccess(BaseWrapper value) {
                            response.onSuccess(value);
                        }

                        @Override
                        public void onError(Throwable e) {
                            response.onFailure(e.getMessage());
                        }
                    });
        } else {
            API.with(context).getRest().loadDialogs()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<retrofit2.Response<DialogsResponse>>() {
                                   @Override
                                   public void onSubscribe(Disposable d) {
                                       disposables.add(d);
                                   }

                                   @Override
                                   public void onSuccess(retrofit2.Response<DialogsResponse> value) {
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

    public void fetchProfiles(List<User> users) {
        String codes = "";
        for (int i = 0; i < users.size(); i++) {
            if (i < users.size() - 1) {
                codes += users.get(i).getCode() + "-";
            } else {
                codes += users.get(i).getCode();
            }
        }

        if (!codes.isEmpty()) {
            API.with(context).getRest().fetchProfiles(codes)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<retrofit2.Response<UserResponse>>() {
                                   @Override
                                   public void onSubscribe(Disposable d) {
                                       disposables.add(d);
                                   }

                                   @Override
                                   public void onSuccess(retrofit2.Response<UserResponse> value) {
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

    public void fetchTeachers() {
        API.with(context).getRest().fetchTeachers()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<retrofit2.Response<TeachersResponse>>() {
                               @Override
                               public void onSubscribe(Disposable d) {
                                   disposables.add(d);
                               }

                               @Override
                               public void onSuccess(retrofit2.Response<TeachersResponse> value) {
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

    public void loadPrivateTeachers(int gradeId) {
        API.with(context).getRest().loadPrivateTeachers(gradeId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<retrofit2.Response<PrivateTeacherResponse>>() {
                               @Override
                               public void onSubscribe(Disposable d) {
                                   disposables.add(d);
                               }

                               @Override
                               public void onSuccess(retrofit2.Response<PrivateTeacherResponse> value) {
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