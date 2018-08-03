package com.example.cassandrakane.goalz.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.transition.Transition;
import android.support.transition.TransitionInflater;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.cassandrakane.goalz.CameraFragment;
import com.example.cassandrakane.goalz.FriendActivity;
import com.example.cassandrakane.goalz.FriendsModalActivity;
import com.example.cassandrakane.goalz.MainActivity;
import com.example.cassandrakane.goalz.ProfileFragment;
import com.example.cassandrakane.goalz.R;
import com.example.cassandrakane.goalz.SearchFriendsActivity;
import com.example.cassandrakane.goalz.StoryFragment;
import com.example.cassandrakane.goalz.models.Goal;
import com.example.cassandrakane.goalz.models.TextNotification;
import com.example.cassandrakane.goalz.utils.NavigationHelper;
import com.example.cassandrakane.goalz.utils.NotificationHelper;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;

public class GoalAdapter extends RecyclerView.Adapter<GoalAdapter.ViewHolder> {

    private final List<Goal> goals;
    private boolean personal; //for determining whether this is for user or for a friend
    Context context;
    Date currentDate;
    ParseUser currentUser;
    float startX = 0;
    float endX = 0;
    int startIndex = 0;
    NavigationHelper navigationHelper;

    public GoalAdapter(List<Goal> gGoals, boolean personal) {
        this.goals = gGoals;
        this.personal = personal;
    }

    // for each row, inflate the layout and cache references into ViewHolder
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        return new ViewHolder(inflater.inflate(R.layout.item_goal, parent, false));
    }

    // bind the values based on the position of the element
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final Goal goal = goals.get(position);
        currentDate = new Date();
        currentUser = ParseUser.getCurrentUser();

        if (personal) {
            final MainActivity activity = (MainActivity) context;

            navigationHelper = new NavigationHelper(activity.centralFragment.horizontalPager);

            final Goal finalGoal = goal;
            final GestureDetector gestureDetector = new GestureDetector(new GestureDetector.OnGestureListener() {
                @Override
                public boolean onDown(MotionEvent motionEvent) {
                    return true;
                }

                @Override
                public void onShowPress(MotionEvent motionEvent) {

                }

                @Override
                public boolean onSingleTapUp(MotionEvent motionEvent) {
                    return false;
                }

                @Override
                public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
                    startX = motionEvent.getX();
                    endX = motionEvent1.getX();
                    if (endX >= startX + 60) {
                        navigationHelper.toCamera();
                        startX = 0;
                        endX = 0;
                    } else if (startX >= endX + 50) {
                        navigationHelper.toFeed();
                        startX = 0;
                        endX = 0;
                    }
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent motionEvent) {
                    if (Math.abs(endX - startX) < 10) {
                        new AlertDialog.Builder(context)
                                .setTitle(R.string.delete_goal)
                                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        NotificationHelper notificationHelper = new NotificationHelper(context.getApplicationContext());
                                        notificationHelper.cancelReminder(finalGoal);
                                        goals.remove(finalGoal);
                                        finalGoal.unpinInBackground();
                                        notificationHelper.cancelReminder(finalGoal);
                                        removeGoal(finalGoal.getObjectId());
                                    }
                                })
                                .setNegativeButton(R.string.no, null)
                                .show();
                    }
                }

                @Override
                public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
                    startX = motionEvent.getX();
                    endX = motionEvent1.getX();
                    if (endX >= startX + 60) {
                        navigationHelper.toCamera();
                        startX = 0;
                        endX = 0;
                    } else if (startX >= endX + 50) {
                        navigationHelper.toFeed();
                        startX = 0;
                        endX = 0;
                    }
                    return false;
                }

            });

            holder.itemView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    return gestureDetector.onTouchEvent(motionEvent);
                }
            });

            holder.btnStory.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    return gestureDetector.onTouchEvent(motionEvent);
                }
            });

        }

        Date updateBy = goal.getUpdateStoryBy();
        if (updateBy != null) {
            if (currentDate.getTime() >= updateBy.getTime()) {
                // check if all users have added here
                if (!goal.getIsItemAdded()) {
                    goal.setStreak(0);

                    // stores the objectId of users who lost a streak
                    ArrayList<ParseUser> streakLostBy = new ArrayList<>();
                    Map<String, String> userAdded = goal.getUserAdded();

                    if (userAdded != null) {
                        for (Map.Entry<String, String> entry : userAdded.entrySet()) {
                            String userId = entry.getKey();
                            String value = entry.getValue();
                            if (value.equals("false")) {
                                ParseUser user = getParseUserFromId(userId);
                                streakLostBy.add(user);
                            }
                        }
                    }


                    sendTextNotifications(goal, streakLostBy);

//                    final ArrayList<ParseObject> story = goal.getStory();
//                    try{
//                        InputStream inputStream = context.getResources().openRawResource(R.raw.crying_gif);
//                        tempFile = File.createTempFile("pre", "suf");
//                        copyFile(inputStream, new FileOutputStream(tempFile));
//
//                        // Now some_file is tempFile .. do what you like
//                    } catch (IOException e) {
//                        throw new RuntimeException("Can't create temp file ", e);
//                    }
//                    final ParseFile videoFile = new ParseFile(tempFile);
//                    Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(tempFile.getPath(), MediaStore.Images.Thumbnails.MINI_KIND);
//                    ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
//                    thumbnail.compress(Bitmap.CompressFormat.PNG,100,byteArrayOutputStream);
//                    byte[] imageByte = byteArrayOutputStream.toByteArray();
//                    final ParseFile parseFileThumbnail = new ParseFile("image_file.png",imageByte);
//                    parseFileThumbnail.saveInBackground();
//                    videoFile.saveInBackground(new SaveCallback() {
//                        @Override
//                        public void done(ParseException e) {
//                            final Video video = new Video(videoFile, "You did not update your goal story in time", parseFileThumbnail, currentUser);
//                            video.saveInBackground(new SaveCallback() {
//                                @Override
//                                public void done(ParseException e) {
//                                    story.add(video);
//                                    goal.setStory(story);
//                                }
//                            });
//                        }
//                    });
                }
                long sum = updateBy.getTime() + TimeUnit.DAYS.toMillis(goal.getFrequency());
                Date newDate = new Date(sum);
                goal.setUpdateStoryBy(newDate);
                goal.setItemAdded(false);
                goal.saveInBackground();
            }
        }

        holder.tvTitle.setText(goal.getTitle());
        holder.tvProgress.setText(goal.getProgress() + "/" + goal.getDuration());
        if (goal.getStreak() > 0) {
            holder.tvStreak.setText(String.format("%d", goal.getStreak()));
            holder.ivStar.setVisibility(View.VISIBLE);
        } else {
            holder.tvStreak.setText("");
            holder.ivStar.setVisibility(View.INVISIBLE);
        }


        if (goal.getApprovedUsers().size() > 1) {
            holder.tvFriends.setText(String.valueOf(goal.getApprovedUsers().size() - 1));
            holder.ivFriends.setImageDrawable(context.getResources().getDrawable(R.drawable.friend));
            holder.btnFriends.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(context, FriendsModalActivity.class);
                    i.putExtra(Goal.class.getSimpleName(), goal);
                    i.putExtra("personal", personal);
                    context.startActivity(i);
                }
            });
        } else {
            holder.tvFriends.setText("");
            if (personal) {
                holder.ivFriends.setImageDrawable(context.getResources().getDrawable(R.drawable.larger_add));
                holder.btnFriends.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent i = new Intent(context, SearchFriendsActivity.class);
                        i.putExtra("requestActivity", FriendsModalActivity.class.getSimpleName());
                        i.putExtra(Goal.class.getSimpleName(), goal);
                        context.startActivity(i);
                    }
                });
            } else {
                holder.ivFriends.setImageDrawable(null);
            }
        }

        if (goal.getCompleted()) {
            holder.tvTitle.setTextColor(context.getResources().getColor(R.color.grey));
            holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.tvTitle.setTextColor(context.getResources().getColor(R.color.white));
            holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
        }

        int timeRunningOutHours = context.getResources().getInteger(R.integer.TIME_RUNNING_OUT_HOURS);
        if (updateBy != null && (updateBy.getTime() - currentDate.getTime()) < TimeUnit.HOURS.toMillis(timeRunningOutHours) && !goal.getIsItemAdded() && !goal.getCompleted()){
            holder.ivStar.setImageResource(R.drawable.clock);
        } else {
            holder.ivStar.setImageResource(R.drawable.star);
        }

        final ArrayList<String> imageUrls = goal.getStoryUrls();
        final ArrayList<ParseObject> story = goal.getStory();


        if (imageUrls.size() > 0) {
            for (int i =0; i<story.size(); i++){
                boolean seen = false;
                ParseObject image = story.get(i);
                List<ParseUser> users = image.getList("viewedBy");
                if (users != null) {
                    if (users.contains(currentUser)){
                        seen = true;
                    }
                }
                if (!seen) {
                    startIndex = i;
                    // TODO - show blue dot under story
                    break;
                }
            }

            Glide.with(context)
                    .load(imageUrls.get(startIndex))
                    .apply(new RequestOptions().transforms(new CenterCrop(), new RoundedCorners(10)))
                    .into(holder.ivStory);

            holder.btnStory.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (context.getClass().isAssignableFrom(MainActivity.class)) {
                        MainActivity activity = (MainActivity) context;
//                        final FragmentManager fragmentManager = activity.getSupportFragmentManager();
//                        FragmentTransaction fragTransStory = fragmentManager.beginTransaction();
//                        fragTransStory.add(R.id.drawer_layout, StoryFragment.newInstance(story, startIndex, currentUser)).commit();
//                        activity.toolbar.setVisibility(View.INVISIBLE);
                        ProfileFragment fragmentOne = new ProfileFragment();
                        StoryFragment fragmentTwo = StoryFragment.newInstance(story, startIndex, currentUser);
                        Transition changeTransform = TransitionInflater.from(context).
                                inflateTransition(R.transition.change_image_transform);
                        Transition changeBoundsTransform = TransitionInflater.from(context).
                                inflateTransition(R.transition.change_bounds);
                        Transition explodeTransform = TransitionInflater.from(context).
                                inflateTransition(android.R.transition.fade);

//                        fragmentTwo.setSharedElementEnterTransition(new DetailsTransition());
//                        fragmentTwo.setEnterTransition(new Fade());
//                        fragmentTwo.setExitTransition(new Fade());
//                        fragmentTwo.setSharedElementReturnTransition(new DetailsTransition());

                        fragmentOne.setSharedElementReturnTransition(changeTransform);
                        fragmentOne.setExitTransition(explodeTransform);

                        fragmentTwo.setSharedElementEnterTransition(changeTransform);
                        fragmentTwo.setEnterTransition(explodeTransform);

                        FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction()
                                .replace(R.id.main_central_fragment, fragmentTwo)
                                .addToBackStack("transaction")
                                .addSharedElement(holder.ivStory, "story");
                        ft.commit();

//                        ((MainActivity) context).storyTransition(story, startIndex, currentUser);
                    }
                    if (context.getClass().isAssignableFrom(FriendActivity.class)) {
                        FriendActivity activity = (FriendActivity) context;
                        final FragmentManager fragmentManager = activity.getSupportFragmentManager();
                        FragmentTransaction fragTransStory = fragmentManager.beginTransaction();
                        fragTransStory.add(R.id.root_layout, StoryFragment.newInstance(story, startIndex, currentUser)).commit();
                        activity.ivProfile.setVisibility(View.INVISIBLE);
                        activity.cardView.setVisibility(View.INVISIBLE);
                        activity.btnBack.setVisibility(View.INVISIBLE);
                        activity.btnUnfriend.setVisibility(View.INVISIBLE);
                    }
                }
            });
        } else {
            if (personal) {
                /*Glide.with(context)
                        .load(R.drawable.placeholder)
                        .apply(new RequestOptions().transforms(new CenterCrop(), new RoundedCorners(10)))
                        .into(holder.ivStory);*/
                final Goal finalGoal1 = goal;
                holder.btnStory.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view){
                        if (context.getClass().isAssignableFrom(MainActivity.class)) {
                            MainActivity activity = (MainActivity) context;
                            CameraFragment cameraFragment = CameraFragment.newInstance(finalGoal1);
                            final FragmentManager fragmentManager = activity.getSupportFragmentManager();
                            FragmentTransaction fragTransStory = fragmentManager.beginTransaction();
                            fragTransStory.add(R.id.main_central_fragment, cameraFragment).commit();
                        }
                    }
                });
            } else {
                /*Glide.with(context)
                        .load(R.drawable.placeholder)
                        .apply(new RequestOptions().transforms(new CenterCrop(), new RoundedCorners(10)))
                        .into(holder.ivStory);*/
            }
        }
    }

    private void removeGoal(String id) {
        final ParseUser user = ParseUser.getCurrentUser();
        user.put("goals", goals);
        ParseACL acl = user.getACL();
        if (!acl.getPublicReadAccess()) {
            acl.setPublicReadAccess(true);
            user.setACL(acl);
        }
        user.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    try {
                        user.fetch();
                    } catch (ParseException e1) {
                        e1.printStackTrace();
                    }
                } else {
                    Log.i("Profile Activity", "Failed to delete object, with error code: " + e.toString());
                }
            }
        });
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Goal");
        query.whereEqualTo("objectId", id);
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                try {
                    object.delete();
                    object.saveInBackground();
                    notifyDataSetChanged();
                } catch (ParseException e1) {
                    e1.printStackTrace();
                }
            }
        });
    }

    public ParseUser getParseUserFromId(String id) {
        final ParseUser[] user = {null};
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo("objectId", id);
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> objects, ParseException e) {
                if (objects != null && objects.size() > 0) {
                    user[0] = objects.get(0);
                }
            }
        });
        return user[0];
    }

    public void sendTextNotifications(Goal goal, List<ParseUser> users) {
        String text = getTextNotificationString(goal.getTitle(), users);
        List<ParseUser> friends = goal.getFriends();
        for (ParseUser friend : friends) {
            TextNotification notification = new TextNotification(text, friend);
            notification.saveInBackground();
        }
    }

    public String getTextNotificationString(String goalTitle, List<ParseUser> users) {
        String text = String.format("Oh no! You lost your streak for \"%s\"! ", goalTitle);
        for (int i = 0; i < users.size(); i++) {
            ParseUser user = users.get(i);
            String username = "";
            if (user != null) {
                try {
                    username = user.fetchIfNeeded().getUsername();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            if (i == users.size() - 1) {
                text += String.format("%s ", username);
            } else if (i == users.size() - 2){
                text += String.format("%s and ", username);
            } else {
                text += String.format("%s, ", username);
            }
        }
        if (users.size() == 0) {
            text += "Someone ";
        }
        text += "forgot to post.";
        return text;
    }

    @Override
    public int getItemCount() {
        return goals.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tvTitle) TextView tvTitle;
        @BindView(R.id.tvStreak) TextView tvStreak;
        @BindView(R.id.tvProgress) TextView tvProgress;
        @BindView(R.id.ivStory) ImageView ivStory;
        @BindView(R.id.ivStar) ImageView ivStar;
        @BindView(R.id.ivFriends) ImageView ivFriends;
        @BindView(R.id.tvFriends) TextView tvFriends;
        @BindView(R.id.btnStory) Button btnStory;
        @BindView(R.id.btnFriends) Button btnFriends;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }
}