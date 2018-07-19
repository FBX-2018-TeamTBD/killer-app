package com.example.cassandrakane.goalz;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.cassandrakane.goalz.adapters.GoalSimpleAdapter;
import com.example.cassandrakane.goalz.models.Goal;
import com.example.cassandrakane.goalz.models.Image;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.SaveCallback;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class GoalsListActivity extends AppCompatActivity {

    List<Goal> goals;
    RecyclerView rvGoals;
    ImageView ivStory;
    TextView tvGoal;
    GoalSimpleAdapter goalSimpleAdapter;
    File file;
    Date currentDate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goals_list);

        currentDate = new Date();

        getSupportActionBar().hide();

        ivStory = findViewById(R.id.ivStory);
        tvGoal = findViewById(R.id.tvTitle);

        file = (File) getIntent().getSerializableExtra("image");
        goals = (List) getIntent().getSerializableExtra("goals");

        goalSimpleAdapter = new GoalSimpleAdapter(goals);
        rvGoals = findViewById(R.id.rvGoals);
        rvGoals.setLayoutManager(new LinearLayoutManager(this));
        rvGoals.setAdapter(goalSimpleAdapter);

    }

    public void addImage(View v) {
        String text = "";
        final ParseFile parseFile = new ParseFile(file);
        parseFile.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null){
                    Log.d("GoalsListActivity", "ParseFile has been saved");
                } else{
                    e.printStackTrace();
                }
            }
        });
        for (final Goal goal : goals){
            if (goal.isSelected()){
                text += goal.getTitle() + " ";
                final ArrayList<ParseObject> story = goal.getStory();
                final Image image = new Image(parseFile, "", goal);
                image.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        story.add(image);
                        goal.setStory(story);
                        goal.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (goal.getStory().size() == 1){
                                    Image lastUpdate = (Image) story.get(story.size() - 1);
                                    long sum = lastUpdate.getCreatedAt().getTime() + TimeUnit.DAYS.toMillis(goal.getFrequency());
                                    Date updateStoryBy = new Date(sum);
                                    goal.setUpdateStoryBy(updateStoryBy);
                                    goal.saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            Intent intent = new Intent(GoalsListActivity.this, ProfileActivity.class);
                                            startActivity(intent);
                                        }
                                    });
                                } else {
//                                    final Image lastImage = (Image) goal.getStory().get(goal.getStory().size() - 1);
                                    if (!goal.itemAdded) {
                                        if (currentDate.getTime() <= goal.getUpdateStoryBy().getTime()) {
                                            goal.itemAdded = true;
                                            goal.setStreak(goal.getStreak() + 1);
                                            goal.saveInBackground(new SaveCallback() {
                                                @Override
                                                public void done(ParseException e) {
                                                    Intent intent = new Intent(GoalsListActivity.this, ProfileActivity.class);
                                                    startActivity(intent);
                                                }
                                            });
                                        }
                                    } else {
                                        Intent intent = new Intent(GoalsListActivity.this, ProfileActivity.class);
                                        startActivity(intent);
                                    }
                                }
                            }
                        });
                    }
                });
            }
        }
        Intent intent = new Intent(GoalsListActivity.this, ProfileActivity.class);
        startActivity(intent);
    }
}
