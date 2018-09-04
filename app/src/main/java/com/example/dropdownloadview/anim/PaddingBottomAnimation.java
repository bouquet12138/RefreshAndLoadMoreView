package com.example.dropdownloadview.anim;

import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class PaddingBottomAnimation extends Animation {

    private int mStartY;
    private int mTotalY;
    private View mView;

    /**
     * 构造方法
     *
     * @param startY
     * @param targetY
     * @param view
     */
    public PaddingBottomAnimation(int startY, int targetY, View view) {
        mStartY = startY;
        mTotalY = targetY - startY;
        mView = view;

        setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

                if (mAnimationEndListener != null)
                    mAnimationEndListener.onAnimationEnd();//动画结束

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

    }

    /**
     * 应用变化
     *
     * @param interpolatedTime
     * @param t
     */
    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        super.applyTransformation(interpolatedTime, t);
        int currentPaddingY = (int) (mStartY + mTotalY * interpolatedTime);
        mView.setPadding(0, 0, 0, currentPaddingY);
    }

    private AnimationEndListener mAnimationEndListener;

    /**
     * 设置动画结束监听
     *
     * @param animationEndListener
     */
    public void setAnimationEndListener(AnimationEndListener animationEndListener) {
        mAnimationEndListener = animationEndListener;
    }


}
