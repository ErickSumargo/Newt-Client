package app.newt.id.server.presenter;

import android.content.Context;
import android.net.Uri;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import app.newt.id.helper.API;
import app.newt.id.helper.Constant;
import app.newt.id.helper.Session;
import app.newt.id.helper.Utils;
import app.newt.id.server.interfaces.Response;
import app.newt.id.server.response.BaseResponse;
import app.newt.id.server.response.RatingResponse;
import app.newt.id.server.response.RegistrationResponse;
import app.newt.id.server.response.UserResponse;
import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;

public class UserPresenter {
    private Context context;
    private Response response;

    public CompositeDisposable disposables;

    public UserPresenter(Context context, Response response) {
        this.context = context;
        this.response = response;
        disposables = new CompositeDisposable();
    }

    public void registerPhone(String phone, String device) {
        Map<String, Object> data = new HashMap<>();
        data.put(Constant.REQ_PHONE, phone);
        data.put(Constant.REQ_DEVICE, device);

        API.with(context).getRest().registerPhone(data)
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

    public void resendCode(String phone, int regType) {
        Map<String, Object> data = new HashMap<>();
        data.put(Constant.REQ_PHONE, phone);
        data.put(Constant.REQ_REG_TYPE, regType);

        API.with(context).getRest().resendCode(data)
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
                                       value.body().setTag(Constant.REQ_RESEND_CODE);
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

    public void verifyCode(String phone, String code, int regType) {
        Map<String, Object> data = new HashMap<>();
        data.put(Constant.REQ_PHONE, phone);
        data.put(Constant.REQ_CODE, code);
        data.put(Constant.REQ_REG_TYPE, regType);

        if (regType == 0 || regType == 1) {
            API.with(context).getRest().verifyCode(data)
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
                                           value.body().setTag(Constant.REQ_CODE);
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

    public void registerStudent(String name, String password, String school, String promoCode, String phone, String device, String firebase) {
        Map<String, Object> data = new HashMap<>();
        data.put(Constant.REQ_NAME, name);
        data.put(Constant.REQ_PASSWORD, password);
        data.put(Constant.REQ_SCHOOL, school);
        data.put(Constant.REQ_PROMO_CODE, promoCode);
        data.put(Constant.REQ_PHONE, phone);
        data.put(Constant.REQ_DEVICE, device);
        data.put(Constant.REQ_FIREBASE, firebase);
        data.put(Constant.REQ_PRO, 0);

        API.with(context).getRest().registerStudent(data)
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

    public void login(String phone, String password, String device, String firebase) {
        Map<String, Object> data = new HashMap<>();
        data.put(Constant.REQ_PHONE, phone);
        data.put(Constant.REQ_PASSWORD, password);
        data.put(Constant.REQ_DEVICE, device);
        data.put(Constant.REQ_FIREBASE, firebase);
        data.put(Constant.REQ_PRO, 0);

        API.with(context).getRest().login(data)
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

    public void reregisterPhone(String phone) {
        Map<String, Object> data = new HashMap<>();
        data.put(Constant.REQ_PHONE, phone);

        API.with(context).getRest().reregisterPhone(data)
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

    public void resetPhone(String phone, String code) {
        Map<String, Object> data = new HashMap<>();
        data.put(Constant.REQ_PHONE, phone);
        data.put(Constant.REQ_CODE, code);

        API.with(context).getRest().resetPhone(data)
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
                                       value.body().setTag(Constant.REQ_CODE);
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

    public void applyPromoCode(String promoCode) {
        API.with(context).getRest().applyPromoCode(promoCode)
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

    public void updateProfile(String name, String school, String photo) {
        Map<String, RequestBody> data = new HashMap<>();
        data.put(Constant.REQ_NAME, Utils.with(context).convertToRequestBody(name));
        data.put(Constant.REQ_SCHOOL, Utils.with(context).convertToRequestBody(school));

        boolean valid = true;
        if (Session.with(context).isProfilePhotoChanged()) {
            if (!photo.isEmpty()) {
                Uri uri = Uri.parse(photo);
                File file = new File(uri.getPath());
                if (file.exists()) {
                    data.put(Constant.REQ_IMAGE + "." + Utils.with(context).getFileExtension(Uri.fromFile(file)) + "\"", Utils.with(context).convertToRequestBody(file));
                    data.put(Constant.REQ_PHOTO_CHANGED, Utils.with(context).convertToRequestBody("true"));
                } else {
                    valid = false;
                }
            } else {
                data.put(Constant.REQ_PHOTO_CHANGED, Utils.with(context).convertToRequestBody("true"));
            }
        }

        if (valid) {
            API.with(context).getRest().updateProfile(data)
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

    public void getRating(int teacherId, int lesson) {
        API.with(context).getRest().getRating(teacherId, lesson)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<retrofit2.Response<RatingResponse>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposables.add(d);
                    }

                    @Override
                    public void onSuccess(retrofit2.Response<RatingResponse> value) {
                        if (value.code() == 200) {
                            value.body().setTag(Constant.REQ_GET_RATING);
                            response.onSuccess(value.body());
                        } else {
                            response.onFailure(Constant.REQ_GET_RATING + value.message());
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        response.onFailure(e.getMessage());
                    }
                });
    }

    public void setRating(int teacherId, int rating, int lesson) {
        Map<String, Object> data = new HashMap<>();
        data.put(Constant.REQ_TEACHER_ID, teacherId);
        data.put(Constant.REQ_RATING, rating);
        data.put(Constant.REQ_LESSON_ID, lesson);

        API.with(context).getRest().setRating(data)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<retrofit2.Response<RatingResponse>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposables.add(d);
                    }

                    @Override
                    public void onSuccess(retrofit2.Response<RatingResponse> value) {
                        if (value.code() == 200) {
                            value.body().setTag(Constant.REQ_SET_RATING);
                            response.onSuccess(value.body());
                        } else {
                            response.onFailure(value.message());
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        response.onFailure(e.getMessage());
                    }
                });
    }

    public void getAvgRating(int teacherId) {
        API.with(context).getRest().getAvgRating(teacherId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<retrofit2.Response<RatingResponse>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposables.add(d);
                    }

                    @Override
                    public void onSuccess(retrofit2.Response<RatingResponse> value) {
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
                });
    }
}