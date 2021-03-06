package com.example.cassandrakane.goalz;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.cassandrakane.goalz.adapters.GoalFriendAdapter;
import com.example.cassandrakane.goalz.models.Goal;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.support.v7.widget.DividerItemDecoration.HORIZONTAL;

public class FriendsModalActivity extends AppCompatActivity {

    @BindView(R.id.rvGoalFriends) RecyclerView rvGoalFriends;
    @BindView(R.id.btnAdd) Button btnAdd;
    @BindView(R.id.tvGoal) TextView tvGoal;
    GoalFriendAdapter goalFriendAdapter;
    List<ParseUser> goalFriends;
    Goal goal;
    Boolean personal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContentView(R.layout.modal_friend);

        ButterKnife.bind(this);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = (int)(dm.widthPixels * 0.75);
        int height = (int)(dm.heightPixels * 0.65);

        getWindow().setLayout(width, height);

        goal = getIntent().getParcelableExtra(Goal.class.getSimpleName());
        personal = getIntent().getBooleanExtra("personal", true);

        goalFriends = goal.getApprovedUsers();
        List<ParseUser> copyFriends = new ArrayList<>();
        copyFriends.addAll(goalFriends);
        copyFriends.remove(ParseUser.getCurrentUser());
        goalFriendAdapter = new GoalFriendAdapter(copyFriends);
        rvGoalFriends.setLayoutManager(new LinearLayoutManager(this));
        rvGoalFriends.setAdapter(goalFriendAdapter);
        DividerItemDecoration itemDecor = new DividerItemDecoration(this, HORIZONTAL);
        rvGoalFriends.addItemDecoration(itemDecor);

        tvGoal.setText(goal.getTitle());

        if (!personal) {
            btnAdd.setVisibility(View.GONE);
        }
    }

    public void goBack(View v) {
        finish();
    }

    public void addFriendToGoal(View v) {
        Intent i = new Intent(getApplicationContext(), SearchFriendsActivity.class);
        i.putExtra("requestActivity", FriendsModalActivity.class.getSimpleName());
        i.putExtra(Goal.class.getSimpleName(), goal);
        startActivity(i);
        finish();
    }

}
