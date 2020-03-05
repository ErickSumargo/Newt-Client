package app.newt.id.background.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import app.newt.id.helper.Internet;
import app.newt.id.helper.Session;
import app.newt.id.helper.Utils;
import app.newt.id.server.interfaces.Response;
import app.newt.id.server.model.Available;
import app.newt.id.server.model.DaysOff;
import app.newt.id.server.model.Lesson;
import app.newt.id.server.model.Teacher;
import app.newt.id.server.model.User;
import app.newt.id.server.presenter.BasePresenter;
import app.newt.id.server.response.BaseResponse;
import app.newt.id.server.response.TeachersResponse;
import io.realm.Realm;
import io.realm.RealmList;
import okhttp3.internal.Util;

/**
 * Created by Erick Sumargo on 2/24/2018.
 */

public class FetchTeachersService extends Service implements Response {
    private Handler mHandler = new Handler();
    private Timer mTimer = null;

    private final long INTERVAL = 60000;

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
            basePresenter = new BasePresenter(getApplicationContext(), this);
        }
        mTimer.scheduleAtFixedRate(new CustomTask(), 0, INTERVAL);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    public void attemptFetch() {
        if (Internet.isConnected(this)) {
            basePresenter.fetchTeachers();

            Utils.with(this).saveServerTime();
        }
    }

    private class CustomTask extends TimerTask {

        @Override
        public void run() {
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    if (Session.with(FetchTeachersService.this).isBaseDataLoaded()) {
                        attemptFetch();
                    }
                }
            });
        }
    }

    @Override
    public void onDestroy() {
        mTimer.cancel();

        super.onDestroy();
    }

    @Override
    public void onSuccess(BaseResponse base) {
        TeachersResponse r = (TeachersResponse) base;
        final List<Teacher> teachers = r.getData().getTeachers();

        realm = Realm.getDefaultInstance();
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                for (Teacher teacher : teachers) {
                    Teacher t = realm.where(Teacher.class).equalTo("user.id", teacher.getUser().getId()).findFirst();
                    if (t != null) {
                        User u = realm.copyToRealmOrUpdate(teacher.getUser());
                        t.setUser(u);

                        RealmList<Lesson> lessons = new RealmList<>();
                        for (Lesson l : teacher.getLessons()) {
                            lessons.add(realm.copyToRealmOrUpdate(l));
                        }
                        t.setLessons(lessons);

                        RealmList<Available> availables = new RealmList<>();
                        for (Available a : teacher.getAvailables()) {
                            availables.add(realm.copyToRealmOrUpdate(a));
                        }
                        t.setAvailables(availables);

                        RealmList<DaysOff> days_off = new RealmList<>();
                        for (DaysOff day : teacher.getDays_off()) {
                            days_off.add(realm.copyToRealmOrUpdate(day));
                        }
                        t.setDays_off(days_off);
                    } else {
                        realm.copyToRealmOrUpdate(teacher);
                    }
                }
            }
        });
        realm.close();
    }

    @Override
    public void onFailure(String message) {
    }
}