package app.newt.id.view.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import app.newt.id.R;
import app.newt.id.server.interfaces.Response;
import app.newt.id.server.model.Challenge;
import app.newt.id.server.presenter.ChallengePresenter;
import app.newt.id.server.response.BaseResponse;
import app.newt.id.server.response.ChallengeResponse;
import app.newt.id.view.adapter.RecordListAdapter;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class RecordActivity extends AppCompatActivity implements Response {
    private AppBarLayout appBar;
    private Toolbar toolbar;

    private ImageView icCoin;
    private TextView points;

    private NestedScrollView contentView;
    private ProgressBar loadingView;
    private RecyclerView recordListView;

    private View noSolvedView;
    private TextView description;

    private int challengerId, lessonId;

    private ChallengePresenter challengePresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        initView();
        setEvent();
    }

    private void initView() {
        appBar = findViewById(R.id.app_bar);
        appBar.setBackgroundResource(R.color.white);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        icCoin = findViewById(R.id.ic_coin);
        points = findViewById(R.id.points);

        contentView = findViewById(R.id.content_view);
        recordListView = findViewById(R.id.record_list_view);
        loadingView = findViewById(R.id.loading_view);

        noSolvedView = findViewById(R.id.no_solved_view);
        description = noSolvedView.findViewById(R.id.description);
        description.setText(R.string.no_solved);

        recordListView.setLayoutManager(new LinearLayoutManager(this));

        icCoin.setColorFilter(getResources().getColor(R.color.colorPoints));
        challengerId = getIntent().getIntExtra("challenger_id", 1);
        lessonId = getIntent().getIntExtra("lesson_id", 1);

        challengePresenter = new ChallengePresenter(getApplicationContext(), this);
        challengePresenter.loadRecords(challengerId, lessonId);
    }

    private void setEvent() {
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    public void loadData(List<Challenge> records) {
        if (records.size() > 0) {
            RecordListAdapter recordListAdpt = new RecordListAdapter(records);
            recordListView.setAdapter(recordListAdpt);

            loadingView.setVisibility(View.GONE);
            contentView.setVisibility(View.VISIBLE);
        } else {
            loadingView.setVisibility(View.GONE);
            noSolvedView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onSuccess(BaseResponse base) {
        ChallengeResponse r = (ChallengeResponse) base;

        getSupportActionBar().setTitle(r.getData().getChallengerName());
        points.setText(r.getData().getPoints() + " pts/" + r.getData().getSolved());

        List<Challenge> records = r.getData().getRecords();
        loadData(records);
    }

    @Override
    public void onFailure(String message) {
        challengePresenter.loadRecords(challengerId, lessonId);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}