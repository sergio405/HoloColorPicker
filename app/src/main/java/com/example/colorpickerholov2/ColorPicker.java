package com.example.colorpickerholov2;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;


/**
 * Color picker abstraction class
 */
public abstract class ColorPicker extends View {
    protected OnColorPickedListener mOnColorPickedListener;
    protected OnColorChangedListener mOnColorChangedListener;

    protected Paint mColorPaint;
    protected Paint mHandlePaint, mHandleStrokePaint;
    protected Paint pincelBlanco, pincelBlanco2, pincelBlanco3, pincelContornoNegro, pincelContornoBlanco, pincelNegro2, pincelNegro3;

    private int mHandleStrokeColor;

    protected float mHandleSize, mTouchSize;

    protected float mHue, mSat, mVal; // HSV color values

    protected float mHalfWidth, mHalfHeight;

    protected boolean mDragging; // whether handle is being dragged

    protected static final int[] COLORS = new int[] { 0xFFFF0000, 0xFFFFFF00, 0xFF00FF00, 0xFF00FFFF, 0xFF0000FF, 0xFFFF00FF, 0xFFFF0000 };

    protected static final int RADIUS_SHADOW_LAYER = 40;
    protected static final int SHADOW_COLOR = 0xFF303030;


    public ColorPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttributes(attrs);
        init();
    }

    public ColorPicker(Context context, AttributeSet attrs, int defStyle) {
        this(context, attrs);
    }

    protected void init() {
        mColorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        // estos son los pinceles de colores usados para pintar todos los elementos

        mHandlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mHandlePaint.setColor(ColorUtils.getColorFromHSV(mHue, mSat, mVal));

        mHandleStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mHandleStrokePaint.setStyle(Paint.Style.STROKE);
        mHandleStrokePaint.setColor(mHandleStrokeColor);
        mHandleStrokePaint.setStrokeWidth(10);

        pincelContornoNegro = new Paint(Paint.FILTER_BITMAP_FLAG);
        pincelContornoNegro.setStyle(Paint.Style.STROKE);
        pincelContornoNegro.setColor(Color.BLACK);
        //pincelContornoNegro.setStrokeWidth(10);
        pincelContornoNegro.setStrokeJoin(Paint.Join.BEVEL);
        //BlurMaskFilter filter = new BlurMaskFilter(60, BlurMaskFilter.Blur.INNER);
        //pincelContornoNegro.setMaskFilter(filter);

        pincelBlanco = new Paint(Paint.ANTI_ALIAS_FLAG);
        pincelBlanco.setColor(Color.WHITE);
        pincelBlanco.setStyle(Paint.Style.FILL);
        //pincelBlanco.setStrokeWidth(100);
        pincelBlanco.setStrokeJoin(Paint.Join.MITER);
        pincelBlanco.setShadowLayer(RADIUS_SHADOW_LAYER,0,10, Color.BLACK);
        //pincelBlanco.setStrokeJoin(Paint.Join.BEVEL);

        pincelBlanco2 = new Paint(Paint.ANTI_ALIAS_FLAG);
        pincelBlanco2.setColor(Color.WHITE);
        pincelBlanco2.setStyle(Paint.Style.FILL);
        //pincelBlanco.setStrokeWidth(100);
        pincelBlanco2.setStrokeJoin(Paint.Join.MITER);
        //pincelBlanco.setStrokeJoin(Paint.Join.BEVEL);

        pincelBlanco3 = new Paint(Paint.ANTI_ALIAS_FLAG);
        pincelBlanco3.setColor(Color.WHITE);
        pincelBlanco3.setStyle(Paint.Style.FILL);
        pincelBlanco3.setStrokeJoin(Paint.Join.MITER);
        pincelBlanco3.setShadowLayer(RADIUS_SHADOW_LAYER,0,15, SHADOW_COLOR);


        pincelContornoBlanco = new Paint();
        pincelContornoBlanco.setColor(Color.WHITE);
        pincelContornoBlanco.setStrokeWidth(5);
        pincelContornoBlanco.setStyle(Paint.Style.STROKE);
        pincelContornoBlanco.setStrokeJoin(Paint.Join.BEVEL);

        pincelNegro2 = new Paint(Paint.ANTI_ALIAS_FLAG);
        BlurMaskFilter filtro = new BlurMaskFilter(45, BlurMaskFilter.Blur.INNER);
        pincelNegro2.setMaskFilter(filtro);
        pincelNegro2.setColor(Color.BLACK);
        pincelNegro2.setStrokeWidth(8);
        pincelNegro2.setStrokeCap(Paint.Cap.SQUARE);
        pincelNegro2.setStrokeJoin(Paint.Join.MITER);
        pincelNegro2.setStyle(Paint.Style.STROKE);

        pincelNegro3 = new Paint(Paint.ANTI_ALIAS_FLAG);
        BlurMaskFilter filtro2 = new BlurMaskFilter(45, BlurMaskFilter.Blur.INNER);
        pincelNegro3.setMaskFilter(filtro2);
        pincelNegro3.setColor(Color.GRAY);
        pincelNegro3.setStrokeWidth(15);
        pincelNegro3.setStrokeCap(Paint.Cap.SQUARE);
        pincelNegro3.setStrokeJoin(Paint.Join.MITER);
        pincelNegro3.setStyle(Paint.Style.STROKE);




    }

    protected void initAttributes(AttributeSet attrs) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.ColorPicker);

        try {
            mHandleStrokeColor = a.getColor(R.styleable.ColorPicker_handleStrokeColor, Color.WHITE);
            mHue = Utils.normalizeAngle(a.getFloat(R.styleable.ColorPicker_hue, 0));
            mSat = Utils.clamp(a.getFloat(R.styleable.ColorPicker_saturation, 1), 0, 1);
            mVal = Utils.clamp(a.getFloat(R.styleable.ColorPicker_value, 1), 0, 1);

            // TODO add to XML attributes
            mHandleSize = getResources().getDimensionPixelSize(R.dimen.default_handleSize);
            mTouchSize = getResources().getDimensionPixelSize(R.dimen.default_touchSize);
        } finally {
            a.recycle();
        }
    }

    @Override
    protected abstract void onDraw(Canvas canvas);

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        mHalfWidth = w / 2f;
        mHalfHeight = h / 2f;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int[] location = new int[2];
        getLocationOnScreen(location);

        float x = event.getRawX() - location[0];
        float y = event.getRawY() - location[1];

        handleTouch(event.getAction(), x, y);
        return true;
    }

    protected abstract void handleTouch(int motionAction, float x, float y);

    protected abstract void moveHandleTo(float x, float y);
    protected abstract void animateHandleTo(float x, float y);

    /**
     * Get view maximum padding
     * @return maximum padding
     */
    protected int getMaxPadding() {
        return Math.max(Math.max(getPaddingLeft(), getPaddingRight()), Math.max(getPaddingTop(), getPaddingBottom()));
    }

    /*
    SETTERS/GETTERS
     */

    /**
     * Set new picker color
     * @param color new picker color
     */
    public abstract void setColor(int color);

    /**
     * Get current picker color
     * @return current color
     */
    public int getColor() {
        return mHandlePaint.getColor();
    }

    /**
     * Set handle stroke color
     * @param color new handle stroke color
     */
    public void setHandleStrokeColor(int color) {
        mHandleStrokeColor = color;
        mHandleStrokePaint.setColor(mHandleStrokeColor);
        invalidate();
    }

    /**
     * Get handle stroke color
     * @return current handle stroke color
     */
    public int getHandleStrokeColor() {
        return mHandleStrokeColor;
    }

    /**
     * Set listener for color picked event
     * @param eventListener OnColorPickedListener event listener
     */
    public void setOnColorPickedListener(OnColorPickedListener eventListener) {
        mOnColorPickedListener = eventListener;
    }

    /**
     * Set listener for color changed event
     * @param eventListener OnColorChangedListener event listener
     */
    public void setOnColorChangedListener(OnColorChangedListener eventListener) {
        mOnColorChangedListener = eventListener;
    }


}