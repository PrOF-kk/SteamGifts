package net.mabako.steamgifts.adapters;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import net.mabako.steamgifts.adapters.viewholder.MessageHeaderViewHolder;
import net.mabako.steamgifts.adapters.viewholder.MessageViewHolder;
import net.mabako.steamgifts.data.Comment;
import net.mabako.steamgifts.data.MessageHeader;
import net.mabako.steamgifts.fragments.profile.MessageListFragment;

import java.io.Serial;
import java.util.List;

public class MessageAdapter extends EndlessAdapter {
    @Serial
    private static final long serialVersionUID = 5997941227817634662L;

    private transient MessageListFragment fragment;

    public void setFragmentValues(MessageListFragment fragment) {
        setLoadListener(fragment);
        this.fragment = fragment;
    }

    @Override
    protected RecyclerView.ViewHolder onCreateActualViewHolder(View view, int viewType) {
        if (fragment == null)
            throw new IllegalStateException("no fragment");

        if (viewType == Comment.VIEW_LAYOUT) {
            return new MessageViewHolder(view, fragment.getActivity(), fragment);
        } else if (viewType == MessageHeader.VIEW_LAYOUT) {
            return new MessageHeaderViewHolder(view, fragment.getActivity());
        }

        throw new IllegalStateException();
    }

    @Override
    protected void onBindActualViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MessageHeaderViewHolder messageHeaderViewHolder) {
            messageHeaderViewHolder.setFrom((MessageHeader) getItem(position));
        } else if (holder instanceof MessageViewHolder messageViewHolder) {
            messageViewHolder.setFrom((Comment) getItem(position));
        }
    }

    @Override
    protected boolean hasEnoughItems(List<IEndlessAdaptable> items) {
        return !items.isEmpty();
    }
}
