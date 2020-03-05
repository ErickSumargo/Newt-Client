package app.newt.id.background.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

import app.newt.id.helper.Constant;
import app.newt.id.helper.Session;
import app.newt.id.helper.Utils;
import app.newt.id.server.model.Dialog;
import app.newt.id.server.model.Teacher;
import app.newt.id.server.model.User;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by Erick Sumargo on 2/24/2018.
 */

public class CheckOnlineService extends Service {
    private Handler mHandler = new Handler();
    private Timer mTimer = null;

    private final long INTERVAL = 5000;
    private boolean serviceDestroyed = false;

    private Realm realm;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        if (onSocketEvent != null) {
            IntentFilter intentFilter = new IntentFilter(Constant.SOCKET_IO);
            registerReceiver(onSocketEvent, intentFilter);
        }

        if (mTimer != null) {
            mTimer.cancel();
        } else {
            mTimer = new Timer();
            realm = Realm.getDefaultInstance();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private class CustomTask extends TimerTask {

        @Override
        public void run() {
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    if (!serviceDestroyed) {
                        RealmResults<User> users = realm.where(User.class).sort("createdAt", Sort.DESCENDING).findAll();

                        JSONArray jsonArray = new JSONArray();
                        for (User user : users) {
                            JSONObject u = Utils.with(CheckOnlineService.this).createJSON(Constant.CHATTING_CHECK_ONLINE, user.getCode());
                            jsonArray.put(u);
                        }

                        if (jsonArray.length() > 0) {
                            emitEvent(Constant.CHATTING_CHECK_ONLINE, jsonArray);
                        }
                    }
                }
            });
        }
    }

    private void emitEvent(String event, JSONArray jsonArray) {
        Intent service = new Intent(this, ChattingService.class);
        service.putExtra(Constant.CHATTING_EVENT, event);
        service.putExtra("params", jsonArray.toString());

        startService(service);
    }

    private BroadcastReceiver onSocketEvent = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {
            if (intent.getAction().equals(Constant.SOCKET_IO)) {
                switch (intent.getStringExtra(Constant.CHATTING_EVENT)) {
                    case Constant.SOCKET_CONNECTED:
                        boolean connected = intent.getBooleanExtra(Constant.SOCKET_CONNECTED, false);
                        if (connected) {
                            mTimer = new Timer();
                            mTimer.scheduleAtFixedRate(new CustomTask(), 0, INTERVAL);
                        } else {
                            mTimer.cancel();

                            realm.executeTransaction(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    RealmResults<User> users = realm.where(User.class).findAll();
                                    for (int i = 0; i < users.size(); i++) {
                                        users.get(i).setOnline(0);
                                    }

                                    // Just for updating in DialogListAdpt...
                                    Dialog d = realm.where(Dialog.class).findFirst();
                                    if (d != null) {
                                        d.setUser(d.getUser());
                                    }

                                    // Just for updating in TeacherListAdpt...
                                    if (Session.with(CheckOnlineService.this).getUserType().equals(Constant.STUDENT)) {
                                        Teacher t = realm.where(Teacher.class).findFirst();
                                        if (t != null) {
                                            t.setUser(t.getUser());
                                        }
                                    }
                                }
                            });
                        }
                        break;
                    case Constant.CHATTING_CHECK_ONLINE:
                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                try {
                                    JSONArray jsonArray = new JSONArray(intent.getStringExtra(Constant.REQ_ONLINE_LIST));
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                                        final String code = jsonObject.getString("code");
                                        final boolean online = jsonObject.getBoolean("online");

                                        User user = realm.where(User.class).equalTo("code", code).findFirst();
                                        if (user != null) {
                                            user.setOnline(online ? 1 : 0);
                                        }
                                    }
                                } catch (JSONException e) {
                                }

                                // Just for updating in DialogListAdpt...
                                Dialog d = realm.where(Dialog.class).findFirst();
                                if (d != null) {
                                    d.setUser(d.getUser());
                                }

                                // Just for updating in TeacherListAdpt...
                                if (Session.with(CheckOnlineService.this).getUserType().equals(Constant.STUDENT)) {
                                    Teacher t = realm.where(Teacher.class).findFirst();
                                    if (t != null) {
                                        t.setUser(t.getUser());
                                    }
                                }
                            }
                        });
                        break;
                }
            }
        }
    };

    @Override
    public void onDestroy() {
        serviceDestroyed = true;

        mTimer.cancel();
        realm.close();

        if (onSocketEvent != null) {
            unregisterReceiver(onSocketEvent);
        }
        super.onDestroy();
    }
}