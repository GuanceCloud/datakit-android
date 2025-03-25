package com.ft;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatCheckedTextView;

import java.util.ArrayList;
import java.util.HashMap;

public class SRActivity extends NameTitleActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_replay);
        findViewById(R.id.session_replay_dialog).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(SRActivity.this).setMessage("这是对话框")
                        .setPositiveButton("确定", null).create().show();
            }

        });

        findViewById(R.id.session_replay_origin_toast).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(SRActivity.this, "Toast:" + System.currentTimeMillis(), Toast.LENGTH_LONG).show();

            }
        });
        findViewById(R.id.session_replay_custom_toast).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomToast.showToast(SRActivity.this, "Toast:" + System.currentTimeMillis(), 1000);

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.session_replay_override, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.session_replay_override) {
            startActivity(new Intent(SRActivity.this, SRPrivacyOverrideActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }
}
