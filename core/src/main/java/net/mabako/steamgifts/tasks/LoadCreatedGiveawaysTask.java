package net.mabako.steamgifts.tasks;

import android.net.Uri;

import net.mabako.steamgifts.adapters.IEndlessAdaptable;
import net.mabako.steamgifts.data.Game;
import net.mabako.steamgifts.fragments.ListFragment;
import net.mabako.steamgifts.fragments.profile.ProfileGiveaway;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class LoadCreatedGiveawaysTask extends LoadEndlessItemsTask {
    public LoadCreatedGiveawaysTask(ListFragment listFragment, int page) {
        super(listFragment, listFragment.getContext(), "giveaways/created", page, null);
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


        Elements columns = element.select(".table__column--width-small.text-center");

        giveaway.setPoints(-1);
        giveaway.setEntries(Utils.parseInt(columns.get(1).text()));

        Element end = firstColumn.expectFirst("span > span");
        giveaway.setEndTime(Integer.parseInt(end.attr("data-timestamp")), end.parent().text().trim());

        giveaway.setEntered("Unsent".equals(columns.get(1).text()));
        giveaway.setDeleted(!element.select(".table__column__deleted").isEmpty());

        return giveaway;
    }
}
