package com.grayfox.android.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.astuetz.PagerSlidingTabStrip;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.grayfox.android.R;
import com.grayfox.android.client.model.Category;
import com.grayfox.android.client.model.User;
import com.grayfox.android.client.task.GetCompletSelfUserAsyncTask;
import com.grayfox.android.widget.FriendAdapter;
import com.grayfox.android.widget.LikeAdapter;
import com.squareup.picasso.Picasso;
import de.hdodenhof.circleimageview.CircleImageView;
import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectView;

import java.lang.ref.WeakReference;

public class ProfileFragment extends RoboFragment {

    @InjectView(R.id.header_layout) private RelativeLayout headerLayout;
    @InjectView(R.id.profile_image) private CircleImageView profileImageView;
    @InjectView(R.id.user_name)     private TextView userNameTextView;
    @InjectView(R.id.edit_button)   private FloatingActionButton editButton;
    @InjectView(R.id.pager_strip)   private PagerSlidingTabStrip pagerStrip;
    @InjectView(R.id.view_pager)    private ViewPager viewPager;
    @InjectView(R.id.progress_bar)  private ProgressBar progressBar;

    private GetSelfUserTask task;
    private User user;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState == null) {
            task = new GetSelfUserTask(this);
            task.execute();
        } else {
            if (task != null && task.isActive()) onPreExecuteTask();
            else if (user != null) {
                onUserAcquired(user);
                onTaskFinally();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (task != null && task.isActive()) task.cancel(true);
    }

    private void onPreExecuteTask() {
        headerLayout.setVisibility(View.GONE);
        editButton.setVisibility(View.GONE);
        pagerStrip.setVisibility(View.GONE);
        viewPager.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void onUserAcquired(User user) {
        this.user = user;
        headerLayout.setVisibility(View.VISIBLE);
        editButton.setVisibility(View.VISIBLE);
        pagerStrip.setVisibility(View.VISIBLE);
        viewPager.setVisibility(View.VISIBLE);
        String userFullName = user.getLastName() == null || user.getLastName().trim().isEmpty() ? user.getName() : new StringBuilder().append(user.getName()).append(" ").append(user.getLastName()).toString();
        userNameTextView.setText(userFullName);
        Picasso.with(getActivity())
                .load(user.getPhotoUrl())
                .placeholder(R.drawable.ic_contact_picture)
                .into(profileImageView);
        viewPager.setAdapter(new SwipeFragmentsAdapter()
                .setFriendsFragment(FriendsFragment.newInstance(user.getFriends()))
                .setLikesFragment(LikesFragment.newInstance(user.getLikes())));
        pagerStrip.setViewPager(viewPager);
    }

    private void onTaskFinally() {
        progressBar.setVisibility(View.GONE);
    }

    private static class GetSelfUserTask extends GetCompletSelfUserAsyncTask {

        private WeakReference<ProfileFragment> reference;

        private GetSelfUserTask(ProfileFragment fragment) {
            super(fragment.getActivity().getApplicationContext());
            reference = new WeakReference<>(fragment);
        }

        @Override
        protected void onPreExecute() throws Exception {
            super.onPreExecute();
            ProfileFragment fragment = reference.get();
            if (fragment != null) fragment.onPreExecuteTask();
        }

        @Override
        protected void onSuccess(User user) throws Exception {
            super.onSuccess(user);
            ProfileFragment fragment = reference.get();
            if (fragment != null) fragment.onUserAcquired(user);
        }

        @Override
        protected void onFinally() throws RuntimeException {
            super.onFinally();
            ProfileFragment fragment = reference.get();
            if (fragment != null) fragment.onTaskFinally();
        }
    }

    private class SwipeFragmentsAdapter extends FragmentPagerAdapter {

        private FriendsFragment friendsFragment;
        private LikesFragment likesFragment;

        private SwipeFragmentsAdapter() {
            super(getChildFragmentManager());
        }

        private SwipeFragmentsAdapter setFriendsFragment(FriendsFragment friendsFragment) {
            this.friendsFragment = friendsFragment;
            return this;
        }

        private SwipeFragmentsAdapter setLikesFragment(LikesFragment likesFragment) {
            this.likesFragment = likesFragment;
            return this;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0: return friendsFragment;
                case 1: return likesFragment;
                default: return null;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0: return getString(R.string.profile_user_friends_tab);
                case 1: return getString(R.string.profile_user_likes_tab);
                default: return null;
            }
        }
    }

    public static class FriendsFragment extends RecyclerFragment {

        private static final String FRIENDS_LENGTH_ARG = "FRIENDS_LENGTH";
        private static final String FRIENDS_FORMAT_ARG = "FRIEND_%d";

        private static FriendsFragment newInstance(User[] friends) {
            FriendsFragment fragment = new FriendsFragment();
            Bundle args = new Bundle();
            args.putInt(FRIENDS_LENGTH_ARG, friends.length);
            for (int i = 0; i < friends.length; i++) args.putSerializable(String.format(FRIENDS_FORMAT_ARG, i), friends[i]);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            getRecyclerView().setHasFixedSize(true);
            getRecyclerView().setLayoutManager(new LinearLayoutManager(getActivity()));
            getRecyclerView().setAdapter(new FriendAdapter(getFriendsArg()));
        }

        private User[] getFriendsArg() {
            User[] friends = new User[getArguments().getInt(FRIENDS_LENGTH_ARG)];
            for (int i = 0; i < friends.length; i++) friends[i] = (User) getArguments().getSerializable(String.format(FRIENDS_FORMAT_ARG, i));
            return friends;
        }
    }

    public static class LikesFragment extends RecyclerFragment {

        private static final String LIKES_LENGTH_ARG = "LIKES_LENGTH";
        private static final String LIKES_FORMAT_ARG = "LIKE_%d";

        private static LikesFragment newInstance(Category[] likes) {
            LikesFragment fragment = new LikesFragment();
            Bundle args = new Bundle();
            args.putInt(LIKES_LENGTH_ARG, likes.length);
            for (int i = 0; i < likes.length; i++) args.putSerializable(String.format(LIKES_FORMAT_ARG, i), likes[i]);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            getRecyclerView().setHasFixedSize(true);
            getRecyclerView().setLayoutManager(new LinearLayoutManager(getActivity()));
            getRecyclerView().setAdapter(new LikeAdapter(getLikesArg()));
        }

        private Category[] getLikesArg() {
            Category[] likes = new Category[getArguments().getInt(LIKES_LENGTH_ARG)];
            for (int i = 0; i < likes.length; i++) likes[i] = (Category) getArguments().getSerializable(String.format(LIKES_FORMAT_ARG, i));
            return likes;
        }
    }
}