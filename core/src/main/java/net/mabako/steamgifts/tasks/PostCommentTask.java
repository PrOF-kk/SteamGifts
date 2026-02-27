package net.mabako.steamgifts.tasks;

import android.app.Activity;

import okhttp3.FormBody;
import okhttp3.Response;

public abstract class PostCommentTask extends AjaxTask<Activity> {
    private final String description;
    private final long parentId;

    public PostCommentTask(Activity activity, String path, String xsrfToken, String description, long parentId) {
        super(activity, activity, xsrfToken, "comment_new");

        setUrl("https://www.steamgifts.com/" + path);
        this.description = description;
        this.parentId = parentId;
    }

    @Override
    public void addExtraParameters(FormBody.Builder body) {
        body.add("parent_id", parentId == 0 ? "" : String.valueOf(parentId));
        body.add("description", description);
    }

    @Override
    protected void onPostExecute(Response response) {
        try (response) {
            if (response != null && response.code() == 301) {
                onSuccess();
            } else {
                onFail();
            }
        }
    }

    protected abstract void onSuccess();

    protected abstract void onFail();
}
