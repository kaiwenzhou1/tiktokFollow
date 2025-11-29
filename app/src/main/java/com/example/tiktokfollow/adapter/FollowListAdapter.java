package com.example.tiktokfollow.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.tiktokfollow.R;
import com.example.tiktokfollow.model.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FollowListAdapter extends RecyclerView.Adapter<FollowListAdapter.FollowViewHolder> {
    private final Context context;
    private final List<User> userList;
    private OnFollowBtnClickListener followBtnClickListener;
    private OnMoreBtnClickListener moreBtnClickListener;

    // 缓存avatar头像到真正的resId，避免滑动时频繁getIdentifier导致卡顿
    private static final Map<String, Integer> AVATAR_RES_CACHE = new HashMap<>();

    public FollowListAdapter(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
        setHasStableIds(true);  // 稳定ID，有利于复用、提升流畅度
    }

    static class FollowViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView tvUsername;
        TextView tvSpecialFollow;
        Button btnFollow;
        ImageView ivMore;

        public FollowViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
            tvUsername = itemView.findViewById(R.id.tv_username);
            tvSpecialFollow = itemView.findViewById(R.id.tv_special_follow);
            btnFollow = itemView.findViewById(R.id.btn_follow);
            ivMore = itemView.findViewById(R.id.iv_more);
        }
    }

    @NonNull
    @Override
    public FollowViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_follow_user, parent, false);
        return new FollowViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FollowViewHolder holder, int position) {
        User user = userList.get(position);

        int avatarResId = getAvatarResId(user.getAvatarResId());

        // 使用Glide异步加载, 并缓存头像，保证快速显示, 滑动流畅
        Glide.with(context)
                .load(avatarResId)
                .placeholder(R.drawable.avatar1) // 适用默认头像
                .error(R.drawable.avatar1)
                .into(holder.ivAvatar);

        holder.tvUsername.setText(user.getShowName());

        if (user.isSpecialFollow() && user.isFollowed()) {
            holder.tvSpecialFollow.setVisibility(View.VISIBLE);
        } else {
            holder.tvSpecialFollow.setVisibility(View.GONE);
        }

        // 点击头像生成吐司
        holder.ivAvatar.setOnClickListener(v -> {
            String toastText = String.format("已选中-%s", user.getShowName());
            Toast.makeText(context, toastText, Toast.LENGTH_SHORT).show();
        });

        // 根据当前用户是否已关注, 显示关注按钮
        if (user.isFollowed()) {
            holder.btnFollow.setText(R.string.followed);
            holder.btnFollow.setBackgroundColor(
                    context.getResources().getColor(R.color.gray_unfollowed));
            holder.btnFollow.setTextColor(
                    context.getResources().getColor(R.color.white));
        } else {
            holder.btnFollow.setText(R.string.to_follow);
            holder.btnFollow.setBackgroundColor(
                    context.getResources().getColor(R.color.red_follow));
            holder.btnFollow.setTextColor(
                    context.getResources().getColor(R.color.white));
        }

        // 为btnFollow设置点击监听器
        holder.btnFollow.setOnClickListener(v -> {
            if (followBtnClickListener != null) {
                int pos = holder.getBindingAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    followBtnClickListener.onClick(pos, userList.get(pos));
                }
            }
        });

        // 只有关注用户，才能对他进行更多操作
        if (user.isFollowed()) {
            holder.ivMore.setEnabled(true);
            holder.ivMore.setAlpha(1.0f);
        } else {
            holder.ivMore.setEnabled(false);
            holder.ivMore.setAlpha(0.5f);
        }

        // 点击更多按钮弹出底部弹窗
        holder.ivMore.setOnClickListener(v -> {
            if (moreBtnClickListener != null) {
                int pos = holder.getBindingAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    moreBtnClickListener.onClick(pos, userList.get(pos));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList == null ? 0 : userList.size();
    }

    @Override
    public long getItemId(int position) {
        // 用userId生成稳定id, 便于复用, 提升流畅度
        return userList.get(position).getUserId().hashCode();
    }

    private int getAvatarResId(String resName) {
        if (resName == null || resName.isEmpty()) return 0;
        Integer cached = AVATAR_RES_CACHE.get(resName);
        if (cached != null) return cached;

        int resId = context.getResources()
                .getIdentifier(resName, "drawable", context.getPackageName());
        AVATAR_RES_CACHE.put(resName, resId);
        return resId;
    }

    // 回调接口
    public interface OnFollowBtnClickListener {
        void onClick(int position, User user);
    }

    public void setOnFollowBtnClickListener(OnFollowBtnClickListener listener) {
        this.followBtnClickListener = listener;
    }

    public interface OnMoreBtnClickListener {
        void onClick(int position, User user);
    }

    public void setOnMoreBtnClickListener(OnMoreBtnClickListener listener) {
        this.moreBtnClickListener = listener;
    }
}