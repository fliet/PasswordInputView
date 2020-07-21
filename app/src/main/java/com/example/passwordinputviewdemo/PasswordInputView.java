package com.example.passwordinputviewdemo;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wp
 * @date 2018/8/10.
 */

/*
    关键点：
        调用drawRect()和drawLine()绘制矩形和线段的时候，我们会指定left和top，
        其实left和top指定的是中间坐标，
        也就是说，如果我们对Paint设置线条的宽度为6，那么实际上是在left左侧绘制3像素的宽度，left右侧绘制3像素的宽度

        所以，对View进行绘制的时候需要在原本长度和宽度的基础上加上边框的宽度
 */
public class PasswordInputView extends View {

    private float textSize;
    private int numberCount;
    private float sideLength;
    private int cursorAlpha;

    private Paint borderPaint = new Paint();
    private Paint cursorPaint = new Paint();
    private Paint textPaint = new Paint();
    private List<Integer> numList = new ArrayList<>();
    private InputMethodManager imm;
    private float margin;
    private Context context;
    private float cursorWidth;
    private float cursorHeight;
    private int cursorColor;
    private int borderColor;
    private int selectBorderColor;
    private float borderWidth;
    private int textColor;
    private String inputType;
    private int dotRadius;

    private OnInputFinishListener onInputFinishListener;

    public PasswordInputView(Context context) {
        super(context);
    }

    public PasswordInputView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        this.context = context;
        margin = DensityUtil.dip2px(context, 10);
        dotRadius = DensityUtil.dip2px(context, 4);

        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.PasswordInputView);
        textSize = typedArray.getDimension(R.styleable.PasswordInputView_text_size, 100);
        numberCount = typedArray.getInt(R.styleable.PasswordInputView_number_count, 5);
        sideLength = typedArray.getDimension(R.styleable.PasswordInputView_side_length, 180);
        cursorWidth = typedArray.getDimension(R.styleable.PasswordInputView_cursor_width, 0);
        cursorHeight = typedArray.getDimension(R.styleable.PasswordInputView_cursor_height, 0);
        cursorColor = typedArray.getColor(R.styleable.PasswordInputView_cursor_color, Color.parseColor("#ffffff"));
        borderColor = typedArray.getColor(R.styleable.PasswordInputView_border_color, Color.parseColor("#ffffff"));
        selectBorderColor = typedArray.getColor(R.styleable.PasswordInputView_select_border_color, Color.parseColor("#ffffff"));
        borderWidth = typedArray.getDimension(R.styleable.PasswordInputView_border_width, DensityUtil.dip2px(context, 1));
        textColor = typedArray.getColor(R.styleable.PasswordInputView_text_color, Color.parseColor("#ffffff"));
        margin = typedArray.getDimension(R.styleable.PasswordInputView_border_margin, DensityUtil.dip2px(context, 5));
        inputType = typedArray.getString(R.styleable.PasswordInputView_input_type);
        typedArray.recycle();

        borderPaint.setColor(Color.GRAY);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(borderWidth);

        cursorPaint.setStrokeWidth(DensityUtil.dip2px(context, 2));
        cursorPaint.setColor(cursorColor);

        textPaint.setTextSize(textSize);
        textPaint.setColor(textColor);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setAntiAlias(true);


        imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        this.setFocusable(true);
        this.setFocusableInTouchMode(true);

        // 设置光标闪烁动画
        final ObjectAnimator cursorAnimator = ObjectAnimator.ofInt(this, "cursorAlpha", 255, 0);
        cursorAnimator.setDuration(1000);
        cursorAnimator.setRepeatMode(ValueAnimator.RESTART);
        cursorAnimator.setRepeatCount(ValueAnimator.INFINITE);
        cursorAnimator.start();

        // 开启软键盘点击监听
        this.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9 && numList.size() != numberCount) {
                        // 如果点击的是数字键，那么收集数据，关闭当前光标动画，光标后移，开启下一光标动画
                        cursorAnimator.cancel();
                        numList.add(keyCode - 7);
                        cursorAnimator.start();

                        if (numList.size() == numberCount && onInputFinishListener != null) {
                            StringBuilder sb = new StringBuilder();
                            for (int num : numList) {
                                sb.append(num);
                            }
                            onInputFinishListener.onInputFinish(sb.toString());
                        }
                    }

                    if (keyCode == KeyEvent.KEYCODE_DEL) {
                        // 如果点击的是删除键，删除数据，关闭当前光标动画，光标前移，开启下一光标动画
                        if (numList.size() > 0) {
                            cursorAnimator.cancel();
                            numList.remove(numList.size() - 1);
                            cursorAnimator.start();
                        }
                    }

                    if (keyCode == KeyEvent.KEYCODE_ENTER) {

                    }
                }

                return false;
            }
        });


    }

    public PasswordInputView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        // 此处需要添加边框的宽度。见上方解释
        setMeasuredDimension(widthMode == MeasureSpec.EXACTLY ? widthSize : (int) (numberCount * sideLength + margin * (numberCount - 1) + borderWidth),
                heightMode == MeasureSpec.EXACTLY ? heightSize : (int) (sideLength + borderWidth));
    }

    @Override
    protected void onDraw(Canvas canvas) {

        // 注意这里
        float left = borderWidth / 2;
        float top = borderWidth / 2;

        for (int i = 0; i < sideLength; i++) {
            if (i != 0) left = left + margin + sideLength;

            borderPaint.setColor(i <= numList.size() ? selectBorderColor : borderColor);
            // a. 绘制边框
            canvas.drawRect(left, top, left + sideLength, top + sideLength, borderPaint);

            if (i == numList.size()) {
                // b. 绘制光标
                drawCursor(canvas, left, top);
            }
        }

        if ("1".equals(inputType)) { // 密码
            textPaint.setStyle(Paint.Style.FILL);
            drawDot(canvas);
        } else if ("2".equals(inputType)) { // 验证码
            // c. 绘制文字
            drawText(canvas);
        }
    }

    private void drawCursor(Canvas canvas, float left, float top) {
        if (cursorHeight == 0 || cursorWidth == 0) return;

        left = left + sideLength / 2;
        top = top + (sideLength - cursorHeight) / 2;
        canvas.drawLine(left, top, left, top + cursorHeight, cursorPaint);
    }

    private void drawDot(Canvas canvas) {
        int inputLength = numList.size();
        if (inputLength == 0) return;

        float left = borderWidth / 2;
        int height = getHeight();
        float cy = height / 2f;

        for (int i = 0; i < inputLength; i++) {
            if (i != 0) left = left + margin + sideLength;
            float startX = left + sideLength / 2;

            canvas.drawCircle(startX, cy, dotRadius, textPaint);
        }
    }

    private void drawText(Canvas canvas) {
        int inputLength = numList.size();
        if (inputLength == 0) return;
        float left = borderWidth / 2;

        int height = getHeight();
        float cy = height / 2f;

        for (int i = 0; i < inputLength; i++) {
            float dx = textPaint.measureText(String.valueOf(numList.get(i))) / 2;
            Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
            float dy = (fontMetrics.ascent + fontMetrics.descent) / 2;

            if (i != 0) left = left + margin + sideLength;
            float startX = left + sideLength / 2;

            canvas.drawText(String.valueOf(numList.get(i)), startX - dx, cy - dy, textPaint);

                /*
                    关于x的计算
                        x设置的是文字左侧在屏幕上的位置，而不是文字中间在屏幕上的位置
                        如果直接设置的话，那么文字会靠右侧
                        所以为了让文字居中，需要左移

                        所以，计算出偏移量后
                            startX - dx

                    关于y的计算
                        我们设置的y值实际上是设置baseline的位置，如果直接将baseline设置在cy的位置
                        那么由于baseline位于文字中下的位置，实际效果会是：文字偏上
                        所以我们需要下移baseline，也就是增加cy的值
                        也就是 cy + 增量= baseline的位置

                        或者说
                                我们想让文字的中间和边框的中间对齐，但是文字的纵向设置的是baseline在边框中的位置


                        那么增加多少呢
                        我们可以计算文字中间的位置向对于baseline的偏移量，
                        ascent位于baseline上方，为负值
                        descent位于baseline下方，为正值

                        ascent的绝对值 大于 descent的绝对值
                        所以 fontMetrics.ascent + fontMetrics.descent < 0


                 */
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            requestFocus();

            imm.showSoftInput(this, InputMethodManager.SHOW_FORCED);
            return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);

        if (!hasWindowFocus) {
            imm.hideSoftInputFromWindow(getWindowToken(), 0);
        }
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        outAttrs.inputType = InputType.TYPE_CLASS_NUMBER;
        outAttrs.imeOptions = EditorInfo.IME_ACTION_DONE;
        return new MyInputConnection(this, false);
    }

    class MyInputConnection extends BaseInputConnection {

        public MyInputConnection(View targetView, boolean fullEditor) {
            super(targetView, fullEditor);
        }

        @Override
        public boolean deleteSurroundingText(int beforeLength, int afterLength) {
            if (beforeLength == 1 && afterLength == 0) {
                return super.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL))
                        && super.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL));
            }
            return super.deleteSurroundingText(beforeLength, afterLength);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        numList.clear();

        // 关闭键盘
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        View v = ((Activity) context).getWindow().peekDecorView();
        if (null != v) {
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }


    public int getCursorAlpha() {
        return cursorAlpha;
    }

    public void setCursorAlpha(int cursorAlpha) {
        this.cursorAlpha = cursorAlpha;
        cursorPaint.setAlpha(cursorAlpha);

        invalidate();
    }

    public interface OnInputFinishListener {
        void onInputFinish(String inputStr);
    }

    public void setOnInputFinishListener(OnInputFinishListener onInputFinishListener) {
        this.onInputFinishListener = onInputFinishListener;
    }
}
