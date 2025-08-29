package com.ft.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.ft.R;

public class ProfileFragment extends Fragment {

    private TextView userNameText;
    private TextView userEmailText;
    private Button editProfileButton;
    private Button settingsButton;
    private Button logoutButton;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        
        initViews(view);
        setupUserInfo();
        setupClickListeners();
        
        return view;
    }

    private void initViews(View view) {
        userNameText = view.findViewById(R.id.user_name_text);
        userEmailText = view.findViewById(R.id.user_email_text);
        editProfileButton = view.findViewById(R.id.edit_profile_button);
        settingsButton = view.findViewById(R.id.settings_button);
        logoutButton = view.findViewById(R.id.logout_button);
    }

    private void setupUserInfo() {
        // Simulate user data
        userNameText.setText("John Doe");
        userEmailText.setText("johndoe@example.com");
    }

    private void setupClickListeners() {
        editProfileButton.setOnClickListener(v -> {
            // Navigate to Edit Profile Fragment
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.main_fragment_container, new EditProfileFragment());
            transaction.addToBackStack(null);
            transaction.commit();
        });

        settingsButton.setOnClickListener(v -> {
            // Navigate to Settings Fragment
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.main_fragment_container, new SettingsFragment());
            transaction.addToBackStack(null);
            transaction.commit();
        });

        logoutButton.setOnClickListener(v -> {
            // Simulate logout operation
            if (getActivity() != null) {
                getActivity().finish();
            }
        });
    }

    // Edit Profile Fragment
    public static class EditProfileFragment extends Fragment {
        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);
            
            Button saveButton = view.findViewById(R.id.save_button);
            Button cancelButton = view.findViewById(R.id.cancel_button);
            
            saveButton.setOnClickListener(v -> {
                // Save profile logic
                getParentFragmentManager().popBackStack();
            });
            
            cancelButton.setOnClickListener(v -> {
                getParentFragmentManager().popBackStack();
            });
            
            return view;
        }
    }

    // Settings Fragment
    public static class SettingsFragment extends Fragment {
        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_settings, container, false);
            
            Button backButton = view.findViewById(R.id.back_button);
            backButton.setOnClickListener(v -> {
                getParentFragmentManager().popBackStack();
            });
            
            return view;
        }
    }
}
