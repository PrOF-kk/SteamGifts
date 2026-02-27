package net.mabako.steamgifts.tasks;

import android.widget.Toast;

import net.mabako.steamgifts.fragments.profile.MessageListFragment;

import okhttp3.Response;

/**
 * Mark all messages read.
 */
public class MarkMessagesReadTask extends AjaxTask<MessageListFragment> {
    public MarkMessagesReadTask(MessageListFragment fragment, String xsrfToken) {
        super(fragment, fragment.getContext(), xsrfToken, "read_messages");
        setUrl("https://www.steamgifts.com/messages");
    }

    @Override
    protected void onPostExecute(Response response) {
        super.onPostExecute(response);
        try (response) {
            if (response != null && response.code() == 301) {
                getFragment().onMarkedMessagesRead();
                Toast.makeText(getFragment().getContext(), "Marked all messages as read", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getFragment().getContext(), "Error marking messages as read", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
