package huhu.com.recycleview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Huhu on 3/1/16.
 * 瀑布流的适配器
 */
public class MyAdapter extends RecyclerView.Adapter {
    //上下文对象
    private final Context mContext;
    //存放bean对象的列表
    private ArrayList<Bean> mList;
    //生成随机高度的Random
    private Random rand;
    //item布局文件
    public View itemview;


    public MyAdapter(Context mContext, ArrayList mList) {
        this.mContext = mContext;
        this.mList = mList;
        rand = new Random();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        itemview = LayoutInflater.from(mContext).inflate(R.layout.item_layout, parent, false);
        return new MyViewHolder(itemview);

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        //设置数据？
        ((MyViewHolder) holder).mTextView.setText(mList.get(position).getText());
        //ImageView生成随机高度
        ViewGroup.LayoutParams lp = ((MyViewHolder) holder).mImageView.getLayoutParams();
        lp.height = rand.nextInt(100) + 200;
        ((MyViewHolder) holder).mImageView.setLayoutParams(lp);
        //新建异步任务
        LoadImage imageLoad = new LoadImage((MyViewHolder) holder);

        //执行下载任务
        imageLoad.execute(mList.get(position).getUrl());

    }

    @Override
    public int getItemCount() {
        return mList.size();
    }


    class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView mTextView;
        public ImageView mImageView;

        public MyViewHolder(View itemView) {
            super(itemView);
            mTextView = (TextView) itemView.findViewById(R.id.textview);
            mImageView = (ImageView) itemView.findViewById(R.id.imageview);
        }

    }

    /**
     * 下载图片的异步任务类
     */
    class LoadImage extends AsyncTask<String, Integer, Bitmap> {
        Bitmap bitmap = null;
        InputStream is = null;

        MyViewHolder myViewHolder = null;

        public LoadImage(MyViewHolder myViewHolder) {
            this.myViewHolder = myViewHolder;
        }

        @Override
        protected Bitmap doInBackground(String... strings) {
            String url = strings[0];

            try {

                URL url_image = new URL(url);
                URLConnection conn = url_image.openConnection();
                conn.setDoInput(true);
                conn.connect();
                is = conn.getInputStream();
                bitmap = BitmapFactory.decodeStream(is);


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {

            ImageView image = myViewHolder.mImageView;
            Drawable drawable = new BitmapDrawable(bitmap);
            image.setBackground(drawable);
            super.onPostExecute(bitmap);

        }
    }
}
