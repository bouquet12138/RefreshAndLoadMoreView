package com.example.dropdownloadview.anim;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class PaddingTopAnimation extends Animation {

    private int mStartY;
    private int mTotalY;
    private View view;

    public PaddingTopAnimation(int startY, int targetY, View view) {
        this.mStartY = startY;
        this.mTotalY = targetY - startY;
        this.view = view;
        //setDuration(Math.abs((targetY - startY)* 100/headViewHeight ));

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

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        super.applyTransformation(interpolatedTime, t);
        int currentPaddingY = (int) (mStartY + mTotalY * interpolatedTime);
        view.setPadding(0, currentPaddingY, 0, 0);
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
