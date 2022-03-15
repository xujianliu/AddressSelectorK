package com.mook.addressselector

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.viewpager.widget.ViewPager


/**
 * Created by xujianliu.
 * Date: 2022/2/13.
 */
internal class AsViewPagerIndicator(context: Context, attr: AttributeSet) : View(context, attr) {

    private val contentPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    /** 文字大小  */
    private val textSize: Int

    /** 文字正常时候颜色  */
    private val textNormalColor: Int

    /** 文字被选中时的颜色  */
    private val textSelectedColor: Int

    /** 指示器颜色  */
    private val indicatorColor: Int

    /** 每个Item之间的间隔  */
    private val itemSpace: Int

    /** 指示器高度  */
    private val indicatorHeight: Int

    /** 横向间距  */
    private val horizontalSpace: Int

    /** 纵向间距  */
    private val verticalSpace: Int

    /** item与指示器的间隔  */
    private val itemIndicatorSpace: Int

    /** 文字中心点偏移值  */
    private val textCenterOffset = 0f

    /** 选择的文字  */
    private val defaultStr = "请选择"

    /** 指示器矩形区域  */
    private val indicatorRectF = RectF()

    /** 所有的Item  */
    private val listItems = mutableListOf<String>()

    /** 所有Item对应RectF区域  */
    private val listRectF = mutableListOf<RectF>()

    /** 每个Item的高度  */
    private var itemHeight = 0f
        get() {
            if (field == 0f) {
                field = contentPaint.measureTextHeight(defaultStr).toFloat()
            }
            return field
        }

    /** 当前选择的位置  */
    private var selectedPosition = -1

    /** 指示器的位置  */
    private var indicatorPosition = -1

    /** 之前触摸的位置  */
    private var preTouchedIndex = -1

    /** 当前触摸的位置  */
    private var curTouchedIndex = -1

    /** 是否进行了布局  */
    private var isOnLayout = false

    /** 是否需要进行计算  */
    private val isNeedcalculate = false

    /** 添加Item动画是否正在运行  */
    var isAnimatorRunning = false

    /** 是否是滑动切换ViewPager的Item  */
    private var isHandChange: Boolean = false

    private var viewPager: ViewPager? = null

    init {
        val tr = context.obtainStyledAttributes(attr, R.styleable.AsViewPagerIndicator)
        try {
            textSize =
                tr.getDimensionPixelSize(R.styleable.AsViewPagerIndicator_asTextSize, sp2px(14f))
            textNormalColor = tr.getColor(R.styleable.AsViewPagerIndicator_asTextColor, Color.BLACK)
            textSelectedColor =
                tr.getColor(R.styleable.AsViewPagerIndicator_asTextSelectedColor, Color.RED)
            indicatorColor =
                tr.getColor(R.styleable.AsViewPagerIndicator_asIndicatorColor, Color.RED)
            indicatorHeight =
                tr.getDimensionPixelSize(R.styleable.AsViewPagerIndicator_asIndicatorHeight, dp2px(1.5f))
            itemSpace =
                tr.getDimensionPixelSize(R.styleable.AsViewPagerIndicator_asItemSpace, dp2px(30f))
        } finally {
            tr.recycle()
        }
        isClickable = true
        contentPaint.apply {
            textSize = this@AsViewPagerIndicator.textSize.toFloat()
            strokeJoin = Paint.Join.ROUND
        }
        itemIndicatorSpace = dp2px(3f)
        horizontalSpace = dp2px(10f)
        verticalSpace = dp2px(3f)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec))
    }

    private fun measureWidth(measureSpec: Int): Int {
        var result: Int
        val specMode = View.MeasureSpec.getMode(measureSpec)
        val specSize = View.MeasureSpec.getSize(measureSpec)
        if (specMode == View.MeasureSpec.EXACTLY)
            result = specSize
        else {
            if (listItems.size == 0) {
                result = 0
            } else {
                result =
                    paddingLeft + paddingRight + itemSpace * (listItems.size - 1) + listItems.sumBy {
                        contentPaint.measureText(it).toInt()
                    }
            }
            if (specMode == View.MeasureSpec.AT_MOST)
                result = Math.min(result, specSize)
        }
        return result
    }

    private fun measureHeight(measureSpec: Int): Int {
        var result = 0
        val specMode = View.MeasureSpec.getMode(measureSpec)
        val specSize = View.MeasureSpec.getSize(measureSpec)
        if (specMode == View.MeasureSpec.EXACTLY)
            result = specSize
        else {
            if (listItems.size != 0)
                result =
                    (paddingTop + paddingBottom + itemHeight + verticalSpace * 2 + itemIndicatorSpace + indicatorHeight).toInt()
            if (specMode == View.MeasureSpec.AT_MOST)
                result = Math.min(result, specSize)
        }
        return result
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        isOnLayout = true

        if (changed) {
            indicatorRectF.bottom = (height - paddingBottom - verticalSpace).toFloat()
            indicatorRectF.top = indicatorRectF.bottom - indicatorHeight
        }
        super.onLayout(changed, left, top, right, bottom)
    }

    private fun getTouchedIndex(x: Float, y: Float): Int {
        return (0..listRectF.size - 1).firstOrNull { listRectF[it].contains(x, y) } ?: -1
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        preTouchedIndex = curTouchedIndex
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                curTouchedIndex = getTouchedIndex(event.x, event.y)
                if (preTouchedIndex != curTouchedIndex) {
                    invalidate()
                }
            }
            MotionEvent.ACTION_MOVE -> {
                curTouchedIndex = getTouchedIndex(event.x, event.y)
                if (preTouchedIndex != curTouchedIndex) {
                    invalidate()
                }
            }
            MotionEvent.ACTION_UP -> {
                curTouchedIndex = getTouchedIndex(event.x, event.y)
                if (!isAnimatorRunning && curTouchedIndex != -1 && curTouchedIndex == preTouchedIndex && indicatorPosition != curTouchedIndex) {
                    scrollIndicatorByAnimator(curTouchedIndex)
                }
                curTouchedIndex = -1
                invalidate()
            }
            MotionEvent.ACTION_CANCEL -> {
                curTouchedIndex = -1
                invalidate()
            }
        }
        return super.onTouchEvent(event)
    }

    override fun onDraw(canvas: Canvas?) {
        if (listItems.size <= 0)
            return
        for (i in 0 until listItems.size) {
            when {
                selectedPosition == i -> contentPaint.color = textSelectedColor
                curTouchedIndex == i -> contentPaint.color = Color.GRAY
                else -> contentPaint.color = textNormalColor
            }
            canvas?.drawText(listItems[i], listRectF[i].left, listRectF[i].bottom, contentPaint)
        }

        contentPaint.color = indicatorColor
        canvas?.drawRoundRect(
            indicatorRectF,
            indicatorRectF.height(),
            indicatorRectF.height(),
            contentPaint
        )
    }

    private fun createRectF(): RectF {
        return RectF(
            0f,
            (paddingTop + verticalSpace).toFloat(),
            0f,
            paddingTop + verticalSpace + itemHeight
        )
    }

    private fun calculateRectF() {
        for (i in 0 until listRectF.size) {
            if (i == 0) {
                listRectF[i].left = (paddingLeft + horizontalSpace).toFloat()
                listRectF[i].right = listRectF[i].left + contentPaint.measureText(listItems[i])
            } else {
                listRectF[i].left = listRectF[i - 1].right + itemSpace
                listRectF[i].right = listRectF[i].left + contentPaint.measureText(listItems[i])
            }
        }
    }

    private fun addItemByAnimator() {
        val rectF1 = listRectF[listRectF.size - 1]
        val rectF2 = listRectF[listRectF.size - 2]
        val oldWidth = indicatorRectF.width()
        val oldCenterX = indicatorRectF.centerX()
        val distance1 = rectF1.left - rectF2.left
        val distance2 = rectF1.centerX() - indicatorRectF.centerX()
        val widthValue = rectF1.width() - oldWidth

        val animator = ValueAnimator.ofFloat(0f, 1f)
        animator.repeatCount = 0
        animator.duration = 600
        animator.addUpdateListener { animation ->
            val offset = animation.animatedValue as Float
            val width = oldWidth + widthValue * offset
            listRectF[listRectF.size - 1].left = rectF2.left + distance1 * offset
            indicatorRectF.left = oldCenterX + distance2 * offset - width / 2
            indicatorRectF.right = indicatorRectF.left + width
            invalidate()
        }
        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
                isAnimatorRunning = true
            }

            override fun onAnimationEnd(animation: Animator) {
                indicatorPosition = selectedPosition
                isAnimatorRunning = false
            }

            override fun onAnimationCancel(animation: Animator) {

            }

            override fun onAnimationRepeat(animation: Animator) {

            }
        })
        animator.start()
    }

    private fun reduceItemByAnimator(position: Int, isHasNext: Boolean) {
        val lastIndex = listItems.size - 1
        val rectF = listRectF[lastIndex]
        val tempRectF = createRectF()
        tempRectF.left = rectF.left
        tempRectF.right = rectF.right
        tempRectF.top = rectF.top
        tempRectF.bottom = rectF.bottom
        val rightRectF = createRectF()
        if (isHasNext) {
            rightRectF.left = listRectF[lastIndex - 1].right + itemSpace
            rightRectF.right = rightRectF.left + contentPaint.measureText(listItems.get(lastIndex))
            rightRectF.top = rectF.top
            rightRectF.bottom = rectF.bottom
            rectF.left = rectF.centerX() - rightRectF.width() / 2
            selectedPosition = position + 1
            viewPager?.currentItem = viewPager?.childCount ?: 1 - 1
        }
        val left = rectF.left
        val distance = rightRectF.left - rectF.left
        val animator = ValueAnimator.ofFloat(0f, 1f)
        animator.repeatCount = 0
        animator.duration = 600
        animator.addUpdateListener { animation ->
            val offset = animation.animatedValue as Float
            rectF.left = left + distance * offset
            rectF.right = rectF.left + rightRectF.width()
            scrollIndicator(tempRectF, rightRectF, offset)
            invalidate()
        }
        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
                isAnimatorRunning = true
            }

            override fun onAnimationEnd(animation: Animator) {
                isAnimatorRunning = false
            }

            override fun onAnimationCancel(animation: Animator) {

            }

            override fun onAnimationRepeat(animation: Animator) {

            }
        })
        animator.start()
    }

    private fun scrollIndicatorByAnimator(position: Int) {
        scrollIndicatorByAnimator(listRectF[indicatorPosition], listRectF[position])
        indicatorPosition = position
        viewPager?.currentItem = position
    }

    private fun scrollIndicatorByAnimator(rectF: RectF, newRectF: RectF) {
        val animator = ValueAnimator.ofFloat(0f, 1f)
        animator.repeatCount = 0
        animator.duration = 600
        animator.addUpdateListener { animation ->
            scrollIndicator(
                rectF,
                newRectF,
                animation.animatedValue as Float
            )
        }
        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
                isAnimatorRunning = true
            }

            override fun onAnimationEnd(animation: Animator) {
                isAnimatorRunning = false
            }

            override fun onAnimationCancel(animation: Animator) {

            }

            override fun onAnimationRepeat(animation: Animator) {

            }
        })
        animator.start()
    }

    private fun scrollIndicator(rectF: RectF, newrRectF: RectF, offset: Float) {
        val distance = newrRectF.centerX() - rectF.centerX()
        val widthValue = newrRectF.width() - rectF.width()
        val width = rectF.width() + widthValue * offset
        indicatorRectF.left = rectF.centerX() + distance * offset - width / 2
        indicatorRectF.right = indicatorRectF.left + width
        invalidate()
    }

    private fun addItem(item: String) {
        if (listItems.size == 0) {
            selectedPosition = 0
            indicatorPosition = selectedPosition
            listItems.add(defaultStr)
            listRectF.add(createRectF())
            requestLayout()
            calculateRectF()
            indicatorRectF.left = listRectF[selectedPosition].left
            indicatorRectF.right = listRectF[selectedPosition].right
        } else {
            if (item == defaultStr) {
                listItems.add(defaultStr)
                listRectF.add(createRectF())
            } else {
                listItems.add(listItems.size - 1, item)
                if (listItems[listItems.size - 1] != defaultStr) {
                    listItems.removeAt(listItems.size - 1)
                    listItems.add(defaultStr)
                }
                listRectF.add(listItems.size - 1, createRectF())
            }
            selectedPosition = listItems.size - 1
            calculateRectF()
            addItemByAnimator()
            viewPager?.currentItem = viewPager?.childCount ?: 1 - 1
        }
    }

    fun changeItem(position: Int, item: String, isHasNext: Boolean) {
        isHandChange = false
        if (listItems.size == 0 || position == listItems.size - 1) {
            if (isHasNext) {
                addItem(item)
                invalidate()
            } else {
                selectedPosition = -1
                listItems.removeAt(position)
                listItems.add(item)
                val oldRectF = listRectF[position]
                calculateRectF()
                scrollIndicatorByAnimator(listRectF[position], oldRectF)
                invalidate()
            }
        } else {
            val lastRectF = listRectF[listRectF.size - 1]
            val lastItem = listItems[listItems.size - 1]
            for (i in listItems.size - 1 downTo position) {
                listItems.removeAt(i)
                listRectF.removeAt(i)
            }
            listRectF.add(createRectF())
            listItems.add(item)
            calculateRectF()
            listRectF.add(lastRectF)
            listItems.add(if (isHasNext) defaultStr else lastItem)
            reduceItemByAnimator(position, isHasNext)
        }
    }

    fun isSelectedItem(position: Int): Boolean = !(position < 0 || listItems.size <= position)

    fun setupWithViewPager(vp: ViewPager) {
        this.viewPager = vp
        vp.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                if (isHandChange) {
                    scrollIndicator(
                        listRectF[position],
                        listRectF[if (position < listRectF.size - 1) position + 1 else position],
                        positionOffset
                    )
                }
            }

            override fun onPageSelected(position: Int) {
                indicatorPosition = position
            }

            override fun onPageScrollStateChanged(state: Int) {
                if (state == ViewPager.SCROLL_STATE_DRAGGING) {
                    isHandChange = true
                } else if (state == ViewPager.SCROLL_STATE_IDLE) {
                    isHandChange = false
                }
            }
        })
    }

    fun getContent(): String {
        var address = ""
        for (item in listItems)
            address += ",$item"
        return address.substring(1, address.length)
    }

    fun getItems() = listItems

}