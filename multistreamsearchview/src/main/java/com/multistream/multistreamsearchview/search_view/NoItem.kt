package com.multistream.multistreamsearchview.search_view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.res.ResourcesCompat
import com.multistream.multistreamsearchview.R

class NoItem : View {

    lateinit var bitmap: Bitmap

    var title: String? = "NO ITEMS"

    lateinit var imageBitmap: Bitmap

    lateinit var circlePaint: Paint

    lateinit var textPaint: Paint

    constructor(context: Context?) : super(context) {
        init(context, null)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context, attrs)
    }

    fun init(context: Context?, attrs: AttributeSet?) {
        attrs?.also {
            val typedArray = context?.obtainStyledAttributes(attrs, R.styleable.NoItem)
            title = typedArray?.getString(R.styleable.NoItem_title)
            typedArray?.recycle()
        }
        circlePaint = Paint().apply {
            color = ResourcesCompat.getColor(resources, R.color.colorLight, null)
        }
        textPaint = Paint().apply {
            typeface = ResourcesCompat.getFont(context!!, R.font.header_font)
            color = ResourcesCompat.getColor(resources, R.color.colorPrimary, null)
            textSize = convertDpToPixel(16, resources).toFloat()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        imageBitmap = getBitmap(
            ResourcesCompat.getDrawable(resources, R.drawable.empty_image, null)!!,
            width / 2,
            height / 2
        )
    }

    override fun onDraw(canvas: Canvas?) {
        val centerX = width / 4f
        val centerY = height / 4f
        canvas?.drawCircle(centerX, centerY, (centerX + centerY) / 3, circlePaint)
        canvas?.drawBitmap(imageBitmap, 0f, 0f, null)
        canvas?.drawText(title!!, width/3f, height/4f, textPaint )
    }
}
