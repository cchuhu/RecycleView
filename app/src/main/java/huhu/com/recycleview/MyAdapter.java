package huhu.com.recycleview;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.util.LruCache;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Random;

import libcore.io.DiskLruCache;

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
    //DiskLruCache的实例
    private DiskLruCache diskLruCache;

    public MyAdapter(Context mContext, ArrayList mList) {
        this.mContext = mContext;
        this.mList = mList;
        rand = new Random();
        //获取缓存上限
        SizeofCache = (int) (Runtime.getRuntime().maxMemory() / 1024) / 8;
        //实例化LruCache
        lruCache = new LruCache<String, Bitmap>(SizeofCache) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // 返回用户定义的item的大小，默认返回1代表item的数量.重写此方法来衡量每张图片的大小。
                return bitmap.getByteCount() / 1024;
            }
        };
        // 获取图片缓存路径
        File diskcacheDir = getDiskCacheDir(mContext, "photos");
        if (!diskcacheDir.exists()) {
            diskcacheDir.mkdirs();
        }
        // 实例化DiskLruCache
        try {
            diskLruCache = DiskLruCache.open(diskcacheDir, getAppVersion(mContext), 1, 10 * 1024 * 1024);
        } catch (IOException e) {
            e.printStackTrace();
        }

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
        ((MyViewHolder) holder).mImageView.setBackgroundResource(R.mipmap.ic_launcher);
        ((MyViewHolder) holder).mImageView.setVisibility(View.INVISIBLE);
        //设置数据
        ((MyViewHolder) holder).mTextView.setText(mList.get(position).getText());
        //设置tag，防止错位
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
            if (bitmap == null) {
                return;
            } else {
                lruCache.put(key, bitmap);
            }
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
     * 将URL进行MD5编码，防止特殊字符出现
     *
     * @param key
     * @return
     */

    public String hashKeyForDisk(String key) {
        String cacheKey;
        try {
            //信息摘要算法提供类
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(key.getBytes());
            cacheKey = bytesToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(key.hashCode());
        }
        return cacheKey;
    }

    private String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            //十六进制转换
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    /**
     * 根据传入的uniqueName获取硬盘缓存的路径地址。
     */
    public File getDiskCacheDir(Context context, String uniqueName) {
        String cachePath;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) || !Environment.isExternalStorageRemovable()) {
            cachePath = context.getExternalCacheDir().getPath();
        } else {
            cachePath = context.getCacheDir().getPath();
        }
        return new File(cachePath + File.separator + uniqueName);
    }

    /**
     * 获取当前应用程序的版本号。
     */
    public int getAppVersion(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 1;
    }

    /**
     * 下载图片的异步任务类
     */
    class LoadImage extends AsyncTask<String, Integer, Bitmap> {
        Bitmap bitmap = null;
        InputStream is = null;
        String url = null;
        MyViewHolder myViewHolder = null;
        DiskLruCache.Snapshot snapshot = null;
        FileInputStream fileInputStream = null;
        FileDescriptor fileDescriptor = null;
        BufferedInputStream in = null;
        BufferedOutputStream out = null;

        public LoadImage(MyViewHolder myViewHolder) {
            this.myViewHolder = myViewHolder;
        }

        @Override
        protected Bitmap doInBackground(String... strings) {
            try {
                url = strings[0];
                Bitmap cachebitmap = getBitmapFromCache(url);
                //先从缓存中取，如果缓存不为空，则返回图片
                if (cachebitmap != null) {
                    Log.e(url,"存在于内存中,直接返回");
                    return cachebitmap;
                } else {

                    //如果内存缓存中取不到，再尝试从硬盘中取
                    Log.e(url,"内存中不存在，从硬盘缓存中找");
                    snapshot = diskLruCache.get(hashKeyForDisk(url));
                    //如果snapshot为空，则从网络下载图片
                    if (snapshot == null) {
                        Log.e(url,"硬盘中没有，从网络下载");
                        URL url_image = new URL(url);
                        HttpURLConnection conn = (HttpURLConnection) url_image.openConnection();
                        conn.setDoInput(true);
                        conn.connect();
                        //利用editor将图片写入硬盘缓存
                        is = conn.getInputStream();
                        in = new BufferedInputStream(is, 8 * 1024);
                        DiskLruCache.Editor editor = diskLruCache.edit(hashKeyForDisk(url));
                        OutputStream outputStream = editor.newOutputStream(0);
                        out = new BufferedOutputStream(outputStream, 8 * 1024);
                        int b;
                        while ((b = in.read()) != -1) {
                            out.write(b);
                        }
                        editor.commit();
                        diskLruCache.flush();
                        conn.disconnect();
                        out.close();
                        in.close();
                    }
                    //再次查找对应缓存
                    snapshot = diskLruCache.get(hashKeyForDisk(url));
                    //将bitmap写入内存缓存
                    fileInputStream = (FileInputStream) snapshot.getInputStream(0);
                    fileDescriptor = fileInputStream.getFD();
                    //如果fileDescriptor不为空，开始解析bitmap,加入到内存缓存中,并返回图片
                    if (fileDescriptor != null) {
                        Bitmap diskcachebitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor);
                        if (diskcachebitmap == null) {
                            return null;
                        } else {
                            Log.e(url,"重新加入到内存缓存中");
                            addBitmapToCache(url, diskcachebitmap);
                            return diskcachebitmap;
                        }

                    }
                    return bitmap;

                }


            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
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
}
