package app.newt.id.background.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.IBinder;

import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import app.newt.id.background.receiver.NetworkReceiver;
import app.newt.id.helper.AES;
import app.newt.id.helper.Constant;
import app.newt.id.helper.Session;
import app.newt.id.helper.Utils;
import app.newt.id.server.interfaces.Response;
import app.newt.id.server.model.Chat;
import app.newt.id.server.model.User;
import app.newt.id.server.presenter.ChatPresenter;
import app.newt.id.server.response.BaseResponse;
import app.newt.id.server.response.ChatResponse;
import app.newt.id.view.interfaces.MessageStatusChanged;
import app.newt.id.view.interfaces.NoInternet;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by Erick Sumargo on 2/24/2018.
 */

public class StoreChatService extends Service implements Response, NoInternet {
    private Handler mHandler = new Handler();
    private Timer mTimer = null;

    private final long INTERVAL = 5000;
    private int sent = -1;
    private boolean connected = false, socketConnected = false, serviceDestroyed = false;

    private Realm realm;
    private RealmResults<Chat> unsends;
    private Chat chat;

    private User user;

    private static MessageStatusChanged messageStatusChanged;

    private NetworkReceiver networkReceiver;
    private ChatPresenter chatPresenter;

    private String uniqueCode = "";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        networkReceiver = new NetworkReceiver();
        networkReceiver.setOnNoInternetListener(this);
        registerReceiver(networkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        if (onSocketEvent != null) {
            IntentFilter intentFilter = new IntentFilter(Constant.SOCKET_IO);
            registerReceiver(onSocketEvent, intentFilter);
        }

        if (mTimer != null) {
            mTimer.cancel();
        } else {
            mTimer = new Timer();
            chatPresenter = new ChatPresenter(getApplicationContext(), this);

            realm = Realm.getDefaultInstance();
            unsends = realm.where(Chat.class)
                    .equalTo("senderCode", Session.with(this).getUser().getCode())
                    .equalTo("sent", 0)
                    .findAll();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    public void setOnStatusChanged(MessageStatusChanged messageStatusChanged) {
        this.messageStatusChanged = messageStatusChanged;
    }

    private void store() {
        if (sent != -1) {
            chatPresenter.add(chat, sent);
        } else {
            mTimer = new Timer();
            mTimer.scheduleAtFixedRate(new CustomTask(), 0, INTERVAL);
        }
    }

    private class CustomTask extends TimerTask {

        @Override
        public void run() {
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    if (!serviceDestroyed) {
                        if (sent == -1) {
                            if ((connected && socketConnected) && unsends.size() > 0) {
                                mTimer.cancel();

                                chat = unsends.get(0);
                                emitMessage();
                            }
                        } else {
                            mTimer.cancel();
                            store();
                        }
                    }
                }
            });
        }
    }

    private void emitMessage() {
        user = Session.with(this).getUser();

        String content = chat.getContent();
        if (chat.getContentType() == 1) {
            String[] parts = AES.decrypt(content).split("-");
            content = AES.encrypt(parts[1] + "-" + parts[2]);
        }
        JSONObject object = Utils.with(this).createJSON(Constant.CHATTING_MESSAGE, chat.getUuid(),
                user.getCode(), chat.getReceiverCode(),
                content, String.valueOf(chat.getContentType()), String.valueOf(chat.getLesson_id()), "0",
                String.valueOf(user.getId()), user.getName(),
                user.getPhone(), user.getPhoto(),
                String.valueOf(user.getActive()), user.getCreatedAt(), String.valueOf(user.getPro()));

        Intent service = new Intent(this, ChattingService.class);
        service.putExtra(Constant.CHATTING_EVENT, Constant.CHATTING_MESSAGE);
        if (object != null) {
            service.putExtra("params", object.toString());
        }
        startService(service);
    }

    @Override
    public void onInternetStatus(boolean connected) {
        this.connected = connected;
        if (connected && socketConnected) {
            mTimer = new Timer();
            mTimer.scheduleAtFixedRate(new CustomTask(), 0, INTERVAL);
        } else {
            mTimer.cancel();
        }
    }

    private BroadcastReceiver onSocketEvent = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Constant.SOCKET_IO)) {
                switch (intent.getStringExtra(Constant.CHATTING_EVENT)) {
                    case Constant.SOCKET_CONNECTED:
                        socketConnected = intent.getBooleanExtra(Constant.SOCKET_CONNECTED, false);
                        if (connected && socketConnected) {
                            mTimer = new Timer();
                            mTimer.scheduleAtFixedRate(new CustomTask(), 0, INTERVAL);
                        } else {
                            mTimer.cancel();
                        }
                        break;
                    case Constant.CHATTING_MESSAGE_SENT:
                        if (uniqueCode.isEmpty() || !uniqueCode.equals(intent.getStringExtra(Constant.REQ_UNIQUE_CODE))) {
                            uniqueCode = intent.getStringExtra(Constant.REQ_UNIQUE_CODE);

                            sent = intent.getIntExtra(Constant.REQ_SENT, -1);
                            store();
                        }
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

        unregisterReceiver(networkReceiver);
        if (onSocketEvent != null) {
            unregisterReceiver(onSocketEvent);
        }
        super.onDestroy();
    }

    @Override
    public void onSuccess(final BaseResponse base) {
        if (chat != null) {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    chat.setSent(1);
                }
            });
            sent = -1;

            String dialogId = Utils.with(this).getDialogId(chat.getSenderCode(), chat.getReceiverCode(), chat.getLesson_id());
            if (dialogId.equals(Session.with(this).getActiveDialog())) {
                messageStatusChanged.onMessageStatusChanged(chat);
            }

            if (unsends.size() > 0) {
                if (connected && socketConnected) {
                    chat = unsends.get(0);
                    emitMessage();
                } else {
                    mTimer.cancel();
                }
            } else {
                chat = null;

                mTimer = new Timer();
                mTimer.scheduleAtFixedRate(new CustomTask(), 0, INTERVAL);
            }
        } else {
            sent = -1;

            mTimer = new Timer();
            mTimer.scheduleAtFixedRate(new CustomTask(), 0, INTERVAL);
        }
    }

    @Override
    public void onFailure(String message) {
        if (connected && socketConnected) {
            store();
        }
    }
}