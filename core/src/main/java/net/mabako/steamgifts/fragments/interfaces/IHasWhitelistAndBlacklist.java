package net.mabako.steamgifts.fragments.interfaces;

import net.mabako.steamgifts.data.BasicUser;

public interface IHasWhitelistAndBlacklist {
    enum What {
        WHITELIST,
        BLACKLIST
    }

    void requestUserListed(BasicUser user, What what, boolean adding);

    void onUserWhitelistOrBlacklistUpdated(BasicUser user, What what, boolean added);
}
