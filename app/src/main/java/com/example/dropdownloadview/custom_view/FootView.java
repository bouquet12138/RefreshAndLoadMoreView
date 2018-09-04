package com.example.dropdownloadview.custom_view;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.dropdownloadview.R;

public class FootView extends RelativeLayout {

    private LinearLayout mLoadMore;//加载更多
    private TextView mInfoText;//信息文本

    public static final int IDLE = 1;//底部空闲状态
    public static final int RELEASE_REFRESH = 2;//底部松开刷新状态
    public static final int LOADING = 3;//底部加载中
    public static final int NO_DATA = 4;//底部无数据

    private int mCurrentState = IDLE;//底部无状态

    public FootView(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.foot_view, this);
        initView();
    }

    /**
     * 初始化view
     */
    private void initView() {
        mLoadMore = findViewById(R.id.loadMore);
        mInfoText = findViewById(R.id.infoText);
    }

    public int getCurrentState() {
        return mCurrentState;
    }

    public void setCurrentState(int currentState) {
        if (mCurrentState != currentState) {
            mCurrentState = currentState;
            refreshState();
        }
    }

    /**
     * 刷新状态
     */
    private void refreshState() {

        switch (mCurrentState) {
            case IDLE://空闲状态
                mInfoText.setVisibility(VISIBLE);//可见
                mLoadMore.setVisibility(INVISIBLE);//不可见
                mInfoText.setText("查看更多");
                break;
            case RELEASE_REFRESH://松开
                mInfoText.setVisibility(VISIBLE);//可见
                mLoadMore.setVisibility(INVISIBLE);//不可见
                mInfoText.setText("松开加载");
                break;
            case LOADING:
                mInfoText.setVisibility(INVISIBLE);//不可见
                mLoadMore.setVisibility(VISIBLE);//可见

                if (mOnLoadMoreListener != null) {
                    mOnLoadMoreListener.onLoadMore();//加载更多去吧
                }

                break;
            case NO_DATA:
                mInfoText.setVisibility(VISIBLE);//可见
                mLoadMore.setVisibility(INVISIBLE);//不可见
                mInfoText.setText("没有更多数据");
                break;
        }
    }

    public interface OnLoadMoreListener {
        void onLoadMore();
    }

    private OnLoadMoreListener mOnLoadMoreListener;

    /**
     * 设置加载更多监听
     *
     * @param onLoadMoreListener
     */
    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        mOnLoadMoreListener = onLoadMoreListener;
    }
}
