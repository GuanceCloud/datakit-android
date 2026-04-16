package com.ft;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.graphics.Rect;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.shape.MaterialShapeDrawable;

public class SRMaterialButtonActivity extends NameTitleActivity {
    private static final String TAG = "SRMaterialButton";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sr_material_button);

        MaterialButton defaultButton = findViewById(R.id.material_button_default);
        MaterialButton tintedButton = findViewById(R.id.material_button_tinted);

        defaultButton.post(() -> {
            Log.i(TAG, "===== default MaterialButton =====");
            dumpDrawableTree("default ", defaultButton.getBackground());
        });

        tintedButton.post(() -> {
            Log.i(TAG, "===== tinted MaterialButton =====");
            dumpDrawableTree("tinted ", tintedButton.getBackground());
        });
    }

    private void dumpDrawableTree(String prefix, Drawable drawable) {
        if (drawable == null) {
            Log.i(TAG, prefix + "drawable=null");
            return;
        }

        Log.i(TAG, prefix + describeDrawable(drawable));

        if (drawable instanceof StateListDrawable) {
            Drawable current = ((StateListDrawable) drawable).getCurrent();
            dumpDrawableTree(prefix + "current -> ", current);
        }

        if (drawable instanceof LayerDrawable) {
            LayerDrawable layerDrawable = (LayerDrawable) drawable;
            for (int i = 0; i < layerDrawable.getNumberOfLayers(); i++) {
                dumpDrawableTree(prefix + "layer[" + i + "] -> ", layerDrawable.getDrawable(i));
            }
        }

        if (drawable instanceof InsetDrawable) {
            dumpDrawableTree(prefix + "inset -> ", ((InsetDrawable) drawable).getDrawable());
        }

        if (drawable instanceof RippleDrawable) {
            RippleDrawable rippleDrawable = (RippleDrawable) drawable;
            for (int i = 0; i < rippleDrawable.getNumberOfLayers(); i++) {
                dumpDrawableTree(prefix + "ripple[" + i + "] -> ", rippleDrawable.getDrawable(i));
            }
        }
    }

    private String describeDrawable(Drawable drawable) {
        StringBuilder builder = new StringBuilder();
        builder.append("class=").append(drawable.getClass().getName());
        builder.append(", alpha=").append(drawable.getAlpha());
        builder.append(", bounds=").append(drawable.getBounds());

        if (drawable instanceof MaterialShapeDrawable) {
            MaterialShapeDrawable materialShapeDrawable = (MaterialShapeDrawable) drawable;
            builder.append(", fillColor=")
                    .append(materialShapeDrawable.getFillColor() != null
                            ? materialShapeDrawable.getFillColor().getDefaultColor()
                            : "null");
            builder.append(", tintList=")
                    .append(materialShapeDrawable.getTintList() != null
                            ? materialShapeDrawable.getTintList().getDefaultColor()
                            : "null");
        } else if (drawable instanceof ColorDrawable) {
            builder.append(", color=").append(((ColorDrawable) drawable).getColor());
        }

        Integer sampledColor = sampleCenterColor(drawable);
        builder.append(", sampledCenterColor=").append(sampledColor);
        if (sampledColor != null) {
            builder.append(", sampledAlpha=").append(Color.alpha(sampledColor));
        }

        return builder.toString();
    }

    private Integer sampleCenterColor(Drawable drawable) {
        int width = Math.max(drawable.getIntrinsicWidth(), 1);
        int height = Math.max(drawable.getIntrinsicHeight(), 1);

        try {
            Drawable sampledDrawable = drawable.getConstantState() != null
                    ? drawable.getConstantState().newDrawable(getResources())
                    : drawable.mutate();
            sampledDrawable.setState(drawable.getState());
            sampledDrawable.setLevel(drawable.getLevel());

            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            Rect oldBounds = sampledDrawable.getBounds();
            sampledDrawable.setBounds(0, 0, width, height);
            sampledDrawable.draw(canvas);
            int centerColor = bitmap.getPixel(Math.max(0, width / 2), Math.max(0, height / 2));
            sampledDrawable.setBounds(oldBounds);
            bitmap.recycle();
            return centerColor;
        } catch (RuntimeException e) {
            Log.w(TAG, "sampleCenterColor failed for " + drawable.getClass().getName(), e);
            return null;
        }
    }
}
