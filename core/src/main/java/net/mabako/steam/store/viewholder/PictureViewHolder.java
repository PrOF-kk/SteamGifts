package net.mabako.steam.store.viewholder;

import android.view.View;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import net.mabako.steam.store.data.Picture;
import net.mabako.steamgifts.core.R;

public class PictureViewHolder extends RecyclerView.ViewHolder {

    public PictureViewHolder(View itemView) {
        super(itemView);
    }

    public void setFrom(Picture picture) {
        Picasso.get().load(picture.url()).into((ImageView) itemView.findViewById(R.id.image));
    }
}
