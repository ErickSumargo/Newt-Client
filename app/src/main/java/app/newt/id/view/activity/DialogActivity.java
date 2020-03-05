package app.newt.id.view.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.romainpiel.shimmer.Shimmer;
import com.romainpiel.shimmer.ShimmerTextView;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import app.newt.id.BuildConfig;
import app.newt.id.Newt;
import app.newt.id.R;
import app.newt.id.background.service.ChattingService;
import app.newt.id.helper.Constant;
import app.newt.id.helper.Internet;
import app.newt.id.helper.Session;
import app.newt.id.helper.Utils;
import app.newt.id.server.interfaces.Response;
import app.newt.id.server.model.Dialog;
import app.newt.id.server.model.Lesson;
import app.newt.id.server.model.Teacher;
import app.newt.id.server.model.User;
import app.newt.id.server.presenter.BasePresenter;
import app.newt.id.server.response.BaseResponse;
import app.newt.id.server.response.DialogsResponse;
import app.newt.id.server.response.UpdateResponse;
import app.newt.id.server.response.wrapper.BaseWrapper;
import app.newt.id.view.bus.BaseBus;
import app.newt.id.view.custom.ProgressDialogCustom;
import app.newt.id.view.custom.ViewPagerCustom;
import app.newt.id.view.fragment.ChemistryDialogFragment;
import app.newt.id.view.fragment.MathematicsDialogFragment;
import app.newt.id.view.fragment.PhysicsDialogFragment;
import app.newt.id.view.interfaces.UnreadBadgeSet;
import de.hdodenhof.circleimageview.CircleImageView;
import io.realm.Realm;
import me.leolin.shortcutbadger.ShortcutBadger;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class DialogActivity extends AppCompatActivity implements Response, UnreadBadgeSet {
    private CoordinatorLayout parentLayout;

    private AppBarLayout appBar;
    private Toolbar toolbar;
    private TabLayout tabLayout;

    private ShimmerTextView proVersion;
    private ImageView home;
    private TextView[] tabsCounter;

    private SectionsPagerAdapter sectionsPagerAdapter;
    private ViewPagerCustom viewPager;

    private ProgressDialogCustom progressDialog;

    private MathematicsDialogFragment mathDialog;
    private PhysicsDialogFragment physDialog;
    private ChemistryDialogFragment chemDialog;

    private BasePresenter basePresenter;

    public User user;
    private boolean loadingBase = false, updatingApp = false;

    public Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog);

        realm = ((Newt) getApplication()).realm;

        initView();
        updateApp();
        loadData();
        setEvent();
    }

    private void initView() {
        parentLayout = findViewById(R.id.parent_layout);

        appBar = findViewById(R.id.app_bar);
        setBaseColor(Session.with(this).getCurrentTheme());

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);

        sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        viewPager = findViewById(R.id.container);
        viewPager.setOffscreenPageLimit(sectionsPagerAdapter.getCount() - 1);
        viewPager.setAdapter(sectionsPagerAdapter);
        viewPager.setPagingEnabled(true);

        tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.getTabAt(Session.with(this).getCurrentTheme()).select();

        tabsCounter = new TextView[sectionsPagerAdapter.getCount()];
        for (int i = 0; i < sectionsPagerAdapter.getCount(); i++) {
            tabLayout.getTabAt(i).setCustomView(R.layout.custom_tab);

            View tabLesson = tabLayout.getTabAt(i).getCustomView();
            initTab(tabLesson, Utils.icLessons[i], i);
        }

        proVersion = findViewById(R.id.pro_version);
        home = findViewById(R.id.home);

        Shimmer shimmer = new Shimmer();
        shimmer.setDuration(2000);
        shimmer.start(proVersion);
        proVersion.setVisibility(View.GONE);

        mathDialog = new MathematicsDialogFragment();
        physDialog = new PhysicsDialogFragment();
        chemDialog = new ChemistryDialogFragment();

        basePresenter = new BasePresenter(getApplicationContext(), this);
    }

    private void loadData() {
        user = Session.with(this).getUser();
        if (!Session.with(this).isBaseDataLoaded()) {
            Intent intent = new Intent(this, ChattingService.class);
            intent.putExtra(Constant.CHATTING_EVENT, Constant.CHATTING_CLEAR_QUEUE);

            startService(intent);
        }

        if (Session.with(this).isUnreadBadgeSet()) {
            for (int i = 0; i < sectionsPagerAdapter.getCount(); i++) {
                int unread = Session.with(this).getUnreadDialogCounter(i);
                setUnreadDialogCounter(i, unread);
            }
            ShortcutBadger.applyCount(this, Session.with(this).getTotalUnreadDialog());
        }
    }

    private void setEvent() {
        proVersion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(DialogActivity.this);
                builder.setNegativeButton("Tutup", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                builder.setPositiveButton("Selengkapnya", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse("market://details?id=app.newt.pro.id"));
                        startActivity(intent);
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.setView(dialog.getLayoutInflater().inflate(R.layout.custom_promote_pro_version_dialog, null));
                dialog.show();
            }
        });

        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Session.with(getApplicationContext()).saveChatSelected(false);

                Intent intent = new Intent(DialogActivity.this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                setBaseColor(position);
                Session.with(DialogActivity.this).saveCurrentTheme(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void setBaseColor(int position) {
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

            window.setStatusBarColor(getResources().getColor(Utils.colorsPrimaryDark[position]));
        }
        appBar.setBackgroundResource(Utils.colorsPrimary[position]);
    }

    private void initTab(View tab, int icon, int pos) {
        TextView tabName = tab.findViewById(R.id.name);
        ImageView tabIcon = tab.findViewById(R.id.icon);

        tabName.setText(sectionsPagerAdapter.getPageTitle(pos));
        tabIcon.setImageResource(icon);

        TextView tabCounter = tab.findViewById(R.id.counter);
        tabsCounter[pos] = tabCounter;
    }

    @Override
    public void onBadgeSet(int lesson, int unread) {
        setUnreadDialogCounter(lesson, unread);

        Session.with(this).saveUnreadDialogCounter(lesson, unread);
        Session.with(this).saveTotalUnreadDialog(Session.with(this).getTotalUnreadDialog() + unread);

        if (lesson == sectionsPagerAdapter.getCount() - 1) {
            ShortcutBadger.applyCount(this, Session.with(this).getTotalUnreadDialog());
            Session.with(this).saveUnreadBadgeSet();
        }
    }

    public void setUnreadDialogCounter(int lesson, int unread) {
        if (unread > 0) {
            if (unread > 9) {
                tabsCounter[lesson].setText("9+");
            } else {
                tabsCounter[lesson].setText("(" + String.valueOf(unread) + ")");
            }
            tabsCounter[lesson].setVisibility(View.VISIBLE);
        } else {
            tabsCounter[lesson].setVisibility(View.GONE);
        }
    }

    private class SectionsPagerAdapter extends FragmentStatePagerAdapter {

        private SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return mathDialog;
                case 1:
                    return physDialog;
                case 2:
                    return chemDialog;
            }
            return null;
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return getString(Utils.lessons[position]).toUpperCase();
        }
    }

    private BroadcastReceiver onQueueCleared = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Constant.CHATTING_QUEUE_CLEARED)) {
                loadingBase = true;
                basePresenter.loadBase();
            }
        }
    };

    private BroadcastReceiver onUpdateUnreadBadge = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Constant.PREF_UNREAD_BADGE_UPDATED)) {
                int lessonId = intent.getIntExtra(Constant.REQ_LESSON_ID, 0);
                int unread = intent.getIntExtra(Constant.REQ_UNREAD_COUNTER, 0);
                setUnreadDialogCounter(lessonId - 1, unread);
            }
        }
    };

    @Override
    public void onSuccess(BaseResponse base) {
        if (loadingBase) {
            loadingBase = false;

            if (Session.with(this).getUserType().equals(Constant.STUDENT)) {
                BaseWrapper baseWrapper = (BaseWrapper) base;
                final List<Dialog> dialogs = baseWrapper.getDialogsResponse().getData().getDialogs();
                final List<Teacher> teachers = baseWrapper.getTeachersResponse().getData().getTeachers();
                final List<Lesson> lessons = baseWrapper.getDialogsResponse().getData().getLessons();

                realm.executeTransactionAsync(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        realm.copyToRealmOrUpdate(dialogs);
                        realm.copyToRealmOrUpdate(lessons);

                        for (Teacher teacher : teachers) {
                            Teacher t = realm.where(Teacher.class).equalTo("user.id", teacher.getUser().getId()).findFirst();
                            if (t != null) {
                                User u = realm.copyToRealmOrUpdate(teacher.getUser());
                                t.setUser(u);
                            } else {
                                realm.copyToRealmOrUpdate(teachers);
                            }
                        }
                    }
                }, new Realm.Transaction.OnSuccess() {
                    @Override
                    public void onSuccess() {
                        Session.with(DialogActivity.this).saveBaseDataLoaded();
                        EventBus.getDefault().post(new BaseBus());
                    }
                });
            } else {
                DialogsResponse r = (DialogsResponse) base;
                final List<Dialog> dialogs = r.getData().getDialogs();
                final List<Lesson> lessons = r.getData().getLessons();

                realm.executeTransactionAsync(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        realm.copyToRealmOrUpdate(dialogs);
                        realm.copyToRealmOrUpdate(lessons);
                    }
                }, new Realm.Transaction.OnSuccess() {
                    @Override
                    public void onSuccess() {
                        Session.with(DialogActivity.this).saveBaseDataLoaded();
                        EventBus.getDefault().post(new BaseBus());
                    }
                });
            }
        } else if (updatingApp) {
            updatingApp = false;

            UpdateResponse r = (UpdateResponse) base;
            final List<Lesson> lessons = r.getData().getLessons();

            realm.executeTransactionAsync(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    realm.copyToRealmOrUpdate(lessons);
                }
            }, new Realm.Transaction.OnSuccess() {
                @Override
                public void onSuccess() {
                    Session.with(DialogActivity.this).saveVersionCode(BuildConfig.VERSION_CODE);
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                    }
                }
            });
        }
    }

    @Override
    public void onFailure(String message) {
        if (loadingBase) {
            basePresenter.loadBase();
        } else if (updatingApp) {
            if (Internet.isConnected(this)) {
                basePresenter.updateApp();
            } else {
                if (progressDialog != null) {
                    progressDialog.dismiss();
                }
                showRetryUpdateAppDialog();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (onQueueCleared != null) {
            IntentFilter intentFilter = new IntentFilter(Constant.CHATTING_QUEUE_CLEARED);
            registerReceiver(onQueueCleared, intentFilter);
        }
        if (onUpdateUnreadBadge != null) {
            IntentFilter intentFilter = new IntentFilter(Constant.PREF_UNREAD_BADGE_UPDATED);
            registerReceiver(onUpdateUnreadBadge, intentFilter);
        }

        if (!Session.with(this).isUnreadBadgeUpdated()) {
            for (int i = 0; i < sectionsPagerAdapter.getCount(); i++) {
                setUnreadDialogCounter(i, Session.with(this).getUnreadDialogCounter(i));
            }
            ShortcutBadger.applyCount(this, Session.with(this).getTotalUnreadDialog());
            Session.with(this).saveUnreadBadgeUpdated(true);
        }

        if (!Utils.with(this).isServiceRunning(ChattingService.class)) {
            Session.with(this).startServices();
        }
        showRateAppDialog();
    }

    @Override
    protected void onPause() {
        if (onQueueCleared != null) {
            unregisterReceiver(onQueueCleared);
        }
        if (onUpdateUnreadBadge != null) {
            unregisterReceiver(onUpdateUnreadBadge);
        }
        super.onPause();
    }

    private void showRateAppDialog() {
        if (!Session.with(this).isAppRaterFirstLaunch()) {
            Session.with(this).saveAppRaterFirstLaunch();
        } else {
            if (System.currentTimeMillis() >= Session.with(this).getAppRaterDateLaunch() +
                    (Constant.PREF_APP_RATER_NEXT_PROMPT * 24 * 60 * 60 * 1000)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Rate Newt")
                        .setMessage("Bila menyukai aplikasi ini, mohon waktu anda sebentar untuk memberikan rating")
                        .setCancelable(false)
                        .setNegativeButton("Tutup", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Session.with(DialogActivity.this).saveAppRaterFirstLaunch();
                            }
                        })
                        .setPositiveButton("Rate", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setData(Uri.parse("market://details?id=app.newt.id"));
                                startActivity(intent);
                            }
                        })
                        .show();
            }
        }
    }

    private void showRetryUpdateAppDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pembaruan Versi")
                .setMessage(getString(R.string.no_internet))
                .setPositiveButton("Coba Lagi", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        updateApp();
                    }
                })
                .setCancelable(false)
                .show();
    }

    private void updateApp() {
        if (Session.with(this).getVersionCode() == 0) {
            Session.with(this).saveVersionCode(BuildConfig.VERSION_CODE);
        } else if (BuildConfig.VERSION_CODE > Session.with(this).getVersionCode()) {
            if (Internet.isConnected(this)) {
                progressDialog = new ProgressDialogCustom(this);
                progressDialog.setMessage("Memperbarui versi " + BuildConfig.VERSION_NAME + "...");

                updatingApp = true;
                basePresenter.updateApp();
            } else {
                showRetryUpdateAppDialog();
            }
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}