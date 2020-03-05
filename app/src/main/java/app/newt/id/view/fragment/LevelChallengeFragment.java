package app.newt.id.view.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import app.newt.id.R;
import app.newt.id.server.model.Challenge;
import app.newt.id.view.adapter.ChallengeListAdapter;

/**
 * Created by Erick Sumargo on 2/20/2018.
 */

public class LevelChallengeFragment extends Fragment {
    private View view;

    private NestedScrollView contentView;
    private ProgressBar loadingView;
    private RecyclerView questionListView;

    private View noChallengeView;
    private TextView description;

    private ChallengeListAdapter challengeListAdpt;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_level_challenge, container, false);
        initView();

        return view;
    }

    private void initView() {
        contentView = view.findViewById(R.id.content_view);
        questionListView = view.findViewById(R.id.question_list_view);
        loadingView = view.findViewById(R.id.loading_view);

        noChallengeView = view.findViewById(R.id.no_challenge_view);
        description = noChallengeView.findViewById(R.id.description);
        description.setText(R.string.no_challenge);

        questionListView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    public void loadData(List<Challenge> questions) {
        if (questions.size() > 0) {
            challengeListAdpt = new ChallengeListAdapter(questions);
            questionListView.setAdapter(challengeListAdpt);

            loadingView.setVisibility(View.GONE);
            contentView.setVisibility(View.VISIBLE);
        } else{
            challengeListAdpt = new ChallengeListAdapter(new ArrayList<Challenge>());

            loadingView.setVisibility(View.GONE);
            noChallengeView.setVisibility(View.VISIBLE);
        }
    }

    public void clearData() {
        challengeListAdpt.clearData();

        loadingView.setVisibility(View.VISIBLE);
        contentView.setVisibility(View.GONE);
    }
}