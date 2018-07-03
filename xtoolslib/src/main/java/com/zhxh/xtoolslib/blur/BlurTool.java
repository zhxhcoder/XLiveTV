package com.zhxh.xtoolslib.blur;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.view.View;
import android.widget.ImageView;

public class BlurTool {

    static Bitmap overlay;

    static JavaBlurProcess process = new JavaBlurProcess();

    /**
     * 高斯模糊
     * @param bkg 模糊视图
     * @param view 模糊覆盖层
     * @param radius 模糊粒度
     */
    public static void blur(Bitmap bkg, ImageView view, float radius) {
        int scaleFactor = 8;
        if (overlay != null) {
//            overlay.recycle();
        }
        overlay = Bitmap.createScaledBitmap(bkg, bkg.getWidth() / scaleFactor, bkg.getHeight() / scaleFactor, false);
        overlay = process.blur(overlay, radius);//高斯模糊
        view.setImageBitmap(overlay);
    }

    public static Bitmap blur(Activity context) {
        Bitmap mBitmap ;
        Bitmap overlay = null;
        if (null != overlay) {
            return overlay;
        }

        View view = context.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache(true);
        mBitmap = view.getDrawingCache();

        float scaleFactor = 8;//图片缩放比例；
        float radius = 10;//模糊程度
        int width = mBitmap.getWidth();
        int height = mBitmap.getHeight();

        overlay = Bitmap.createBitmap((int) (width / scaleFactor), (int) (height / scaleFactor), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(overlay);
        canvas.scale(1 / scaleFactor, 1 / scaleFactor);
        Paint paint = new Paint();
        paint.setFlags(Paint.FILTER_BITMAP_FLAG);
        canvas.drawBitmap(mBitmap, 0, 0, paint);

        overlay = doBlur(context, overlay, (int) radius);
        return overlay;
    }

    @SuppressLint("NewApi")
    public static Bitmap doBlur(Context context, Bitmap sentBitmap, int radius) {

        Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);

        final RenderScript rs = RenderScript.create(context);
        final Allocation input = Allocation.createFromBitmap(rs, sentBitmap, Allocation.MipmapControl.MIPMAP_NONE,
                Allocation.USAGE_SCRIPT);
        final Allocation output = Allocation.createTyped(rs, input.getType());
        final ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        script.setRadius(radius /* e.g. 3.f */);
        script.setInput(input);
        script.forEach(output);
        output.copyTo(bitmap);
        return bitmap;
    }

}
