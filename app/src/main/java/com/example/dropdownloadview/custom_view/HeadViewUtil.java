package com.example.dropdownloadview.custom_view;

import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.example.dropdownloadview.R;
import com.example.dropdownloadview.anim.PaddingBottomAnimation;
import com.example.dropdownloadview.utils.DensityUtil;

public class HeadViewUtil {

    private View mHeadView;

    private static final String TAG = "HeadViewUtil";

    private int mBalloonHeight;//气球高
    private int mBalloonWidth;//气球宽

    private GifView mGifView;
    private ImageView mBalloon;//气球
    private ImageView mBox;//盒子
    private RelativeLayout mEntirety;//整体

    public static final int PULL_REFRESH = 1;//下拉刷新
    public static final int RELEASE_REFRESH = 2;//松开刷新
    public static final int REFRESHING = 3;//刷新中
    public static final int NONE = 4;

    private int mCurrentState = NONE;//当前状态为空闲

    private AnimationSet mAnimationSet;
    private AnimationSet mStartAnimationSet;


    public HeadViewUtil(View headView) {
        mHeadView = headView;
        initView();
        initAnimation();
    }

    /**
     * 初始化view
     */
    private void initView() {
        mGifView = mHeadView.findViewById(R.id.gifView);
        mEntirety = mHeadView.findViewById(R.id.entirety);
        mBox = mHeadView.findViewById(R.id.box);
        mBalloon = mHeadView.findViewById(R.id.balloon);

        mBalloon.measure(0, 0);
        mBalloonHeight = mBalloon.getMeasuredHeight();
        mBalloonWidth = mBalloon.getMeasuredWidth();

        Log.d(TAG, "initHeadItem: balloonWidth " + mBalloonWidth + "balloonHeight " + mBalloonHeight);
    }

    /**
     * 初始化动画
     */
    private void initAnimation() {

        RotateAnimation rotateAnimation = new RotateAnimation(-10, 10,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.75f);
        rotateAnimation.setRepeatCount(-1);


        TranslateAnimation translateAnimation = new TranslateAnimation
                (-DensityUtil.dipToPx(8), DensityUtil.dipToPx(8),
                        -DensityUtil.dipToPx(10), DensityUtil.dipToPx(10));
        translateAnimation.setRepeatCount(-1);

        mAnimationSet = new AnimationSet(false);//初始化
        // mAnimationSet.setInterpolator(mContext, android.R.anim.accelerate_decelerate_interpolator);
        mAnimationSet.setRepeatMode(Animation.REVERSE);//颠倒
        mAnimationSet.setDuration(600);//时间
        mAnimationSet.addAnimation(rotateAnimation);
        mAnimationSet.addAnimation(translateAnimation);//将动画添加进来

        RotateAnimation rotateAnim = new RotateAnimation(0, -10,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.75f);

        TranslateAnimation translateAnim = new TranslateAnimation(0, -DensityUtil.dipToPx(8),
                0, -DensityUtil.dipToPx(10));

        mStartAnimationSet = new AnimationSet(false);
        // mStartAnimationSet.setInterpolator(mContext, android.R.anim.accelerate_decelerate_interpolator);//先快速后减速
        mStartAnimationSet.setDuration(300);
        mStartAnimationSet.addAnimation(rotateAnim);
        mStartAnimationSet.addAnimation(translateAnim);
        mStartAnimationSet.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mEntirety.startAnimation(mAnimationSet);//开启下一个动画
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

    }


    /**
     * 得到当前状态
     *
     * @return
     */
    public int getCurrentState() {
        return mCurrentState;
    }

    /**
     * 设置当前状态
     *
     * @param currentState
     */
    public void setCurrentState(int currentState) {
        if (currentState != mCurrentState) {

            if (mCurrentState == NONE && currentState == PULL_REFRESH
                    || (currentState == NONE && mCurrentState != REFRESHING)) {
                mCurrentState = currentState;//只改状态
            } else {
                if (mCurrentState != currentState) {
                    mCurrentState = currentState;
                    refreshHeadView();//其他是要刷新UI的
                }
            }

        }
    }

    /**
     * 刷新头部view
     */
    private void refreshHeadView() {

        switch (mCurrentState) {//判断当前状态

            case PULL_REFRESH://下拉刷新
                PaddingBottomAnimation padBottomAnim1 = new PaddingBottomAnimation(mBox.getPaddingBottom(),
                        0, mBox);
                padBottomAnim1.setDuration(200);
                mBox.startAnimation(padBottomAnim1);//盒子升上去
                break;
            case RELEASE_REFRESH://松开刷新
                PaddingBottomAnimation padBottomAnim = new PaddingBottomAnimation(mBox.getPaddingBottom(),
                        -mBalloonHeight / 4, mBox);
                padBottomAnim.setDuration(300);
                mBox.startAnimation(padBottomAnim);//盒子掉下来
                break;
            case REFRESHING://刷新中
                mBox.clearAnimation();//清除动画
                PaddingBottomAnimation paddingBottomAnimation = new PaddingBottomAnimation(mBox.getPaddingBottom(), 0, mBox);

                int duration = (int) (Math.abs(mBox.getPaddingBottom()) / (float) (mBalloonHeight / 4) * 400);
                Log.d(TAG, "refreshHeadView: duration " + duration);
                paddingBottomAnimation.setDuration(duration);

                mBox.startAnimation(paddingBottomAnimation);//升上去

                paddingBottomAnimation.setAnimationEndListener(() -> {

                    if (mBox.getPaddingBottom() != 0)
                        mBox.setPadding(0, 0, 0, 0);//设置一下padding

                    mGifView.setPaused(false);//云动起来
                    mEntirety.startAnimation(mStartAnimationSet);
                    if (mOnRefreshListener != null) {
                        mOnRefreshListener.onRefresh();//去刷新吧
                    }
                });
                break;
            case NONE:
                mGifView.setPaused(true);//动画结束
                mGifView.setMovieTime(0);//回到开头
                mEntirety.clearAnimation();//清除晃动的动画
                break;
        }
    }

    /**
     * 设置刷新监听
     *
     * @param onRefreshListener
     */
    public void setOnRefreshListener(OnRefreshListener onRefreshListener) {
        mOnRefreshListener = onRefreshListener;
    }

    //刷新监听
    public interface OnRefreshListener {
        void onRefresh();//刷新中
    }

    private OnRefreshListener mOnRefreshListener;

}
