package com.cloudcare.ft.mobile.sdk.demo.utils

import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Shader
import com.squareup.picasso.Transformation

class CircleTransform : Transformation {
    override fun transform(source: Bitmap): Bitmap {
        val size = Math.min(source.width, source.height)
        val squaredBitmap = Bitmap.createBitmap(
            source,
            (source.width - size) / 2,
            (source.height - size) / 2,
            size,
            size
        )
        if (squaredBitmap != source) {
            source.recycle()
        }
        val bitmap = Bitmap.createBitmap(size, size, source.config)
        val canvas = Canvas(bitmap)
        val paint = Paint()
        val shader =
            BitmapShader(squaredBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        paint.shader = shader
        paint.isAntiAlias = true
        val r = size / 2f
        canvas.drawCircle(r, r, r, paint)
        squaredBitmap.recycle()
        return bitmap
    }

    override fun key(): String {
        return "circle"
    }
}