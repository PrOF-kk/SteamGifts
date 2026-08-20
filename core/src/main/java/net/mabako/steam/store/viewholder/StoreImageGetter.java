package net.mabako.steam.store.viewholder;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.Html;
import android.util.Log;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

public class StoreImageGetter implements Html.ImageGetter {
    private static final String TAG = StoreImageGetter.class.getSimpleName();

    final TextView textView;

    public StoreImageGetter(final TextView textView) {
        this.textView = textView;
    }

    @Override
    public Drawable getDrawable(final String source) {
        Uri uri = Uri.parse(source);
        if (uri.getHost() == null || !uri.getHost().contains(".steamstatic.com")) {
            Log.w(TAG, "Not a Steam image: " + source);
            return null;
        }

        var resources = textView.getResources();
        int maxWidth = textView.getWidth() > 0 ? textView.getWidth() : resources.getDisplayMetrics().widthPixels;
        BitmapDrawablePlaceHolder result = new BitmapDrawablePlaceHolder(resources);

        Picasso.get().load(source).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                int originalWidth = bitmap.getWidth();
                int originalHeight = bitmap.getHeight();
                int finalWidth = originalWidth;
                int finalHeight = originalHeight;
                if (originalWidth > maxWidth) {
                    finalWidth = maxWidth;
                    finalHeight = (int) (originalHeight * ((float) maxWidth / originalWidth));
                }

                BitmapDrawable drawable = new BitmapDrawable(resources, bitmap);
                drawable.setBounds(0, 0, finalWidth, finalHeight);

                result.setDrawable(drawable);
                result.setBounds(0, 0, finalWidth, finalHeight);

                textView.setText(textView.getText());
            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {}
            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) { }
        });

        return result;
    }

    static class BitmapDrawablePlaceHolder extends BitmapDrawable {
        protected Drawable drawable;

        @SuppressWarnings("deprecation")
        public BitmapDrawablePlaceHolder(Resources res) {
            super(res);
        }

        @Override
        public void draw(final Canvas canvas) {
            if (drawable != null) {
                drawable.draw(canvas);
            }
        }

        public void setDrawable(Drawable drawable) {
            this.drawable = drawable;
        }
    }
}