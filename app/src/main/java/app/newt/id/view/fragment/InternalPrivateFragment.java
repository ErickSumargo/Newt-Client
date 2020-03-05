package app.newt.id.view.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextSwitcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import app.newt.id.R;
import app.newt.id.server.model.PrivateTeacher;
import app.newt.id.server.model.Provider;
import app.newt.id.view.adapter.ExternalPrivateTeacherListAdapter;
import app.newt.id.view.adapter.InternalPrivateTeacherListAdapter;

/**
 * Created by Erick Sumargo on 2/20/2018.
 */

public class InternalPrivateFragment extends Fragment {
    private View view;

    private ScrollView contentView;
    private ProgressBar loadingView;
    private RecyclerView teacherListView;

    private LinearLayout promoCont;
    private TextSwitcher promo;

    private Timer timer;
    private int counter = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_internal_private, container, false);
        initView();

        return view;
    }

    private void initView() {
        contentView = view.findViewById(R.id.content_view);
        teacherListView = view.findViewById(R.id.teacher_list_view);
        loadingView = view.findViewById(R.id.loading_view);

        promoCont = view.findViewById(R.id.promo_container);
        promo = view.findViewById(R.id.promo);
        promo.setInAnimation(getContext(), android.R.anim.slide_in_left);
        promo.setOutAnimation(getContext(), android.R.anim.slide_out_right);

        teacherListView.setLayoutManager(new LinearLayoutManager(getContext()));
        teacherListView.setNestedScrollingEnabled(false);
    }

    public void loadData(List<String> promos, List<PrivateTeacher> teachers) {
        if (promos.size() > 0) {
            startTimer(promos);
        } else {
            promoCont.setVisibility(View.GONE);
        }
        InternalPrivateTeacherListAdapter internalPrivateTeacherListAdpt = new InternalPrivateTeacherListAdapter(teachers);
        teacherListView.setAdapter(internalPrivateTeacherListAdpt);

        loadingView.setVisibility(View.GONE);
        contentView.setVisibility(View.VISIBLE);
    }

    private void startTimer(final List<String> promos) {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        promo.setText(promos.get(counter));
                        if (counter == promos.size() - 1) {
                            counter = 0;
                        } else {
                            counter++;
                        }
                    }
                });
            }
        }, 0, 3000);
    }

    public void stopTimer() {
        if (timer != null) {
            timer.cancel();
        }
    }
}