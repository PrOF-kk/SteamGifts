package net.mabako.steamgifts.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import net.mabako.Constants;
import net.mabako.steamgifts.adapters.IEndlessAdaptable;
import net.mabako.steamgifts.data.Comment;
import net.mabako.steamgifts.data.MessageHeader;
import net.mabako.steamgifts.fragments.interfaces.ILoadItemsListener;
import net.mabako.steamgifts.persistentdata.SteamGiftsUserData;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LoadMessagesTask extends AsyncTask<Void, Void, List<IEndlessAdaptable>> {
    private static final String TAG = LoadMessagesTask.class.getSimpleName();

    private final ILoadItemsListener listener;
    private final Context context;
    private final int page;

    private String foundXsrfToken = null;

    public LoadMessagesTask(ILoadItemsListener listener, Context context, int page) {
        this.listener = listener;
        this.context = context;
        this.page = page;
    }

    @Override
    protected List<IEndlessAdaptable> doInBackground(Void... params) {
        try {
            // Fetch the messages page

            OkHttpClient.Builder client = new OkHttpClient.Builder()
                    .callTimeout(Constants.JSOUP_TIMEOUT, TimeUnit.MILLISECONDS);
            Request.Builder request = new Request.Builder()
                    .url("https://www.steamgifts.com/messages/search?page=" + page)
                    .header("Cookie", "PHPSESSID=" + SteamGiftsUserData.getCurrent(context).getSessionId());

            Document document;
            try (Response response = client.build().newCall(request.build()).execute()) {
                document = Jsoup.parse(response.body().string());
            }

            SteamGiftsUserData.extract(context, document);

            // Fetch the xsrf token
            Element xsrfToken = document.selectFirst("input[name=xsrf_token]");
            if (xsrfToken != null)
                foundXsrfToken = xsrfToken.attr("value");

            // Parse all rows of giveaways
            return loadMessages(document);
        } catch (Exception e) {
            Log.e(TAG, "Error fetching URL", e);
            return null;
        }
    }

    private List<IEndlessAdaptable> loadMessages(Document document) {
        List<IEndlessAdaptable> list = new ArrayList<>();
        Elements children = document.select(".comments__entity");
        for (Element element : children) {
            Element link = element.selectFirst(".comments__entity__name a");
            if (link != null) {
                MessageHeader message = new MessageHeader(link.text(), link.absUrl("href"));

                Element commentElement = element.nextElementSibling();
                if (commentElement != null)
                    Utils.loadComments(commentElement, message, Comment.Type.COMMENT);

                // add the message & all associated comments.
                list.add(message);
                list.addAll(message.getComments());
            }
        }

        return list;
    }

    @Override
    protected void onPostExecute(List<IEndlessAdaptable> iEndlessAdaptables) {
        super.onPostExecute(iEndlessAdaptables);
        listener.addItems(iEndlessAdaptables, page == 1, foundXsrfToken);
    }
}
