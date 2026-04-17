package com.ft;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class LargeImageTestActivity extends NameTitleActivity {

    private ImageGalleryAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_image_gallery);

        RecyclerView recyclerView = findViewById(R.id.image_gallery_recycler);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(layoutManager);

        List<String> imageUrls = createImageList();
        adapter = new ImageGalleryAdapter(imageUrls);
        recyclerView.setAdapter(adapter);
    }

    @NotNull
    private List<String> createImageList() {
        List<String> urls = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            urls.add("https://picsum.photos/seed/large" + i + "/800/800");
        }
        return urls;
    }

    private class ImageGalleryAdapter extends RecyclerView.Adapter<ImageGalleryAdapter.ImageViewHolder> {

        private final List<String> imageUrls;

        ImageGalleryAdapter(List<String> imageUrls) {
            this.imageUrls = imageUrls;
        }

        @NonNull
        @Override
        public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_large_image, parent, false);
            return new ImageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
            String imageUrl = imageUrls.get(position);
            Glide.with(holder.imageView)
                    .load(imageUrl)
                    .fitCenter()
                    .into(holder.imageView);
        }

        @Override
        public int getItemCount() {
            return imageUrls.size();
        }

        class ImageViewHolder extends RecyclerView.ViewHolder {
            final ImageView imageView;

            ImageViewHolder(@NonNull View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.large_image_view);
            }
        }
    }
}
