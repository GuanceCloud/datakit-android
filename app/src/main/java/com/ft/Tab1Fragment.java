package com.ft;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.ft.sdk.FTAutoTrack;
import com.ft.sdk.garble.utils.LogUtils;


public class Tab1Fragment extends Fragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tab1, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.button_jump).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(),Main2Activity.class));
            }
        });
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        LogUtils.d("Fragment[\nhidden="+hidden+"=====>fragment:"+getClass().getSimpleName());
        super.onHiddenChanged(hidden);
    }

    @Override
    public void onResume() {
        LogUtils.d("Fragment[\nonResume=====>fragment:"+getClass().getSimpleName());
        super.onResume();
    }

    @Override
    public void onPause() {
        LogUtils.d("Fragment[\nonPause=====>fragment:"+getClass().getSimpleName());
        super.onPause();
    }
}