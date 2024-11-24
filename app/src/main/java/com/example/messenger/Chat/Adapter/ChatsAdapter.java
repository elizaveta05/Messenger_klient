package com.example.messenger.Chat.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.messenger.Model.Chat;
import com.example.messenger.R;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
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
            // Устанавливаем изображение через Picasso
            Picasso.get()
                    .load(chat.getUserImage())
                    .placeholder(R.drawable.icon_user)
                    .error(R.drawable.icon_user)
                    .into(holder.userImage);
        } else {
            // Если ссылки на изображение нет, пробуем загрузить из Firebase Storage
            loadImageFromFirebaseStorage(chat.getUserId(), holder.userImage);
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

    // Метод для загрузки изображения из Firebase Storage
    private void loadImageFromFirebaseStorage(String userId, CircleImageView imageView) {
        if (userId == null || userId.isEmpty()) return;

        StorageReference storageReference = FirebaseStorage.getInstance()
                .getReference("users_profile_image/" + userId + ".jpg");

        storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
            Picasso.get()
                    .load(uri)
                    .placeholder(R.drawable.icon_user)
                    .error(R.drawable.icon_user)
                    .into(imageView);
        }).addOnFailureListener(e -> {
            Log.e("ChatsAdapter", "Failed to load image for userId: " + userId, e);
            imageView.setImageResource(R.drawable.icon_user);
        });
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
