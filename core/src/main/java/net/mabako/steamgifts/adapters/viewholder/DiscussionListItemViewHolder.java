package net.mabako.steamgifts.adapters.viewholder;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import net.mabako.steamgifts.activities.CommonActivity;
import net.mabako.steamgifts.activities.DetailActivity;
import net.mabako.steamgifts.adapters.EndlessAdapter;
import net.mabako.steamgifts.core.R;
import net.mabako.steamgifts.data.Discussion;
import net.mabako.steamgifts.fragments.DiscussionDetailFragment;

import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

public class DiscussionListItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private final View itemContainer;
    private final TextView discussionTitle;
    private final TextView discussionAuthor;
    private final ImageView discussionAuthorAvatar;
    private final TextView discussionTime;

    private final EndlessAdapter adapter;
    private final Activity activity;

    public DiscussionListItemViewHolder(View itemView, Activity activity, EndlessAdapter adapter) {
        super(itemView);
        this.adapter = adapter;
        this.activity = activity;

        itemContainer = itemView.findViewById(R.id.list_item);
        discussionTitle = itemView.findViewById(R.id.discussion_title);
        discussionAuthor = itemView.findViewById(R.id.discussion_author);
        discussionAuthorAvatar = itemView.findViewById(R.id.author_avatar);
        discussionTime = itemView.findViewById(R.id.discussion_time);

        itemView.setOnClickListener(this);
    }

    public void setFrom(Discussion discussion) {
        StringBuilder text = new StringBuilder();
        if (discussion.isPinned())
            text.append("{faw-long-arrow-right} ");
        if (discussion.isPoll())
            text.append("{faw-align-left} ");
        text.append(discussion.getTitle());

        discussionTitle.setText(text);
        discussionAuthor.setText(discussion.getCreator());
        discussionTime.setText(discussion.getRelativeCreatedTime(activity));

        StringUtils.setBackgroundDrawable(activity, itemContainer, discussion.isLocked());

        Picasso.get().load(discussion.getCreatorAvatar()).placeholder(R.drawable.default_avatar_mask).transform(new RoundedCornersTransformation(20, 0)).into(discussionAuthorAvatar);
    }

    @Override
    public void onClick(View v) {
        Discussion discussion = (Discussion) adapter.getItem(getAdapterPosition());

        Intent intent = new Intent(activity, DetailActivity.class);
        intent.putExtra(DiscussionDetailFragment.ARG_DISCUSSION, discussion);

        activity.startActivityForResult(intent, CommonActivity.REQUEST_LOGIN_PASSIVE);
    }
}
