package net.mabako;

import android.os.Build;

import java.util.List;

public final class Constants {
    private Constants() {}

    /// Important user roles we want to display. Does not include "Member" or "Game Developer".
    public static final List<String> IMPORTANT_USER_ROLES = List.of("Administrator", "Ultra Moderator", "Super Moderator", "Senior Moderator", "Junior Moderator");

    /// User agent to be used for jsoup connections, which is a generic Chrome build.
    public static final String JSOUP_USER_AGENT = "Mozilla/5.0 (Linux; Android " + Build.VERSION.RELEASE + ") AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.0 Safari/537.36";

    /// Timeout to be used for web requests in milliseconds.
    /// The default is:
    /// * jsoup: 30 seconds
    /// * OkHttp: 10 (connect), 10 (write), 10 (read)
    public static final int HTTP_TIMEOUT = 10000;
}
