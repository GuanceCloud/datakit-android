package com.ft.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ft.R;

import java.util.ArrayList;
import java.util.List;

public class NotificationsFragment extends Fragment {

    private RecyclerView recyclerView;
    private NotificationsAdapter adapter;
    private Button showDetailButton;

    public NotificationsFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);
        
        recyclerView = view.findViewById(R.id.notifications_recycler);
        showDetailButton = view.findViewById(R.id.show_detail_button);
        
        setupRecyclerView();
        setupButton();
        
        return view;
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        List<NotificationItem> notifications = new ArrayList<>();
        notifications.add(new NotificationItem("System Notification", "Welcome to the app", "2 hours ago"));
        notifications.add(new NotificationItem("Update Reminder", "New version available", "1 day ago"));
        notifications.add(new NotificationItem("Activity Notice", "Limited time offer", "3 days ago"));
        
        adapter = new NotificationsAdapter(notifications);
        recyclerView.setAdapter(adapter);
    }

    private void setupButton() {
        showDetailButton.setOnClickListener(v -> {
            // Show nested DetailFragment
            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
            transaction.replace(R.id.notifications_detail_container, new NotificationDetailFragment());
            transaction.addToBackStack(null);
            transaction.commit();
        });
    }

    // Notification item data class
    public static class NotificationItem {
        private String title;
        private String content;
        private String time;

        public NotificationItem(String title, String content, String time) {
            this.title = title;
            this.content = content;
            this.time = time;
        }

        public String getTitle() { return title; }
        public String getContent() { return content; }
        public String getTime() { return time; }
    }

    // Notifications adapter
    private static class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.ViewHolder> {
        
        private List<NotificationItem> notifications;

        public NotificationsAdapter(List<NotificationItem> notifications) {
            this.notifications = notifications;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_2, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            NotificationItem item = notifications.get(position);
            holder.text1.setText(item.getTitle());
            holder.text2.setText(item.getContent() + " - " + item.getTime());
        }

        @Override
        public int getItemCount() {
            return notifications.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            android.widget.TextView text1;
            android.widget.TextView text2;

            ViewHolder(View itemView) {
                super(itemView);
                text1 = itemView.findViewById(android.R.id.text1);
                text2 = itemView.findViewById(android.R.id.text2);
            }
        }
    }

    // Nested DetailFragment
    public static class NotificationDetailFragment extends Fragment {
        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_notification_detail, container, false);
            
            Button backButton = view.findViewById(R.id.back_button);
            backButton.setOnClickListener(v -> {
                getParentFragmentManager().popBackStack();
            });
            
            return view;
        }
    }
}
