package com.example.tiktokfollow.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewStub;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.tiktokfollow.R;
import com.example.tiktokfollow.adapter.FollowListAdapter;
import com.example.tiktokfollow.model.User;
import com.example.tiktokfollow.util.SharedPreferencesUtil;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class FollowActivity extends AppCompatActivity {
    private TabLayout tabLayout;
    private LinearLayout layoutFollow;
    private TextView tvFollowCount;
    private RecyclerView rvFollowList;
    private FollowListAdapter adapter;
    private List<User> followList;
    private ViewStub stubMutualFollow;
    private ViewStub stubFans;
    private ViewStub stubFriends;
    private View layoutMutualFollow;
    private View layoutFans;
    private View layoutFriends;
    private SwipeRefreshLayout srlFollow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow);
        // 初始化非关注栏
        initView();
        // 初始化数据
        initData();
        // 初始化Tab监听
        initTabListener();
        // 初始化关注栏适配器
        initFollowAdapter();
        // 初始化刷新
        initRefreshListener();
    }

    private void initView() {
        // 初始化关注栏控件
        tabLayout = findViewById(R.id.tab_layout);
        layoutFollow = findViewById(R.id.layout_follow);
        tvFollowCount = findViewById(R.id.tv_follow_count);
        rvFollowList = findViewById(R.id.rv_follow_list);
        rvFollowList.setLayoutManager(new LinearLayoutManager(this));

        // 初始化非关注栏
        stubMutualFollow = findViewById(R.id.stub_mutual_follow);
        stubFans = findViewById(R.id.stub_fans);
        stubFriends = findViewById(R.id.stub_friends);

        // 初始化下拉刷新组件
        srlFollow = findViewById(R.id.srl_follow);
        srlFollow.setColorSchemeResources(
                R.color.red_follow,
                R.color.colorPrimary,
                R.color.special_follow_bg
        );

        // 避免初始显示错误
        layoutFollow.setVisibility(View.GONE);
    }

    private void initData() {
        // 初始化时读取所有关注
        followList = SharedPreferencesUtil.getFollowList(this);
    }

    // Tab切换加载对应布局，隐藏其他布局
    private void initTabListener() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                // 隐藏所有栏目
                hideAllLayouts();

                // 根据Tab位置显示对应布局
                switch (position) {
                    case 0: // 互关栏
                        showMutualFollowLayout();
                        break;
                    case 1: // 关注栏
                        showFollowLayout();
                        break;
                    case 2: // 粉丝栏
                        showFansLayout();
                        break;
                    case 3: // 朋友栏
                        showFriendsLayout();
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    // 隐藏所有栏目
    private void hideAllLayouts() {
        // 隐藏关注栏
        if (layoutFollow != null) {
            layoutFollow.setVisibility(View.GONE);
        }
        // 隐藏互关栏
        if (layoutMutualFollow != null) {
            layoutMutualFollow.setVisibility(View.GONE);
        }
        // 隐藏粉丝栏
        if (layoutFans != null) {
            layoutFans.setVisibility(View.GONE);
        }
        // 隐藏朋友栏
        if (layoutFriends != null) {
            layoutFriends.setVisibility(View.GONE);
        }
    }

    // 显示关注栏
    private void showFollowLayout() {
        if (layoutFollow != null) {
            layoutFollow.setVisibility(View.VISIBLE);
        }

        // 读取本地存储已关注用户
        List<User> allUsers = SharedPreferencesUtil.getFollowList(this);

        // 避免本地存储数据异常
        followList.clear();
        for (User user : allUsers) {
            if (user.isFollowed()) {
                followList.add(user);
            }
        }

        // 更新关注人数统计
        tvFollowCount.setText(String.format("我的关注(%d人)", followList.size()));
        adapter.updateData(followList);
    }

    // 显示互关栏
    private void showMutualFollowLayout() {
        // Inflate ViewStub
        if (layoutMutualFollow == null && stubMutualFollow != null) {
            layoutMutualFollow = stubMutualFollow.inflate();
            // 初始化互关栏数据
            TextView tvMutualCount = layoutMutualFollow.findViewById(R.id.tv_mutual_follow_count);
            tvMutualCount.setText(getString(R.string.my_mutual_follow)); // 可动态替换括号内数字
        }
        // 显示互关栏
        if (layoutMutualFollow != null) {
            layoutMutualFollow.setVisibility(View.VISIBLE);
        }
    }

    // 显示粉丝栏
    private void showFansLayout() {
        // Inflate ViewStub
        if (layoutFans == null && stubFans != null) {
            layoutFans = stubFans.inflate();
            // 动态更新粉丝人数
            TextView tvFansCount = layoutFans.findViewById(R.id.tv_fans_count);
            tvFansCount.setText(getString(R.string.my_fans));
        }
        // 显示粉丝栏
        if (layoutFans != null) {
            layoutFans.setVisibility(View.VISIBLE);
        }
    }

    // 显示朋友栏
    private void showFriendsLayout() {
        // Inflate ViewStub
        if (layoutFriends == null && stubFriends != null) {
            layoutFriends = stubFriends.inflate();
            // 动态更新朋友人数
            TextView tvFriendsCount = layoutFriends.findViewById(R.id.tv_friends_count);
            tvFriendsCount.setText(getString(R.string.my_friends));
        }
        // 显示朋友栏
        if (layoutFriends != null) {
            layoutFriends.setVisibility(View.VISIBLE);
        }
    }

    // 初始化关注栏适配器
    private void initFollowAdapter() {
        adapter = new FollowListAdapter(this, followList);
        // 关注按钮点击监听
        adapter.setOnFollowBtnClickListener((position, user) -> {
            if (user.isFollowed()) {
                showUnfollowDialog(position, user);
            } else {
                user.setFollowed(true);
                updateUserAndSave(position, user);
            }
        });

        // 更多设置监听
        adapter.setOnMoreBtnClickListener((position, user) -> {
            showBottomSheetDialog(position, user);
        });

        rvFollowList.setAdapter(adapter);
    }

    // 取消关注确认弹窗
    private void showUnfollowDialog(int position, User user) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_unfollow)
                .setPositiveButton(R.string.confirm, (dialog, which) -> {
                    // 仅更新状态, 已关注变为未关注，取消特别关注
                    user.setFollowed(false);
                    user.setSpecialFollow(false);
                    // 保存状态到本地
                    updateUserAndSave(position, user);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showBottomSheetDialog(int position, User user) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_setting, null);
        dialog.setContentView(view);

        TextView tvUserInfo = view.findViewById(R.id.tv_user_info);
        androidx.appcompat.widget.SwitchCompat switchSpecial = view.findViewById(R.id.switch_special_follow);
        TextView tvSetRemark = view.findViewById(R.id.tv_set_remark);
        TextView tvUnfollow = view.findViewById(R.id.tv_unfollow);

        String userInfo = user.getRemark().isEmpty() ?
                user.getNickname() : user.getRemark() + "（" + user.getNickname() + "）";
        tvUserInfo.setText(userInfo);

        switchSpecial.setChecked(user.isSpecialFollow() && user.isFollowed());
        switchSpecial.setEnabled(user.isFollowed());

        switchSpecial.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (user.isFollowed()) {
                user.setSpecialFollow(isChecked);
                updateUserAndSave(position, user);
            } else {
                switchSpecial.setChecked(false);
                Toast.makeText(FollowActivity.this, "请先关注该用户", Toast.LENGTH_SHORT).show();
            }
        });

        tvSetRemark.setOnClickListener(v -> {
            showSetRemarkDialog(position, user);
            dialog.dismiss();
        });

        tvUnfollow.setOnClickListener(v -> {
            showUnfollowDialog(position, user);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showSetRemarkDialog(int position, User user) {
        EditText editText = new EditText(this);
        editText.setHint(R.string.input_remark);
        editText.setText(user.getRemark());

        new AlertDialog.Builder(this)
                .setTitle(R.string.set_remark)
                .setView(editText)
                .setPositiveButton(R.string.confirm, (dialog, which) -> {
                    String remark = editText.getText().toString().trim();
                    user.setRemark(remark);
                    updateUserAndSave(position, user);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    // 数据更新保存
    private void updateUserAndSave(int position, User user) {
        followList.set(position, user);
        SharedPreferencesUtil.saveFollowList(this, followList);
        adapter.notifyItemChanged(position);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (tabLayout.getSelectedTabPosition() == 1) { // 关注栏刷新
            // 读取本地存储中已关注用户
            List<User> allUsers = SharedPreferencesUtil.getFollowList(this);

            // 过滤未关注用户
            followList.clear();
            for (User user : allUsers) {
                if (user.isFollowed()) {
                    followList.add(user);
                }
            }

            // 更新人数统计和列表
            tvFollowCount.setText(String.format("我的关注(%d人)", followList.size()));
            adapter.updateData(followList);
        }
    }

    // 下拉刷新,彻底删除未关注用户
    private void initRefreshListener() {
        srlFollow.setOnRefreshListener(() -> {
            // 重新读取本地存储的所有用户数据
            List<User> allUsers = SharedPreferencesUtil.getFollowList(this);
            // 仅保留已关注的用户
            List<User> followedUsers = new ArrayList<>();
            for (User user : allUsers) {
                if (user.isFollowed()) {
                    followedUsers.add(user);
                }
            }
            // 过滤后的结果重新保存到本地存储
            SharedPreferencesUtil.saveFollowList(this, followedUsers);
            // 更新内存列表,仅显示已关注用户
            followList.clear();
            followList.addAll(followedUsers);
            // 更新关注人数统计
            tvFollowCount.setText(String.format("我的关注(%d人)", followList.size()));
            // 通知适配器更新列表
            adapter.updateData(followList);
            // 停止刷新动画
            srlFollow.setRefreshing(false);
            Toast.makeText(this, "刷新成功，已移除未关注用户", Toast.LENGTH_SHORT).show();
        });
    }
}