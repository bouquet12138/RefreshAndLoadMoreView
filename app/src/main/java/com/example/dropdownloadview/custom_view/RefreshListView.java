package com.example.dropdownloadview.custom_view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

import com.example.dropdownloadview.R;
import com.example.dropdownloadview.anim.PaddingBottomAnimation;
import com.example.dropdownloadview.anim.PaddingTopAnimation;

public class RefreshListView extends ListView implements AbsListView.OnScrollListener {

    private static final String TAG = "RefreshListView";

    private HeadViewUtil mHeadViewUtil;
    private View mHeadView;//头view
    private FootView mFootView;//底部view

    private int mHeadHeight;//头View的高度
    private int mFootViewHeight;//底部view的高度

    private int mDownY;//按下时y坐标
    private boolean mCanScroll = true;//用户是否可以滑动
    private boolean mIntercept = false;//未拦截
    private boolean mUserFling = false;

    public RefreshListView(Context context) {
        super(context);
        init();
    }

    public RefreshListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * 初始化
     */
    private void init() {
        initHeadView();//初始化头部view
        initFootView();//初始化底部View
        initListener();//初始化监听
    }

    /**
     * 初始化头部view
     */
    private void initHeadView() {
        mHeadView = View.inflate(getContext(), R.layout.head_view, null);//头部view
        mHeadView.measure(0, 0);
        mHeadHeight = mHeadView.getMeasuredHeight();//得到测量的高度

        Log.d("RefreshListView", "initHeadView: mHeadHeight " + mHeadHeight);

        mHeadView.setPadding(0, -mHeadHeight, 0, 0);//将headView隐藏起来
        mHeadViewUtil = new HeadViewUtil(mHeadView);
        addHeaderView(mHeadView);//将headView添加到头部
    }

    /**
     * 初始化底部布局
     */
    private void initFootView() {
        mFootView = new FootView(getContext());
        mFootView.measure(0, 0);
        mFootViewHeight = mFootView.getMeasuredHeight();

        Log.d(TAG, "initFootView: mFootViewHeight 底部view的高 " + mFootViewHeight);
        addFooterView(mFootView);
    }

    /**
     * 初始化监听
     */
    private void initListener() {
        mHeadViewUtil.setOnRefreshListener(() -> {
            if (mOnRefreshListener != null) {
                mOnRefreshListener.onRefresh();//供外部使用
            }
        });

        mFootView.setOnLoadMoreListener(() -> {
            if (mOnRefreshListener != null) {
                mOnRefreshListener.onLoadingMore();//去加载更多吧
            }
        });

        mFootView.setOnClickListener((v) -> {//footView点击监听

            if (mFootView.getCurrentState() == FootView.IDLE) {
                mCanScroll = false;//用户不能滑动了
                mFootView.setCurrentState(mFootView.LOADING);//去加载更多吧
            }
        });

        setOnScrollListener(this);//设置滚动监听
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        if (!mCanScroll)//如果正在动画用户先不能操作
            return false;

        switch (ev.getAction()) {

            case MotionEvent.ACTION_DOWN:
                mIntercept = false;//未拦截
                mUserFling = false;//用户一扔也解除
                mDownY = (int) ev.getY();
                break;

            case MotionEvent.ACTION_MOVE:

                if (getChildCount() > 0 && getFirstVisiblePosition() == 0) {//可见的为第一个时

                    int nowPaddingY = mHeadView.getPaddingTop();//得到当前paddingY
                    int maxPadding = getHeight() - mHeadHeight;//能移动到的最大距离

                    if (nowPaddingY < -mHeadHeight) //其实我也不知道那是啥bug
                        nowPaddingY = -mHeadHeight;
                    else if (nowPaddingY > maxPadding)//限制一下移动
                        nowPaddingY = maxPadding;

                    float deltaY = (ev.getY() - mDownY);//偏移Y

                    if (nowPaddingY > -mHeadHeight || (nowPaddingY == -mHeadHeight && deltaY > 0)) {
                        if (nowPaddingY > (-mHeadHeight / 2) && deltaY > 0) { //为了产生一种弹性滑动效果
                            float delta = (maxPadding - nowPaddingY) / (float) (maxPadding + mHeadHeight / 2);//TODO:
                            deltaY *= Math.pow(delta, 2);
                        }
                        nowPaddingY += deltaY;

                        if (nowPaddingY >= -mHeadHeight && nowPaddingY <= maxPadding) {//headView可以滑动的范围
                            mHeadView.setPadding(0, nowPaddingY, 0, 0);
                            if (nowPaddingY >= -mHeadHeight / 2)
                                mHeadViewUtil.setCurrentState(HeadViewUtil.RELEASE_REFRESH);//松开刷新
                            else
                                mHeadViewUtil.setCurrentState(HeadViewUtil.PULL_REFRESH);//下拉刷新
                        }
                        mDownY = (int) ev.getY();
                        mIntercept = true;//拦截了
                        return true;//这里需要拦截一下
                    }
                } else {
                    if (mIntercept) {
                        mIntercept = false;
                    }
                    mHeadViewUtil.setCurrentState(HeadViewUtil.NONE);//置为空闲状态
                }

                if (mFootView.getCurrentState() == FootView.NO_DATA) {//不能加载更多了
                    mDownY = (int) ev.getY();
                    break;
                }

                if (getLastVisiblePosition() != getCount() - 1) {
                    mFootView.setCurrentState(FootView.IDLE);//设置为空闲状态
                } else {

                    float deltaY = (ev.getY() - mDownY);
                    if (mFootView.getBottom() == getHeight() && deltaY < 0)
                        mFootView.setCurrentState(FootView.RELEASE_REFRESH);//设置为松开刷新状态

                    if (mFootView.getCurrentState() != FootView.RELEASE_REFRESH)//如果当前状态不是松开刷新状态
                        break;

                    int nowPaddingY = mFootView.getPaddingBottom();
                    int realBottom = mFootView.getBottom() - nowPaddingY;

                    if (realBottom > getHeight() + mFootViewHeight)
                        break;

                    int maxPaddingY = 4 * mFootViewHeight;//paddingBottom 最大值
                    if (nowPaddingY < 0)
                        nowPaddingY = 0;
                    else if (nowPaddingY > maxPaddingY)
                        nowPaddingY = maxPaddingY;

                    if (deltaY < 0)
                        deltaY *= (maxPaddingY - nowPaddingY) / (float) (maxPaddingY);//偏移Y

                    nowPaddingY -= deltaY;

                    if (nowPaddingY >= 0 && nowPaddingY <= maxPaddingY)//设置paddingBottom
                        mFootView.setPadding(0, 0, 0, nowPaddingY);
                }

                mDownY = (int) ev.getY();
                break;
            case MotionEvent.ACTION_UP://如果是手指抬起

                if (mHeadViewUtil.getCurrentState() == HeadViewUtil.PULL_REFRESH) {//如果当前状态为下拉刷新
                    mCanScroll = false;//先暂时不可触摸
                    PaddingTopAnimation paddingTopAnim = new PaddingTopAnimation(mHeadView.getPaddingTop(), -mHeadHeight, mHeadView);
                    long duration = Math.abs((mHeadHeight + mHeadView.getPaddingTop()) / 3);
                    paddingTopAnim.setDuration(duration);
                    mHeadView.startAnimation(paddingTopAnim);//开启弹出动画
                    paddingTopAnim.setAnimationEndListener(() -> {
                        mCanScroll = true; //动画结束用户可以触摸了
                        mHeadViewUtil.setCurrentState(HeadViewUtil.NONE);
                    });
                } else if (mHeadViewUtil.getCurrentState() == HeadViewUtil.RELEASE_REFRESH) {//如果当前状态为松开刷新
                    mCanScroll = false;//用户不可以动了

                    long duration = Math.abs((mHeadView.getPaddingTop() + mHeadHeight / 2) / 3);
                    PaddingTopAnimation paddingTopAnim = new PaddingTopAnimation(mHeadView.getPaddingTop(), -mHeadHeight / 2, mHeadView);//回归中间
                    Log.d(TAG, "onTouchEvent: head升起 duration " + (duration + 100));
                    paddingTopAnim.setDuration(100 + duration);
                    mHeadView.startAnimation(paddingTopAnim);//启动动画
                    mHeadViewUtil.setCurrentState(HeadViewUtil.REFRESHING);//变为刷新状态
                }

                if (mFootView.getCurrentState() == FootView.RELEASE_REFRESH) {//如果为松开刷新
                    mCanScroll = false;//加载时用户不能操作
                    PaddingBottomAnimation paddingBottomAnimation = new
                            PaddingBottomAnimation(mFootView.getPaddingBottom(), 0, mFootView);
                    paddingBottomAnimation.setDuration(mFootView.getPaddingBottom());
                    mFootView.startAnimation(paddingBottomAnimation);//启动动画

                    paddingBottomAnimation.setAnimationEndListener(() -> {
                        mFootView.setCurrentState(FootView.LOADING);//转化为加载更多的状态
                    });
                }

                break;
        }
        return super.onTouchEvent(ev);
    }

    /**
     * 完成下拉刷新
     */
    public void completeRefresh() {

        PaddingTopAnimation paddingTopAnimation = new PaddingTopAnimation(mHeadView.getPaddingTop(), -mHeadHeight, mHeadView);
        paddingTopAnimation.setDuration(300);
        startAnimation(paddingTopAnimation);//headView先收回去

        paddingTopAnimation.setAnimationEndListener(() -> {

            mCanScroll = true;//动画结束 用户又可以操作了

            mHeadViewUtil.setCurrentState(HeadViewUtil.NONE);//完成刷新 当前状态为空闲

            if (mOnRefreshListener != null)
                mOnRefreshListener.refreshComplete();//告诉用户刷新完成可以更新UI了
        });
    }


    /**
     * 完成加载更多
     */
    public void completeLoadMore() {
        mCanScroll = true;//用户又可以触摸了
        mFootView.setCurrentState(FootView.IDLE);//重新置为空闲
    }

    /**
     * 设置没有下拉加载的数据
     */
    public void setFootNoLoadData() {
        mFootView.setCurrentState(FootView.NO_DATA);
    }

    private OnRefreshListener mOnRefreshListener;

    /**
     * 设置下拉刷新监听
     *
     * @param onRefreshListener
     */
    public void setOnRefreshListener(OnRefreshListener onRefreshListener) {
        mOnRefreshListener = onRefreshListener;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == SCROLL_STATE_IDLE) {
            if (mUserFling && getLastVisiblePosition() == getCount() - 1 && mFootView.getBottom() == getHeight()
                    && mFootView.getCurrentState() == FootView.IDLE) {
                mCanScroll = false;//用户不可以触摸
                mFootView.setCurrentState(FootView.LOADING);//去加载吧
            }
        } else if (scrollState == SCROLL_STATE_FLING) {
            mUserFling = true;
        } else {
            mUserFling = false;//用户一扔状态接触
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
    }

    public interface OnRefreshListener {
        void onRefresh();//下拉刷新

        void onLoadingMore();//加载更多

        void refreshComplete();//刷新完成监听
    }
}
