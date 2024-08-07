package com.ft;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;

public class SessionReplayActivity extends NameTitleActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_replay);

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
