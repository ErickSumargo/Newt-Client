package app.newt.id.view.activity;

import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import app.newt.id.R;
import app.newt.id.server.interfaces.Response;
import app.newt.id.server.model.Challenge;
import app.newt.id.server.presenter.ChallengePresenter;
import app.newt.id.server.response.BaseResponse;
import app.newt.id.server.response.ChallengeResponse;
import app.newt.id.view.adapter.ChallengeHistoryListAdapter;
import app.newt.id.view.adapter.RecordListAdapter;
import app.newt.id.view.interfaces.LoadMore;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ChallengeHistoryActivity extends AppCompatActivity implements Response, LoadMore {
    private AppBarLayout appBar;
    private Toolbar toolbar;

    private LinearLayout contentView;
    private ProgressBar loadingView;
    private RecyclerView historyListView;
    private ChallengeHistoryListAdapter challengeHistoryListAdpt;

    private View noHistoryView;
    private TextView description;

    private List<Challenge> histories;
    private int lastId = 0, lessonId;

    private ChallengePresenter challengePresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge_history);

        initView();
        setEvent();
    }

    private void initView() {
        appBar = findViewById(R.id.app_bar);
        appBar.setBackgroundResource(R.color.white);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        contentView = findViewById(R.id.content_view);
        historyListView = findViewById(R.id.history_list_view);
        loadingView = findViewById(R.id.loading_view);

        noHistoryView = findViewById(R.id.no_history_view);
        description = noHistoryView.findViewById(R.id.description);
        description.setText(R.string.no_history);

        historyListView.setLayoutManager(new LinearLayoutManager(this));

        histories = new ArrayList<>();
        challengeHistoryListAdpt = new ChallengeHistoryListAdapter(historyListView, histories);
        challengeHistoryListAdpt.setOnLoadMoreListener(this);
        historyListView.setAdapter(challengeHistoryListAdpt);

        lessonId = getIntent().getIntExtra("lesson_id", 0);

        challengePresenter = new ChallengePresenter(getApplicationContext(), this);
        challengePresenter.loadHistories(lessonId, lastId);
    }

    private void setEvent() {
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    public void loadData(List<Challenge> histories) {
        if (histories.size() > 0) {
            if (lastId == 0) {
                this.histories = histories;

                loadingView.setVisibility(View.GONE);
                contentView.setVisibility(View.VISIBLE);
            } else {
                this.histories.addAll(histories);
            }
            challengeHistoryListAdpt.updateList(this.histories);
            challengeHistoryListAdpt.setLoaded();

            lastId = this.histories.get(this.histories.size() - 1).getQuestion().getId();
        } else {
            if (lastId == 0) {
                loadingView.setVisibility(View.GONE);
                noHistoryView.setVisibility(View.VISIBLE);
            } else {
                challengeHistoryListAdpt.updateList(this.histories);
            }
        }
    }

    @Override
    public void onLoadMore() {
        challengePresenter.loadHistories(lessonId, lastId);
    }

    @Override
    public void onSuccess(BaseResponse base) {
        ChallengeResponse r = (ChallengeResponse) base;

        List<Challenge> histories = r.getData().getHistories();
        loadData(histories);
    }

    @Override
    public void onFailure(String message) {
        if (lastId == 0) {
            challengePresenter.loadHistories(lessonId, lastId);
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}