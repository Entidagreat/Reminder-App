package com.example.medicinereminder;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.medicinereminder.adapters.NotificationAdapter;
import com.example.medicinereminder.models.NotificationItem;
import com.example.medicinereminder.utils.DatabaseHelper;
import java.util.List;

public class NotificationCenterActivity extends AppCompatActivity {
    private RecyclerView notificationRecycler;
    private TextView noNotificationsText;
    private NotificationAdapter adapter;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_center);

        initViews();
        setupRecyclerView();
        loadNotifications();
    }

    private void initViews() {
        notificationRecycler = findViewById(R.id.notificationRecycler);
        noNotificationsText = findViewById(R.id.noNotificationsText);
        dbHelper = new DatabaseHelper(this);

        findViewById(R.id.backButton).setOnClickListener(v -> finish());
        findViewById(R.id.clearAllButton).setOnClickListener(v -> clearAllNotifications());
    }

    private void setupRecyclerView() {
        adapter = new NotificationAdapter(this::onNotificationClick);
        notificationRecycler.setLayoutManager(new LinearLayoutManager(this));
        notificationRecycler.setAdapter(adapter);
    }

    private void loadNotifications() {
        List<NotificationItem> notifications = dbHelper.getAllNotifications();
        if (notifications.isEmpty()) {
            noNotificationsText.setVisibility(View.VISIBLE);
            notificationRecycler.setVisibility(View.GONE);
        } else {
            noNotificationsText.setVisibility(View.GONE);
            notificationRecycler.setVisibility(View.VISIBLE);
            adapter.updateNotifications(notifications);
        }
    }

    private void onNotificationClick(NotificationItem notification) {
        // Mark as read
        dbHelper.markNotificationAsRead(notification.getId());
        loadNotifications();

        // Gửi broadcast để cập nhật chấm đỏ
        Intent broadcastIntent = new Intent("com.example.medicinereminder.NOTIFICATION_ADDED");
        sendBroadcast(broadcastIntent);
    }

    private void clearAllNotifications() {
        dbHelper.clearAllNotifications();
        loadNotifications();

        // Gửi broadcast để cập nhật chấm đỏ
        Intent broadcastIntent = new Intent("com.example.medicinereminder.NOTIFICATION_ADDED");
        sendBroadcast(broadcastIntent);
    }
}