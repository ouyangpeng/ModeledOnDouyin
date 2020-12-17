package com.csdn.oyp.douyin.utils

import android.content.Context

/**
 * 尺寸转换工具
 */
object SizeConvertUtil {
    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     *
     * @param context 上下文
     * @param dp      dp的值
     * @return px的值
     */
    fun dpToPx(context: Context, dp: Float): Int {
        val scale = getScale(context)
        return (dp * scale + 0.5f).toInt()
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     *
     * @param context 上下文
     * @param px      px的值
     * @return dp的值
     */
    fun pxToDp(context: Context, px: Float): Int {
        val scale = getScale(context)
        return (px / scale + 0.5f).toInt()
    }


    /**
     * 将px值转换为sp值，保证文字大小不变
     *
     * @param context 上下文
     * @param px      px的值
     * @return sp的值
     */
    fun pxToSp(context: Context, px: Float): Int {
        //DisplayMetrics类中属性scaledDensity
        val fontScale = getScaledDensity(context)
        return (px / fontScale + 0.5f).toInt()
    }

    /**
     * 将sp值转换为px值，保证文字大小不变
     *
     * @param context 上下文
     * @param sp      sp的值
     * @return px的值
     */
    fun spToPx(context: Context, sp: Float): Int {
        //DisplayMetrics类中属性scaledDensity
        val fontScale = getScaledDensity(context)
        return (sp * fontScale + 0.5f).toInt()
    }

    /**
     * 获取系统当前的比例
     */
    private fun getScaledDensity(context: Context): Float {
        return context.resources.displayMetrics.scaledDensity
    }

    private fun getScale(context: Context): Float {
        return context.resources.displayMetrics.density
    }
}