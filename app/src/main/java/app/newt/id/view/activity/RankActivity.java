package app.newt.id.view.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import app.newt.id.R;
import app.newt.id.helper.Constant;
import app.newt.id.helper.Internet;
import app.newt.id.helper.Session;
import app.newt.id.helper.Utils;
import app.newt.id.server.interfaces.Response;
import app.newt.id.server.model.Challenger;
import app.newt.id.server.presenter.ChallengePresenter;
import app.newt.id.server.response.BaseResponse;
import app.newt.id.server.response.ChallengeResponse;
import app.newt.id.view.adapter.RankListAdapter;

public class RankActivity extends AppCompatActivity implements Response {
    private AppBarLayout appBar;
    private Toolbar toolbar;
    private CoordinatorLayout parentLayout;

    private SwipeRefreshLayout swipeView;
    private NestedScrollView contentView;

    private RecyclerView rankListView;
    private RankListAdapter rankListAdpt;

    private LinearLayout periodeCont;
    private TextView periode;

    private View noRankView;
    private TextView description;

    public int lessonId;
    private boolean loadingRanks = true;

    private ChallengePresenter challengePresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        lessonId = getIntent().getIntExtra("lesson_id", 0);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rank);

        initView();
        setEvent();
    }

    private void initView() {
        appBar = findViewById(R.id.app_bar);
        appBar.setBackgroundResource(R.color.white);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Top Skor (" + getString(Utils.challenges[lessonId - 1]) + ")");

        parentLayout = findViewById(R.id.parent_layout);

        swipeView = findViewById(R.id.swipe_view);
        contentView = findViewById(R.id.content_view);

        swipeView.setColorScheme(R.color.colorPrimary);
        swipeView.setRefreshing(true);

        rankListView = findViewById(R.id.rank_list_view);
        rankListView.setLayoutManager(new LinearLayoutManager(this));

        periodeCont = findViewById(R.id.periode_container);
        periode = findViewById(R.id.periode);

        noRankView = findViewById(R.id.no_rank_view);
        description = noRankView.findViewById(R.id.description);
        description.setText(R.string.no_rank);

        challengePresenter = new ChallengePresenter(getApplicationContext(), this);
        challengePresenter.loadRanks(lessonId);
    }

    private void setEvent() {
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        swipeView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadingRanks = true;
                challengePresenter.loadRanks(lessonId);
            }
        });
    }

    private void loadData(List<Challenger> challengers) {
        if (rankListAdpt != null) {
            rankListAdpt.clearData();
        }
        rankListAdpt = new RankListAdapter(challengers);
        rankListView.setAdapter(rankListAdpt);

        if (challengers.size() > 0) {
            contentView.setVisibility(View.VISIBLE);
            noRankView.setVisibility(View.GONE);
        } else {
            contentView.setVisibility(View.GONE);
            noRankView.setVisibility(View.VISIBLE);
        }
        swipeView.setRefreshing(false);
    }

    @Override
    public void onSuccess(BaseResponse base) {
        if (loadingRanks) {
            loadingRanks = false;

            ChallengeResponse r = (ChallengeResponse) base;
            periode.setText(r.getData().getPeriode());
            periodeCont.setVisibility(View.VISIBLE);

            if (Session.with(this).getUserType().equals(Constant.STUDENT)) {
                int rank = r.getData().getRank();
                if (rank != -1) {
                    if (rank == 1) {
                        Snackbar snackbar = Snackbar.make(parentLayout, "Anda berada di Ranking " + rank + ". Perfect!", Snackbar.LENGTH_LONG);
                        TextView tv = (TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
                        tv.setTextColor(getResources().getColor(R.color.colorPrimary));
                        snackbar.show();
                    } else {
                        Snackbar.make(parentLayout, "Anda berada di Ranking " + rank + ". Boost it Up!", Snackbar.LENGTH_LONG).show();
                    }
                } else {
                    Snackbar.make(parentLayout, R.string.no_rank_generated, Snackbar.LENGTH_LONG).show();
                }
            }
            loadData(r.getData().getChallengers());
        }
    }

    @Override
    public void onFailure(String message) {
        if (loadingRanks) {
            challengePresenter.loadRanks(lessonId);
        }
    }
}