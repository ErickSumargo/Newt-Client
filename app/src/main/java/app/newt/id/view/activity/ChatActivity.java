package app.newt.id.view.activity;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import app.newt.id.R;
import app.newt.id.background.receiver.NetworkReceiver;
import app.newt.id.background.service.ChattingService;
import app.newt.id.background.service.StoreChatService;
import app.newt.id.helper.AES;
import app.newt.id.helper.Constant;
import app.newt.id.helper.Internet;
import app.newt.id.helper.Session;
import app.newt.id.helper.Utils;
import app.newt.id.server.interfaces.Response;
import app.newt.id.server.model.Chat;
import app.newt.id.server.model.Dialog;
import app.newt.id.server.model.Lesson;
import app.newt.id.server.model.Teacher;
import app.newt.id.server.model.User;
import app.newt.id.server.presenter.UserPresenter;
import app.newt.id.server.response.BaseResponse;
import app.newt.id.server.response.RatingResponse;
import app.newt.id.view.adapter.ChatListAdapter;
import app.newt.id.view.interfaces.LoadMore;
import app.newt.id.view.interfaces.MessageStatusChanged;
import app.newt.id.view.interfaces.NoInternet;
import cn.pedant.SweetAlert.SweetAlertDialog;
import de.hdodenhof.circleimageview.CircleImageView;
import gun0912.tedbottompicker.TedBottomPicker;
import id.zelory.compressor.Compressor;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import me.leolin.shortcutbadger.ShortcutBadger;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ChatActivity extends AppCompatActivity implements Response, LoadMore, MessageStatusChanged, NoInternet {
    private Toolbar toolbar;

    private CircleImageView profile;
    private TextView name, active;
    private SpinKitView typing;

    private ImageView rating;
    private ProgressBar progressBar;
    private AlertDialog.Builder ratingBuilder;

    private AlertDialog ratingDialog;
    private ProgressBar progressBarRating;
    private LinearLayout ratingContainer;
    private ImageView[] stars;

    private RecyclerView chatListView;
    private ChatListAdapter chatListAdpt;

    private View chatInputLayout;
    private EditText input;
    private ImageView pickImage, fullScreen;
    private ImageView send;

    private GradientDrawable gd;
    private Handler mTypingHandler = new Handler();
    private Handler mIsActiveHandler = new Handler();
    private Timer mTimer;

    private User sender, receiver;
    private Dialog dialog;
    private String dialogId, senderCode, receiverCode;
    private RealmList<Chat> chats;

    private int lesson, lastIdx;

    private int ratingVal = -1;
    private boolean isNewDialog = false, isTyping = false, isActive = false;
    public boolean edited = false;

    private final long INTERVAL = 4000;

    private UserPresenter userPresenter;
    private NetworkReceiver networkReceiver;

    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Session.with(this).getEnvChat() == 0) {
            Bundle extras = getIntent().getExtras();
            lesson = extras.getInt("lesson");
        } else {
            lesson = Session.with(this).getCurrentTheme();
        }
        setTheme(Utils.styles[lesson]);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        realm = Realm.getDefaultInstance();

//        showBannerAds();
        initView();
        loadData();
        setEvent();
    }

    private void showBannerAds() {
        final RelativeLayout adsContainer = findViewById(R.id.ads_container);
        AdView adsView = findViewById(R.id.ads_view);

        adsView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                adsContainer.setVisibility(View.VISIBLE);
            }
        });
        adsView.loadAd(new AdRequest.Builder().build());
    }

    private void initView() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(null);

        profile = findViewById(R.id.profile);
        name = findViewById(R.id.name);
        active = findViewById(R.id.active);
        typing = findViewById(R.id.typing);

        rating = findViewById(R.id.rating);
        progressBar = findViewById(R.id.progress_bar);

        chatListView = findViewById(R.id.chat_list_view);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        chatListView.setLayoutManager(layoutManager);

        chatInputLayout = findViewById(R.id.chat_input_layout);
        input = chatInputLayout.findViewById(R.id.input);
        pickImage = chatInputLayout.findViewById(R.id.pick_image);
        fullScreen = chatInputLayout.findViewById(R.id.full_screen);
        send = chatInputLayout.findViewById(R.id.send);

        if (Build.VERSION.SDK_INT < 21) {
            Drawable pbBg = progressBar.getIndeterminateDrawable();
            pbBg = pbBg.mutate();
            pbBg.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_IN);

            gd = (GradientDrawable) send.getBackground().getCurrent();
        }
        pickImage.setColorFilter(getResources().getColor(R.color.holo_light));
        fullScreen.setColorFilter(getResources().getColor(R.color.holo_light));

        input.requestFocus();
        disableSendButton();

        sender = Session.with(this).getUser();
        if (sender.getType().equals(Constant.STUDENT)) {
            rating.setVisibility(View.VISIBLE);

            if (!Utils.with(ChatActivity.this).isExpired(sender.getSubscription())) {
                long left = Math.abs(Utils.with(this).getDifferentDays(sender.getSubscription(), 1));
                if (left <= 3 && left != 0) {
                    if (System.currentTimeMillis() >= Session.with(this).getAppSubscriptionDateLaunch() + (24 * 60 * 60 * 1000)) {
                        showSubscriptionDialog((int) left);
                    }
                }
            }
        } else {
            rating.setVisibility(View.GONE);
        }

        StoreChatService storeChatService = new StoreChatService();
        storeChatService.setOnStatusChanged(this);

        userPresenter = new UserPresenter(getApplicationContext(), this);
    }

    private void loadData() {
        String receiverType;
        if (Session.with(this).getEnvChat() == 0) {
            Bundle extras = getIntent().getExtras();

            dialogId = extras.getString("id");
            receiverCode = extras.getString("receiver_code");
            receiverType = extras.getString("receiver_type");
            lesson = extras.getInt("lesson");
        } else {
            dialogId = Session.with(this).getCurrentDialog();
            receiverCode = Session.with(this).getCurrentReceiverCode();
            receiverType = Session.with(this).getCurrentReceiverType();
            lesson = Session.with(this).getCurrentTheme();

            Session.with(this).saveEnvChat(0);
        }
        this.senderCode = sender.getCode();

        // from DialogListAdapter
        if (receiverType.equals(Constant.USER)) {
            receiver = realm.where(User.class).equalTo("code", receiverCode).findFirst();
        }
        // from TeacherListAdapter
        else if (receiverType.equals(Constant.TEACHER)) {
            Teacher teacher = realm.where(Teacher.class).equalTo("user.code", receiverCode).findFirst();
            receiver = teacher.getUser();
        }

        Picasso.with(this).load(Utils.with(this).getURLMediaImage(receiver.getPhoto(), Utils.with(this).getUserType(receiver.getCode())))
                .placeholder(R.drawable.avatar)
                .fit()
                .centerCrop()
                .into(profile);
        name.setText(receiver.getName());

        Session.with(this).saveActiveDialog(dialogId);
        emitEvent(Constant.CHATTING_JOIN_DIALOG, Utils.with(this).createJSON(Constant.CHATTING_JOIN_DIALOG, dialogId, receiverCode));

        dialog = realm.where(Dialog.class).equalTo("id", dialogId).findFirst();
        if (dialog != null) {
            setReadStatus();

            chats = dialog.getChats();
            if (chats.size() >= Constant.MAX_HISTORIES) {
                lastIdx = chats.size() - Constant.MAX_HISTORIES;
            } else {
                lastIdx = 0;
            }
            chats = setDateHeader(realm.copyFromRealm(chats.subList(lastIdx, chats.size())));
        } else {
            final Lesson l = realm.where(Lesson.class).equalTo("id", lesson + 1).findFirst();
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    realm.copyToRealmOrUpdate(Utils.with(ChatActivity.this).createDialog(dialogId, receiver, new RealmList<Chat>(), l));
                }
            });
            dialog = realm.where(Dialog.class).equalTo("id", dialogId).findFirst();
            chats = setDateHeader(realm.copyFromRealm(dialog.getChats().subList(0, dialog.getChats().size())));

            isNewDialog = true;
        }

        mTimer = new Timer();
        mTimer.scheduleAtFixedRate(new CustomTask(), 0, INTERVAL);

        chatListAdpt = new ChatListAdapter(chatListView, sender, chats, lesson);
        chatListView.setAdapter(chatListAdpt);
        chatListAdpt.setOnLoadMoreListener(this);
    }

    private void setEvent() {
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        rating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ratingDialog == null) {
                    buildRatingDialog();
                } else {
                    ratingDialog.show();
                }
            }
        });

        pickImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Internet.isConnected(getApplicationContext())) {
                    if (sender.getType().equals(Constant.STUDENT)) {
                        if (!Utils.with(ChatActivity.this).isExpired(Session.with(getApplicationContext()).getUser().getSubscription())) {
                            TedBottomPicker picker = new TedBottomPicker.Builder(ChatActivity.this)
                                    .setOnImageSelectedListener(new TedBottomPicker.OnImageSelectedListener() {
                                        @Override
                                        public void onImageSelected(final Uri uri) {
                                            AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
                                            builder.setTitle("Konfirmasi")
                                                    .setMessage("Kirim gambar ini?")
                                                    .setNegativeButton("Tutup", new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int which) {

                                                        }
                                                    })
                                                    .setPositiveButton("Kirim", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            setImage(uri);
                                                        }
                                                    })
                                                    .show();
                                        }
                                    })
                                    .setTitle("Pilih Gambar")
                                    .setPeekHeightResId(R.dimen.ted_picker_height)
                                    .create();
                            picker.show(getSupportFragmentManager());
                        } else {
                            showSubscriptionDialog(0);
                        }
                    } else {
                        TedBottomPicker picker = new TedBottomPicker.Builder(ChatActivity.this)
                                .setOnImageSelectedListener(new TedBottomPicker.OnImageSelectedListener() {
                                    @Override
                                    public void onImageSelected(final Uri uri) {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
                                        builder.setTitle("Konfirmasi")
                                                .setMessage("Kirim gambar ini?")
                                                .setNegativeButton("Tutup", new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int which) {

                                                    }
                                                })
                                                .setPositiveButton("Kirim", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        setImage(uri);
                                                    }
                                                })
                                                .show();
                                    }
                                })
                                .setTitle("Pilih Gambar")
                                .setPeekHeightResId(R.dimen.ted_picker_height)
                                .create();
                        picker.show(getSupportFragmentManager());
                    }
                } else {
                    Toast.makeText(getApplicationContext(), R.string.no_internet, Toast.LENGTH_SHORT).show();
                }
            }
        });

        fullScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChatActivity.this, FullScreenInputActivity.class);
                intent.putExtra("content", input.getText().toString());

                startActivityForResult(intent, 0);
            }
        });

        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() > 0) {
                    enableSendButton();
                } else {
                    disableSendButton();
                }

                if (!isTyping) {
                    isTyping = true;
                    emitEvent(Constant.CHATTING_TYPING, null);
                }
                mTypingHandler.removeCallbacks(onTypingTimeout);
                mTypingHandler.postDelayed(onTypingTimeout, 2000);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Internet.isConnected(getApplicationContext())) {
                    if (sender.getType().equals(Constant.STUDENT)) {
                        if (!Utils.with(ChatActivity.this).isExpired(Session.with(getApplicationContext()).getUser().getSubscription())) {
                            addMessage(input.getText().toString(), 0);
                            input.getText().clear();
                        } else {
                            showSubscriptionDialog(0);
                        }
                    } else {
                        addMessage(input.getText().toString(), 0);
                        input.getText().clear();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), R.string.no_internet, Toast.LENGTH_SHORT).show();
                }
            }
        });

        send.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (Build.VERSION.SDK_INT < 21) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            gd.setColor(getResources().getColor(Utils.colorsPrimaryDark[lesson]));
                            break;
                        case MotionEvent.ACTION_UP:
                            gd.setColor(getResources().getColor(Utils.colorsPrimary[lesson]));
                            break;
                    }
                }
                return false;
            }
        });
    }

    private void setImage(Uri uri) {
        Utils utils = Utils.with(ChatActivity.this);
        String fileName = "local-" + senderCode + "-" + System.currentTimeMillis() + ".jpg";

        File file = null;
        try {
            file = new Compressor(ChatActivity.this).compressToFile(new File(uri.getPath()));
        } catch (IOException e) {
        }

        if ((double) file.length() / 1000000 <= Constant.MAX_UPLOAD_FILE_SIZE) {
            uri = Uri.fromFile(file);
            Bitmap b = null;
            try {
                b = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            } catch (Exception e) {
            }
            utils.saveImage(b, fileName, Constant.DIR_PICTURES_INTERNAL, false);

            uri = Uri.fromFile(utils.getFile(fileName, Constant.DIR_PICTURES_INTERNAL, false));
            b = utils.adjustBitmap(utils.maintainBitmap(uri), uri);
            utils.saveImage(b, fileName, Constant.DIR_PICTURES_INTERNAL, false);

            addMessage(fileName, 1);
        } else {
            Toast.makeText(getApplicationContext(), R.string.file_size_overflow, Toast.LENGTH_SHORT).show();
        }
    }

    private void addMessage(final String content, final int contentType) {
        final Chat chat = Utils.with(this).createChat(sender.getCode(), receiver.getCode(), AES.encrypt(content), contentType,
                lesson + 1, 0, Utils.with(ChatActivity.this).getCurrentDate());
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealmOrUpdate(chat);

                dialog.getChats().add(chat);
                chats.add(chat);
            }
        });

        chatListAdpt.notifyItemRangeChanged(chats.size() - 1, 1);
        chatListView.post(new Runnable() {
            @Override
            public void run() {
                chatListView.smoothScrollToPosition(chats.size());
            }
        });
        edited = true;
    }

    private void setReadStatus() {
        final RealmResults<Chat> chatList = dialog.getChats().where()
                .equalTo("senderCode", receiver.getCode())
                .lessThan("sent", 2).findAll();
        if (chatList.size() > 0) {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    for (Chat c : chatList) {
                        c.setSent(2);
                    }
                }
            });

            Session.with(this).saveUnreadDialogCounter(lesson, Session.with(this).getUnreadDialogCounter(lesson) - 1);
            Session.with(this).saveTotalUnreadDialog(Session.with(this).getTotalUnreadDialog() - 1);
            Session.with(this).saveUnreadBadgeUpdated(false);

            ShortcutBadger.applyCount(this, Session.with(this).getTotalUnreadDialog());
        }
    }

    private RealmList<Chat> setDateHeader(List<Chat> chatList) {
        RealmList<Chat> formatted = new RealmList<>();
        if (chatList.size() == 1) {
            Chat date = new Chat();
            date.setCreatedAt(chatList.get(0).getCreatedAt());
            chatList.add(0, date);

            formatted.addAll(chatList);
        } else if (chatList.size() > 1) {
            String cursor = chatList.get(chatList.size() - 1).getCreatedAt();
            for (int i = chatList.size() - 2; i >= 0; i--) {
                if (!Utils.with(this).isSameDate(cursor, chatList.get(i).getCreatedAt())) {
                    Chat date = new Chat();
                    date.setCreatedAt(cursor);

                    cursor = chatList.get(i).getCreatedAt();
                    chatList.add(i + 1, date);
                }
                if (i == 0) {
                    Chat date = new Chat();
                    date.setCreatedAt(chatList.get(0).getCreatedAt());
                    chatList.add(i, date);
                }
            }
            formatted.addAll(chatList);
        }
        return formatted;
    }

    private void buildRatingDialog() {
        ratingBuilder = new AlertDialog.Builder(this);
        ratingDialog = ratingBuilder.create();
        ratingDialog.setView(ratingDialog.getLayoutInflater().inflate(R.layout.custom_rating_dialog, null));
        ratingDialog.show();

        LinearLayout headerBg = ratingDialog.findViewById(R.id.header_bg);
        progressBarRating = ratingDialog.findViewById(R.id.progress_bar);

        ratingContainer = ratingDialog.findViewById(R.id.rating_container);
        CircleImageView photo = ratingDialog.findViewById(R.id.photo);
        TextView name = ratingDialog.findViewById(R.id.name);

        ImageView star1 = ratingDialog.findViewById(R.id.star1);
        ImageView star2 = ratingDialog.findViewById(R.id.star2);
        ImageView star3 = ratingDialog.findViewById(R.id.star3);
        ImageView star4 = ratingDialog.findViewById(R.id.star4);
        ImageView star5 = ratingDialog.findViewById(R.id.star5);

        stars = new ImageView[]{star1, star2, star3, star4, star5};
        for (int i = 0; i < stars.length; i++) {
            stars[i].setColorFilter(getResources().getColor(Utils.colorsPrimary[lesson]));
            stars[i].setOnClickListener(ratingClickListener);
        }

        headerBg.setBackgroundColor(getResources().getColor(Utils.colorsPrimary[lesson]));

        Picasso.with(this).load(Utils.with(this).getURLMediaImage(receiver.getPhoto(), Utils.with(this).getUserType(receiver.getCode())))
                .placeholder(R.drawable.avatar)
                .fit()
                .centerCrop()
                .into(photo);

        name.setText(receiver.getName());
        if (ratingVal == -1) {
            userPresenter.getRating(receiver.getId(), lesson + 1);
        } else {
            fillStars(ratingVal);
        }
    }

    private void showSubscriptionDialog(final int left) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setNegativeButton("Tutup", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Session.with(ChatActivity.this).saveAppSubscriptionLaunch();
            }
        });
        builder.setPositiveButton("Perpanjang", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Session.with(ChatActivity.this).saveAppSubscriptionLaunch();

                Intent intent = new Intent(ChatActivity.this, PaymentActivity.class);
                startActivity(intent);
            }
        });
        AlertDialog dialog = builder.create();
        dialog.setView(dialog.getLayoutInflater().inflate(R.layout.custom_subscription_dialog, null));
        dialog.show();

        LinearLayout headerBg = dialog.findViewById(R.id.header_bg);
        TextView expression = dialog.findViewById(R.id.expression);
        TextView message = dialog.findViewById(R.id.message);

        headerBg.setBackgroundColor(getResources().getColor(Utils.colorsPrimary[lesson]));
        if (left == 0) {
            expression.setText(":(");
            message.setText("Maaf, masa langganan anda telah habis. Anda dapat melanjutkan penggunaan layanan kami kembali melalui perpanjangan masa.");
        } else {
            expression.setText(":)");
            message.setText("Masa langganan tinggal " + left + " hari. Anda dapat menambah masa langganan melalui tombol \"Perpanjang\" di bawah ini.");
        }
    }

    private View.OnClickListener ratingClickListener = new View.OnClickListener() {
        private int num;

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.star1:
                    num = 1;
                    fillStars(num);
                    break;
                case R.id.star2:
                    num = 2;
                    fillStars(num);
                    break;
                case R.id.star3:
                    num = 3;
                    fillStars(num);
                    break;
                case R.id.star4:
                    num = 4;
                    fillStars(num);
                    break;
                case R.id.star5:
                    num = 5;
                    fillStars(num);
                    break;
                default:
                    break;
            }

            if (Internet.isConnected(getApplicationContext())) {
                userPresenter.setRating(receiver.getId(), num, lesson + 1);
            } else {
                notifyNoInternet();
            }
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    ratingDialog.dismiss();
                }
            }, 250);
        }
    };

    private void fillStars(int num) {
        for (int i = 0; i < 5; i++) {
            if (i < num) {
                stars[i].setImageResource(R.drawable.ic_star);
            } else {
                stars[i].setImageResource(R.drawable.ic_star_border);
            }
            stars[i].setColorFilter(getResources().getColor(Utils.colorsPrimary[lesson]));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == RESULT_OK) {
            input.setText(data.getStringExtra("content"));
            input.setSelection(input.getText().length());
        }
    }

    private void enableSendButton() {
        if (Build.VERSION.SDK_INT >= 21) {
            send.setBackground(getDrawable(R.drawable.button_circle));
        } else {
            gd.setColor(getResources().getColor(Utils.colorsPrimary[lesson]));
        }
        send.setColorFilter(getResources().getColor(R.color.white));
        send.setEnabled(true);
    }

    private void disableSendButton() {
        if (Build.VERSION.SDK_INT >= 21) {
            send.setBackground(ContextCompat.getDrawable(this, R.drawable.button_circle_disabled));
        } else {
            gd.setColor(getResources().getColor(R.color.colorSelectableItemBackground));
        }
        send.setColorFilter(getResources().getColor(R.color.grayDark));
        send.setEnabled(false);
    }

    private void notifyNoInternet() {
        Toast.makeText(getApplicationContext(), R.string.no_internet, Toast.LENGTH_SHORT).show();
    }

    private Runnable onTypingTimeout = new Runnable() {
        @Override
        public void run() {
            if (!isTyping) return;

            isTyping = false;
            emitEvent(Constant.CHATTING_STOP_TYPING, null);
        }
    };

    private void emitEvent(String event, JSONObject object) {
        Intent service = new Intent(this, ChattingService.class);
        service.putExtra(Constant.CHATTING_EVENT, event);
        if (object != null) {
            service.putExtra("params", object.toString());
        }
        startService(service);
    }

    private BroadcastReceiver onChattingEvent = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Constant.SOCKET_IO)) {
                switch (intent.getStringExtra(Constant.CHATTING_EVENT)) {
                    case Constant.CHATTING_JOIN_DIALOG:
                        emitEvent(Constant.CHATTING_JOIN_DIALOG, Utils.with(ChatActivity.this).createJSON(Constant.CHATTING_JOIN_DIALOG, dialogId, receiverCode));

                        break;
                    case Constant.CHATTING_MESSAGE:
                        String senderCode = intent.getStringExtra(Constant.REQ_SENDER_CODE);
                        String receiverCode = intent.getStringExtra(Constant.REQ_RECEIVER_CODE);
                        String content = intent.getStringExtra(Constant.REQ_CONTENT);
                        int contentType = intent.getIntExtra(Constant.REQ_CONTENT_TYPE, -1);
                        int lessonId = intent.getIntExtra(Constant.REQ_LESSON_ID, -1);

                        final Chat chat = Utils.with(ChatActivity.this).createChat(senderCode, receiverCode, content, contentType,
                                lessonId, 2, Utils.with(ChatActivity.this).getCurrentDate());

                        int i = 1;
                        if (chats.size() > 0) {
                            if (!Utils.with(ChatActivity.this).isSameDate(chat.getCreatedAt(), chats.get(chats.size() - 1).getCreatedAt())) {
                                Chat c = new Chat();
                                c.setCreatedAt(chat.getCreatedAt());
                                chats.add(c);

                                i++;
                            }
                        } else {
                            Chat c = new Chat();
                            c.setCreatedAt(chat.getCreatedAt());
                            chats.add(c);

                            i++;
                        }
                        chats.add(chat);

                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                realm.copyToRealmOrUpdate(chat);

                                dialog.getChats().add(chat);
                                dialog.setUpdatedAt(Calendar.getInstance().getTime());
                            }
                        });

                        chatListAdpt.notifyItemRangeInserted(chats.size() - i, i);
                        chatListView.post(new Runnable() {
                            @Override
                            public void run() {
                                chatListView.smoothScrollToPosition(chats.size());
                            }
                        });

                        if (Session.with(ChatActivity.this).isReplySoundEnabled()) {
                            Utils.with(ChatActivity.this).playReplySound();
                        }
                        edited = true;

                        break;
                    case Constant.CHATTING_TYPING:
                        active.setVisibility(View.GONE);
                        typing.setVisibility(View.VISIBLE);

                        break;
                    case Constant.CHATTING_STOP_TYPING:
                        if (isActive) {
                            active.setVisibility(View.VISIBLE);
                        }
                        typing.setVisibility(View.GONE);

                        break;
                    case Constant.CHATTING_IS_ACTIVE:
                        isActive = intent.getBooleanExtra("active", false);
                        if (isActive) {
                            if (typing.getVisibility() != View.VISIBLE) {
                                active.setVisibility(View.VISIBLE);
                            }
                            realm.executeTransaction(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    for (int i = chats.size() - 1; i >= 0; i--) {
                                        if (chats.get(i).getSenderCode() != null) {
                                            if (chats.get(i).getSenderCode().equals(sender.getCode())) {
                                                if (chats.get(i).getSent() == 1) {
                                                    chats.get(i).setSent(2);

                                                    chatListAdpt.notifyItemChanged(i);
                                                } else if (chats.get(i).getSent() == 2) {
                                                    break;
                                                }
                                            }
                                        }
                                    }

                                    for (int i = dialog.getChats().size() - 1; i >= 0; i--) {
                                        if (dialog.getChats().get(i).getSenderCode() != null) {
                                            if (dialog.getChats().get(i).getSenderCode().equals(sender.getCode())) {
                                                if (dialog.getChats().get(i).getSent() == 1) {
                                                    dialog.getChats().get(i).setSent(2);
                                                } else if (dialog.getChats().get(i).getSent() == 2) {
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }
                            });
                        } else {
                            typing.setVisibility(View.GONE);
                            active.setVisibility(View.GONE);
                        }
                        break;
                }
            }
        }
    };

    private class CustomTask extends TimerTask {

        @Override
        public void run() {
            mIsActiveHandler.post(new Runnable() {

                @Override
                public void run() {
                    emitEvent(Constant.CHATTING_IS_ACTIVE, Utils.with(ChatActivity.this).createJSON(Constant.CHATTING_IS_ACTIVE, dialogId, receiverCode));
                }
            });
        }
    }

    @Override
    public void onLoadMore() {
        progressBar.setVisibility(View.VISIBLE);
        if (lastIdx == 0) {
            chatListAdpt.isAllLoaded = true;
        } else {
            List<Chat> histories;
            if (lastIdx - Constant.MAX_HISTORIES > 0) {
                histories = realm.copyFromRealm(dialog.getChats().subList(lastIdx - Constant.MAX_HISTORIES, lastIdx));
                lastIdx -= Constant.MAX_HISTORIES;
            } else {
                histories = realm.copyFromRealm(dialog.getChats().subList(0, lastIdx));
                lastIdx = 0;

                chatListAdpt.isAllLoaded = true;
            }
            histories = setDateHeader(histories);
            if (Utils.with(this).isSameDate(chats.get(0).getCreatedAt(), histories.get(histories.size() - 1).getCreatedAt())) {
                chats.remove(0);
            }
            chats.addAll(0, histories);
            chatListAdpt.isLoading = false;
            chatListAdpt.notifyItemRangeInserted(0, histories.size() - 1);

            progressBar.setVisibility(View.GONE);
        }
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onMessageStatusChanged(final Chat chat) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                for (int i = chats.size() - 1; i >= 0; i--) {
                    if (chats.get(i).getUuid().equals(chat.getUuid())) {
                        chats.get(i).setSent(1);
                        chatListAdpt.notifyItemChanged(i);

                        break;
                    }
                }
            }
        });
    }

    @Override
    public void onInternetStatus(boolean connected) {
        if (!connected) {
            active.setVisibility(View.GONE);
            typing.setVisibility(View.GONE);

            isActive = isTyping = false;
        }
    }

    @Override
    public void onSuccess(BaseResponse base) {
        if (base instanceof RatingResponse) {
            if (base.getTag().equals(Constant.REQ_GET_RATING)) {
                ratingVal = ((RatingResponse) base).getData().getRating();

                progressBarRating.setVisibility(View.GONE);
                ratingContainer.setVisibility(View.VISIBLE);

                fillStars(ratingVal);
            } else if (base.getTag().equals(Constant.REQ_SET_RATING)) {
                if (!isDestroyed()) {
                    new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                            .setTitleText("Terima kasih")
                            .setContentText(ratingVal == -1 ? "Rating tersimpan" : "Rating terbarui")
                            .setConfirmText("Tutup").show();
                }
            }
        }
    }

    @Override
    public void onFailure(String message) {
        if (message.contains(Constant.REQ_GET_RATING)) {
            userPresenter.getRating(receiver.getId(), lesson + 1);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (onChattingEvent != null) {
            IntentFilter intentFilter = new IntentFilter(Constant.SOCKET_IO);
            registerReceiver(onChattingEvent, intentFilter);
        }

        networkReceiver = new NetworkReceiver();
        networkReceiver.setOnNoInternetListener(this);
        registerReceiver(networkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        if (Session.with(this).isChattingPaused()) {
            emitEvent(Constant.CHATTING_JOIN_DIALOG, Utils.with(this).createJSON(Constant.CHATTING_JOIN_DIALOG, dialogId, receiverCode));

            RealmResults<Chat> missed = dialog.getChats()
                    .where().equalTo("senderCode", receiver.getCode()).findAll()
                    .where().equalTo("sent", 1).findAll();
            if (missed.size() > 0) {
                chats.addAll(missed);

                chatListAdpt.notifyItemRangeInserted(chats.size() - missed.size(), missed.size());
                chatListView.post(new Runnable() {
                    @Override
                    public void run() {
                        chatListView.smoothScrollToPosition(chats.size());
                    }
                });
            }
            Session.with(this).saveChattingPaused(false);
        }

        Session.with(this).saveActiveDialog(dialogId);
        if (dialog != null) {
            setReadStatus();
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(Utils.with(this).getNumericDialogId(senderCode, receiverCode, lesson + 1));
    }

    @Override
    protected void onPause() {
        unregisterReceiver(networkReceiver);
        if (onChattingEvent != null) {
            unregisterReceiver(onChattingEvent);
        }
        emitEvent(Constant.CHATTING_LEAVE_DIALOG, Utils.with(this).createJSON(Constant.CHATTING_LEAVE_DIALOG, dialogId));

        Session.with(this).saveActiveDialog("");
        Session.with(this).saveChattingPaused(true);

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (isNewDialog) {
            if (chats.size() == 0) {
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        dialog.deleteFromRealm();
                    }
                });
            }
        } else {
            if (chats.size() > 0) {
                if (edited) {
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            dialog.setUpdatedAt(Calendar.getInstance().getTime());
                        }
                    });
                }
            }
        }
        realm.close();

        Session.with(this).saveActiveDialog("");
        Session.with(this).saveChattingPaused(false);

        mTimer.cancel();
        super.onDestroy();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}