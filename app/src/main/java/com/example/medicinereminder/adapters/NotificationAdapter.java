package com.example.medicinereminder.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.medicinereminder.R;
import com.example.medicinereminder.models.NotificationItem;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {
    private List<NotificationItem> notifications = new ArrayList<>();
    private OnNotificationClickListener clickListener;

    public interface OnNotificationClickListener {
        void onNotificationClick(NotificationItem notification);
    }

    public NotificationAdapter(OnNotificationClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void updateNotifications(List<NotificationItem> notifications) {
        this.notifications = notifications;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        holder.bind(notifications.get(position));
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    class NotificationViewHolder extends RecyclerView.ViewHolder {
        private ImageView notificationIcon;
        private TextView titleText;
        private TextView messageText;
        private TextView timeText;
        private View unreadIndicator;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            notificationIcon = itemView.findViewById(R.id.notificationIcon);
            titleText = itemView.findViewById(R.id.titleText);
            messageText = itemView.findViewById(R.id.messageText);
            timeText = itemView.findViewById(R.id.timeText);
            unreadIndicator = itemView.findViewById(R.id.unreadIndicator);
        }

        public void bind(NotificationItem notification) {
            titleText.setText(notification.getTitle());
            messageText.setText(notification.getMessage());

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            timeText.setText(sdf.format(notification.getCreatedAt()));

            // Set icon based on type
            switch (notification.getType()) {
                case "missed_dose":
                    notificationIcon.setImageResource(R.drawable.ic_warning);
                    break;
                default:
                    notificationIcon.setImageResource(R.drawable.ic_notifications);
                    break;
            }

            // Show/hide unread indicator
            unreadIndicator.setVisibility(notification.isRead() ? View.GONE : View.VISIBLE);

            itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onNotificationClick(notification);
                }
            });
        }
    }
}