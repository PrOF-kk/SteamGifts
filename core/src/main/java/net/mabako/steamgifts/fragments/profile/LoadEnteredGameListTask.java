package net.mabako.steamgifts.fragments.profile;

import android.net.Uri;

import net.mabako.steamgifts.adapters.IEndlessAdaptable;
import net.mabako.steamgifts.data.Game;
import net.mabako.steamgifts.fragments.ListFragment;
import net.mabako.steamgifts.tasks.LoadGameListTask;
import net.mabako.steamgifts.tasks.Utils;

import org.jsoup.nodes.Element;

public class LoadEnteredGameListTask extends LoadGameListTask {
    public static final int ENTRIES_PER_PAGE = 50;

    public LoadEnteredGameListTask(ListFragment listFragment, int page) {
        super(listFragment, listFragment.getContext(), "giveaways/entered", page, null);
    }

    @Override
    protected IEndlessAdaptable load(Element element) {
        Element firstColumn = element.expectFirst(".table__column--width-fill");
        Element link = firstColumn.expectFirst("a.table__column__heading");

        Uri linkUri = Uri.parse(link.attr("href"));
        String giveawayLink = linkUri.getPathSegments().get(1);
        String giveawayName = linkUri.getPathSegments().get(2);

        ProfileGiveaway giveaway = new ProfileGiveaway(giveawayLink);
        giveaway.setName(giveawayName);
        giveaway.setTitle(link.text());

        giveaway.setGame(new Game());
        Element thumbnail = element.selectFirst(".table_image_thumbnail");
        if (thumbnail != null) {
            Game game = Utils.extractGameFromThumbnail(thumbnail);
            if (game != null) {
                giveaway.setGame(game);
            }
        }

        giveaway.setPoints(-1);
        giveaway.setEntries(Utils.parseInt(element.expectFirst(".table__column--width-small").text()));

        Element end = firstColumn.selectFirst("p > span");
        if (end != null) {
            giveaway.setEndTime(Integer.parseInt(end.attr("data-timestamp")), end.parent().text().trim());
        }

        giveaway.setEntered(giveaway.isOpen());
        giveaway.setDeleted(element.selectFirst(".table__column__deleted") != null);

        return giveaway;
    }
}
