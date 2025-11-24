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
import com.example.tiktokfollow.R;
import com.example.tiktokfollow.model.User;
import java.util.List;

public class FollowListAdapter extends RecyclerView.Adapter<FollowListAdapter.FollowViewHolder> {
    private Context context;
    private List<User> userList;
    private OnFollowBtnClickListener followBtnClickListener;
    private OnMoreBtnClickListener moreBtnClickListener;
    public FollowListAdapter(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
    }

    // 创建ViewHolder
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
        View view = LayoutInflater.from(context).inflate(R.layout.item_follow_user, parent, false);
        return new FollowViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FollowViewHolder holder, int position) {
        User user = userList.get(position);

        // 设置头像
        int avatarResId = context.getResources().getIdentifier(
                user.getAvatarResId(), "drawable", context.getPackageName()
        );
        holder.ivAvatar.setImageResource(avatarResId);

        // 设置用户名
        holder.tvUsername.setText(user.getShowName());

        // 设置特别关注标识
        if (user.isSpecialFollow() && user.isFollowed()) {
            holder.tvSpecialFollow.setVisibility(View.VISIBLE);
        } else {
            holder.tvSpecialFollow.setVisibility(View.GONE);
        }

        // 头像点击消息提示
        holder.ivAvatar.setOnClickListener(v -> {
            String toastText = String.format("已选中-%s", user.getShowName());
            Toast.makeText(context, toastText, Toast.LENGTH_SHORT).show();
        });

        // 设置关注按钮状态
        if (user.isFollowed()) {
            holder.btnFollow.setText(R.string.followed);
            holder.btnFollow.setBackgroundColor(context.getResources().getColor(R.color.gray_unfollowed));
            holder.btnFollow.setTextColor(context.getResources().getColor(R.color.white));
        } else {
            holder.btnFollow.setText(R.string.to_follow);
            holder.btnFollow.setBackgroundColor(context.getResources().getColor(R.color.red_follow));
            holder.btnFollow.setTextColor(context.getResources().getColor(R.color.white));
        }

        // 关注按钮点击
        holder.btnFollow.setOnClickListener(v -> {
            if (followBtnClickListener != null) {
                followBtnClickListener.onClick(position, user);
            }
        });

        // 更多操作点击
        holder.ivMore.setOnClickListener(v -> {
            if (moreBtnClickListener != null) {
                moreBtnClickListener.onClick(position, user);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public void updateData(List<User> newUserList) {
        this.userList = newUserList;
        notifyDataSetChanged();
    }

    // 关注按钮点击回调
    public interface OnFollowBtnClickListener {
        void onClick(int position, User user);
    }

    public void setOnFollowBtnClickListener(OnFollowBtnClickListener listener) {
        this.followBtnClickListener = listener;
    }

    // 更多设置按钮点击
    public interface OnMoreBtnClickListener {
        void onClick(int position, User user);
    }

    // 更多设置监听
    public void setOnMoreBtnClickListener(OnMoreBtnClickListener listener) {
        this.moreBtnClickListener = listener;
    }
}