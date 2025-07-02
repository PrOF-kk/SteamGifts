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
    final Resources resources;
    final TextView textView;

    public StoreImageGetter(final TextView textView, final Resources resources) {
        this.textView = textView;
        this.resources = resources;
    }

    @Override
    public Drawable getDrawable(final String source) {
        Uri uri = Uri.parse(source);
        if (uri.getHost() == null || !uri.getHost().contains(".steamstatic.com")) {
            Log.w(TAG, "Not a Steam image: " + source);
            return null;
        }

        final BitmapDrawablePlaceHolder result = new BitmapDrawablePlaceHolder(resources);

        Picasso.get().load(source).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                final BitmapDrawable drawable = new BitmapDrawable(resources, bitmap);
                drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());

                result.setDrawable(drawable);
                result.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());

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