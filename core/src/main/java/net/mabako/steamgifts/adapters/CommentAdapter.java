package net.mabako.steamgifts.adapters;

import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import net.mabako.steamgifts.adapters.viewholder.CommentContextViewHolder;
import net.mabako.steamgifts.adapters.viewholder.CommentViewHolder;
import net.mabako.steamgifts.adapters.viewholder.DiscussionCardViewHolder;
import net.mabako.steamgifts.adapters.viewholder.GiveawayCardViewHolder;
import net.mabako.steamgifts.adapters.viewholder.PollAnswerViewHolder;
import net.mabako.steamgifts.adapters.viewholder.PollHeaderViewHolder;
import net.mabako.steamgifts.data.Comment;
import net.mabako.steamgifts.data.Poll;
import net.mabako.steamgifts.fragments.DiscussionDetailFragment;
import net.mabako.steamgifts.fragments.GiveawayDetailFragment;
import net.mabako.steamgifts.fragments.ListFragment;
import net.mabako.steamgifts.fragments.interfaces.ICommentableFragment;
import net.mabako.steamgifts.fragments.interfaces.IHasPoll;
import net.mabako.steamgifts.fragments.util.DiscussionDetailsCard;
import net.mabako.steamgifts.fragments.util.GiveawayDetailsCard;

import java.util.List;

/**
 * Adapter to hold comments for a giveaway/discussion.
 */
public class CommentAdapter extends EndlessAdapter {
    private static final long serialVersionUID = 5961119226634909060L;

    /**
     * Amount of top-level items on a full comments page.
     */
    private static final int ITEMS_PER_PAGE = 25;

    /**
     * Fragment this all is shown in.
     */
    private transient Fragment fragment;

    public CommentAdapter() {
        this.alternativeEnd = true;
    }

    public void setFragmentValues(ListFragment fragment) {
        setLoadListener(fragment);
        this.fragment = fragment;
    }

    @Override
    public RecyclerView.ViewHolder onCreateActualViewHolder(View view, int viewType) {
        if (fragment == null)
            throw new IllegalStateException("Ain't got no fragment");

        if (viewType == Comment.VIEW_LAYOUT) {
            return new CommentViewHolder(view, fragment.getActivity(), (ICommentableFragment) fragment);
        } else if (viewType == GiveawayDetailsCard.VIEW_LAYOUT) {
            return new GiveawayCardViewHolder(view, (GiveawayDetailFragment) fragment);
        } else if (viewType == DiscussionDetailsCard.VIEW_LAYOUT) {
            return new DiscussionCardViewHolder(view, (DiscussionDetailFragment) fragment, fragment.getContext());

        } else if (viewType == CommentContextViewHolder.VIEW_LAYOUT) {
            return new CommentContextViewHolder(view, fragment.getActivity());

        } else if (viewType == Poll.Header.VIEW_LAYOUT) {
            return new PollHeaderViewHolder(view);
        } else if (viewType == Poll.Answer.VIEW_LAYOUT) {
            return new PollAnswerViewHolder(view);
        } else if (viewType == Poll.CommentSeparator.VIEW_LAYOUT) {
            return new RecyclerView.ViewHolder(view) {
            };
        }

        return null;
    }

    @Override
    public void onBindActualViewHolder(RecyclerView.ViewHolder h, int position) {
        if (h instanceof CommentViewHolder) {
            CommentViewHolder holder = (CommentViewHolder) h;
            Comment comment = (Comment) getItem(position);

            holder.setFrom(comment);
        } else if (h instanceof GiveawayCardViewHolder) {
            GiveawayCardViewHolder holder = (GiveawayCardViewHolder) h;
            GiveawayDetailsCard card = (GiveawayDetailsCard) getItem(position);

            holder.setFrom(card);
        } else if (h instanceof DiscussionCardViewHolder) {
            DiscussionCardViewHolder holder = (DiscussionCardViewHolder) h;
            DiscussionDetailsCard card = (DiscussionDetailsCard) getItem(position);

            holder.setFrom(card);
        } else if (h instanceof CommentContextViewHolder) {
            CommentContextViewHolder holder = (CommentContextViewHolder) h;
            CommentContextViewHolder.SerializableHolder info = (CommentContextViewHolder.SerializableHolder) getItem(position);

            holder.setFrom(info);
        } else if (h instanceof PollHeaderViewHolder) {
            ((PollHeaderViewHolder) h).setFrom((Poll.Header) getItem(position));
        } else if (h instanceof PollAnswerViewHolder && fragment instanceof IHasPoll) {
            ((PollAnswerViewHolder) h).setFrom((Poll.Answer) getItem(position), (IHasPoll) fragment);
        }
    }

    @Override
    protected boolean hasEnoughItems(List<IEndlessAdaptable> items) {
        if (items.size() < ITEMS_PER_PAGE)
            return false;

        int rootLevelComments = 0;
        for (IEndlessAdaptable adaptable : items)
            if (adaptable instanceof Comment comment && comment.getDepth() == 0)
                ++rootLevelComments;

        return rootLevelComments == ITEMS_PER_PAGE;
    }

    /**
     * Finds the comment with the given comment id
     *
     * @param commentId
     * @return comment with the given id, if found, null otherwise
     */
    public @Nullable Comment findItem(long commentId) {
        if (commentId == 0)
            return null;

        for (IEndlessAdaptable item : getItems()) {
            if (item instanceof Comment comment && comment.getId() == commentId) {
                return comment;
            }
        }

        return null;
    }

    public Poll.Answer findPollAnswer(int answerId) {
        if (answerId == 0)
            return null;

        for (IEndlessAdaptable item : getStickyItems()) {
            if (item instanceof Poll.Answer answer && answer.getId() == answerId) {
                return answer;
            }
        }

        return null;
    }

    /**
     * Replace an existing comment with a new one.
     *
     * @param comment new comment to add, should have the same comment id as the old one
     */
    public void replaceComment(Comment comment) {
        List<IEndlessAdaptable> items = getItems();
        for (int i = 0; i < items.size(); ++i) {
            IEndlessAdaptable item = items.get(i);
            if (item instanceof Comment c) {
                if (c.getId() == comment.getId()) {
                    // update the found comment
                    items.set(i, comment);

                    // notify of update
                    notifyItemChanged(comment);

                    return;
                }
            }
        }

        Log.w(CommentAdapter.class.getSimpleName(), "Comment with " + comment.getId() + " not found to update");
    }
}
