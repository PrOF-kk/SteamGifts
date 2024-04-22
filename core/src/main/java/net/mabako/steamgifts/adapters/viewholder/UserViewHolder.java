package net.mabako.steamgifts.adapters.viewholder;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import net.mabako.steamgifts.activities.DetailActivity;
import net.mabako.steamgifts.core.R;
import net.mabako.steamgifts.data.BasicUser;
import net.mabako.steamgifts.data.Winner;
import net.mabako.steamgifts.fragments.ListFragment;
import net.mabako.steamgifts.fragments.UserDetailFragment;
import net.mabako.steamgifts.fragments.WhitelistBlacklistFragment;

import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

public class UserViewHolder extends RecyclerView.ViewHolder {
    private final ListFragment<?> fragment;

    private final View userHolder;

    private final TextView userName;
    private final TextView userStatus;
    private final ImageView userAvatar;

    private final Button removeUser;

    public UserViewHolder(View itemView, ListFragment<?> fragment) {
        super(itemView);
        this.fragment = fragment;

        userHolder = itemView.findViewById(R.id.user_holder);

        userName = itemView.findViewById(R.id.user);
        userStatus = itemView.findViewById(R.id.status);
        userAvatar = itemView.findViewById(R.id.avatar);

        removeUser = itemView.findViewById(R.id.remove_user);
    }

    public void setFrom(final BasicUser user) {
        userName.setText(user.getName());
        if (!TextUtils.isEmpty(user.getAvatar())) {
            Picasso.get().load(user.getAvatar()).placeholder(R.drawable.default_avatar_mask).transform(new RoundedCornersTransformation(20, 0)).into(userAvatar);

            userHolder.setOnClickListener(v -> {
                Intent intent = new Intent(fragment.getContext(), DetailActivity.class);
                intent.putExtra(UserDetailFragment.ARG_USER, user.getName());
                fragment.getActivity().startActivity(intent);
            });
        }

        if (removeUser != null && fragment instanceof WhitelistBlacklistFragment) {
            removeUser.setOnClickListener(v -> {
                ((WhitelistBlacklistFragment) fragment).requestUserListed(user, ((WhitelistBlacklistFragment) fragment).getType(), false);
            });
        }

        if (userStatus != null && user instanceof Winner) {
            userStatus.setText(((Winner) user).getStatus());
        }
    }
}
