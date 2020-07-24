package com.example.colorpickerholov2;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;


/**
 * Color Ring Picker View
 */
public class RingColorPicker extends ColorPicker {

    private Paint mInnerPaint;

    private RectF mHandleRect, rectangulo;
    private Path pathPointerPart1, pathPointerPart2, pathPointerPart3, pathRingBackground;


    private ValueLinearColorPicker mValLCP;
    private SaturationLinearColorPicker mSatLCP;

    private int mRingWidth, mGapWidth, mDiameterPointer; // view attributes
    private float mInnerRadius, mOuterRadius; // view measurements

    private static final float AJUSTE = 30;      // movimiento horizaontal del puntero, hacia adentro o hacia afuera del anillo de colores desde el borde
    private static final int HANDLE_PADDING = 0;
    private static final int HANDLE_EDGE_RADIUS = 0;

    public RingColorPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RingColorPicker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * Initialize member objects
     */
    protected void init() {
        super.init();

        mHandleRect = new RectF();

        // init paints
        mColorPaint.setStyle(Paint.Style.STROKE);
        mColorPaint.setStrokeWidth(mRingWidth);
        //mColorPaint.setShadowLayer(mOuterRadius + mColorPaint.getStrokeWidth()/2 + 20, 0,0, Color.BLACK );
        mColorPaint.setShader(new SweepGradient(0, 0, ColorUtils.getHueRingColors(7, mSat, mVal), null));


        // Pincel del circulo interior
        mInnerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        //mInnerPaint.setStyle(Paint.Style.STROKE);
        //mInnerPaint.setStrokeWidth(30);
        mInnerPaint.setShadowLayer(RADIUS_SHADOW_LAYER, 0,15, SHADOW_COLOR);
        mInnerPaint.setColor(getColor());

        if (isInEditMode()) {
            mColorPaint.setShader(new SweepGradient(0, 0, COLORS, null));
            mInnerPaint.setColor(Color.RED);
            mHandlePaint.setColor(Color.RED);
        }
    }

    /**
     * Initialize XML attributes
     * @param attrs xml attribute set
     */
    protected void initAttributes(AttributeSet attrs) {
        super.initAttributes(attrs);

        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.RingColorPicker);

        try {
            mRingWidth = a.getDimensionPixelSize(R.styleable.RingColorPicker_ringWidth, (int)mHandleSize);
            mGapWidth = a.getDimensionPixelSize(R.styleable.RingColorPicker_gapWidth, HANDLE_PADDING + (int)mHandleSize);
            mDiameterPointer = a.getDimensionPixelSize(R.styleable.RingColorPicker_diameterPointer, mRingWidth/2);
        } finally {
            a.recycle();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);

        int padding = getMaxPadding();
        if (padding == 0){
            padding = w/7;
        }

        mOuterRadius = Math.min(mHalfWidth, mHalfHeight) - mColorPaint.getStrokeWidth()/2 - padding - HANDLE_PADDING;
        mInnerRadius = mOuterRadius - mColorPaint.getStrokeWidth()/2 - mGapWidth;


        // Ovalos auxiliares para dibujar los arcos del puntero (pointer)
        RectF ovalo = new RectF(
                mOuterRadius + mColorPaint.getStrokeWidth()/2f - mDiameterPointer + AJUSTE,
                -mDiameterPointer /2f,
                mOuterRadius + mColorPaint.getStrokeWidth()/2f + AJUSTE,
                mDiameterPointer /2f );
        RectF ovalo2 = new RectF(
                mOuterRadius + mColorPaint.getStrokeWidth()/2f + AJUSTE - mDiameterPointer /2f - mDiameterPointer,
                -mDiameterPointer /2f,
                mOuterRadius + mColorPaint.getStrokeWidth()/2f + AJUSTE - mDiameterPointer /2f + mDiameterPointer,
                mDiameterPointer /2f + mDiameterPointer);
        RectF ovalo3 = new RectF(
                mOuterRadius + mColorPaint.getStrokeWidth()/2f + AJUSTE - mDiameterPointer /2f - mDiameterPointer,
                -mDiameterPointer /2f - mDiameterPointer,
                mOuterRadius + mColorPaint.getStrokeWidth()/2f + AJUSTE - mDiameterPointer /2f + mDiameterPointer,
                -mDiameterPointer /2f + mDiameterPointer);

        //tres medios circulos cerrados del puntero
        pathPointerPart1 = new Path();
        pathPointerPart1.addArc(ovalo, 270, 180);
        pathPointerPart1.addArc(ovalo2, 210, 60);
        pathPointerPart1.addArc(ovalo3, 90, 60);
        pathPointerPart1.close();

        //dibuja un triangulo interior en el puntero
        pathPointerPart2 = new Path();
        pathPointerPart2.moveTo(mOuterRadius + mColorPaint.getStrokeWidth()/2f + AJUSTE - mDiameterPointer /2f, -mDiameterPointer /2f );
        pathPointerPart2.lineTo(mOuterRadius + mColorPaint.getStrokeWidth()/2f + AJUSTE - mDiameterPointer /2f, mDiameterPointer /2f);
        pathPointerPart2.lineTo(-((float)Math.sqrt((float)Math.pow(mDiameterPointer, 2)-(float)Math.pow(mDiameterPointer /2f, 2)))+mOuterRadius + mColorPaint.getStrokeWidth()/2f - mDiameterPointer /2f + AJUSTE, 0f); //trigonometria aplicada para el triangulo interior
        pathPointerPart2.lineTo(mOuterRadius + mColorPaint.getStrokeWidth()/2f + AJUSTE - mDiameterPointer /2f, -mDiameterPointer /2f);
        pathPointerPart2.close();

        //contorno del puntero
        pathPointerPart3 = new Path();
        pathPointerPart3.addArc(ovalo, 270, 180);
        pathPointerPart3.addArc(ovalo2, 210, 60);
        pathPointerPart3.addArc(ovalo3, 90, 60);

        //base de fundo detras del anillo de colores para colocar la sombra
        pathRingBackground = new Path();
        pathRingBackground.addCircle(0,0, mOuterRadius + mColorPaint.getStrokeWidth()/2, Path.Direction.CW);
        pathRingBackground.addCircle(0,0, mOuterRadius - mColorPaint.getStrokeWidth()/2, Path.Direction.CW);
        pathRingBackground.setFillType(Path.FillType.EVEN_ODD);

    }


    @Override
    protected void onDraw(Canvas canvas) {
        canvas.translate(mHalfWidth, mHalfHeight);

        // anillo de color blanco para la base de la sombra
        canvas.drawPath(pathRingBackground, pincelBlanco3);

        // outer ring
        canvas.drawCircle(0, 0, mOuterRadius, mColorPaint);

        // inner circle
        canvas.drawCircle(0, 0, mInnerRadius, mInnerPaint);

        // rotate handle
        canvas.rotate(mHue);

        //ring's inner pointer (puntero interno del anillo)
        canvas.drawPath(pathPointerPart1, pincelBlanco);
        canvas.drawPath(pathPointerPart2, pincelBlanco2);
        canvas.drawPath(pathPointerPart3, pincelNegro2);

        canvas.drawCircle(0,0,mHalfWidth, pincelContornoNegro);

    }

    @Override
    protected void handleTouch(int motionAction, float x, float y) {
        // set origin to center
        x -= mHalfWidth;
        y -= mHalfHeight;

        float dist = Utils.getDistance(0, 0, x, y);

        boolean isTouchingRing = dist > mInnerRadius + mGapWidth - HANDLE_PADDING
                && dist < mOuterRadius + mColorPaint.getStrokeWidth()/2 + HANDLE_PADDING;
        boolean isTouchingCenter = dist < mInnerRadius;

        switch (motionAction) {
            case MotionEvent.ACTION_DOWN:
                // check if touching handle
                float angle = Utils.normalizeAngle(Utils.getAngleDeg(0, 0, x, y));
                float absDiff = Math.abs(angle - mHue);
                absDiff = absDiff > 180 ? 360 - absDiff : absDiff;
                float touchDist = (float)Math.toRadians(absDiff) * dist;
                mDragging = touchDist < mTouchSize/2 && isTouchingRing;
                break;

            case MotionEvent.ACTION_MOVE:
                // check if dragging AND touching ring
                if (mDragging && !isTouchingCenter)
                    moveHandleTo(x, y);
                break;

            case MotionEvent.ACTION_UP:
                if (mDragging) {
                    // release handle if dragging
                    mDragging = false;
                } else if (mOnColorPickedListener != null && isTouchingCenter) {
                    // fire event if touching center
                    mOnColorPickedListener.colorPicked(ColorUtils.getColorFromHue(mHue));
                    playSoundEffect(SoundEffectConstants.CLICK);
                } else if (isTouchingRing) {
                    animateHandleTo(x, y);
                }
                break;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int min = Math.min(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
        setMeasuredDimension(min, min);
    }


    @Override
    protected void moveHandleTo(float x, float y) {
        float angle = Utils.normalizeAngle(Utils.getAngleDeg(0, 0, x, y));
        moveHandleTo(angle);
    }

    private void moveHandleTo(float angle) {
        mHue = Utils.normalizeAngle(angle);
        int color = ColorUtils.getColorFromHSV(mHue, mSat, mVal);

        // repaint
        mInnerPaint.setColor(color);
        mHandlePaint.setColor(color);
        invalidate();

        // fire event
        if (mOnColorChangedListener != null)
            mOnColorChangedListener.colorChanged(color);

        // set linear pickers if attached
        if (mSatLCP != null)
            mSatLCP.updateHSV(mHue, mSat, mVal);
        if (mValLCP != null)
            mValLCP.updateHSV(mHue, mSat, mVal);
    }

    @Override
    protected void animateHandleTo(float x, float y) {
        float angle = Utils.normalizeAngle(Utils.getAngleDeg(0, 0, x, y));
        animateHandleTo(angle);
    }

    private void animateHandleTo(float angle) {
        float diff = mHue - angle;

        // correct angles
        if (diff < -180) diff += 360;
        else if (diff > 180) diff -= 360;

        // start animating
        ValueAnimator anim = ValueAnimator.ofFloat(mHue, mHue - diff);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                moveHandleTo((float) animation.getAnimatedValue());
            }
        });
        anim.start();
    }

    /*
    SETTERS/GETTERS
     */

    @Override
    public void setColor(int color) {
        float angle = ColorUtils.getHueFromColor(color);
        mSat = ColorUtils.getSaturationFromColor(color);
        mVal = ColorUtils.getValueFromColor(color);
        mColorPaint.setShader(new SweepGradient(0, 0, ColorUtils.getHueRingColors(7, mSat, mVal), null));
        animateHandleTo(angle);
    }

    /**
     * Set outer color ring width
     * @param ringWidth outer ring width in pixels
     */
    public void setRingWidth(int ringWidth) {
        mRingWidth = ringWidth;
        mColorPaint.setStrokeWidth(mRingWidth);

        onSizeChanged(getWidth(), getHeight(), 0, 0);
        invalidate();
    }

    /**
     * Get outer color ring width
     * @return outer ring width in pixels
     */
    public int getRingWidth() {
        return mRingWidth;
    }

    /**
     * Set gap width between outer ring and inner circle. Values are clamped
     * @param gapWidth gap width in pixels
     */
    public void setGapWidth(int gapWidth) {
        mGapWidth = Math.max(gapWidth, HANDLE_PADDING * 2);//(int)Utils.clamp(gapWidth, HANDLE_PADDING*2, mHandleRect.width());

        onSizeChanged(getWidth(), getHeight(), 0, 0);
        invalidate();
    }

    /**
     * Get gap width between outer ring and inner circle.
     * @return gap width in pixels
     */
    public int getGapWidth() {
        return mGapWidth;
    }

    public void setSaturationLinearColorPicker(SaturationLinearColorPicker lcp) {
        mSatLCP = lcp;
        if (mSatLCP != null) {
            mSatLCP.updateHSV(mHue, mSat, mVal);
            mSatLCP.setOnColorChangedListener(new OnColorChangedListener() {
                @Override
                public void colorChanged(int color) {
                    mSat = ColorUtils.getSaturationFromColor(color);
                    mColorPaint.setShader(new SweepGradient(0, 0, ColorUtils.getHueRingColors(7, mSat, mVal), null));
                    mHandlePaint.setColor(color);
                    mInnerPaint.setColor(color);
                    if (mValLCP != null)
                        mValLCP.updateHSV(mHue, mSat, mVal);
                    invalidate();
                }
            });
        }
    }

    public void setValueLinearColorPicker(ValueLinearColorPicker lcp) {
        mValLCP = lcp;
        if (mValLCP != null) {
            mValLCP.updateHSV(mHue, mSat, mVal);
            mValLCP.setOnColorChangedListener(new OnColorChangedListener() {
                @Override
                public void colorChanged(int color) {
                    mVal = ColorUtils.getValueFromColor(color);
                    mColorPaint.setShader(new SweepGradient(0, 0, ColorUtils.getHueRingColors(7, mSat, mVal), null));
                    mHandlePaint.setColor(color);
                    mInnerPaint.setColor(color);
                    if (mSatLCP != null)
                        mSatLCP.updateHSV(mHue, mSat, mVal);
                    invalidate();
                }
            });
        }
    }
}