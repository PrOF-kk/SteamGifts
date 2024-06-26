package net.mabako.steam.store.viewholder;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import net.mabako.steam.store.data.Picture;
import net.mabako.steamgifts.core.R;

public class PictureViewHolder extends RecyclerView.ViewHolder {
    private final Context context;

    public PictureViewHolder(View itemView, Context context) {
        super(itemView);
        this.context = context;
    }

    public void setFrom(Picture picture) {
        Picasso.get().load(picture.getUrl()).into((ImageView) itemView.findViewById(R.id.image));
    }
}
