package app.newt.id.background.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.os.Build;

import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.text.Html;
import android.util.Base64;
import android.util.Log;

import com.google.gson.GsonBuilder;
import com.squareup.picasso.Target;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import app.newt.id.background.receiver.NetworkReceiver;
import app.newt.id.helper.AES;
import app.newt.id.helper.Constant;
import app.newt.id.helper.Internet;
import app.newt.id.helper.Session;
import app.newt.id.helper.Utils;
import app.newt.id.server.interfaces.Response;
import app.newt.id.server.model.Chat;
import app.newt.id.server.model.Dialog;
import app.newt.id.server.model.Lesson;
import app.newt.id.server.model.User;
import app.newt.id.server.presenter.ChatPresenter;
import app.newt.id.server.response.BaseResponse;
import app.newt.id.server.response.DialogsResponse;
import app.newt.id.view.activity.ChatActivity;
import app.newt.id.R;
import app.newt.id.view.custom.AckCustom;
import app.newt.id.view.interfaces.NoInternet;
import io.realm.Realm;
import io.realm.RealmList;
import io.socket.client.Ack;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import io.socket.engineio.client.transports.WebSocket;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import me.leolin.shortcutbadger.ShortcutBadger;
import okhttp3.OkHttpClient;

public class ChattingService extends Service implements Response {
    private Socket socket;
    private boolean socketStarted = false, connected = false, loadingQueue = false, markingQueue = false;
    private String uniqueCode ="";

    private ChatPresenter chatPresenter;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {

        initSocket();
        chatPresenter = new ChatPresenter(ChattingService.this, this);
    }

    private void initSocket() {
        try {
            HostnameVerifier hostnameVerifier = new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };
            SSLContext sslContext = SSLContext.getInstance("TLS");
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return new java.security.cert.X509Certificate[]{};
                }

                public void checkClientTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {
                }

                public void checkServerTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {
                }
            }};
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .hostnameVerifier(hostnameVerifier)
                    .sslSocketFactory(sslContext.getSocketFactory(), new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {

                        }

                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }
                    })
                    .build();

            IO.Options opts = new IO.Options();
            opts.forceNew = true;
            opts.reconnection = true;
            opts.reconnectionDelay = 1000;
            opts.secure = true;
            opts.transports = new String[]{"polling"};
            opts.query = "user=" + new Gson().toJson(Session.with(this).getUser());
            opts.callFactory = okHttpClient;
            opts.webSocketFactory = okHttpClient;

            socket = IO.socket(Constant.NODE_JS, opts);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
        socket.on(Socket.EVENT_CONNECT, onConnect);
        socket.on(Socket.EVENT_DISCONNECT, onDisconnect);
        socket.on(Socket.EVENT_RECONNECT, onReconnect);
        socket.on(Constant.CHATTING_MESSAGE, onMessage);
        socket.on(Constant.CHATTING_TYPING, onTyping);
        socket.on(Constant.CHATTING_STOP_TYPING, onStopTyping);
        socket.on(Constant.CHATTING_IS_ACTIVE, onActive);

        socket.connect();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String event = intent.getStringExtra(Constant.CHATTING_EVENT);
            if (event != null) {
                switch (event) {
                    case Constant.CHATTING_JOIN_DIALOG:
                        JSONObject data = null;
                        try {
                            data = new JSONObject(intent.getStringExtra("params"));
                        } catch (JSONException e) {
                        }
                        socket.emit(Constant.CHATTING_JOIN_DIALOG, data);

                        break;
                    case Constant.CHATTING_LEAVE_DIALOG:
                        data = null;
                        try {
                            data = new JSONObject(intent.getStringExtra("params"));
                        } catch (JSONException e) {
                        }
                        socket.emit(Constant.CHATTING_LEAVE_DIALOG, data);

                        break;
                    case Constant.CHATTING_MESSAGE:
                        data = null;
                        try {
                            data = new JSONObject(intent.getStringExtra("params"));
                        } catch (JSONException e) {
                        }
                        emitMessage(data);

                        break;
                    case Constant.CHATTING_TYPING:
                        socket.emit(Constant.CHATTING_TYPING);
                        break;
                    case Constant.CHATTING_STOP_TYPING:
                        socket.emit(Constant.CHATTING_STOP_TYPING);
                        break;
                    case Constant.CHATTING_IS_ACTIVE:
                        data = null;
                        try {
                            data = new JSONObject(intent.getStringExtra("params"));
                        } catch (JSONException e) {
                        }
                        emitIsActive(data);

                        break;
                    case Constant.CHATTING_CHECK_ONLINE:
                        JSONArray datas = null;
                        try {
                            datas = new JSONArray(intent.getStringExtra("params"));
                        } catch (JSONException e) {
                        }
                        emitCheckOnline(datas);

                        break;
                    case Constant.CHATTING_CLEAR_QUEUE:
                        emitClearQueue();
                        break;
                }
            }
        }
        return START_STICKY;
    }

    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            connected = true;

            Intent intent = new Intent();
            intent.setAction(Constant.SOCKET_IO);
            intent.putExtra(Constant.CHATTING_EVENT, Constant.SOCKET_CONNECTED);
            intent.putExtra(Constant.SOCKET_CONNECTED, connected);

            sendBroadcast(intent);

            if (!socketStarted) {
                if (Session.with(ChattingService.this).isBaseDataLoaded()) {
                    loadingQueue = true;

                    emitLoadReadQueue();
                    loadQueue();
                }
            }
            socketStarted = true;
        }
    };

    private Emitter.Listener onDisconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            connected = false;

            Intent intent = new Intent();
            intent.setAction(Constant.SOCKET_IO);
            intent.putExtra(Constant.CHATTING_EVENT, Constant.SOCKET_CONNECTED);
            intent.putExtra(Constant.SOCKET_CONNECTED, connected);

            sendBroadcast(intent);
        }
    };

    private Emitter.Listener onReconnect = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            connected = true;

            loadingQueue = true;
            emitLoadReadQueue();
            loadQueue();

            if (!Session.with(ChattingService.this).getActiveDialog().isEmpty()) {
                Intent intent = new Intent();
                intent.setAction(Constant.SOCKET_IO);
                intent.putExtra(Constant.CHATTING_EVENT, Constant.CHATTING_JOIN_DIALOG);

                sendBroadcast(intent);
            }
        }
    };

    private void emitMessage(final JSONObject data) {
        if (connected) {
            socket.emit(Constant.CHATTING_MESSAGE, data, new AckCustom(10000) {
                @Override
                public void call(Object... args) {
                    if (args != null) {
                        if (args[0].toString().equalsIgnoreCase("No Ack")) {
                            emitMessage(data);
                        } else {
                            if ((Integer) args[0] == 1234) {
                                cancelTimer();

                                Intent intent = new Intent();
                                intent.setAction(Constant.SOCKET_IO);
                                intent.putExtra(Constant.CHATTING_EVENT, Constant.CHATTING_MESSAGE_SENT);
                                intent.putExtra(Constant.REQ_UNIQUE_CODE, (String) args[1]);
                                intent.putExtra(Constant.REQ_SENT, (Integer) args[2]);

                                sendBroadcast(intent);
                            }
                        }
                    }
                }
            });
        }
    }

    private Emitter.Listener onMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            Ack ack = (Ack) args[args.length - 1];
            ack.call();

            JSONObject object = (JSONObject) args[0];
            try {
                if (uniqueCode.isEmpty() || !uniqueCode.equals(object.getString(Constant.REQ_UNIQUE_CODE))) {
                    uniqueCode = object.getString(Constant.REQ_UNIQUE_CODE);

                    String senderCode = object.getString(Constant.REQ_SENDER_CODE);
                    String receiverCode = object.getString(Constant.REQ_RECEIVER_CODE);
                    String content = object.getString(Constant.REQ_CONTENT);
                    int contentType = Integer.valueOf(object.getString(Constant.REQ_CONTENT_TYPE));
                    int lessonId = Integer.valueOf(object.getString(Constant.REQ_LESSON_ID));

                    // Additional
                    int senderId = Integer.valueOf(object.getString(Constant.REQ_SENDER_ID));
                    String senderName = object.getString(Constant.REQ_SENDER_NAME);
                    String senderPhone = object.getString(Constant.REQ_SENDER_PHONE);
                    String senderPhoto = object.getString(Constant.REQ_SENDER_PHOTO);
                    int senderActive = Integer.valueOf(object.getString(Constant.REQ_SENDER_ACTIVE));
                    String senderCreatedAt = object.getString(Constant.REQ_SENDER_CREATED_AT);
                    String senderType = senderCode.split("_")[0].equals("STU") ? Constant.STUDENT : Constant.TEACHER;
                    int senderPro = Integer.valueOf(object.getString(Constant.REQ_SENDER_PRO));

                    final String dialogId = Utils.with(ChattingService.this).getDialogId(senderCode, receiverCode, lessonId);
                    if (dialogId.equals(Session.with(ChattingService.this).getActiveDialog())) {
                        Intent intent = new Intent();
                        intent.setAction(Constant.SOCKET_IO);
                        intent.putExtra(Constant.CHATTING_EVENT, Constant.CHATTING_MESSAGE);
                        intent.putExtra(Constant.REQ_SENDER_CODE, senderCode);
                        intent.putExtra(Constant.REQ_RECEIVER_CODE, receiverCode);
                        intent.putExtra(Constant.REQ_CONTENT, content);
                        intent.putExtra(Constant.REQ_CONTENT_TYPE, contentType);
                        intent.putExtra(Constant.REQ_LESSON_ID, lessonId);

                        sendBroadcast(intent);
                    } else {
                        Realm realm = Realm.getDefaultInstance();

                        final Chat chat = Utils.with(ChattingService.this).createChat(senderCode, receiverCode, content, contentType,
                                lessonId, 1, Utils.with(ChattingService.this).getCurrentDate());

                        final Dialog d = realm.where(Dialog.class).equalTo("id", dialogId).findFirst();
                        List<Chat> chatRealmList = new ArrayList<>();
                        if (d != null) {
                            chatRealmList.addAll(realm.copyFromRealm(d.getChats().where().equalTo("senderCode", senderCode).findAll()
                                    .where().equalTo("sent", 1).findAll()));
                            if (chatRealmList.size() == 0) {
                                updateUnreadBadge(lessonId - 1);
                            }
                            chatRealmList.add(chat);

                            realm.executeTransaction(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    realm.copyToRealmOrUpdate(chat);

                                    d.getChats().add(chat);
                                    d.setUpdatedAt(Calendar.getInstance().getTime());
                                }
                            });
                        } else {
                            final User sender = new User();
                            sender.setId(senderId);
                            sender.setCode(senderCode);
                            sender.setName(senderName);
                            sender.setPhone(senderPhone);
                            sender.setPhoto(senderPhoto);
                            sender.setActive(senderActive);
                            sender.setCreatedAt(senderCreatedAt);
                            sender.setType(senderType);

                            final RealmList chats = new RealmList();
                            chats.add(chat);
                            chatRealmList.add(chat);

                            final Lesson lesson = realm.where(Lesson.class).equalTo("id", lessonId).findFirst();

                            realm.executeTransaction(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    realm.copyToRealmOrUpdate(sender);

                                    realm.copyToRealmOrUpdate(chat);
                                    realm.copyToRealmOrUpdate(Utils.with(ChattingService.this).createDialog(dialogId, sender, chats, lesson));
                                }
                            });
                            updateUnreadBadge(lessonId - 1);
                        }
                        realm.close();
                        pushNotification(senderCode, senderName, senderPhoto, senderType, senderPro, receiverCode, dialogId, chatRealmList, lessonId - 1);
                    }
                }
            } catch (JSONException e) {
            }
        }
    };

    private Emitter.Listener onTyping = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            Intent intent = new Intent();
            intent.setAction(Constant.SOCKET_IO);
            intent.putExtra(Constant.CHATTING_EVENT, Constant.CHATTING_TYPING);

            sendBroadcast(intent);
        }
    };

    private Emitter.Listener onStopTyping = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            Intent intent = new Intent();
            intent.setAction(Constant.SOCKET_IO);
            intent.putExtra(Constant.CHATTING_EVENT, Constant.CHATTING_STOP_TYPING);

            sendBroadcast(intent);
        }
    };

    private void emitIsActive(final JSONObject data) {
        socket.emit(Constant.CHATTING_IS_ACTIVE, data, new AckCustom(2500) {
            @Override
            public void call(Object... args) {
                if (args != null) {
                    if (!args[0].toString().equalsIgnoreCase("No Ack")) {
                        if ((Integer) args[0] == 1234) {
                            cancelTimer();

                            setActive((JSONObject) args[1]);
                        }
                    }
                }
            }
        });
    }

    private void setActive(JSONObject object) {
        try {
            boolean isActive = object.getBoolean("active");
            String dialogId = object.getString("dialog");

            if (dialogId.equals(Session.with(ChattingService.this).getActiveDialog())) {
                Intent intent = new Intent();
                intent.setAction(Constant.SOCKET_IO);
                intent.putExtra(Constant.CHATTING_EVENT, Constant.CHATTING_IS_ACTIVE);
                intent.putExtra("active", isActive);

                sendBroadcast(intent);
            } else {
                if (isActive) {
                    Realm realm = Realm.getDefaultInstance();

                    final Dialog dialog = realm.where(Dialog.class).equalTo("id", dialogId).findFirst();
                    if (dialog != null) {
                        final RealmList<Chat> chats = dialog.getChats();
                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                for (int i = chats.size() - 1; i >= 0; i--) {
                                    if (chats.get(i).getSenderCode() != null) {
                                        if (chats.get(i).getSenderCode().equals(Session.with(ChattingService.this).getUser().getCode())) {
                                            if (chats.get(i).getSent() == 1) {
                                                chats.get(i).setSent(2);
                                            } else if (chats.get(i).getSent() == 2) {
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        });
                    }
                    realm.close();
                }
            }
        } catch (JSONException e) {
        }
    }

    private Emitter.Listener onActive = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            Ack ack = (Ack) args[args.length - 1];
            ack.call();

            JSONObject object = (JSONObject) args[0];
            try {
                boolean isActive = object.getBoolean("active");
                String dialogId = object.getString("dialog");

                if (dialogId.equals(Session.with(ChattingService.this).getActiveDialog())) {
                    Intent intent = new Intent();
                    intent.setAction(Constant.SOCKET_IO);
                    intent.putExtra(Constant.CHATTING_EVENT, Constant.CHATTING_IS_ACTIVE);
                    intent.putExtra("active", isActive);

                    sendBroadcast(intent);
                } else {
                    if (isActive) {
                        Realm realm = Realm.getDefaultInstance();

                        final Dialog dialog = realm.where(Dialog.class).equalTo("id", dialogId).findFirst();
                        if (dialog != null) {
                            final RealmList<Chat> chats = dialog.getChats();
                            realm.executeTransaction(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    for (int i = chats.size() - 1; i >= 0; i--) {
                                        if (chats.get(i).getSenderCode() != null) {
                                            if (chats.get(i).getSenderCode().equals(Session.with(ChattingService.this).getUser().getCode())) {
                                                if (chats.get(i).getSent() == 1) {
                                                    chats.get(i).setSent(2);
                                                } else if (chats.get(i).getSent() == 2) {
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }
                            });
                        }
                        realm.close();
                    }
                }
            } catch (JSONException e) {
            }
        }
    };

    private void emitCheckOnline(final JSONArray data) {
        socket.emit(Constant.CHATTING_CHECK_ONLINE, data, new AckCustom(2500) {
            @Override
            public void call(Object... args) {
                if (args != null) {
                    if (!args[0].toString().equalsIgnoreCase("No Ack")) {
                        if ((Integer) args[0] == 1234) {
                            cancelTimer();

                            Intent intent = new Intent();
                            intent.setAction(Constant.SOCKET_IO);
                            intent.putExtra(Constant.CHATTING_EVENT, Constant.CHATTING_CHECK_ONLINE);
                            intent.putExtra(Constant.REQ_ONLINE_LIST, args[1].toString());

                            sendBroadcast(intent);
                        }
                    }
                }
            }
        });
    }

    private void emitLoadReadQueue() {
        if (connected) {
            socket.emit(Constant.CHATTING_LOAD_READ_QUEUE, new AckCustom(2500) {
                @Override
                public void call(Object... args) {
                    if (args != null) {
                        if (args[0].toString().equalsIgnoreCase("No Ack")) {
                            emitLoadReadQueue();
                        } else {
                            if ((Integer) args[0] == 1234) {
                                cancelTimer();
                                emitReadQueueLoaded();

                                loadReadQueue((JSONObject) args[1]);
                            }
                        }
                    }
                }
            });
        }
    }

    private void loadQueue(List<Dialog> dialogs) {
        for (final Dialog dialog : dialogs) {
            if (dialog.getId().equals(Session.with(ChattingService.this).getActiveDialog())) {
                for (Chat c : dialog.getChats()) {
                    Intent intent = new Intent();
                    intent.setAction(Constant.SOCKET_IO);
                    intent.putExtra(Constant.CHATTING_EVENT, Constant.CHATTING_MESSAGE);
                    intent.putExtra(Constant.REQ_SENDER_CODE, c.getSenderCode());
                    intent.putExtra(Constant.REQ_RECEIVER_CODE, c.getReceiverCode());
                    intent.putExtra(Constant.REQ_CONTENT, c.getContent());
                    intent.putExtra(Constant.REQ_CONTENT_TYPE, c.getContentType());
                    intent.putExtra(Constant.REQ_LESSON_ID, c.getLesson_id());

                    sendBroadcast(intent);
                }
            } else {
                Realm realm = Realm.getDefaultInstance();
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        for (Chat c : dialog.getChats()) {
                            c.setCreatedAt(Utils.with(ChattingService.this).getCurrentDate());
                            realm.copyToRealmOrUpdate(c);
                        }
                    }
                });

                final Dialog d = realm.where(Dialog.class).equalTo("id", dialog.getId()).findFirst();
                List<Chat> chatRealmList = new ArrayList<>();
                if (d != null) {
                    chatRealmList.addAll(realm.copyFromRealm(d.getChats().where().equalTo("senderCode", dialog.getUser().getCode()).findAll()
                            .where().equalTo("sent", 1).findAll()));
                    if (chatRealmList.size() == 0) {
                        updateUnreadBadge(d.getLesson().getId() - 1);
                    }
                    chatRealmList.addAll(dialog.getChats());

                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            d.getChats().addAll(dialog.getChats());
                            d.setUpdatedAt(Calendar.getInstance().getTime());
                        }
                    });
                } else {
                    chatRealmList.addAll(dialog.getChats());

                    final Lesson lesson = realm.where(Lesson.class).equalTo("id", dialog.getChats().get(0).getLesson_id()).findFirst();
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            realm.copyToRealmOrUpdate(Utils.with(ChattingService.this).createDialog(dialog.getId(), dialog.getUser(), dialog.getChats(), lesson));
                        }
                    });
                    updateUnreadBadge(lesson.getId() - 1);
                }
                realm.close();
                pushNotification(dialog.getUser().getCode(), dialog.getUser().getName(), dialog.getUser().getPhoto(), dialog.getUser().getType(), dialog.getUser().getPro(), dialog.getUser().getCode(), dialog.getId(), chatRealmList, dialog.getChats().get(0).getLesson_id() - 1);
            }
        }
    }

    private void loadReadQueue(JSONObject arg) {
        final List<Dialog> reads = new GsonBuilder().create().fromJson(arg.toString(), Data.class).getReads();
        for (final Dialog dialog : reads) {
            if (dialog.getId().equals(Session.with(ChattingService.this).getActiveDialog())) {
                Intent intent = new Intent();
                intent.setAction(Constant.SOCKET_IO);
                intent.putExtra(Constant.CHATTING_EVENT, Constant.CHATTING_IS_ACTIVE);

                sendBroadcast(intent);
            } else {
                Realm realm = Realm.getDefaultInstance();

                final Dialog d = realm.where(Dialog.class).equalTo("id", dialog.getId()).findFirst();
                if (d != null) {
                    final RealmList<Chat> chats = d.getChats();
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            for (int i = chats.size() - 1; i >= 0; i--) {
                                if (chats.get(i).getSenderCode() != null) {
                                    if (chats.get(i).getSenderCode().equals(Session.with(ChattingService.this).getUser().getCode())) {
                                        if (chats.get(i).getSent() == 1) {
                                            chats.get(i).setSent(2);
                                        } else if (chats.get(i).getSent() == 2) {
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    });
                }
                realm.close();
            }
        }
    }

    private void emitReadQueueLoaded() {
        if (connected) {
            socket.emit(Constant.CHATTING_READ_QUEUE_LOADED, new AckCustom(2500) {
                @Override
                public void call(Object... args) {
                    if (args != null) {
                        if (args[0].toString().equalsIgnoreCase("No Ack")) {
                            emitReadQueueLoaded();
                        } else {
                            if ((Integer) args[0] == 1234) {
                                cancelTimer();
                            }
                        }
                    }
                }
            });
        }
    }

    private void emitClearQueue() {
        socket.emit(Constant.CHATTING_READ_QUEUE_LOADED, new AckCustom(2500) {
            @Override
            public void call(Object... args) {
                if (args != null) {
                    if (args[0].toString().equalsIgnoreCase("No Ack")) {
                        emitClearQueue();
                    } else {
                        if ((Integer) args[0] == 1234) {
                            cancelTimer();

                            Intent intent = new Intent();
                            intent.setAction(Constant.CHATTING_QUEUE_CLEARED);

                            sendBroadcast(intent);
                        }
                    }
                }
            }
        });
    }

    private void pushNotification(final String senderCode, final String senderName, final String senderPhoto, final String senderType, final int senderPro, final String receiverCode, final String dialogId, final List<Chat> chats, final int lesson) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("receiver_code", senderCode);
        intent.putExtra("receiver_type", Constant.USER);
        intent.putExtra("id", dialogId);
        intent.putExtra("lesson", lesson);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        final int UNIQUE_REQ = Utils.with(this).getNumericDialogId(senderCode, receiverCode, lesson + 1);
        final PendingIntent pendingIntent = PendingIntent.getActivity(this, UNIQUE_REQ, intent, PendingIntent.FLAG_ONE_SHOT);

        final Notification.Builder notificationBuilder = new Notification.Builder(this);
        if (Build.VERSION.SDK_INT >= 21) {
            notificationBuilder.setColor(getResources().getColor(Utils.colorsPrimary[lesson]));
        }
        if (Build.VERSION.SDK_INT > 15) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Bitmap bitmap = null;
                    try {
                        if (!senderPhoto.isEmpty()) {
                            bitmap = Picasso.with(ChattingService.this).load(Utils.with(ChattingService.this)
                                    .getURLMediaImage(senderPhoto, senderType))
                                    .transform(new CropCircleTransformation())
                                    .get();
                        } else {
                            bitmap = Picasso.with(ChattingService.this).load(R.drawable.avatar)
                                    .transform(new CropCircleTransformation())
                                    .get();
                        }
                    } catch (IOException e) {
                    }

                    Notification.InboxStyle inboxStyle = new Notification.InboxStyle(new Notification.Builder(ChattingService.this));
                    int num = 0;
                    for (int i = chats.size() - 1; i >= 0; i--) {
                        if (chats.get(i).getContentType() == 0) {
                            inboxStyle.addLine(AES.decrypt(chats.get(i).getContent()));
                        } else if (chats.get(i).getContentType() == 1) {
                            inboxStyle.addLine("\uD83D\uDCF7 Mengirimkan Gambar");
                        } else if (chats.get(i).getContentType() == 2) {
                            inboxStyle.addLine("\uD83D\uDCC4 Mengirimkan Dokumen");
                        }

                        num++;
                        if (num == Constant.MAX_NOTIF && chats.size() > Constant.MAX_NOTIF) {
                            inboxStyle.addLine(Html.fromHtml("<b>+" + (chats.size() - Constant.MAX_NOTIF) + " pesan lainnya..." + "</b>"));
                            break;
                        }
                    }
                    inboxStyle.setSummaryText(getString(Utils.lessons[lesson]) + " (" + chats.size() + " Pesan)");

                    String content = "Mengirimkan Pesan";
                    if (chats.get(chats.size() - 1).getContentType() == 0) {
                        content = AES.decrypt(chats.get(chats.size() - 1).getContent());
                    } else if (chats.get(chats.size() - 1).getContentType() == 1) {
                        content = "\uD83D\uDCF7 Mengirimkan Gambar";
                    } else if (chats.get(chats.size() - 1).getContentType() == 2) {
                        content = "\uD83D\uDCC4 Mengirimkan Dokumen";
                    }
                    notificationBuilder.setSmallIcon(R.drawable.ic_notif)
                            .setContentTitle(senderPro == 1 ? "(PRO) " + senderName : senderName)
                            .setContentText(content)
                            .setStyle(inboxStyle)
                            .setLargeIcon(Bitmap.createScaledBitmap(bitmap, 120, 120, true))
                            .setAutoCancel(true)
                            .setShowWhen(true)
                            .setContentIntent(pendingIntent);

                    boolean enabled = Session.with(ChattingService.this).isNotificationEnabled();
                    if (enabled) {
                        notificationBuilder.setDefaults(Notification.DEFAULT_VIBRATE)
                                .setPriority(Notification.PRIORITY_HIGH)
                                .setLights(Utils.colorsHex[lesson], 500, 2000);
                        try {
                            notificationBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
                        } catch (Exception e) {
                            Utils.with(ChattingService.this).playDefSound();
                        }
                    }

                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        NotificationChannel notificationChannel = new NotificationChannel("app.newt.id.notification", "Message", NotificationManager.IMPORTANCE_HIGH);
                        notificationChannel.enableLights(true);
                        notificationChannel.setLightColor(Utils.colorsHex[lesson]);
                        notificationChannel.enableVibration(true);
                        notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400});

                        notificationBuilder.setChannelId("app.newt.id.notification");
                        notificationManager.createNotificationChannel(notificationChannel);
                    }
                    Notification notification = notificationBuilder.build();
                    notificationManager.notify(UNIQUE_REQ, notification);
                }
            }).start();
        }
    }

    private void updateUnreadBadge(int lesson) {
        Session.with(ChattingService.this).saveUnreadDialogCounter(lesson, Session.with(ChattingService.this).getUnreadDialogCounter(lesson) + 1);
        Session.with(ChattingService.this).saveTotalUnreadDialog(Session.with(ChattingService.this).getTotalUnreadDialog() + 1);
        Session.with(ChattingService.this).saveUnreadBadgeUpdated(false);

        ShortcutBadger.applyCount(this, Session.with(ChattingService.this).getTotalUnreadDialog());

        Intent intent = new Intent();
        intent.setAction(Constant.PREF_UNREAD_BADGE_UPDATED);
        intent.putExtra(Constant.REQ_LESSON_ID, lesson + 1);
        intent.putExtra(Constant.REQ_UNREAD_COUNTER, Session.with(ChattingService.this).getUnreadDialogCounter(lesson));

        sendBroadcast(intent);
    }

    private class Data {
        private List<Dialog> reads;

        public List<Dialog> getReads() {
            return reads;
        }
    }

    private void loadQueue() {
        chatPresenter.loadQueue();
    }

    private void markQueue() {
        chatPresenter.markQueue();
    }

    @Override
    public void onSuccess(final BaseResponse base) {
        if (loadingQueue) {
            loadingQueue = false;

            List<Dialog> dialogs = ((DialogsResponse) base).getData().getDialogs();
            if (dialogs.size() > 0) {
                markingQueue = true;
                markQueue();

                loadQueue(dialogs);
            }
        } else if (markingQueue) {
            markingQueue = false;
        }
    }

    @Override
    public void onFailure(String message) {
        if (Internet.isConnected(this)) {
            if (loadingQueue) {
                loadQueue();
            } else if (markingQueue) {
                markQueue();
            }
        }
    }

    @Override
    public void onDestroy() {
        socket.disconnect();
        socket.off(Socket.EVENT_CONNECT, onConnect);
        socket.off(Socket.EVENT_DISCONNECT, onDisconnect);
        socket.off(Socket.EVENT_RECONNECT, onReconnect);
        socket.off(Constant.CHATTING_MESSAGE, onMessage);
        socket.off(Constant.CHATTING_TYPING, onTyping);
        socket.off(Constant.CHATTING_STOP_TYPING, onStopTyping);
        socket.off(Constant.CHATTING_IS_ACTIVE, onActive);

        super.onDestroy();
    }
}