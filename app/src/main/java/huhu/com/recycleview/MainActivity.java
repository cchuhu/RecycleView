package huhu.com.recycleview;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;

import java.util.ArrayList;

public class MainActivity extends Activity {
    //RecycleView实例
    private RecyclerView rv;
    //数据适配器
    private MyAdapter mAdapter;
    //数据集
    private ArrayList<Bean> mDatas = new ArrayList<>();
    //item的view
    private View itemview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initDatas();
        initRecycleView();


    }

    /**
     * 初始化数据集
     */
    private void initDatas() {
        for (int i = 1; i < 101; i++) {
            Bean bean = new Bean();
            bean.setText("第" + i + "张头像");
            bean.setUrl(Config.URL_HEAD + i + ".jpg");
            mDatas.add(bean);
        }
    }

    /**
     * 初始化RecycleView
     */
    private void initRecycleView() {
        rv = (RecyclerView) findViewById(R.id.view_recycleview);
        //设置布局管理器
        rv.setLayoutManager(new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL));
        //初始化Adapter
        mAdapter = new MyAdapter(this, mDatas);
        //设置adapter
        rv.setAdapter(mAdapter);
    }

}
