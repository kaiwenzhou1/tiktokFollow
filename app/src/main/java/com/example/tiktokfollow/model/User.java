package com.example.tiktokfollow.model;

import java.io.Serializable;

public class User implements Serializable {
    private String userId;
    private String nickname;       // 昵称
    private String remark;         // 备注
    private String avatarResId;    // 头像ID
    private boolean isFollowed;    // 是否已关注
    private boolean isSpecialFollow;  // 是否特别关注

    // 构造方法
    public User(String userId, String nickname, String avatarResId) {
        this.userId = userId;
        this.nickname = nickname;
        this.avatarResId = avatarResId;
        this.isFollowed = true;    // 初始默认关注
        this.isSpecialFollow = false;
        this.remark = "";
    }

    // getter, setter
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getShowName() {  // 先显示备注，无备注则显示昵称
        return remark.isEmpty() ? nickname : remark;
    }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public String getAvatarResId() { return avatarResId; }
    public void setAvatarResId(String avatarResId) { this.avatarResId = avatarResId; }

    public boolean isFollowed() { return isFollowed; }
    public void setFollowed(boolean followed) { isFollowed = followed; }

    public boolean isSpecialFollow() { return isSpecialFollow; }
    public void setSpecialFollow(boolean specialFollow) { isSpecialFollow = specialFollow; }
}