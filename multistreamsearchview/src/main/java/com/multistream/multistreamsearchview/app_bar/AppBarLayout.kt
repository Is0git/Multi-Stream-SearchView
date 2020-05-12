package com.multistream.multistreamsearchview.app_bar

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.motion.widget.MotionLayout
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.textview.MaterialTextView
import com.multistream.multistreamsearchview.R

class AppBarLayout : MotionLayout, AppBarLayout.OnOffsetChangedListener {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )
    var materialTextView : MaterialTextView? = null

    override fun onOffsetChanged(appBarLayout: AppBarLayout?, verticalOffset: Int) {
        val position = -verticalOffset / appBarLayout?.totalScrollRange?.toFloat()!!
        progress = position
         materialTextView?.apply {
            this.scaleX = 1 + progress/3
            this.scaleY = 1 + progress/3
             this.translationX = 200 * progress
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        (parent as? AppBarLayout)?.apply {
            addOnOffsetChangedListener(this@AppBarLayout)
        }

    }
}