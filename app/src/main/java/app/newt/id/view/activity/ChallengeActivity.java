package app.newt.id.view.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.romainpiel.shimmer.Shimmer;
import com.romainpiel.shimmer.ShimmerTextView;

import java.util.List;

import app.newt.id.R;
import app.newt.id.helper.Constant;
import app.newt.id.helper.Session;
import app.newt.id.server.interfaces.Response;
import app.newt.id.server.model.Challenge;
import app.newt.id.server.model.User;
import app.newt.id.server.presenter.ChallengePresenter;
import app.newt.id.server.response.BaseResponse;
import app.newt.id.server.response.ChallengeResponse;
import app.newt.id.view.custom.ViewPagerCustom;
import app.newt.id.view.fragment.LevelChallengeFragment;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ChallengeActivity extends AppCompatActivity implements Response {
    private AppBarLayout appBar;
    private Toolbar toolbar;

    private SectionsPagerAdapter sectionsPagerAdapter;
    private ViewPagerCustom viewPager;
    private TabLayout tabLayout;

    private ShimmerTextView topScore;
    private LinearLayout pointsCont;
    private ImageView icCoin;
    private TextView points;

    private FloatingActionButton history;

    private LevelChallengeFragment easyFragment, mediumFragment, hardFragment;

    private User user;
    private int lessonId;
    private boolean ended;
    private ChallengePresenter challengePresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        lessonId = getIntent().getIntExtra("lesson_id", 0);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge);

        initView();
        setEvent();
    }

    private void initView() {
        appBar = findViewById(R.id.app_bar);
        appBar.setBackgroundResource(R.color.white);

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

        for (int i = 0; i < sectionsPagerAdapter.getCount(); i++) {
            tabLayout.getTabAt(i).setCustomView(R.layout.custom_tab);

            View tabLesson = tabLayout.getTabAt(i).getCustomView();
            initTab(tabLesson, i);
        }
        if (lessonId == 1) {
            tabLayout.setVisibility(View.GONE);
        }

        pointsCont = findViewById(R.id.points_container);
        icCoin = findViewById(R.id.ic_coin);
        topScore = findViewById(R.id.top_score);
        points = findViewById(R.id.points);
        history = findViewById(R.id.history);

        topScore.setTypeface(topScore.getTypeface(), Typeface.BOLD);

        Shimmer shimmer = new Shimmer();
        shimmer.setDuration(1500);
        shimmer.start(topScore);

        icCoin.setColorFilter(getResources().getColor(R.color.colorPoints));

        user = Session.with(this).getUser();
        if (user.getType().equals(Constant.TEACHER)) {
            pointsCont.setVisibility(View.GONE);
        }

        easyFragment = new LevelChallengeFragment();
        mediumFragment = new LevelChallengeFragment();
        hardFragment = new LevelChallengeFragment();

        challengePresenter = new ChallengePresenter(getApplicationContext(), this);
        challengePresenter.loadQuestions(lessonId);
    }

    private void setEvent() {
        topScore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), RankActivity.class);
                intent.putExtra("lesson_id", lessonId);
                startActivity(intent);
            }
        });

        history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ChallengeHistoryActivity.class);
                intent.putExtra("lesson_id", lessonId);
                startActivity(intent);
            }
        });
    }

    private void initTab(View tab, int pos) {
        ImageView tabIcon = tab.findViewById(R.id.icon);
        TextView tabName = tab.findViewById(R.id.name);
        TextView tabCounter = tab.findViewById(R.id.counter);

        tabName.setText(sectionsPagerAdapter.getPageTitle(pos));
        tabName.setTextColor(getResources().getColor(R.color.black));

        tabIcon.setVisibility(View.GONE);
        tabCounter.setVisibility(View.GONE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == RESULT_OK) {
            ended = data.getBooleanExtra("ended", false);
            if (ended) {
                if (lessonId == 1) {
                    hardFragment.clearData();
                } else {
                    easyFragment.clearData();
                    mediumFragment.clearData();
                    hardFragment.clearData();
                }
                challengePresenter.loadQuestions(lessonId);
            }
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("ended", ended);
        setResult(RESULT_OK, intent);

        finish();
    }

    private class SectionsPagerAdapter extends FragmentStatePagerAdapter {

        private SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (lessonId == 1) {
                switch (position) {
                    case 0:
                        return hardFragment;
                }
            } else {
                switch (position) {
                    case 0:
                        return easyFragment;
                    case 1:
                        return mediumFragment;
                    case 2:
                        return hardFragment;
                }
            }
            return null;
        }

        @Override
        public int getCount() {
            if (lessonId == 1) {
                return 1;
            } else {
                return 3;
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "EASY";
                case 1:
                    return "MEDIUM";
                case 2:
                    return "HARD";
            }
            return null;
        }
    }

    @Override
    public void onSuccess(BaseResponse base) {
        ChallengeResponse r = (ChallengeResponse) base;
        List<Challenge> easyQuestions = r.getData().getEasyQuestions();
        List<Challenge> mediumQuestions = r.getData().getMediumQuestions();
        List<Challenge> hardQuestions = r.getData().getHardQuestions();

        if (user.getType().equals(Constant.STUDENT)) {
            points.setText(r.getData().getPoints() + " pts/" + r.getData().getSolved());
        }
        if (lessonId == 1) {
            hardFragment.loadData(hardQuestions);
        } else {
            easyFragment.loadData(easyQuestions);
            mediumFragment.loadData(mediumQuestions);
            hardFragment.loadData(hardQuestions);
        }
    }

    @Override
    public void onFailure(String message) {
        challengePresenter.loadQuestions(lessonId);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}