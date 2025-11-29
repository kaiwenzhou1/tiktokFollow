//package com.example.tiktokfollow.util;
//
//import android.content.Context;
//import android.content.SharedPreferences;
//import com.example.tiktokfollow.model.User;
//import com.google.gson.Gson;
//import com.google.gson.reflect.TypeToken;
//import java.util.ArrayList;
//import java.util.List;
//
//public class SharedPreferencesUtil {
//    private static final String SP_NAME = "TikTokFollowPrefs";
//    private static final String KEY_FOLLOW_LIST = "follow_user_list";
//    private static SharedPreferences sp;
//    private static Gson gson = new Gson();
//
//    // 初始化SP
//    private static void init(Context context) {
//        if (sp == null) {
//            sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
//        }
//    }
//
//    // 保存关注列表
//    public static void saveFollowList(Context context, List<User> userList) {
//        init(context);
//        String json = gson.toJson(userList);
//        sp.edit().putString(KEY_FOLLOW_LIST, json).apply();
//    }
//
//    // 获取关注列表
//    public static List<User> getFollowList(Context context) {
//        init(context);
//        String json = sp.getString(KEY_FOLLOW_LIST, "");
//        if (json.isEmpty()) {
//            return generateDefaultData();  // 无数据生成默认关注列表
//        }
//        return gson.fromJson(json, new TypeToken<ArrayList<User>>() {}.getType());
//    }
//
//    // 无数据时生成默认关注数据
//    private static List<User> generateDefaultData() {
//        List<User> userList = new ArrayList<>();
//        for (int i = 1; i <= 150; i++) {
//            userList.add(new User(
//                    "user" + i,
//                    "关注用户" + i,
//                    "avatar" + (i % 15 + 1)
//            ));
//        }
//        return userList;
//    }
//}