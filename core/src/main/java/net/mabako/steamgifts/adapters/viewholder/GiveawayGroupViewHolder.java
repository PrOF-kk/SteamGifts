package net.mabako.steamgifts.adapters.viewholder;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import net.mabako.steamgifts.core.R;
import net.mabako.steamgifts.data.GiveawayGroup;

import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

public class GiveawayGroupViewHolder extends RecyclerView.ViewHolder {
    private final TextView groupName;
    private final ImageView groupAvatar;

    private final Context context;

    public GiveawayGroupViewHolder(View v, Context context) {
        super(v);

        this.context = context;

        groupName = v.findViewById(R.id.group_name);
        groupAvatar = v.findViewById(R.id.group_avatar);
    }

    public void setFrom(GiveawayGroup group) {
        groupName.setText(group.getTitle());
        Picasso.get().load(group.getAvatar()).placeholder(R.drawable.default_avatar_mask).transform(new RoundedCornersTransformation(20, 0)).into(groupAvatar);

    }
}
