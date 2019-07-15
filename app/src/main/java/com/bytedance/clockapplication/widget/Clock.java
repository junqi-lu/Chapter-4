package com.bytedance.clockapplication.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.style.RelativeSizeSpan;
import android.util.AttributeSet;
import android.view.View;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.Locale;

public class Clock extends View {

    private final static String TAG = Clock.class.getSimpleName();

    private static final int FULL_ANGLE = 360;

    private static final int CUSTOM_ALPHA = 140;
    private static final int FULL_ALPHA = 255;

    private static final int DEFAULT_PRIMARY_COLOR = Color.WHITE;
    private static final int DEFAULT_SECONDARY_COLOR = Color.LTGRAY;

    private static final float DEFAULT_DEGREE_STROKE_WIDTH = 0.010f;
    private static final int DEFAULT_CENTER_INNER_RADIUS = 20;
    private static final int DEFAULT_CENTER_OUTER_RADIUS = 20;
    private static final int MAX_HOUR = 12;
    private static final int MAX_SECOND = 60;
    private static final int MAX_MINUTE = 60;
    public final static int AM = 0;

    private static final int RIGHT_ANGLE = 90;

    private int mWidth, mCenterX, mCenterY, mRadius;

    private int mHour, mMinute, mSecond, mAmPm;


    /**
     * properties
     */
    private int centerInnerColor;
    private int centerOuterColor;

    private int secondsNeedleColor;
    private int hoursNeedleColor;
    private int minutesNeedleColor;

    private int degreesColor;

    private int hoursValuesColor;

    private int numbersColor;

    private boolean mShowAnalog = true;

    private TimeHandler mHandler;

    public Clock(Context context) {
        super(context);
        init(context, null);
        mHandler = new TimeHandler(this);
        getTime();
        mHandler.sendEmptyMessageDelayed(1, 1000);
    }

    public Clock(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
        mHandler = new TimeHandler(this);
        getTime();
        mHandler.sendEmptyMessageDelayed(1, 1000);
    }

    public Clock(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context,attrs);
        mHandler = new TimeHandler(this);
        getTime();
        mHandler.sendEmptyMessageDelayed(1, 1000);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int size;
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        int widthWithoutPadding = width - getPaddingLeft() - getPaddingRight();
        int heightWithoutPadding = height - getPaddingTop() - getPaddingBottom();

        if (widthWithoutPadding > heightWithoutPadding) {
            size = heightWithoutPadding;
        } else {
            size = widthWithoutPadding;
        }

        setMeasuredDimension(size + getPaddingLeft() + getPaddingRight(), size + getPaddingTop() + getPaddingBottom());
    }

    private void init(Context context, AttributeSet attrs) {

        this.centerInnerColor = Color.LTGRAY;
        this.centerOuterColor = DEFAULT_PRIMARY_COLOR;

        this.secondsNeedleColor = DEFAULT_SECONDARY_COLOR;
        this.hoursNeedleColor = DEFAULT_PRIMARY_COLOR;
        this.minutesNeedleColor = DEFAULT_PRIMARY_COLOR;

        this.degreesColor = DEFAULT_PRIMARY_COLOR;

        this.hoursValuesColor = DEFAULT_PRIMARY_COLOR;

        numbersColor = Color.WHITE;
    }

    private static final class TimeHandler extends Handler {

        private WeakReference<Clock> mClockWeakReference;

        private TimeHandler(Clock clock) {
            mClockWeakReference = new WeakReference<>(clock);
        }

        @Override
        public void handleMessage(Message msg) {
            Clock clockView = mClockWeakReference.get();
            if (clockView != null) {
                clockView.getTime();
                clockView.invalidate();
                sendEmptyMessageDelayed(1, 1000);
            }
        }

    }
    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);

        mWidth = getHeight() > getWidth() ? getWidth() : getHeight();

        int halfWidth = mWidth / 2;
        mCenterX = halfWidth;
        mCenterY = halfWidth;
        mRadius = halfWidth;

        if (mShowAnalog) {
            drawDegrees(canvas);
            drawHoursValues(canvas);
            drawNeedles(canvas);
            drawCenter(canvas);
        } else {
            drawNumbers(canvas);
        }
//        postInvalidateDelayed(1000);

    }

    private void drawDegrees(Canvas canvas) {

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(mWidth * DEFAULT_DEGREE_STROKE_WIDTH);
        paint.setColor(degreesColor);

        int rPadded = mCenterX - (int) (mWidth * 0.01f);
        int rEnd = mCenterX - (int) (mWidth * 0.05f);

        for (int i = 0; i < FULL_ANGLE; i += 6 /* Step */) {

            if ((i % RIGHT_ANGLE) != 0 && (i % 15) != 0) {
                paint.setAlpha(CUSTOM_ALPHA);
            } else {
                paint.setAlpha(FULL_ALPHA);
            }

            int startX = (int) (mCenterX + rPadded * Math.cos(Math.toRadians(i)));
            int startY = (int) (mCenterX - rPadded * Math.sin(Math.toRadians(i)));

            int stopX = (int) (mCenterX + rEnd * Math.cos(Math.toRadians(i)));
            int stopY = (int) (mCenterX - rEnd * Math.sin(Math.toRadians(i)));

            canvas.drawLine(startX, startY, stopX, stopY, paint);

        }
    }

    /**
     * @param canvas
     */
    private void drawNumbers(Canvas canvas) {

        TextPaint textPaint = new TextPaint();
        textPaint.setTextSize(mWidth * 0.2f);
        textPaint.setColor(numbersColor);
        textPaint.setColor(numbersColor);
        textPaint.setAntiAlias(true);

        getTime();

        String time = String.format("%s:%s:%s%s",
                String.format(Locale.getDefault(), "%02d", mHour),
                String.format(Locale.getDefault(), "%02d", mMinute),
                String.format(Locale.getDefault(), "%02d", mSecond),
                mAmPm == AM ? "AM" : "PM");

        SpannableStringBuilder spannableString = new SpannableStringBuilder(time);
        spannableString.setSpan(new RelativeSizeSpan(0.3f), spannableString.toString().length() - 2, spannableString.toString().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // se superscript percent

        StaticLayout layout = new StaticLayout(spannableString, textPaint, canvas.getWidth(), Layout.Alignment.ALIGN_CENTER, 1, 1, true);
        canvas.translate(mCenterX - layout.getWidth() / 2f, mCenterY - layout.getHeight() / 2f);
        layout.draw(canvas);
    }

    /**
     * Draw Hour Text Values, such as 1 2 3 ...
     *
     * @param canvas
     */
    private void drawHoursValues(Canvas canvas) {
        // Default Color:
        // - hoursValuesColor
        Paint textPaint = new Paint();
        textPaint.setColor(hoursValuesColor);
        textPaint.setTextSize(DEFAULT_DEGREE_STROKE_WIDTH * mWidth * 5);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextAlign(Paint.Align.CENTER);


        for (int i = 1; i <= MAX_HOUR; i++) {
            int baseLineX;
            int baseLineY;
            int angle;
            Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();

            float top = fontMetrics.top;
            float bottom = fontMetrics.bottom;
            angle = FULL_ANGLE / MAX_HOUR * i;
            baseLineX = (int) (mCenterX + Math.cos(Math.toRadians(angle - RIGHT_ANGLE)) * (mRadius - mWidth * 0.1f));
            baseLineY = (int) (mCenterY + Math.sin(Math.toRadians(angle - RIGHT_ANGLE)) * (mRadius - mWidth * 0.1f) + (bottom - top) / 4);
            canvas.drawText(String.format(Locale.getDefault(), "%02d", i), baseLineX, baseLineY, textPaint);
        }
    }

    /**
     * Draw hours, minutes needles
     * Draw progress that indicates hours needle disposition.
     *
     * @param canvas
     */
    private void drawNeedles(final Canvas canvas) {
        // Default Color:
        // - secondsNeedleColor
        // - hoursNeedleColor
        // - minutesNeedleColor
        getTime();
        int hourStopX = (int) (mCenterX + Math.sin(Math.toRadians((double) mHour / MAX_HOUR * FULL_ANGLE)) * (mRadius - mWidth * 0.4f));
        int minuteStopX = (int) (mCenterX + Math.sin(Math.toRadians((double) mMinute / MAX_MINUTE * FULL_ANGLE)) * (mRadius - mWidth * 0.3f));
        int secondStopX = (int) (mCenterX + Math.sin(Math.toRadians((double) mSecond / MAX_SECOND * FULL_ANGLE)) * (mRadius - mWidth * 0.2f));

        int hourStopY = (int) (mCenterY - Math.cos(Math.toRadians((double) mHour / MAX_HOUR * FULL_ANGLE)) * (mRadius - mWidth * 0.4f));
        int minuteStopY = (int) (mCenterY - Math.cos(Math.toRadians((double) mMinute / MAX_MINUTE * FULL_ANGLE)) * (mRadius - mWidth * 0.3f));
        int secondStopY = (int) (mCenterY - Math.cos(Math.toRadians((double) mSecond / MAX_SECOND * FULL_ANGLE)) * (mRadius - mWidth * 0.2f));

        Paint linePaint = new Paint();
        linePaint.setColor(secondsNeedleColor);
        linePaint.setStyle(Paint.Style.FILL);
        linePaint.setStrokeWidth(mWidth * 0.01f);
        canvas.drawLine(mCenterX, mCenterY, secondStopX, secondStopY, linePaint);

        linePaint.setColor(minutesNeedleColor);
        linePaint.setStrokeWidth(mWidth * 0.01f);
        canvas.drawLine(mCenterX, mCenterY, minuteStopX, minuteStopY, linePaint);

        linePaint.setColor(hoursNeedleColor);
        linePaint.setStrokeWidth(mWidth * 0.01f);
        canvas.drawLine(mCenterX, mCenterY, hourStopX, hourStopY, linePaint);
    }

    /**
     * Draw Center Dot
     *
     * @param canvas
     */
    private void drawCenter(Canvas canvas) {
        // Default Color:
        // - centerInnerColor
        // - centerOuterColor

        //draw inner
        Paint paint = new Paint();
        paint.setStrokeWidth(DEFAULT_DEGREE_STROKE_WIDTH * mWidth);
        paint.setColor(centerInnerColor);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(mCenterX, mCenterY, DEFAULT_CENTER_INNER_RADIUS, paint);

        paint.setStrokeWidth(DEFAULT_DEGREE_STROKE_WIDTH * mWidth / 2);
        paint.setColor(centerOuterColor);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawCircle(mCenterX, mCenterY, DEFAULT_CENTER_OUTER_RADIUS, paint);
    }

    public void setShowAnalog(boolean showAnalog) {
        mShowAnalog = showAnalog;
        invalidate();
    }

    public boolean isShowAnalog() {
        return mShowAnalog;
    }

    private void getTime() {
        Calendar calendar = Calendar.getInstance();

        mHour = calendar.get(Calendar.HOUR);
        mMinute = calendar.get(Calendar.MINUTE);
        mSecond = calendar.get(Calendar.SECOND);
        mAmPm = calendar.get(Calendar.AM_PM);
    }
}