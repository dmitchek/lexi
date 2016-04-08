package com.lexmark.lexi;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.Image;
import android.util.LruCache;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

/**
 * Created by dave on 3/10/16.
 */
public class VolleySingleton {

    private static VolleySingleton mInstance;
    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;
    private static Context mContext;

    ImageLoader.ImageCache mImageCache = new ImageLoader.ImageCache() {
        private final LruCache<String, Bitmap>
                cache = new LruCache<String, Bitmap>(20);

        @Override
        public Bitmap getBitmap(String url) {
            return cache.get(url);
        }

        @Override
        public void putBitmap(String url, Bitmap bitmap) {
            cache.put(url, bitmap);
        }
    };

    private VolleySingleton(Context context)
    {
        mContext = context;
        mRequestQueue = getRequestQueue();

        mImageLoader = new ImageLoader(mRequestQueue,mImageCache);
    }

    public static synchronized VolleySingleton getInstance(Context context)
    {
        if(mInstance == null)
            mInstance = new VolleySingleton(context);

        return mInstance;
    }

    public RequestQueue getRequestQueue(){
        if(mRequestQueue == null)
            mRequestQueue = Volley.newRequestQueue(mContext.getApplicationContext());

        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req)
    {
        getRequestQueue().add(req);
    }

    public ImageLoader getImageLoader()
    {
        return mImageLoader;
    }

    public void cancelAllRequest()
    {
        getInstance(mContext).getRequestQueue().cancelAll(new RequestQueue.RequestFilter() {
            @Override
            public boolean apply(Request<?> request) {
                return true;
            }
        });
    }
}
