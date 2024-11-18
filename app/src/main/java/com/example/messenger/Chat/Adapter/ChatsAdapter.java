package com.example.messenger.Chat.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.messenger.Model.Chat;
import com.example.messenger.R;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.ViewHolder> {
    private List<Chat> chatList;
    private LayoutInflater mInflater;
    private OnUserClickListener onUserClickListener;

    public ChatsAdapter(Context context, List<Chat> data, OnUserClickListener onUserClickListener) {
        this.mInflater = LayoutInflater.from(context);
        this.chatList = data;
        this.onUserClickListener = onUserClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.all_chats_adapter, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Chat chat = chatList.get(position);

        holder.userName.setText(chat.getUserLogin());

        holder.userMessage.setText(chat.getLastMessage());

        if (chat.getUserImage() != null) {
            Picasso.get()
                    .load(chat.getUserImage())
                    .placeholder(R.drawable.icon_user)
                    .error(R.drawable.icon_user)
                    .into(holder.userImage);
        } else {
            holder.userImage.setImageResource(R.drawable.icon_user);
        }

        holder.itemView.setOnClickListener(v -> {
            if (onUserClickListener != null) {
                onUserClickListener.onUserClick(chat);
            }
        });
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView userName, userMessage;
        CircleImageView userImage;

        public ViewHolder(View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.tv_name_user);
            userImage = itemView.findViewById(R.id.image_photo_user);
            userMessage = itemView.findViewById(R.id.tv_message);
        }
    }

    public interface OnUserClickListener {
        void onUserClick(Chat chat);
    }
}