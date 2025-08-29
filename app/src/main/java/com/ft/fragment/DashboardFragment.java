package com.ft.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;


import com.ft.R;
// import com.google.android.material.tabs.TabLayout;

public class DashboardFragment extends Fragment {

    private ViewPager2 viewPager;
    // private TabLayout tabLayout;
    private DashboardPagerAdapter pagerAdapter;

    public DashboardFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        
        viewPager = view.findViewById(R.id.dashboard_viewpager);
        // tabLayout = view.findViewById(R.id.dashboard_tabs);
        
        pagerAdapter = new DashboardPagerAdapter(getChildFragmentManager(), getLifecycle());
        viewPager.setAdapter(pagerAdapter);
        
        // Temporarily disable TabLayout functionality to avoid crashes
        // TabLayout will be re-enabled once compatibility issues are resolved
        
        // Set up ViewPager2 with page change callback for debugging
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                // Log page changes for debugging
                String[] pageNames = {"Statistics", "Chart", "Data"};
                if (position < pageNames.length) {
                    // Log.d("DashboardFragment", "Page selected: " + pageNames[position]);
                }
            }
        });
        
        return view;
    }

    private static class DashboardPagerAdapter extends FragmentStateAdapter {
        
        public DashboardPagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
            super(fragmentManager, lifecycle);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new StatsFragment();
                case 1:
                    return new ChartFragment();
                case 2:
                    return new DataFragment();
                default:
                    return new StatsFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 3;
        }
    }

    // Statistics Fragment
    public static class StatsFragment extends Fragment {
        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_stats, container, false);
        }
    }

    // Chart Fragment
    public static class ChartFragment extends Fragment {
        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_chart, container, false);
        }
    }

    // Data Fragment
    public static class DataFragment extends Fragment {
        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_data, container, false);
        }
    }
}
