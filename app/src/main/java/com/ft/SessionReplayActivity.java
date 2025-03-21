package com.ft;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatCheckedTextView;

import java.util.ArrayList;
import java.util.HashMap;

public class SessionReplayActivity extends NameTitleActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_replay);
        findViewById(R.id.session_replay_dialog).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(SessionReplayActivity.this).setMessage("这是对话框")
                        .setPositiveButton("确定", null).create().show();
            }

        });

        findViewById(R.id.session_replay_origin_toast).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(SessionReplayActivity.this, "Toast:" + System.currentTimeMillis(), Toast.LENGTH_LONG).show();

            }
        });
        findViewById(R.id.session_replay_custom_toast).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomToast.showToast(SessionReplayActivity.this, "Toast:" + System.currentTimeMillis(), 1000);

            }
        });

        findViewById(R.id.session_replay_privacy_override).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SessionReplayActivity.this, SessionReplayPrivacyOverrideActivity.class));
            }
        });


        AppCompatCheckedTextView appCompatCheckedTextView = findViewById(R.id.session_replay_checked_text_view);
        appCompatCheckedTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                appCompatCheckedTextView.toggle();
            }
        });

        ListView list = findViewById(R.id.session_replay_list);

        ArrayList<HashMap<String, Object>> arrayList = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("userName", "name " + i);
            map.put("userImage", R.drawable.ic_launcher_background);
            arrayList.add(map);
        }

        String[] fromArray = {"userName", "userImage"};
        int[] to = {R.id.list_simple_item_tv, R.id.list_simple_item_iv};
        list.setAdapter(new SimpleAdapter(this, arrayList, R.layout.list_simple_item, fromArray, to));

    }
}
