package net.mabako.steamgifts.adapters.viewholder;

import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.fragment.app.FragmentManager;

import net.mabako.steamgifts.activities.CommonActivity;
import net.mabako.steamgifts.core.R;
import net.mabako.steamgifts.data.IImageHolder;
import net.mabako.steamgifts.data.Image;
import net.mabako.steamgifts.fragments.images.ImagePagerFragment;

import java.util.List;

public final class AttachedImageUtils {
    private AttachedImageUtils() {
        /* empty private constructor */
    }

    private static final String IMAGE_VIEW_TAG = "imageview";

    public static void setFrom(View itemView, IImageHolder imageHolder, final CommonActivity activity) {
        final List<Image> images = imageHolder == null ? null : imageHolder.getAttachedImages();

        if (images == null || images.isEmpty()) {
            itemView.findViewById(R.id.image_link_holder).setVisibility(View.GONE);
        } else {
            if (images.contains("") || images.contains(null))
                Log.w(AttachedImageUtils.class.getSimpleName(), "Attached Images contain empty string");

            Button button = itemView.findViewById(R.id.image_link_holder);
            button.setVisibility(View.VISIBLE);

            String text = "{faw-picture-o}";
            if (images.size() > 1)
                text += " " + images.size();
            button.setText(text);

            button.setOnClickListener(v -> {
                FragmentManager fm = activity.getSupportFragmentManager();
                fm.beginTransaction().add(android.R.id.content, ImagePagerFragment.newInstance(images), IMAGE_VIEW_TAG).addToBackStack(IMAGE_VIEW_TAG).commit();
            });
        }
    }
}
