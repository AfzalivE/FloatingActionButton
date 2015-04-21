package com.github.clans.fab;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Xfermode;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.Shape;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageButton;
import android.widget.TextView;

public class FloatingActionButton extends ImageButton {

    public static final int SIZE_NORMAL = 0;
    public static final int SIZE_MINI = 1;

    int mFabSize;
    boolean mShowShadow;
    int mShadowColor;
    int mShadowRadius = Util.dpToPx(getContext(), 4f);
    int mShadowXOffset = Util.dpToPx(getContext(), 1f);
    int mShadowYOffset = Util.dpToPx(getContext(), 3f);

    private static final Xfermode PORTER_DUFF_CLEAR = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);

    private int mColorNormal;
    private int mColorPressed;
    private int mColorRipple;
    private Drawable mIcon;
    private int mIconSize = Util.dpToPx(getContext(), 24f);
    private Animation mShowAnimation;
    private Animation mHideAnimation;
    private String mLabelText;
    private OnClickListener mClickListener;
    private Drawable mBackgroundDrawable;
    private boolean mUsingElevation;
    private boolean mUsingElevationCompat;

    public FloatingActionButton(Context context) {
        this(context, null);
    }

    public FloatingActionButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FloatingActionButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public FloatingActionButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray attr = context.obtainStyledAttributes(attrs, R.styleable.FloatingActionButton, defStyleAttr, 0);
        mColorNormal = attr.getColor(R.styleable.FloatingActionButton_fab_colorNormal, 0xFFDA4336);
        mColorPressed = attr.getColor(R.styleable.FloatingActionButton_fab_colorPressed, 0xFFE75043);
        mColorRipple = attr.getColor(R.styleable.FloatingActionButton_fab_colorRipple, 0x99FFFFFF);
        mShowShadow = attr.getBoolean(R.styleable.FloatingActionButton_fab_showShadow, true);
        mShadowColor = attr.getColor(R.styleable.FloatingActionButton_fab_shadowColor, 0x66000000);
        mShadowRadius = attr.getDimensionPixelSize(R.styleable.FloatingActionButton_fab_shadowRadius, mShadowRadius);
        mShadowXOffset = attr.getDimensionPixelSize(R.styleable.FloatingActionButton_fab_shadowXOffset, mShadowXOffset);
        mShadowYOffset = attr.getDimensionPixelSize(R.styleable.FloatingActionButton_fab_shadowYOffset, mShadowYOffset);
        mFabSize = attr.getInt(R.styleable.FloatingActionButton_fab_size, SIZE_NORMAL);
        mLabelText = attr.getString(R.styleable.FloatingActionButton_fab_label);
        if (attr.hasValue(R.styleable.FloatingActionButton_fab_elevationCompat)) {
            float elevation = attr.getDimensionPixelOffset(R.styleable.FloatingActionButton_fab_elevationCompat, 0);
            setElevationCompat(elevation);

            if (isInEditMode()) {
                setElevation(elevation);
            }
        }
        initShowAnimation(attr);
        initHideAnimation(attr);
        attr.recycle();

        updateBackground();
    }

    private void initShowAnimation(TypedArray attr) {
        int resourceId = attr.getResourceId(R.styleable.FloatingActionButton_fab_showAnimation, R.anim.fab_scale_up);
        mShowAnimation = AnimationUtils.loadAnimation(getContext(), resourceId);
    }

    private void initHideAnimation(TypedArray attr) {
        int resourceId = attr.getResourceId(R.styleable.FloatingActionButton_fab_hideAnimation, R.anim.fab_scale_down);
        mHideAnimation = AnimationUtils.loadAnimation(getContext(), resourceId);
    }

    private int getCircleSize() {
        return getResources().getDimensionPixelSize(mFabSize == SIZE_NORMAL
                ? R.dimen.fab_size_normal : R.dimen.fab_size_mini);
    }

    private int calculateMeasuredWidth() {
        return getCircleSize() + calculateShadowWidth();
    }

    private int calculateMeasuredHeight() {
        return getCircleSize() + calculateShadowHeight();
    }

    int calculateShadowWidth() {
        return hasShadow() ? getShadowX() * 2 : 0;
    }

    int calculateShadowHeight() {
        return hasShadow() ? getShadowY() * 2 : 0;
    }

    private int getShadowX() {
        return mShadowRadius + Math.abs(mShadowXOffset);
    }

    private int getShadowY() {
        return mShadowRadius + Math.abs(mShadowYOffset);
    }

    private float calculateCenterX() {
        return (float) (getMeasuredWidth() / 2);
    }

    private float calculateCenterY() {
        return (float) (getMeasuredHeight() / 2);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(calculateMeasuredWidth(), calculateMeasuredHeight());
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void setLayoutParams(ViewGroup.LayoutParams params) {
        if (params instanceof ViewGroup.MarginLayoutParams && mUsingElevationCompat) {
            ((ViewGroup.MarginLayoutParams) params).leftMargin += getShadowX();
            ((ViewGroup.MarginLayoutParams) params).topMargin += getShadowY();
            ((ViewGroup.MarginLayoutParams) params).rightMargin += getShadowX();
            ((ViewGroup.MarginLayoutParams) params).bottomMargin += getShadowY();
        }
        super.setLayoutParams(params);
    }

    void updateBackground() {
        LayerDrawable layerDrawable;
        if (hasShadow()) {
            layerDrawable = new LayerDrawable(new Drawable[] {
                    new Shadow(),
                    createFillDrawable(),
                    getIconDrawable()
            });
        } else {
            layerDrawable = new LayerDrawable(new Drawable[] {
                    createFillDrawable(),
                    getIconDrawable()
            });
        }

        int iconSize = -1;
        if (getIconDrawable() != null) {
            iconSize = Math.max(getIconDrawable().getIntrinsicWidth(), getIconDrawable().getIntrinsicHeight());
        }
        int iconOffset = (getCircleSize() - (iconSize > 0 ? iconSize : mIconSize)) / 2;
        int circleInsetHorizontal = hasShadow() ? mShadowRadius + Math.abs(mShadowXOffset) : 0;
        int circleInsetVertical = hasShadow() ? mShadowRadius + Math.abs(mShadowYOffset) : 0;

        /*layerDrawable.setLayerInset(
                mShowShadow ? 1 : 0,
                circleInsetHorizontal,
                circleInsetVertical,
                circleInsetHorizontal,
                circleInsetVertical
        );*/
        layerDrawable.setLayerInset(
                hasShadow() ? 2 : 1,
                circleInsetHorizontal + iconOffset,
                circleInsetVertical + iconOffset,
                circleInsetHorizontal + iconOffset,
                circleInsetVertical + iconOffset
        );

        setBackgroundCompat(layerDrawable);
    }

    protected Drawable getIconDrawable() {
        if (mIcon != null) {
            return mIcon;
        } else {
            return new ColorDrawable(Color.TRANSPARENT);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private Drawable createFillDrawable() {
        StateListDrawable drawable = new StateListDrawable();
        drawable.addState(new int[] {android.R.attr.state_pressed}, createCircleDrawable(mColorPressed));
        drawable.addState(new int[] {}, createCircleDrawable(mColorNormal));

        if (Util.hasLollipop()) {
            RippleDrawable ripple = new RippleDrawable(new ColorStateList(new int[][] {{}},
                    new int[] {mColorRipple}), drawable, null);
            setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setOval(0, 0, view.getWidth(), view.getHeight());
                }
            });
            setClipToOutline(true);
            mBackgroundDrawable = ripple;
            return ripple;
        }

        mBackgroundDrawable = drawable;
        return drawable;
    }

    private Drawable createCircleDrawable(int color) {
        CircleDrawable shapeDrawable = new CircleDrawable(new OvalShape());
        shapeDrawable.getPaint().setColor(color);
        return shapeDrawable;
    }

    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void setBackgroundCompat(Drawable drawable) {
        if (Util.hasJellyBean()) {
            setBackground(drawable);
        } else {
            setBackgroundDrawable(drawable);
        }
    }

    Animation getShowAnimation() {
        return mShowAnimation;
    }

    Animation getHideAnimation() {
        return mHideAnimation;
    }

    void playShowAnimation() {
        startAnimation(mShowAnimation);
    }

    void playHideAnimation() {
        startAnimation(mHideAnimation);
    }

    OnClickListener getOnClickListener() {
        return mClickListener;
    }

    TextView getLabelView() {
        return (TextView) getTag(R.id.fab_label);
    }

    void setColors(int colorNormal, int colorPressed, int colorRipple) {
        mColorNormal = colorNormal;
        mColorPressed = colorPressed;
        mColorRipple = colorRipple;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    void onActionDown() {
        if (mBackgroundDrawable instanceof StateListDrawable) {
            StateListDrawable drawable = (StateListDrawable) mBackgroundDrawable;
            drawable.setState(new int[] {android.R.attr.state_pressed});
        } else if (Util.hasLollipop()) {
            RippleDrawable ripple = (RippleDrawable) mBackgroundDrawable;
            ripple.setState(new int[] {android.R.attr.state_enabled, android.R.attr.state_pressed});
            ripple.setHotspot(calculateCenterX(), calculateCenterY());
            ripple.setVisible(true, true);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    void onActionUp() {
        if (mBackgroundDrawable instanceof StateListDrawable) {
            StateListDrawable drawable = (StateListDrawable) mBackgroundDrawable;
            drawable.setState(new int[] {});
        } else if (Util.hasLollipop()) {
            RippleDrawable ripple = (RippleDrawable) mBackgroundDrawable;
            ripple.setState(new int[] {});
            ripple.setHotspot(calculateCenterX(), calculateCenterY());
            ripple.setVisible(true, true);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mClickListener == null) return false;

        Label label = (Label) getTag(R.id.fab_label);
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_UP:
                if (label != null) {
                    label.onActionUp();
                }
                break;
        }

        mGestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    GestureDetector mGestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {

        @Override
        public boolean onDown(MotionEvent e) {
            Label label = (Label) getTag(R.id.fab_label);
            if (label != null) {
                label.onActionDown();
            }
            onActionDown();
            return super.onDown(e);
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            Label label = (Label) getTag(R.id.fab_label);
            if (label != null) {
                label.onActionUp();
            }
            onActionUp();
            return super.onSingleTapUp(e);
        }
    });

    private class CircleDrawable extends ShapeDrawable {

        private int circleInsetHorizontal;
        private int circleInsetVertical;

        private CircleDrawable() {
        }

        private CircleDrawable(Shape s) {
            super(s);
            circleInsetHorizontal = hasShadow() ? mShadowRadius + Math.abs(mShadowXOffset) : 0;
            circleInsetVertical = hasShadow() ? mShadowRadius + Math.abs(mShadowYOffset) : 0;
        }

        @Override
        public void draw(Canvas canvas) {
            setBounds(circleInsetHorizontal, circleInsetVertical, calculateMeasuredWidth()
                    - circleInsetHorizontal, calculateMeasuredHeight() - circleInsetVertical);
            super.draw(canvas);
        }
    }

    private class Shadow extends Drawable {

        private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private Paint mErase = new Paint(Paint.ANTI_ALIAS_FLAG);
        private float mRadius;

        private Shadow() {
            this.init();
        }

        private void init() {
            setLayerType(LAYER_TYPE_SOFTWARE, null);
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(mColorNormal);

            mErase.setXfermode(PORTER_DUFF_CLEAR);

            if (!isInEditMode()) {
                mPaint.setShadowLayer(mShadowRadius, mShadowXOffset, mShadowYOffset, mShadowColor);
            }

            mRadius = getCircleSize() / 2;
        }

        @Override
        public void draw(Canvas canvas) {
            canvas.drawCircle(calculateCenterX(), calculateCenterY(), mRadius, mPaint);
            canvas.drawCircle(calculateCenterX(), calculateCenterY(), mRadius, mErase);
        }

        @Override
        public void setAlpha(int alpha) {

        }

        @Override
        public void setColorFilter(ColorFilter cf) {

        }

        @Override
        public int getOpacity() {
            return 0;
        }
    }

    /* ===== API methods ===== */

    @Override
    public void setImageDrawable(Drawable drawable) {
        if (mIcon != drawable) {
            mIcon = drawable;
            updateBackground();
        }
    }

    @Override
    public void setImageResource(int resId) {
        Drawable drawable = getResources().getDrawable(resId);
        if (mIcon != drawable) {
            mIcon = drawable;
            updateBackground();
        }
    }

    @Override
    public void setOnClickListener(final OnClickListener l) {
        super.setOnClickListener(l);
        mClickListener = l;
        View label = (View) getTag(R.id.fab_label);
        if (label != null) {
            label.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mClickListener != null) {
                        mClickListener.onClick(FloatingActionButton.this);
                    }
                }
            });
        }
    }

    /**
     * Sets the size of the <b>FloatingActionButton</b> and invalidates its layout.
     *
     * @param size size of the <b>FloatingActionButton</b>. Accepted values: SIZE_NORMAL, SIZE_MINI.
     */
    public void setButtonSize(int size) {
        if (size != SIZE_NORMAL && size != SIZE_MINI) {
            throw new IllegalArgumentException("Use @FabSize constants only!");
        }

        if (mFabSize != size) {
            mFabSize = size;
            updateBackground();
        }
    }

    public int getButtonSize() {
        return mFabSize;
    }

    public void setColorNormal(int color) {
        if (mColorNormal != color) {
            mColorNormal = color;
            updateBackground();
        }
    }

    public void setColorNormalResId(int colorResId) {
        setColorNormal(getResources().getColor(colorResId));
    }

    public int getColorNormal() {
        return mColorNormal;
    }

    public void setColorPressed(int color) {
        if (color != mColorPressed) {
            mColorPressed = color;
            updateBackground();
        }
    }

    public void setColorPressedResId(int colorResId) {
        setColorPressed(getResources().getColor(colorResId));
    }

    public int getColorPressed() {
        return mColorPressed;
    }

    public void setColorRipple(int color) {
        if (color != mColorRipple) {
            mColorRipple = color;
            updateBackground();
        }
    }

    public void setColorRippleResId(int colorResId) {
        setColorRipple(getResources().getColor(colorResId));
    }

    public int getColorRipple() {
        return mColorRipple;
    }

    public void setShowShadow(boolean show) {
        if (mShowShadow != show) {
            mShowShadow = show;
            updateBackground();
        }
    }

    public boolean hasShadow() {
        return !mUsingElevation && mShowShadow;
    }

    /**
     * Sets the shadow radius of the <b>FloatingActionButton</b> and invalidates its layout.
     *
     * @param dimenResId the resource identifier of the dimension
     */
    public void setShadowRadius(int dimenResId) {
        int shadowRadius = getResources().getDimensionPixelSize(dimenResId);
        if (mShadowRadius != shadowRadius) {
            mShadowRadius = shadowRadius;
            requestLayout();
            updateBackground();
        }
    }

    /**
     * Sets the shadow radius of the <b>FloatingActionButton</b> and invalidates its layout.
     * <p>
     * Must be specified in density-independent (dp) pixels, which are then converted into actual
     * pixels (px).
     *
     * @param shadowRadiusDp shadow radius specified in density-independent (dp) pixels
     */
    public void setShadowRadius(float shadowRadiusDp) {
        mShadowRadius = Util.dpToPx(getContext(), shadowRadiusDp);
        requestLayout();
        updateBackground();
    }

    public int getShadowRadius() {
        return mShadowRadius;
    }

    /**
     * Sets the shadow x offset of the <b>FloatingActionButton</b> and invalidates its layout.
     *
     * @param dimenResId the resource identifier of the dimension
     */
    public void setShadowXOffset(int dimenResId) {
        int shadowXOffset = getResources().getDimensionPixelSize(dimenResId);
        if (mShadowXOffset != shadowXOffset) {
            mShadowXOffset = shadowXOffset;
            requestLayout();
            updateBackground();
        }
    }

    /**
     * Sets the shadow x offset of the <b>FloatingActionButton</b> and invalidates its layout.
     * <p>
     * Must be specified in density-independent (dp) pixels, which are then converted into actual
     * pixels (px).
     *
     * @param shadowXOffsetDp shadow radius specified in density-independent (dp) pixels
     */
    public void setShadowXOffset(float shadowXOffsetDp) {
        mShadowXOffset = Util.dpToPx(getContext(), shadowXOffsetDp);
        requestLayout();
        updateBackground();
    }

    public int getShadowXOffset() {
        return mShadowXOffset;
    }

    /**
     * Sets the shadow y offset of the <b>FloatingActionButton</b> and invalidates its layout.
     *
     * @param dimenResId the resource identifier of the dimension
     */
    public void setShadowYOffset(int dimenResId) {
        int shadowYOffset = getResources().getDimensionPixelSize(dimenResId);
        if (mShadowYOffset != shadowYOffset) {
            mShadowYOffset = shadowYOffset;
            requestLayout();
            updateBackground();
        }
    }

    /**
     * Sets the shadow y offset of the <b>FloatingActionButton</b> and invalidates its layout.
     * <p>
     * Must be specified in density-independent (dp) pixels, which are then converted into actual
     * pixels (px).
     *
     * @param shadowYOffsetDp shadow radius specified in density-independent (dp) pixels
     */
    public void setShadowYOffset(float shadowYOffsetDp) {
        mShadowYOffset = Util.dpToPx(getContext(), shadowYOffsetDp);
        requestLayout();
        updateBackground();
    }

    public int getShadowYOffset() {
        return mShadowYOffset;
    }

    public void setShadowColorResource(int colorResId) {
        int shadowColor = getResources().getColor(colorResId);
        if (mShadowColor != shadowColor) {
            mShadowColor = shadowColor;
            updateBackground();
        }
    }

    public void setShadowColor(int color) {
        if (mShadowColor != color) {
            mShadowColor = color;
            updateBackground();
        }
    }

    public int getShadowColor() {
        return mShadowColor;
    }

    /**
     * Checks whether <b>FloatingActionButton</b> is hidden
     *
     * @return true if <b>FloatingActionButton</b> is hidden, false otherwise
     */
    public boolean isHidden() {
        return getVisibility() == INVISIBLE;
    }

    /**
     * Makes the <b>FloatingActionButton</b> to appear and sets its visibility to {@link #VISIBLE}
     *
     * @param showAnimation plays specified show animation
     */
    public void show(int showAnimation) {
        if (isHidden()) {
            startAnimation(AnimationUtils.loadAnimation(getContext(), showAnimation));
            setVisibility(VISIBLE);
        }
    }

    /**
     * Makes the <b>FloatingActionButton</b> to appear and sets its visibility to {@link #VISIBLE}
     *
     * @param animate if true - plays "show animation"
     */
    public void show(boolean animate) {
        if (isHidden()) {
            if (animate) {
                playShowAnimation();
            }
            setVisibility(VISIBLE);
        }
    }

    /**
     * Makes the <b>FloatingActionButton</b> to disappear and sets its visibility to {@link #INVISIBLE}
     *
     * @param hideAnimation plays specified hide animation
     */
    public void hide(int hideAnimation) {
        if (!isHidden()) {
            startAnimation(AnimationUtils.loadAnimation(getContext(), hideAnimation));
            setVisibility(INVISIBLE);
        }
    }

    /**
     * Makes the <b>FloatingActionButton</b> to disappear and sets its visibility to {@link #INVISIBLE}
     *
     * @param animate if true - plays "hide animation"
     */
    public void hide(boolean animate) {
        if (!isHidden()) {
            if (animate) {
                playHideAnimation();
            }
            setVisibility(INVISIBLE);
        }
    }

    public void toggle(boolean animate) {
        if (isHidden()) {
            show(animate);
        } else {
            hide(animate);
        }
    }

    public void setLabelText(String text) {
        mLabelText = text;
        TextView labelView = getLabelView();
        if (labelView != null) {
            labelView.setText(text);
        }
    }

    public String getLabelText() {
        return mLabelText;
    }

    public void setShowAnimation(Animation showAnimation) {
        mShowAnimation = showAnimation;
    }

    public void setHideAnimation(Animation hideAnimation) {
        mHideAnimation = hideAnimation;
    }

    public void setLabelVisibility(int visibility) {
        TextView labelView = getLabelView();
        if (labelView != null) {
            labelView.setVisibility(visibility);
        }
    }

    public int getLabelVisibility() {
        TextView labelView = getLabelView();
        if (labelView != null) {
            return labelView.getVisibility();
        }

        return -1;
    }

    @Override
    public void setElevation(float elevation) {
        if (Util.hasLollipop() && elevation > 0) {
            super.setElevation(elevation);
            mUsingElevation = true;
            mShowShadow = false;
            updateBackground();
        }
    }

    /**
     * Sets the shadow color and radius to mimic the native elevation.
     *
     * <p><b>API 21+</b>: Sets the native elevation of this view, in pixels. Updates margins to
     * make the view hold its position in layout across different platform versions.</p>
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void setElevationCompat(float elevation) {
        mShadowColor = 0x26000000;
        mShadowRadius = Math.round(elevation / 2);
        mShadowXOffset = 0;
        mShadowYOffset = Math.round(mFabSize == SIZE_NORMAL ? elevation : elevation / 2);

        if (Util.hasLollipop()) {
            super.setElevation(elevation);
            mUsingElevationCompat = true;
            mShowShadow = false;
            updateBackground();

            ViewGroup.LayoutParams layoutParams = getLayoutParams();
            if (layoutParams != null) {
                setLayoutParams(layoutParams);
            }
        } else {
            mShowShadow = true;
            updateBackground();
        }
    }

    /**
     * Attaches the fab to a RecyclerView to toggle hide/show animation on scroll.
     * The slide up/down animation is used.
     *
     * @param recyclerView The RecyclerView to attach with
     */
    public void attachToRecyclerView(RecyclerView recyclerView) {
        attachToRecyclerView(recyclerView, 4);
    }

    /**
     * Attaches the fab to a RecyclerView to toggle hide/show animation on scroll.
     * The slide up/down animation is used
     *
     * @param recyclerView The RecyclerView to attach with
     * @param scrollOffset
     */
    private void attachToRecyclerView(RecyclerView recyclerView, final int scrollOffset) {
        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (Math.abs(dy) > scrollOffset) {
                    if (dy > 0) {
                        hide(R.anim.fab_hide_to_bottom);
                    } else {
                        show(R.anim.fab_show_from_bottom);
                    }
                }
            }
        });
    }

    /**
     * Attaches the fab to a AbsListView to toggle hide/show animation on scroll.
     * The slide up/down animation is used
     *
     * @param listView The AbsListView to attach with
     */
    public void attachToListView(AbsListView listView) {
        listView.setOnScrollListener(new OnScrollListener() {
            public int mPreviousVisibleItem;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem > mPreviousVisibleItem) {
                    hide(R.anim.fab_hide_to_bottom);
                } else if (firstVisibleItem < mPreviousVisibleItem) {
                    show(R.anim.fab_show_from_bottom);
                }
                mPreviousVisibleItem = firstVisibleItem;
            }
        });
    }
}
