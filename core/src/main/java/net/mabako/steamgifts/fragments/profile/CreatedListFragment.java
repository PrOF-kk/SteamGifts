package net.mabako.steamgifts.fragments.profile;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import net.mabako.steamgifts.adapters.GiveawayAdapter;
import net.mabako.steamgifts.adapters.IEndlessAdaptable;
import net.mabako.steamgifts.core.R;
import net.mabako.steamgifts.data.Game;
import net.mabako.steamgifts.fragments.ListFragment;
import net.mabako.steamgifts.fragments.interfaces.IActivityTitle;
import net.mabako.steamgifts.tasks.LoadGameListTask;
import net.mabako.steamgifts.tasks.Utils;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.Serializable;

public class CreatedListFragment extends ListFragment<GiveawayAdapter> implements IActivityTitle {
    @Override
    public int getTitleResource() {
        return R.string.user_tab_created;
    }

    @Override
    public String getExtraTitle() {
        return null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter.setFragmentValues(getActivity(), this, null);
    }

    @NonNull
    @Override
    protected GiveawayAdapter createAdapter() {
        return new GiveawayAdapter(50, PreferenceManager.getDefaultSharedPreferences(getContext()));
    }

    @Override
    protected AsyncTask<Void, Void, ?> getFetchItemsTask(int page) {
        return new LoadGameListTask(this, getContext(), "giveaways/created", page, null) {
            @Override
            protected IEndlessAdaptable load(Element element) {
                Element firstColumn = element.select(".table__column--width-fill").first();
                Element link = firstColumn.select("a.table__column__heading").first();

                Uri linkUri = Uri.parse(link.attr("href"));
                String giveawayLink = linkUri.getPathSegments().get(1);
                String giveawayName = linkUri.getPathSegments().get(2);

                ProfileGiveaway giveaway = new ProfileGiveaway(giveawayLink);
                giveaway.setName(giveawayName);
                giveaway.setTitle(link.text());

                giveaway.setGame(new Game());
                Element thumbnail = element.select(".table_image_thumbnail").first();
                if (thumbnail != null) {
                    Game game = Utils.extractGameFromThumbnail(thumbnail);
                    if (game != null) {
                        giveaway.setGame(game);
                    }
                }


                Elements columns = element.select(".table__column--width-small.text-center");

                giveaway.setPoints(-1);
                giveaway.setEntries(Utils.parseInt(columns.get(1).text()));

                Element end = firstColumn.select("span > span").first();
                giveaway.setEndTime(Integer.parseInt(end.attr("data-timestamp")), end.parent().text().trim());

                giveaway.setEntered("Unsent".equals(columns.get(1).text()));
                giveaway.setDeleted(!element.select(".table__column__deleted").isEmpty());

                return giveaway;
            }
        };
    }

    @Override
    protected Serializable getType() {
        return null;
    }
}
