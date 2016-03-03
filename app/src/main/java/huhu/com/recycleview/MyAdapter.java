package huhu.com.recycleview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v4.util.LruCache;
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
    //图片缓存容量的上限
    private int SizeofCache = 0;
    //LruCache实例
    private LruCache lruCache;

    public MyAdapter(Context mContext, ArrayList mList) {
        this.mContext = mContext;
        this.mList = mList;
        rand = new Random();
        SizeofCache = (int) (Runtime.getRuntime().maxMemory() / 1024) / 8;
        //实例化LruCache
        lruCache = new LruCache<String, Bitmap>(SizeofCache) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // 返回用户定义的item的大小，默认返回1代表item的数量.重写此方法来衡量每张图片的大小。
                return bitmap.getByteCount() / 1024;
            }
        };

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemview = LayoutInflater.from(mContext).inflate(R.layout.item_layout, parent, false);
        return new MyViewHolder(itemview);

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        /*1.先用本地图片占位
        * 2.现将imageView设置为不可见状态*/
        //((MyViewHolder) holder).mImageView.setBackgroundResource(R.mipmap.ic_launcher);
        ((MyViewHolder) holder).mImageView.setVisibility(View.INVISIBLE);
        //设置数据
        ((MyViewHolder) holder).mTextView.setText(mList.get(position).getText());
        ((MyViewHolder) holder).mImageView.setTag(mList.get(position).getUrl());
        //ImageView生成随机高度
        ViewGroup.LayoutParams lp = ((MyViewHolder) holder).mImageView.getLayoutParams();
        lp.height = rand.nextInt(100) + 200;
        ((MyViewHolder) holder).mImageView.setLayoutParams(lp);
        //新建异步任务
        LoadImage imageLoad = new LoadImage((MyViewHolder) holder);
        //执行下载任务
        imageLoad.execute(mList.get(position).getUrl());
        ((MyViewHolder) holder).mImageView.setTag(mList.get(position).getUrl());


    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    /**
     * @param key    传入图片的key值，一般用图片url代替
     * @param bitmap 要缓存的图片对象
     */
    public void addBitmapToCache(String key, Bitmap bitmap) {
        if (getBitmapFromCache(key) == null) {
            lruCache.put(key, bitmap);
        }

    }

    /**
     * @param key 要取出的bitmap的key值
     * @return 返回取出的bitmap
     */
    public Bitmap getBitmapFromCache(String key) {

        return (Bitmap) lruCache.get(key);
    }

    /**
     * 定制ViewHolder
     */
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
        String url;
        MyViewHolder myViewHolder = null;

        public LoadImage(MyViewHolder myViewHolder) {
            this.myViewHolder = myViewHolder;
        }

        @Override
        protected Bitmap doInBackground(String... strings) {
            url = strings[0];
            final Bitmap cachebitmap = getBitmapFromCache(url);
            if (cachebitmap != null) {
                return cachebitmap;

            } else {
                try {

                    URL url_image = new URL(url);
                    URLConnection conn = url_image.openConnection();
                    conn.setDoInput(true);
                    conn.connect();
                    is = conn.getInputStream();
                    bitmap = BitmapFactory.decodeStream(is);
                    //将下载好的bitmap放入缓存中
                    addBitmapToCache(url, bitmap);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return bitmap;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {

            ImageView image = myViewHolder.mImageView;
            if (image.getTag().equals(url)) {
                image.setVisibility(View.VISIBLE);
                Drawable drawable = new BitmapDrawable(bitmap);
                image.setBackground(drawable);
            }

            super.onPostExecute(bitmap);

        }
    }

}
