package com.example.tiktokfollow.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewStub;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.tiktokfollow.R;
import com.example.tiktokfollow.adapter.FollowListAdapter;
import com.example.tiktokfollow.data.FollowRemoteDataSource;
import com.example.tiktokfollow.model.User;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class FollowActivity extends AppCompatActivity {
    // 模拟服务端每次返回10条数据
    private static final int PAGE_SIZE = 10;

    private TabLayout tabLayout;
    private LinearLayout layoutFollow;
    private TextView tvFollowCount;
    private RecyclerView rvFollowList;
    private SwipeRefreshLayout srlFollow;

    private FollowListAdapter adapter;
    private List<User> followList;

    private ViewStub stubMutualFollow;
    private ViewStub stubFans;
    private ViewStub stubFriends;
    private View layoutMutualFollow;
    private View layoutFans;
    private View layoutFriends;

    // 分页管理
    private int currentPage = 1;
    private boolean isLoading = false;
    // 是否还有更多数据
    private boolean hasMore = true;
    // 服务端总关注数, 初始化模拟为1200条
    private int totalCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow);

        // 初始化模拟服务端关注数据
        FollowRemoteDataSource.init(getApplicationContext());

        initView();
        initData();
        initTabListener();
        initFollowAdapter();
        initRefreshListener();
    }

    private void initView() {
        tabLayout = findViewById(R.id.tab_layout);
        layoutFollow = findViewById(R.id.layout_follow);
        tvFollowCount = findViewById(R.id.tv_follow_count);
        rvFollowList = findViewById(R.id.rv_follow_list);
        srlFollow = findViewById(R.id.srl_follow);

        rvFollowList.setLayoutManager(new LinearLayoutManager(this));
        rvFollowList.setHasFixedSize(true);
        rvFollowList.setItemViewCacheSize(20);

        // 关闭item内容变更动画，提升列表滑动流畅度, 减少卡顿
        if (rvFollowList.getItemAnimator() instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) rvFollowList.getItemAnimator())
                    .setSupportsChangeAnimations(false);
        }

        stubMutualFollow = findViewById(R.id.stub_mutual_follow);
        stubFans = findViewById(R.id.stub_fans);
        stubFriends = findViewById(R.id.stub_friends);

        srlFollow.setColorSchemeResources(
                R.color.red_follow,
                R.color.colorPrimary,
                R.color.special_follow_bg
        );

        // 避免初始页面显示错误
        layoutFollow.setVisibility(View.GONE);
    }

    private void initData() {
        // 不从SharedPreferences本地加载, 模拟远程服务器请求返回
        followList = new ArrayList<>();
    }

    // 顶栏Tab切换
    private void initTabListener() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                hideAllLayouts();

                switch (position) {
                    case 0:
                        showMutualFollowLayout();
                        break;
                    case 1:
                        showFollowLayout();
                        break;
                    case 2:
                        showFansLayout();
                        break;
                    case 3:
                        showFriendsLayout();
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });
    }

    private void hideAllLayouts() {
        if (layoutFollow != null) layoutFollow.setVisibility(View.GONE);
        if (layoutMutualFollow != null) layoutMutualFollow.setVisibility(View.GONE);
        if (layoutFans != null) layoutFans.setVisibility(View.GONE);
        if (layoutFriends != null) layoutFriends.setVisibility(View.GONE);
    }

    // 显示关注栏, 分页从服务端拉取请求
    private void showFollowLayout() {
        if (layoutFollow != null) {
            layoutFollow.setVisibility(View.VISIBLE);
        }

        // 本地为空，说明第一次进入该页，拉第一页
        if (followList.isEmpty()) {
            loadFirstPage();
        } else {
            tvFollowCount.setText(String.format("我的关注(%d人)", totalCount));
        }
    }

    private void showMutualFollowLayout() {
        if (layoutMutualFollow == null && stubMutualFollow != null) {
            layoutMutualFollow = stubMutualFollow.inflate();
            TextView tvMutualCount = layoutMutualFollow.findViewById(R.id.tv_mutual_follow_count);
            tvMutualCount.setText(getString(R.string.my_mutual_follow));
        }
        if (layoutMutualFollow != null) {
            layoutMutualFollow.setVisibility(View.VISIBLE);
        }
    }

    private void showFansLayout() {
        if (layoutFans == null && stubFans != null) {
            layoutFans = stubFans.inflate();
            TextView tvFansCount = layoutFans.findViewById(R.id.tv_fans_count);
            tvFansCount.setText(getString(R.string.my_fans));
        }
        if (layoutFans != null) {
            layoutFans.setVisibility(View.VISIBLE);
        }
    }

    private void showFriendsLayout() {
        if (layoutFriends == null && stubFriends != null) {
            layoutFriends = stubFriends.inflate();
            TextView tvFriendsCount = layoutFriends.findViewById(R.id.tv_friends_count);
            tvFriendsCount.setText(getString(R.string.my_friends));
        }
        if (layoutFriends != null) {
            layoutFriends.setVisibility(View.VISIBLE);
        }
    }

    // 初始化关注列表适配器, 上拉加载
    private void initFollowAdapter() {
        adapter = new FollowListAdapter(this, followList);

        // 关注/取关
        adapter.setOnFollowBtnClickListener((position, user) -> {
            if (user.isFollowed()) {
                showUnfollowDialog(position, user);
            } else {
                user.setFollowed(true);
                updateUser(position, user);
            }
        });

        // 更多按钮点击监听
        adapter.setOnMoreBtnClickListener((position, user) -> {
            showBottomSheetDialog(position, user);
        });

        rvFollowList.setAdapter(adapter);

        // 上拉加载更多数据
        rvFollowList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy <= 0) return;

                LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (lm == null) return;

                int lastVisible = lm.findLastVisibleItemPosition();
                int totalItemCount = lm.getItemCount();

                if (!isLoading && hasMore && lastVisible >= totalItemCount - 3) {
                    loadNextPage();
                }
            }
        });
    }

    // 取消关注提示弹窗
    private void showUnfollowDialog(int position, User user) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_unfollow)
                .setPositiveButton(R.string.confirm, (dialog, which) -> {
                    user.setFollowed(false);
                    user.setSpecialFollow(false);
                    updateUser(position, user);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    // 显示底部弹窗, 用于用户更多操作, 备注、特别关注、取消关注等
    private void showBottomSheetDialog(int position, User user) {
        // 创建BottomSheetDialog实例
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        // 加载弹窗布局文件
        View view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_setting, null);
        dialog.setContentView(view);

        // 初始化绑定各个控件
        TextView tvUserInfo = view.findViewById(R.id.tv_user_info);
        androidx.appcompat.widget.SwitchCompat switchSpecial =
                view.findViewById(R.id.switch_special_follow);
        TextView tvSetRemark = view.findViewById(R.id.tv_set_remark);
        TextView tvUnfollow = view.findViewById(R.id.tv_unfollow);

        // 构造显示的用户信息
        String userInfo = user.getRemark().isEmpty()
                ? user.getNickname()
                : user.getRemark() + "（" + user.getNickname() + "）";
        tvUserInfo.setText(userInfo);

        // 设置特别关注开关状态, 仅当用户已关注时，才允许开启特别关注
        switchSpecial.setChecked(user.isSpecialFollow() && user.isFollowed());
        switchSpecial.setEnabled(user.isFollowed());

        // 监听特别关注开关变化
        switchSpecial.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (user.isFollowed()) {
                user.setSpecialFollow(isChecked);
                updateUser(position, user);
            } else {
                switchSpecial.setChecked(false);
                Toast.makeText(FollowActivity.this, "请先关注该用户", Toast.LENGTH_SHORT).show();
            }
        });

        // 点击设置备注, 弹出备注输入框，并关闭当前弹窗
        tvSetRemark.setOnClickListener(v -> {
            showSetRemarkDialog(position, user);
            dialog.dismiss();
        });

        // 点击取消关注, 弹出确认对话框，并关闭当前弹窗
        tvUnfollow.setOnClickListener(v -> {
            showUnfollowDialog(position, user);
            dialog.dismiss();
        });

        dialog.show();
    }

    // 弹出对话框为指定用户设置备注名
    private void showSetRemarkDialog(int position, User user) {
        EditText editText = new EditText(this); // 创建输入框
        editText.setHint(R.string.input_remark);
        editText.setText(user.getRemark());
        // 构建并显示备注设置对话框
        new AlertDialog.Builder(this)
                .setTitle(R.string.set_remark)
                .setView(editText)
                .setPositiveButton(R.string.confirm, (dialog, which) -> {
                    // 点击确认：获取输入内容，更新用户备注，并同步到列表和服务器
                    String remark = editText.getText().toString().trim();
                    user.setRemark(remark);
                    updateUser(position, user);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    // 更新用户的统一入口, 关注/取关/备注/特别关注，同时更新服务端和当前列表数据
    private void updateUser(int position, User user) {
        FollowRemoteDataSource.updateUser(user, new FollowRemoteDataSource.UpdateCallback() {
            @Override
            public void onSuccess(User updatedUser) {
                // 更新当前页面上的数据
                if (position >= 0 && position < followList.size()) {
                    followList.set(position, updatedUser);
                    adapter.notifyItemChanged(position);
                }
            }

            @Override
            public void onError(Throwable t) {
                Toast.makeText(FollowActivity.this,
                        "操作失败，请稍后重试", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 下拉刷新, 重新请求第一页
    private void initRefreshListener() {
        srlFollow.setOnRefreshListener(this::loadFirstPage);
    }

    private void loadFirstPage() {
        currentPage = 1;
        hasMore = true;
        requestPage(currentPage, true);
    }

    private void loadNextPage() {
        if (!hasMore) return;
        requestPage(currentPage + 1, false);
    }

    private void requestPage(int page, boolean isRefresh) {
        if (isLoading) return;
        isLoading = true;

        if (isRefresh && !srlFollow.isRefreshing()) {
            srlFollow.setRefreshing(true);
        }

        // 调用模拟服务端数据源，分页加载关注列表
        FollowRemoteDataSource.getFollowUsers(page, PAGE_SIZE,
                new FollowRemoteDataSource.PageCallback() {
                    @Override
                    public void onSuccess(List<User> users, int total, boolean more) {
                        // 标记加载结束，关闭加载状态
                        isLoading = false;
                        // 关闭下拉刷新动画
                        srlFollow.setRefreshing(false);

                        // 保存分页信息
                        totalCount = total;
                        hasMore = more;
                        currentPage = page;

                        // 下拉刷新, 清空旧数据，加入新数据
                        if (isRefresh) {
                            followList.clear();
                            followList.addAll(users);
                            adapter.notifyDataSetChanged();
                        } else {
                            //上拉加载更多, 末尾追加新数据
                            int start = followList.size();
                            followList.addAll(users);
                            adapter.notifyItemRangeInserted(start, users.size());
                        }

                        tvFollowCount.setText(String.format("我的关注(%d人)", totalCount));
                    }

                    @Override
                    public void onError(Throwable t) {
                        // 加载失败, 关闭加载状态，提示用户
                        isLoading = false;
                        srlFollow.setRefreshing(false);
                        Toast.makeText(FollowActivity.this,
                                "加载失败：" + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 分页数据由内存维护，刷新时不再每次从服务端重新读取
    }
}