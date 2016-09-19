package com.example.globalclasses;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;


public class CustomProgressBar extends View {

    private int progressWidth = 12;
    private int outerWidth = 15;
    private int max = 100;
    int outerColor = 0xd5ffffff;
    int innerColor = 0x85ffffff;

    private Path path = new Path();
    private Path innerPath = new Path();
    int progressColor = getResources().getColor(R.color.primary);
    private Paint paint;
    private Paint mPaintProgress;
    private RectF mRectF;
    private Paint textPaint;
    private Paint innerPaint;
    private String text = "0%";
    private final Rect textBounds = new Rect();
    private int centerY;
    private int centerX;
    private int swipeAngle = 0;

    public CustomProgressBar(Context context) {
        super(context);
        initUI();
    }

    public CustomProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initUI();
    }

    public CustomProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initUI();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CustomProgressBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initUI();
    }

    private void initUI() {
        innerPaint = new Paint();
        innerPaint.setAntiAlias(true);
        innerPaint.setColor(innerColor);
        innerPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStrokeWidth(dpToPx(outerWidth));
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(outerColor);

        mPaintProgress = new Paint();
        mPaintProgress.setAntiAlias(true);
        mPaintProgress.setStyle(Paint.Style.STROKE);
        mPaintProgress.setStrokeWidth(dpToPx(progressWidth));
        mPaintProgress.setColor(progressColor);

        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setColor(getResources().getColor(android.R.color.white));
        textPaint.setStrokeWidth(2);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int viewWidth = MeasureSpec.getSize(widthMeasureSpec);
        int viewHeight = MeasureSpec.getSize(heightMeasureSpec);

        int radius = (Math.min(viewWidth, viewHeight) - (int) dpToPx(2)) / 2;

        path.reset();
        innerPath.reset();

        centerX = viewWidth / 2;
        centerY = viewHeight / 2;

        int outerCircleRadius = radius - (int) dpToPx(6);
        path.addCircle(centerX, centerY, outerCircleRadius, Path.Direction.CW);
        outerCircleRadius -= dpToPx(4);

        mRectF = new RectF(centerX - outerCircleRadius, centerY - outerCircleRadius,
                centerX + outerCircleRadius, centerY + outerCircleRadius);

        innerPath.addCircle(centerX, centerY, outerCircleRadius - (dpToPx(outerWidth) - dpToPx(progressWidth)), Path.Direction.CW);

        textPaint.setTextSize(radius * 0.5f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawPath(path, paint);
        canvas.drawPath(innerPath, innerPaint);
        canvas.drawArc(mRectF, 270, swipeAngle, false, mPaintProgress);
        drawTextCentred(canvas);
    }

    public void drawTextCentred(Canvas canvas) {
        textPaint.getTextBounds(text, 0, text.length(), textBounds);
        canvas.drawText(text, centerX - textBounds.exactCenterX(), centerY - textBounds.exactCenterY(), textPaint);
    }

    public void setMax(int max) {
        this.max = max;
    }

    public void setProgress(int progress) {
        int percentage = progress * 100 / max;
        swipeAngle = percentage * 360 / 100;
        text = percentage + "%";
        invalidate();
    }

    public void setMillis(long millis) {
        final long millisPerPercent = millis / 100;
        final int[] progress = {0};
        final Handler handler = new Handler();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (progress[0] <= 100) {
                    setVisibility(View.VISIBLE);
                    setProgress(progress[0]++);
                    handler.postDelayed(this, millisPerPercent);
                } else {
                    setVisibility(View.INVISIBLE);
                }
            }
        };
        handler.post(runnable);
    }

    public void setProgressColor(int progressColor) {
        this.progressColor = progressColor;
        initUI();
    }

    public void setInnerColor(int innerColor) {
        this.innerColor = innerColor;
        initUI();
    }

    public void setOuterColor(int outerColor) {
        this.outerColor = outerColor;
        initUI();
    }

    private float dpToPx(int dp) {
        Resources r = getResources();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
    }

    public void setProgressWidth(int width) {
        this.progressWidth = width;
        initUI();
    }

    public void setOuterWidth(int width) {
        this.outerWidth = width;
        initUI();
    }
}
