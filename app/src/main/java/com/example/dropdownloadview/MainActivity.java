package com.example.dropdownloadview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.dropdownloadview.adapter.LayoutAdapter;
import com.example.dropdownloadview.custom_view.RefreshListView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RefreshListView mRefreshListView;
    private LayoutAdapter mLayoutAdapter;
    private List<String> mStrList;
    private int mNowRefresh = 0;//当前刷新出来的
    private int mNowLoadMore = 0;//当前加载更多

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initData();
        initView();
        initListener();
    }

    /**
     * 初始化数据
     */
    private void initData() {
        mStrList = new ArrayList<>();
        for (int i = 0; i < 0; i++) {
            mStrList.add(i + "");
        }
    }

    /**
     * 初始化view
     */
    private void initView() {
        mRefreshListView = findViewById(R.id.refreshListView);
        mLayoutAdapter = new LayoutAdapter(MainActivity.this,
                R.layout.item_layout, mStrList);
        mRefreshListView.setAdapter(mLayoutAdapter);//设置适配器
    }

    /**
     * 初始化监听
     */
    private void initListener() {
        mRefreshListView.setOnRefreshListener(new RefreshListView.OnRefreshListener() {
            @Override
            public void onRefresh() {

                new Thread(() -> {
                    try {
                        Thread.sleep(1000);//先休息2秒
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    int max = mNowRefresh + 2;
                    for (; mNowRefresh < max; mNowRefresh++) {
                        mStrList.add(0, "刷新 " + mNowRefresh);
                    }

                    runOnUiThread(() -> {
                        mLayoutAdapter.notifyDataSetChanged();//唤醒数据更新
                        mRefreshListView.completeRefresh();
                    });
                }).start();

            }

            @Override
            public void onLoadingMore() {

                new Thread(() -> {
                    try {
                        Thread.sleep(500);//先休息0.5秒
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    int max = mNowLoadMore + 2;
                    for (; mNowLoadMore < max; mNowLoadMore++) {
                        mStrList.add("加载 " + mNowLoadMore);
                    }
                    final int MAX = max;
                    runOnUiThread(() -> {
                        mLayoutAdapter.notifyDataSetChanged();//唤醒数据更新
                        mRefreshListView.completeLoadMore();//完成加载更多

                        if (MAX > 10) {
                            mRefreshListView.setFootNoLoadData();//加载到底了
                        }

                    });
                }).start();

            }

            @Override
            public void refreshComplete() {

            }
        });

    }

}
