package gavinli.translator.util.imageloader.load;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by gavin on 17-8-16.
 */

public class NetworkLoader {
    /**
     * 取消加载
     */
    private volatile boolean mCanceled = false;

    /**
     * 从网络获取图片
     *
     * @param url 图片url
     *
     * @param reqWidth 图片最大宽度，用来缩放图片。
     *
     * @param reqHeight 图片最大高度，用来缩放图片。
     *
     * @throws IOException 网络连接错误
     */
    public Bitmap fetchBitmap(String url, int reqWidth, int reqHeight) throws IOException {
        URL realUrl = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) realUrl.openConnection();
        try(InputStream in = connection.getInputStream();
                ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] temp = new byte[1024];
            int length;
            while((length = in.read(temp)) > -1) {
                if (mCanceled) return null;
                out.write(temp, 0, length);
            }
            out.flush();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            byte[] bitmapData = out.toByteArray();
            BitmapFactory.decodeByteArray(bitmapData,
                    0, bitmapData.length, options);
            options.inJustDecodeBounds = false;
            options.inSampleSize = cancaulateSampleSize(options.outWidth, options.outHeight,
                    reqWidth, reqHeight);
            return BitmapFactory.decodeByteArray(bitmapData,
                    0, bitmapData.length, options);
        } finally {
            connection.disconnect();
        }
    }

    /**
     * 取消加载，直接返回null。
     */
    public void cancel() {
        mCanceled = true;
    }

    /**
     * 由reqWidth, reqHeight计算图片缩放系数
     *
     * @param width 图片原始宽度
     *
     * @param height 图片原始高度
     *
     * @param reqWidth 图片最大宽度
     *
     * @param reqHeight 图片最大高度
     *
     * @return 图片缩放系数
     */
    private int cancaulateSampleSize(int width, int height, int reqWidth, int reqHeight) {
        int sampleSize = 1;
        while(width > reqWidth || height > reqHeight) {
            sampleSize *= 2;
            width /= 2;
            height /= 2;
        }
        return sampleSize;
    }
}