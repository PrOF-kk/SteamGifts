package net.mabako.steamgifts.adapters.viewholder;

import android.content.Context;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import net.mabako.steamgifts.activities.CommonActivity;
import net.mabako.steamgifts.core.R;
import net.mabako.steamgifts.data.Discussion;
import net.mabako.steamgifts.data.DiscussionExtras;
import net.mabako.steamgifts.fragments.DiscussionDetailFragment;
import net.mabako.steamgifts.fragments.interfaces.ICommentableFragment;
import net.mabako.steamgifts.fragments.util.DiscussionDetailsCard;

public class DiscussionCardViewHolder extends RecyclerView.ViewHolder {
    private final ICommentableFragment fragment;
    private final Context context;

    private final View progressBar;
    private final TextView discussionTime;
    private final TextView user;
    private final TextView description;
    private final View separator;
    private final View actionSeparator;
    private final TextView title;

    private final Button commentDiscussion;

    public DiscussionCardViewHolder(View v, DiscussionDetailFragment fragment, Context context) {
        super(v);
        this.fragment = fragment;
        this.context = context;

        progressBar = v.findViewById(R.id.progressBar);
        user = v.findViewById(R.id.user);
        discussionTime = v.findViewById(R.id.time);
        description = v.findViewById(R.id.description);
        description.setMovementMethod(LinkMovementMethod.getInstance());
        separator = v.findViewById(R.id.separator);
        actionSeparator = v.findViewById(R.id.action_separator);
        title = v.findViewById(R.id.discussion_title);

        commentDiscussion = v.findViewById(R.id.comment);
    }

    public void setFrom(final DiscussionDetailsCard card) {
        final Discussion discussion = card.getDiscussion();
        DiscussionExtras extras = card.getExtras();

        for (View view : new View[]{commentDiscussion, description, discussionTime, user, title, separator, actionSeparator})
            view.setVisibility(View.GONE);

        if (discussion == null) {
            progressBar.setVisibility(View.VISIBLE);
        } else {
            for (View view : new View[]{discussionTime, user, title})
                view.setVisibility(View.VISIBLE);

            title.setText(discussion.getTitle());

            user.setText("{faw-user} " + discussion.getCreator());
            user.setOnClickListener(v -> fragment.showProfile(discussion.getCreator()));
            discussionTime.setText("{faw-calendar-o} " + discussion.getRelativeCreatedTime(context));

            if (extras == null) {
                // Still loading...
                progressBar.setVisibility(View.VISIBLE);
            } else {
                progressBar.setVisibility(View.GONE);

                if (!TextUtils.isEmpty(extras.getDescription())) {
                    description.setText(StringUtils.fromHtml(((Fragment) fragment).getActivity(), extras.getDescription()));
                    description.setVisibility(View.VISIBLE);
                    separator.setVisibility(View.VISIBLE);
                }

                if (extras.getXsrfToken() != null) {
                    commentDiscussion.setVisibility(View.VISIBLE);
                    actionSeparator.setVisibility(View.VISIBLE);
                }
            }

            commentDiscussion.setOnClickListener(v -> fragment.requestComment(null));
        }

        AttachedImageUtils.setFrom(itemView, extras, (CommonActivity) (((Fragment) fragment).getActivity()));
    }
}
