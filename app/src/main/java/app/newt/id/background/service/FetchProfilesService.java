package app.newt.id.background.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import app.newt.id.helper.Constant;
import app.newt.id.helper.Internet;
import app.newt.id.helper.Session;
import app.newt.id.server.interfaces.Response;
import app.newt.id.server.model.Dialog;
import app.newt.id.server.model.Teacher;
import app.newt.id.server.model.User;
import app.newt.id.server.presenter.BasePresenter;
import app.newt.id.server.response.BaseResponse;
import app.newt.id.server.response.UserResponse;
import io.realm.Realm;

/**
 * Created by Erick Sumargo on 2/24/2018.
 */

public class FetchProfilesService extends Service implements Response {
    private Handler mHandler = new Handler();
    private Timer mTimer = null;

    private final long INTERVAL = 300000;

    private BasePresenter basePresenter;
    private Realm realm;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        if (mTimer != null) {
            mTimer.cancel();
        } else {
            mTimer = new Timer();
            realm = Realm.getDefaultInstance();

            basePresenter = new BasePresenter(getApplicationContext(), this);
        }
        mTimer.scheduleAtFixedRate(new CustomTask(), 0, INTERVAL);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    public void attemptUpdate() {
        if (Internet.isConnected(this)) {
            List<User> users = realm.where(User.class).notEqualTo("code", Session.with(this).getUser().getCode()).findAll();
            basePresenter.fetchProfiles(users);
        }
    }

    private class CustomTask extends TimerTask {

        @Override
        public void run() {
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    if (Session.with(FetchProfilesService.this).isBaseDataLoaded()) {
                        attemptUpdate();
                    }
                }
            });
        }
    }

    @Override
    public void onDestroy() {
        mTimer.cancel();
        realm.close();

        super.onDestroy();
    }

    @Override
    public void onSuccess(BaseResponse base) {
        UserResponse r = (UserResponse) base;
        final List<User> users = r.getData().getUsers();

        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                for (User user : users) {
                    Dialog dialog = realm.where(Dialog.class).equalTo("user.code", user.getCode()).findFirst();
                    if (dialog != null) {
                        dialog.setUser(realm.copyToRealmOrUpdate(user));
                    }

                    if (user.getType().equals(Constant.TEACHER)) {
                        Teacher teacher = realm.where(Teacher.class).equalTo("user.code", user.getCode()).findFirst();
                        if (teacher != null) {
                            teacher.setUser(realm.copyToRealmOrUpdate(user));
                        }
                    }
                }
            }
        });
    }

    @Override
    public void onFailure(String message) {
    }
}