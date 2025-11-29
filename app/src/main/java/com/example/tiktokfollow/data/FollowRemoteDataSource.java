package com.example.tiktokfollow.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import com.example.tiktokfollow.model.User;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 模拟服务端：
 * - 首次启动时生成1200个用户并保存到本地SharedPreferences
 * - 之后从本地加载，模拟服务器交互的分页获取, 并能更新关注用户
 * - 所有接口都使用异步回调，内部用线程池+Handler模拟网络
 */
public class FollowRemoteDataSource {
    // 初始化关注用户数据模拟服务端关注数
    private static final int INITIAL_TOTAL = 1200;
    private static final int AVATAR_COUNT = 15;

    private static final String SP_NAME = "FakeFollowServer";
    private static final String KEY_SERVER_USERS = "server_user_list";

    // 创建一个单线程的后台线程池
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    // 创建一个向UI主线程发送消息的Handler
    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());
    private static final Gson gson = new Gson();

    // 模拟服务端完整用户表(包含已取关的，标记isFollowed=false)
    private static List<User> ALL_USERS = new ArrayList<>();
    private static boolean initialized = false;
    private static SharedPreferences sp;

    // 首次安装APP关注数据初始化：仅初次调用一次
    public static void init(Context context) {
        if (initialized) return;

        sp = context.getApplicationContext()
                .getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);

        String json = sp.getString(KEY_SERVER_USERS, "");
        if (json.isEmpty()) {
            // 首次安装, 生成1200条默认关注用户
            for (int i = 1; i <= INITIAL_TOTAL; i++) {
                String userId = "user" + i;
                String nickname = "关注用户" + i;
                // 头像循环使用avatar1~avatar15
                String avatarResId = "avatar" + ((i - 1) % AVATAR_COUNT + 1);
                ALL_USERS.add(new User(userId, nickname, avatarResId));
            }
            saveAllUsers();
        } else {
            Type type = new TypeToken<ArrayList<User>>() {}.getType();
            List<User> list = gson.fromJson(json, type);
            if (list != null) {
                ALL_USERS = list;
            }
        }
        initialized = true;
    }

    private static void checkInit() {
        // 检查是否初始化过
        if (!initialized) {
            throw new IllegalStateException(
                    "FollowRemoteDataSource还未初始化...");
        }
    }

    // 将关注数据保存到本地, apply异步提交更改提高效率
    private static void saveAllUsers() {
        if (sp == null) return;
        String json = gson.toJson(ALL_USERS);
        sp.edit().putString(KEY_SERVER_USERS, json).apply();
    }

    // Mock回调, 异步操作结果通知
    public interface PageCallback {
        void onSuccess(List<User> users, int totalCount, boolean hasMore);
        void onError(Throwable t);
    }
    public interface UpdateCallback {
        void onSuccess(User updatedUser);
        void onError(Throwable t);
    }

    // 分页获取关注列表(返回isFollowed = true的用户)
    public static void getFollowUsers(int page, int pageSize, PageCallback callback) {
        checkInit();

        EXECUTOR.execute(() -> {
            try {
                // 过滤出已关注用户
                List<User> followed = new ArrayList<>();
                for (User u : ALL_USERS) {
                    if (u.isFollowed()) {
                        followed.add(u);
                    }
                }
                int totalCount = followed.size();

                int fromIndex = (page - 1) * pageSize;
                if (fromIndex >= totalCount) {
                    MAIN_HANDLER.post(() ->
                            callback.onSuccess(new ArrayList<>(), totalCount, false));
                    return;
                }

                int toIndex = Math.min(fromIndex + pageSize, totalCount);
                List<User> subList = new ArrayList<>(followed.subList(fromIndex, toIndex));
                boolean hasMore = toIndex < totalCount;

                // 模拟网络延迟
                MAIN_HANDLER.postDelayed(
                        () -> callback.onSuccess(subList, totalCount, hasMore),
                        50
                );
            } catch (Exception e) {
                MAIN_HANDLER.post(() -> callback.onError(e));
            }
        });
    }

    // 更新用户信息, 取关/关注/特别关注/备注等
    public static void updateUser(User user, UpdateCallback callback) {
        checkInit();

        EXECUTOR.execute(() -> {
            try {
                int index = -1;
                for (int i = 0; i < ALL_USERS.size(); i++) {
                    if (ALL_USERS.get(i).getUserId().equals(user.getUserId())) {
                        index = i;
                        break;
                    }
                }
                if (index != -1) {
                    ALL_USERS.set(index, user);
                    saveAllUsers();
                }
                MAIN_HANDLER.post(() -> callback.onSuccess(user));
            } catch (Exception e) {
                MAIN_HANDLER.post(() -> callback.onError(e));
            }
        });
    }
}