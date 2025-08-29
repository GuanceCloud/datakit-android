package com.ft;


import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.ft.fragment.DashboardFragment;
import com.ft.fragment.FragmentManagerHelper;
import com.ft.fragment.HomeFragment;
import com.ft.fragment.NotificationsFragment;
import com.ft.fragment.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class FragmentActivity extends NameTitleActivity {

    private BottomNavigationView bottomNavigationView;
    private Fragment currentFragment;
    private FragmentManagerHelper fragmentManagerHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);

        initViews();
        setupBottomNavigation();

        // Initial display HomeFragment
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }

    }

    private void initViews() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        fragmentManagerHelper = new FragmentManagerHelper(getSupportFragmentManager(), R.id.main_fragment_container);
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment fragment = null;

            if (item.getItemId() == R.id.navigation_home) {
                fragment = new HomeFragment();
            } else if (item.getItemId() == R.id.navigation_dashboard) {
                fragment = new DashboardFragment();
            } else if (item.getItemId() == R.id.navigation_notifications) {
                fragment = new NotificationsFragment();
            } else if (item.getItemId() == R.id.navigation_profile) {
                fragment = new ProfileFragment();
            }

            if (fragment != null) {
                loadFragment(fragment);
                return true;
            }
            return false;
        });
    }

    private void loadFragment(Fragment fragment) {
        if (currentFragment != fragment) {
            // Use FragmentManagerHelper to manage Fragment
            fragmentManagerHelper.addFragment(fragment, true);
            currentFragment = fragment;

        }
    }

    @Override
    public void onBackPressed() {
        // If current Fragment has child Fragment, return to child Fragment first
        if (currentFragment instanceof DashboardFragment) {
            DashboardFragment dashboardFragment = (DashboardFragment) currentFragment;
            if (dashboardFragment.getChildFragmentManager().getBackStackEntryCount() > 0) {
                dashboardFragment.getChildFragmentManager().popBackStack();
                return;
            }
        }

        if (currentFragment instanceof NotificationsFragment) {
            NotificationsFragment notificationsFragment = (NotificationsFragment) currentFragment;
            if (notificationsFragment.getChildFragmentManager().getBackStackEntryCount() > 0) {
                notificationsFragment.getChildFragmentManager().popBackStack();
                return;
            }
        }

        if (currentFragment instanceof ProfileFragment) {
            ProfileFragment profileFragment = (ProfileFragment) currentFragment;
            if (profileFragment.getParentFragmentManager().getBackStackEntryCount() > 1) {
                profileFragment.getParentFragmentManager().popBackStack();
                return;
            }
        }

        // If there are other Fragments in back stack, return to previous one
        if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
            getSupportFragmentManager().popBackStack();
            // Log fragment tree after pop back stack
        } else {
            super.onBackPressed();
            // Log fragment tree after super.onBackPressed
        }
    }

}
