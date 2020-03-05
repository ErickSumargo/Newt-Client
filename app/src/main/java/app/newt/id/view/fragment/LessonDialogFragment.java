package app.newt.id.view.fragment;

import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import app.newt.id.Newt;
import app.newt.id.R;
import app.newt.id.helper.Constant;
import app.newt.id.helper.Session;
import app.newt.id.helper.Utils;
import app.newt.id.server.model.Chat;
import app.newt.id.server.model.Dialog;
import app.newt.id.server.model.Teacher;
import app.newt.id.server.model.User;
import app.newt.id.view.activity.DialogActivity;
import app.newt.id.view.adapter.DialogListAdapter;
import app.newt.id.view.adapter.TeacherListAdapter;
import app.newt.id.view.bus.BaseBus;
import app.newt.id.view.interfaces.UnreadBadgeSet;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmList;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by Erick Sumargo on 2/20/2018.
 */

public class LessonDialogFragment extends Fragment {
    private View view;
    private InterstitialAd mInterstitialAd;

    private LinearLayout mainLayout, teacherContainer;
    private ProgressBar loadingView;
    private View noActivityView;

    private RelativeLayout dialogListToggleLayout, teacherListToggleLayout;
    private TextView dialogListLbl, teacherListLbl;
    private ImageView dialogListIndicator, teacherListIndicator;

    private RecyclerView dialogListView, teacherListView;

    private DialogListAdapter dialogListAdpt;
    private TeacherListAdapter teacherListAdpt;

    private RealmResults<Dialog> dialogs;
    private RealmResults<Teacher> teachers;

    private static UnreadBadgeSet mUnreadBadgeSet;

    protected int lesson;

    private boolean dialogListExpanded, teacherListExpanded;
    private boolean dialogsLoaded, teachersLoaded;

    private User user;
    private String userType;

    private Realm realm;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        realm = ((DialogActivity) getActivity()).realm;
        realm = ((Newt) getActivity().getApplication()).realm;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_dialog, container, false);

//        showBannerAds();
//        initInterstitialAds();
        initView();
        setViewColor();
        loadingViewShown(true);

        loadData();
        setEvent();

        return view;
    }

    private void showBannerAds() {
        final RelativeLayout adsContainer = view.findViewById(R.id.ads_container);
        AdView adsView = view.findViewById(R.id.ads_view);

        adsView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                adsContainer.setVisibility(View.VISIBLE);
            }
        });
        adsView.loadAd(new AdRequest.Builder().build());
    }

    private void initInterstitialAds() {
        MobileAds.initialize(getContext(), getString(R.string.mobile_ads_id));

        mInterstitialAd = new InterstitialAd(getContext());
        mInterstitialAd.setAdUnitId(getString(R.string.interstitial_ads_id));
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
    }

    private void initView() {
        mainLayout = view.findViewById(R.id.main_layout);
        teacherContainer = view.findViewById(R.id.teacher_container);
        loadingView = view.findViewById(R.id.loading_view);
        noActivityView = view.findViewById(R.id.no_activity_view);

        dialogListToggleLayout = view.findViewById(R.id.dialog_list_toggle_layout);
        teacherListToggleLayout = view.findViewById(R.id.teacher_list_toggle_layout);

        dialogListLbl = view.findViewById(R.id.dialog_list_lbl);
        teacherListLbl = view.findViewById(R.id.teacher_list_lbl);

        dialogListIndicator = view.findViewById(R.id.dialog_list_indicator);
        teacherListIndicator = view.findViewById(R.id.teacher_list_indicator);

        dialogListView = view.findViewById(R.id.dialog_list_view);
        dialogListView.setLayoutManager(new LinearLayoutManager(getContext()));
        dialogListView.setNestedScrollingEnabled(false);

        teacherListView = view.findViewById(R.id.teacher_list_view);
        teacherListView.setLayoutManager(new LinearLayoutManager(getContext()));
        teacherListView.setNestedScrollingEnabled(false);

        loadingView.setVisibility(View.GONE);

        dialogListExpanded = teacherListExpanded = true;
        dialogsLoaded = teachersLoaded = false;

        user = ((DialogActivity) getActivity()).user;

        userType = user.getType();
        if (userType.equals(Constant.TEACHER)) {
            teacherContainer.setVisibility(View.GONE);
            teachersLoaded = true;
        }
        mUnreadBadgeSet = (UnreadBadgeSet) getContext();
    }

    private void setViewColor() {
        if (Build.VERSION.SDK_INT >= 21) {
            loadingView.getIndeterminateDrawable().setColorFilter(getContext().getResources().getColor(Utils.colorsPrimary[lesson]), PorterDuff.Mode.SRC_IN);
        }

        dialogListLbl.setTextColor(getResources().getColor(Utils.colorsPrimary[lesson]));
        teacherListLbl.setTextColor(getResources().getColor(Utils.colorsPrimary[lesson]));

        dialogListIndicator.setColorFilter(getResources().getColor(Utils.colorsPrimary[lesson]));
        teacherListIndicator.setColorFilter(getResources().getColor(Utils.colorsPrimary[lesson]));
    }

    private void loadData(){
        if (Session.with(getContext()).isBaseDataLoaded()) {
            loadDialogs();
            if (userType.equals(Constant.STUDENT)) {
                loadTeachers();
            }
        }
    }

    private void loadDialogs() {
        dialogs = realm.where(Dialog.class)
                .sort("updatedAt", Sort.DESCENDING)
                .equalTo("lesson.id", lesson + 1)
                .findAll();
        dialogListAdpt = new DialogListAdapter(mInterstitialAd, user, dialogs, lesson);
        dialogListView.setAdapter(dialogListAdpt);

        dialogs.addChangeListener(new RealmChangeListener<RealmResults<Dialog>>() {
            @Override
            public void onChange(RealmResults<Dialog> dialogs) {
                if (dialogListAdpt.getItemCount() > 0) {
                    dialogListToggleLayout.setVisibility(View.VISIBLE);
                    if (userType.equals(Constant.TEACHER)) {
                        noActivityView.setVisibility(View.GONE);
                    }
                } else {
                    dialogListToggleLayout.setVisibility(View.GONE);
                    if (userType.equals(Constant.TEACHER)) {
                        noActivityView.setVisibility(View.VISIBLE);
                    }
                }
                dialogListAdpt.notifyDataSetChanged();
            }
        });

        if (!Session.with(getContext()).isUnreadBadgeSet()) {
            countUnreadDialog();
        }

        if (dialogs.size() == 0) {
            dialogListToggleLayout.setVisibility(View.GONE);
            if (userType.equals(Constant.TEACHER)) {
                noActivityView.setVisibility(View.VISIBLE);
            }
        }
        dialogsLoaded = true;

        hideLoadingView();
    }

    private void loadTeachers() {
        teachers = realm.where(Teacher.class)
                .equalTo("lessons.id", lesson + 1)
                .sort("user.name", Sort.ASCENDING)
                .findAll();

        teacherListAdpt = new TeacherListAdapter(mInterstitialAd, user, teachers, lesson);
        teacherListView.setAdapter(teacherListAdpt);

        teachers.addChangeListener(new RealmChangeListener<RealmResults<Teacher>>() {
            @Override
            public void onChange(RealmResults<Teacher> teachers) {
                teacherListAdpt.notifyDataSetChanged();
            }
        });

        teachersLoaded = true;

        hideLoadingView();
    }

    private void countUnreadDialog() {
        int counter = 0;
        for (int i = 0; i < dialogs.size(); i++) {
            RealmList<Chat> chats = dialogs.get(i).getChats();
            for (int j = 0; j < chats.size(); j++) {
                if (!chats.get(j).getSenderCode().equals(user.getCode()) && chats.get(j).getSent() == 1) {
                    counter++;
                    break;
                }
            }
        }

        if (mUnreadBadgeSet != null) {
            mUnreadBadgeSet.onBadgeSet(lesson, counter);
        }
    }

    private void setEvent() {
        dialogListToggleLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialogListExpanded) {
                    dialogListIndicator.setImageResource(R.drawable.ic_arrow_down);
                    dialogListView.setVisibility(View.GONE);

                    dialogListExpanded = false;
                } else {
                    dialogListIndicator.setImageResource(R.drawable.ic_arrow_up);
                    dialogListView.setVisibility(View.VISIBLE);

                    dialogListExpanded = true;
                }
            }
        });

        teacherListToggleLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (teacherListExpanded) {
                    teacherListIndicator.setImageResource(R.drawable.ic_arrow_down);
                    teacherListView.setVisibility(View.GONE);

                    teacherListExpanded = false;
                } else {
                    teacherListIndicator.setImageResource(R.drawable.ic_arrow_up);
                    teacherListView.setVisibility(View.VISIBLE);

                    teacherListExpanded = true;
                }
            }
        });
    }

    private void hideLoadingView() {
        if (dialogsLoaded && teachersLoaded) {
            loadingViewShown(false);
        }
    }

    private void loadingViewShown(boolean shown) {
        if (shown) {
            mainLayout.setVisibility(View.GONE);
            loadingView.setVisibility(View.VISIBLE);
        } else {
            mainLayout.setVisibility(View.VISIBLE);
            loadingView.setVisibility(View.GONE);
        }
    }

    @Subscribe
    public void onEventSubscribe(BaseBus bus) {
        loadDialogs();
        if (userType.equals(Constant.STUDENT)) {
            loadTeachers();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    public void onPause() {
        EventBus.getDefault().unregister(this);
        super.onPause();
    }
}