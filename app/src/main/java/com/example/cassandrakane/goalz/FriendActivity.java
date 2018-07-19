package com.example.cassandrakane.goalz;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.design.widget.NavigationView;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.cassandrakane.goalz.adapters.GoalAdapter;
import com.example.cassandrakane.goalz.models.Goal;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FriendActivity extends AppCompatActivity {

    @BindView(R.id.ivProfile) ImageView ivProfile;
    @BindView(R.id.tvProgress) TextView tvProgress;
    @BindView(R.id.tvCompleted) TextView tvCompleted;
    @BindView(R.id.tvFriends) TextView tvFriends;
    @BindView(R.id.tvUsername) TextView tvUsername;

    @BindView(R.id.rvGoals) RecyclerView rvGoals;
    @BindView(R.id.toolbar) public Toolbar toolbar;

    private ParseUser user;

    private List<Goal> goals;
    private GoalAdapter goalAdapter;

    private int completedGoals = 0;
    private int progressGoals = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        ButterKnife.bind(this);

        user = getIntent().getParcelableExtra(ParseUser.class.getSimpleName());

        goals = new ArrayList<>();
        goalAdapter = new GoalAdapter(goals);
        rvGoals.setLayoutManager(new LinearLayoutManager(this));
        rvGoals.setAdapter(goalAdapter);

        if (user.getParseFile("image") != null) {
            ParseFile imageFile = ParseUser.getCurrentUser().getParseFile("image");
            Bitmap bitmap = null;
            try {
                bitmap = BitmapFactory.decodeFile(imageFile.getFile().getAbsolutePath());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
            roundedBitmapDrawable.setCornerRadius(80.0f);
            roundedBitmapDrawable.setAntiAlias(true);
        }

        populateGoals();
    }

    public void populateGoals() {
        List<ParseObject> arr = user.getList("goals");
        completedGoals = 0;
        progressGoals = 0;
        if (arr != null) {
            try {
                ParseObject.fetchAllIfNeeded(arr);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            for(int i = 0; i < arr.size(); i++) {
                Goal goal = (Goal) arr.get(i);
                goals.add(goal);
                if (goal.getCompleted()) {
                    completedGoals += 1;
                } else {
                    progressGoals += 1;
                }
            }
            goalAdapter.notifyDataSetChanged();
            tvProgress.setText(progressGoals + " Current\nGoals");
            tvCompleted.setText(completedGoals + " Completed\nGoal");
            tvFriends.setText(user.getList("friends").size() + "\nFriends");
            tvUsername.setText(user.getUsername());
        };
    }
}
