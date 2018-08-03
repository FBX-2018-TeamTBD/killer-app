package com.example.cassandrakane.goalz;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.cassandrakane.goalz.adapters.NotificationAdapter;
import com.example.cassandrakane.goalz.models.Goal;
import com.example.cassandrakane.goalz.models.GoalRequests;
import com.example.cassandrakane.goalz.models.SentFriendRequests;
import com.example.cassandrakane.goalz.models.TextNotification;
import com.example.cassandrakane.goalz.utils.NavigationHelper;
import com.example.cassandrakane.goalz.utils.Util;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NotificationsFragment extends Fragment {

    MainActivity mainActivity;

    NotificationAdapter notificationAdapter;
    List<TextNotification> textNotifications;
    List<Goal> goalRequests;
    List<GoalRequests> allGoalRequests;
    List<ParseUser> friendRequests;
    List<SentFriendRequests> allFriendRequests;

    List<Goal> goals;
    List<Goal> completed;
    List<Goal> incompleted;

    @BindView(R.id.tvProgress) TextView tvProgress;
    @BindView(R.id.tvCompleted) TextView tvCompleted;
    @BindView(R.id.tvFriends) TextView tvFriends;
    @BindView(R.id.tvUsername) TextView tvUsername;
    @BindView(R.id.ivProfile) ImageView ivProfile;
    @BindView(R.id.btnLogout) Button btnLogout;
    @BindView(R.id.rvNotifications) public RecyclerView rvNotifications;
    @BindView(R.id.swipeContainer) SwipeRefreshLayout swipeContainer;

    public NotificationsFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mainActivity = (MainActivity) getActivity();
        super.onAttach(mainActivity.getBaseContext());
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);
        ButterKnife.bind(this, view);

        ParseUser user = ParseUser.getCurrentUser();

        goals = new ArrayList<>();
        completed = new ArrayList<>();
        incompleted = new ArrayList<>();
        setNotificationHeader();

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavigationHelper navigationHelper = new NavigationHelper(mainActivity.centralFragment.horizontalPager);
                navigationHelper.logout(mainActivity);
            }
        });

        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                getTextNotifications();
                getGoalRequests();
                getFriendRequests();
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_orange_light,
                android.R.color.holo_green_light,
                android.R.color.holo_blue_light,
                android.R.color.holo_red_light);

        textNotifications = new ArrayList<>();
        goalRequests = new ArrayList<>();
        allGoalRequests = new ArrayList<>();
        friendRequests = new ArrayList<>();
        allFriendRequests = new ArrayList<>();
        notificationAdapter = new NotificationAdapter(textNotifications, goalRequests, allGoalRequests, friendRequests, allFriendRequests, this);
        rvNotifications.setLayoutManager(new LinearLayoutManager(getContext()));
        rvNotifications.setAdapter(notificationAdapter);

        getTextNotifications();
        getGoalRequests();
        getFriendRequests();

        return view;
    }

    public void getTextNotifications() {
        ParseQuery<TextNotification> query = ParseQuery.getQuery("TextNotification");
        query.whereEqualTo("user", ParseUser.getCurrentUser());
        query.orderByDescending("createdAt");
        query.findInBackground(new FindCallback<TextNotification>() {
            @Override
            public void done(List<TextNotification> objects, ParseException e) {
                textNotifications.clear();
                if (objects != null) {
                    for (int i = 0; i < objects.size(); i++) {
                        TextNotification notif = objects.get(i);
                        textNotifications.add(notif);
                    }
                }
                notificationAdapter.notifyDataSetChanged();
            }
        });
    }

    public void getGoalRequests() {
        ParseQuery<GoalRequests> query = ParseQuery.getQuery("GoalRequests");
        query.include("goal");
        query.whereEqualTo("user", ParseUser.getCurrentUser());
        query.orderByDescending("createdAt");
        query.findInBackground(new FindCallback<GoalRequests>() {
            @Override
            public void done(List<GoalRequests> objects, ParseException e) {
                goalRequests.clear();
                allGoalRequests.clear();
                if (objects != null) {
                    for (int i = 0; i < objects.size(); i++) {
                        GoalRequests request = objects.get(i);
                        try {
                            Goal goal = (Goal) request.getParseObject("goal");
                            if (goal != null) {
                                goal.fetch();
                            }
                            goalRequests.add(goal);
                            allGoalRequests.add(request);
                        } catch (ParseException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
                notificationAdapter.notifyDataSetChanged();
            }
        });
    }

    public void getFriendRequests() {
        ParseQuery<SentFriendRequests> query = ParseQuery.getQuery("SentFriendRequests");
        query.include("toUser");
        query.include("fromUser");
        query.whereEqualTo("toUser", ParseUser.getCurrentUser());
        query.orderByDescending("createdAt");
        query.findInBackground(new FindCallback<SentFriendRequests>() {
            @Override
            public void done(List<SentFriendRequests> objects, ParseException e) {
                friendRequests.clear();
                allFriendRequests.clear();
                if (objects != null) {
                    for (int i = 0; i < objects.size(); i++) {
                        SentFriendRequests request = objects.get(i);
                        try {
                            friendRequests.add(request.getParseUser("fromUser").fetch());
                            allFriendRequests.add(request);
                        } catch (ParseException e1) {
                            e1.printStackTrace();
                        }
                    }
                    swipeContainer.setRefreshing(false);
                }
                notificationAdapter.notifyDataSetChanged();
            }
        });
    }

    public void setNotificationHeader() {
        Util.populateNotificationsHeader(getContext(), ParseUser.getCurrentUser(), tvProgress, tvCompleted, tvFriends, tvUsername, ivProfile, goals, incompleted);
    }
}