package io.oxigen.quiosgrama.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;

/**
 * Created by Alexandre on 31/03/2016.
 *
 */
public class ImageUtil {

    private Context mContext;

    public ImageUtil(Context context) {
        mContext = context;
    }

    public static ImageView getImgIconDifferColor(Context mContext, int iconRes, int colorRes, int width, int height) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);

        ImageView imgIconService = new ImageView(mContext);
        imgIconService.setLayoutParams(params);

        imgIconService.setImageBitmap(ImageUtil.changeColorIconBitmap(mContext, iconRes, colorRes));

        return imgIconService;
    }

    public static Bitmap changeColorIconBitmap(Context mContext, int drawableRes, int colorRes) {
        Drawable sourceDrawable;
        if (Build.VERSION.SDK_INT >= 21) {
            sourceDrawable = mContext.getResources().getDrawable(drawableRes, null);
        } else {
            sourceDrawable = mContext.getResources().getDrawable(drawableRes);
        }

        //Convert drawable in to bitmap
        Bitmap sourceBitmap = ImageUtil.convertDrawableToBitmap(sourceDrawable);

        //Pass the bitmap and color code to change the icon color dynamically.

        return ImageUtil.changeImageColor(sourceBitmap, mContext.getResources().getColor(colorRes));
    }

    public static Drawable changeColorIconDrawable(Context mContext, int drawableRes, int colorRes, int width, int height) {
        return covertBitmapToDrawable(mContext, changeColorIconBitmap(mContext, drawableRes, colorRes), width, height);
    }

    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int pixels) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap
                .getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = pixels;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    public static Bitmap changeImageColor(Bitmap sourceBitmap, int color) {
        Bitmap resultBitmap = Bitmap.createBitmap(sourceBitmap, 0, 0, sourceBitmap.getWidth() - 1, sourceBitmap.getHeight() - 1);
        Paint p = new Paint();
        ColorFilter filter = new LightingColorFilter(color, 1);
        p.setColorFilter(filter);

        Canvas canvas = new Canvas(resultBitmap);
        canvas.drawBitmap(resultBitmap, 0, 0, p);
        return resultBitmap;
    }


    public static Drawable covertBitmapToDrawable(Context context, Bitmap bitmap, int width, int height) {
        Drawable d = new BitmapDrawable(context.getResources(), bitmap);
        d.setBounds(0, 0, width, height);
        return d;
    }

    public static Bitmap convertDrawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    public void setImageViewInBackground(int imageViewRes, ImageView imageView, int placeHolderRes, String imageUrl) {
        if (cancelPotentialWork(imageViewRes, imageView)) {
            final BitmapWorkerTask task = new BitmapWorkerTask(imageView, imageUrl);

            Bitmap placeHolderBitmap = BitmapFactory.decodeResource(mContext.getResources(),
                    placeHolderRes);

            final AsyncDrawable asyncDrawable =
                    new AsyncDrawable(mContext.getResources(), placeHolderBitmap, task);
            imageView.setImageDrawable(asyncDrawable);
            task.execute(imageViewRes);
        }

        BitmapWorkerTask task = new BitmapWorkerTask(imageView, imageUrl);
        task.execute(imageViewRes);
    }

    public static boolean cancelPotentialWork(int data, ImageView imageView) {
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

        if (bitmapWorkerTask != null) {
            final int bitmapData = bitmapWorkerTask.data;
            // If bitmapData is not yet set or it differs from the new data
            if (bitmapData == 0 || bitmapData != data) {
                // Cancel previous task
                bitmapWorkerTask.cancel(true);
            } else {
                // The same work is already in progress
                return false;
            }
        }
        // No task associated with the ImageView, or an existing task was cancelled
        return true;
    }

    class BitmapWorkerTask extends AsyncTask<Integer, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;
        private int data = 0;
        private String mImageUrl;

        public BitmapWorkerTask(ImageView imageView, String imageUrl) {
            // Use a WeakReference to ensure the ImageView can be garbage collected
            mImageUrl = imageUrl;
            imageViewReference = new WeakReference<>(imageView);
        }

        // Decode image in background.
        @Override
        protected Bitmap doInBackground(Integer... params) {
            data = params[0];
            try {
                InputStream input = (InputStream) new URL(mImageUrl).getContent();
                return BitmapFactory.decodeStream(input);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        // Once complete, see if ImageView is still around and set bitmap.
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap != null) {
                final ImageView imageView = imageViewReference.get();
                if (imageView != null) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
    }

    static class AsyncDrawable extends BitmapDrawable {
        private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

        public AsyncDrawable(Resources res, Bitmap bitmap,
                             BitmapWorkerTask bitmapWorkerTask) {
            super(res, bitmap);
            bitmapWorkerTaskReference =
                    new WeakReference<>(bitmapWorkerTask);
        }

        public BitmapWorkerTask getBitmapWorkerTask() {
            return bitmapWorkerTaskReference.get();
        }
    }

    private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }
}
