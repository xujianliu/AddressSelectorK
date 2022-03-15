package com.mook.addressselector

import android.content.Context
import android.graphics.Paint
import android.graphics.Rect
import android.util.TypedValue
import android.view.View

/**
 * Created by xujianliu.
 * Date: 2022/2/13.
 */
internal fun Context.dp2px(dp: Float) = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
        resources.displayMetrics).toInt()

internal fun Context.sp2px(sp: Float) = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp,
        resources.displayMetrics).toInt()

internal fun View.dp2px(dp: Float) = context.dp2px(dp)

internal fun View.sp2px(sp: Float) = context.sp2px(sp)

internal fun Paint.measureTextHeight(text: String): Int {
    val bounds = Rect()
    getTextBounds(text, 0, text.length, bounds)
    return bounds.height()
}

internal fun Paint.getTextCenterYOffset(): Float {
    if (fontMetrics == null) {
        return 0f
    }
    return Math.abs(fontMetrics!!.top + fontMetrics!!.bottom) / 2
}