package com.example.messenger;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.messenger.Model.Users;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private Context context;
    private ArrayList<Users> userList;
    private OnUserClickListener onUserClickListener;

    public UserAdapter(Context context, ArrayList<Users> userList, OnUserClickListener onUserClickListener) {
        this.context = context;
        this.userList = userList;
        this.onUserClickListener = onUserClickListener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.all_chats_adapter, parent, false);
        return new UserViewHolder(itemView);
    }

    public void setUserList(ArrayList<Users> user) {
        userList = user;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        Users user = userList.get(position);

        holder.tvName.setText(user.getLogin());

        // Загрузка изображения пользователя
        if (user.getImage_url() != null && !user.getImage_url().isEmpty()) {
            // Если URL изображения пользователя доступен
            Picasso.get()
                    .load(user.getImage_url())
                    .placeholder(R.drawable.icon_user)
                    .error(R.drawable.icon_user)
                    .into(holder.image_photo_user);
        } else {
            // Если изображения нет, пытаемся загрузить его из Firebase Storage
            loadImageFromFirebaseStorage(user.getUserId(), holder.image_photo_user);
        }

        holder.itemView.setOnClickListener(v -> {
            if (onUserClickListener != null) {
                onUserClickListener.onUserClick(user);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
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
            Log.e("UserAdapter", "Failed to load image for userId: " + userId, e);
            imageView.setImageResource(R.drawable.icon_user);
        });
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        CircleImageView image_photo_user;

        public UserViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name_user);
            image_photo_user = itemView.findViewById(R.id.image_photo_user);
        }
    }

    public interface OnUserClickListener {
        void onUserClick(Users user);
    }
}
