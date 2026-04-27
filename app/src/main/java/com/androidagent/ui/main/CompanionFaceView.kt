package com.androidagent.ui.main

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import com.androidagent.agents.RobotMood

class CompanionFaceView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val headPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val eyePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val pupilPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mouthPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val antennaPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val accentPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var blinkProgress = 1f // 1 = open, 0 = closed
    private var mouthProgress = 0f // 0 = smile, 1 = talking
    private var eyeOffsetX = 0f
    private var eyeOffsetY = 0f
    private var currentMood = RobotMood.IDLE
    private var accentColor = Color.parseColor("#2196F3")

    private val blinkAnimator = ValueAnimator.ofFloat(1f, 0f, 1f).apply {
        duration = 200
        repeatDelay = 3000
        repeatCount = ValueAnimator.INFINITE
        interpolator = LinearInterpolator()
        addUpdateListener {
            blinkProgress = it.animatedValue as Float
            invalidate()
        }
    }

    private val mouthAnimator = ValueAnimator.ofFloat(0f, 1f, 0f).apply {
        duration = 600
        repeatCount = ValueAnimator.INFINITE
        interpolator = LinearInterpolator()
        addUpdateListener {
            mouthProgress = it.animatedValue as Float
            invalidate()
        }
    }

    private val eyeWanderAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = 4000
        repeatCount = ValueAnimator.INFINITE
        repeatMode = ValueAnimator.REVERSE
        addUpdateListener {
            val t = it.animatedValue as Float
            eyeOffsetX = (t - 0.5f) * 8f
            eyeOffsetY = Math.sin(t * Math.PI).toFloat() * 4f
            invalidate()
        }
    }

    init {
        headPaint.color = Color.parseColor("#37474F")
        headPaint.style = Paint.Style.FILL
        eyePaint.color = Color.WHITE
        eyePaint.style = Paint.Style.FILL
        pupilPaint.color = accentColor
        pupilPaint.style = Paint.Style.FILL
        mouthPaint.color = accentColor
        mouthPaint.style = Paint.Style.STROKE
        mouthPaint.strokeWidth = 6f
        mouthPaint.strokeCap = Paint.Cap.ROUND
        antennaPaint.color = Color.parseColor("#78909C")
        antennaPaint.style = Paint.Style.FILL
        accentPaint.color = accentColor
        accentPaint.style = Paint.Style.STROKE
        accentPaint.strokeWidth = 4f
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        blinkAnimator.start()
        eyeWanderAnimator.start()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        blinkAnimator.cancel()
        mouthAnimator.cancel()
        eyeWanderAnimator.cancel()
    }

    fun setMood(mood: RobotMood) {
        currentMood = mood
        accentColor = when (mood) {
            RobotMood.HAPPY -> Color.parseColor("#4CAF50")
            RobotMood.THINKING -> Color.parseColor("#FFC107")
            RobotMood.CURIOUS -> Color.parseColor("#03A9F4")
            RobotMood.EXCITED -> Color.parseColor("#FF5722")
            RobotMood.BUSY -> Color.parseColor("#9C27B0")
            RobotMood.ALERT -> Color.parseColor("#F44336")
            RobotMood.IDLE -> Color.parseColor("#2196F3")
        }
        pupilPaint.color = accentColor
        mouthPaint.color = accentColor
        accentPaint.color = accentColor

        if (mood == RobotMood.BUSY || mood == RobotMood.THINKING) {
            if (!mouthAnimator.isRunning) mouthAnimator.start()
        } else {
            mouthAnimator.cancel()
            mouthProgress = 0f
        }
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cx = width / 2f
        val cy = height / 2f
        val radius = (minOf(width, height) / 2f) * 0.75f

        // Antenna
        antennaPaint.color = Color.parseColor("#78909C")
        canvas.drawRect(cx - 4f, cy - radius - 30f, cx + 4f, cy - radius, antennaPaint)
        antennaPaint.style = Paint.Style.FILL
        canvas.drawCircle(cx, cy - radius - 32f, 10f, antennaPaint)
        accentPaint.style = Paint.Style.FILL
        accentPaint.color = accentColor
        canvas.drawCircle(cx, cy - radius - 32f, 6f, accentPaint)

        // Head
        val shaderColors = intArrayOf(Color.parseColor("#546E7A"), Color.parseColor("#263238"))
        val shader = LinearGradient(cx - radius, cy - radius, cx + radius, cy + radius,
            shaderColors, null, Shader.TileMode.CLAMP)
        headPaint.shader = shader
        canvas.drawRoundRect(cx - radius, cy - radius, cx + radius, cy + radius,
            radius * 0.3f, radius * 0.3f, headPaint)

        // Accent border
        accentPaint.style = Paint.Style.STROKE
        accentPaint.strokeWidth = 3f
        canvas.drawRoundRect(cx - radius, cy - radius, cx + radius, cy + radius,
            radius * 0.3f, radius * 0.3f, accentPaint)

        // Eyes
        val eyeY = cy - radius * 0.15f
        val eyeSpacing = radius * 0.38f
        val eyeRadius = radius * 0.18f

        // Left eye
        canvas.drawCircle(cx - eyeSpacing, eyeY, eyeRadius, eyePaint)
        if (blinkProgress > 0.1f) {
            val pupilSize = eyeRadius * 0.55f * blinkProgress
            canvas.drawCircle(cx - eyeSpacing + eyeOffsetX, eyeY + eyeOffsetY, pupilSize, pupilPaint)
        }

        // Right eye
        canvas.drawCircle(cx + eyeSpacing, eyeY, eyeRadius, eyePaint)
        if (blinkProgress > 0.1f) {
            val pupilSize = eyeRadius * 0.55f * blinkProgress
            canvas.drawCircle(cx + eyeSpacing + eyeOffsetX, eyeY + eyeOffsetY, pupilSize, pupilPaint)
        }

        // Mouth
        val mouthY = cy + radius * 0.35f
        val mouthWidth = radius * 0.5f
        mouthPaint.style = Paint.Style.STROKE
        mouthPaint.strokeWidth = 5f

        val path = Path()
        val smileHeight = radius * 0.12f * (1f - mouthProgress * 0.5f)
        path.moveTo(cx - mouthWidth, mouthY)
        path.quadTo(cx, mouthY + smileHeight, cx + mouthWidth, mouthY)
        canvas.drawPath(path, mouthPaint)
    }
}
