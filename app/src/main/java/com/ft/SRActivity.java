package com.ft;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatCheckedTextView;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Session Replay Activity Class
 * Used to demonstrate various UI component interactions for session replay functionality
 */
public class SRActivity extends NameTitleActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_replay);

        // Set dialog button click event
        findViewById(R.id.session_replay_dialog).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create a simple dialog
                new AlertDialog.Builder(SRActivity.this).setMessage("This is a dialog")
                        .setPositiveButton("OK", null).create().show();
            }

        });

        // Set native Toast button click event
        findViewById(R.id.session_replay_origin_toast).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show native Toast message
                Toast.makeText(SRActivity.this, "Toast:" + System.currentTimeMillis(), Toast.LENGTH_LONG).show();

            }
        });

        // Set custom Toast button click event
        findViewById(R.id.session_replay_custom_toast).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show custom Toast message for 1 second
                CustomToast.showToast(SRActivity.this, "Toast:" + System.currentTimeMillis(), 1000);

            }
        });

        // Set clickable text view click event
        AppCompatCheckedTextView appCompatCheckedTextView = findViewById(R.id.session_replay_checked_text_view);
        appCompatCheckedTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toggle selection state
                appCompatCheckedTextView.toggle();
            }
        });

        // Set list view
        ListView list = findViewById(R.id.session_replay_list);

        // Create list data
        ArrayList<HashMap<String, Object>> arrayList = new ArrayList<>();

        // Add test items for bitmap recycled/immutable scenario
        HashMap<String, Object> recycledTestMap = new HashMap<>();
        recycledTestMap.put("userName", "[Test] Recycled Bitmap");
        recycledTestMap.put("userImage", "TEST_RECYCLED");
        arrayList.add(recycledTestMap);

        HashMap<String, Object> immutableTestMap = new HashMap<>();
        immutableTestMap.put("userName", "[Test] Immutable Bitmap");
        immutableTestMap.put("userImage", "TEST_IMMUTABLE");
        arrayList.add(immutableTestMap);

        // Add normal items
        for (int i = 0; i < 18; i++) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("userName", "name " + i);
            // Use online image URL
            map.put("userImage", "https://picsum.photos/200/200?random=" + i);
            arrayList.add(map);
        }

        // Set adapter data mapping
        String[] fromArray = {"userName", "userImage"};
        int[] to = {R.id.list_simple_item_tv, R.id.list_simple_item_iv};
        SimpleAdapter adapter = new SimpleAdapter(this, arrayList, R.layout.list_simple_item, fromArray, to);

        // Custom image loading
        adapter.setViewBinder(new SimpleAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Object data, String textRepresentation) {
                if (view instanceof ImageView && data instanceof String) {
                    String imageUrl = (String) data;
                    loadImageFromUrl((ImageView) view, imageUrl);
                    return true;
                }
                return false;
            }
        });

        list.setAdapter(adapter);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Load menu resources
        getMenuInflater().inflate(R.menu.session_replay_override, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Handle menu item selection event
        if (item.getItemId() == R.id.session_replay_override) {
            // Launch privacy override activity
            startActivity(new Intent(SRActivity.this, SRPrivacyOverrideActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    // Use thread pool and main thread Handler to load network images
    private final ExecutorService executorService = Executors.newFixedThreadPool(4);
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    /**
     * Load image from URL
     *
     * @param imageView The ImageView to set the image
     * @param imageUrl  Image URL or test marker
     */
    private void loadImageFromUrl(ImageView imageView, String imageUrl) {
        // Use tag to mark the ImageView's corresponding URL to prevent image misalignment
        imageView.setTag(imageUrl);

        // Handle test scenarios for bitmap recycled/immutable
        if ("TEST_RECYCLED".equals(imageUrl)) {
            // Scenario 1: Create a bitmap, set it to ImageView, then recycle it after a delay
            // This simulates the scenario where Session Replay tries to capture a bitmap
            // that has been recycled in the meantime (race condition)
            executorService.execute(() -> {
                try {
                    // Load a bitmap from network
                    URL url = new URL("https://picsum.photos/200/200?random=recycled");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setConnectTimeout(5000);
                    connection.setReadTimeout(5000);
                    connection.connect();

                    InputStream input = connection.getInputStream();
                    final Bitmap bitmap = BitmapFactory.decodeStream(input);
                    input.close();

                    if (bitmap != null) {
                        // First set the bitmap to ImageView (valid state)
                        mainHandler.post(() -> {
                            if ("TEST_RECYCLED".equals(imageView.getTag())) {
                                imageView.setImageBitmap(bitmap);
                            }
                        });

                        // After a delay, recycle the bitmap
                        // This simulates memory pressure or LRU cache eviction
                        // When Session Replay tries to capture, bitmap may be recycled
                        mainHandler.postDelayed(() -> {
                            if (!bitmap.isRecycled()) {
                                bitmap.recycle();
                                // Show a toast to indicate bitmap is recycled
                            }
                        }, 2000); // Recycle after 2 seconds
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    mainHandler.post(() -> {
                        if ("TEST_RECYCLED".equals(imageView.getTag())) {
                            imageView.setImageResource(R.drawable.ic_launcher_background);
                        }
                    });
                }
            });
            return;
        }

        if ("TEST_IMMUTABLE".equals(imageUrl)) {
            // Scenario 2: Use an immutable bitmap
            // Load bitmap from resources which creates immutable bitmap
            executorService.execute(() -> {
                try {
                    // Create a bitmap from resources - this is typically immutable
                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher_background);

                    mainHandler.post(() -> {
                        if ("TEST_IMMUTABLE".equals(imageView.getTag())) {
                            if (bitmap != null) {
                                imageView.setImageBitmap(bitmap);

                                // Verify if bitmap is immutable and show toast
                                imageView.post(() -> {
                                    if (!bitmap.isMutable()) {
                                        Toast.makeText(SRActivity.this,
                                                "Immutable bitmap set - Canvas creation will fail (mutable=false)",
                                                Toast.LENGTH_LONG).show();
                                    }
                                });
                            } else {
                                imageView.setImageResource(R.drawable.ic_launcher_background);
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    mainHandler.post(() -> {
                        if ("TEST_IMMUTABLE".equals(imageView.getTag())) {
                            imageView.setImageResource(R.drawable.ic_launcher_background);
                        }
                    });
                }
            });
            return;
        }

        // Normal image loading from URL
        executorService.execute(() -> {
            try {
                URL url = new URL(imageUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                connection.connect();

                InputStream input = connection.getInputStream();
                final Bitmap bitmap = BitmapFactory.decodeStream(input);
                input.close();

                // Update UI on main thread
                mainHandler.post(() -> {
                    // Check if ImageView's tag matches to prevent image misalignment during list scrolling
                    if (imageUrl.equals(imageView.getTag())) {
                        imageView.setImageBitmap(bitmap);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                // Show default image when loading fails
                mainHandler.post(() -> {
                    if (imageUrl.equals(imageView.getTag())) {
                        imageView.setImageResource(R.drawable.ic_launcher_background);
                    }
                });
            }
        });
    }
}
