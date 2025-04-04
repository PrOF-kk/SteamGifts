package net.mabako.steamgifts.adapters.viewholder;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.net.Uri;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.mabako.steamgifts.activities.UrlHandlingActivity;
import net.mabako.steamgifts.core.R;

import java.util.regex.Pattern;

public final class StringUtils {
    private static final String TAG = StringUtils.class.getSimpleName();

    /**
     * Base path to resolve relative URLs.
     */
    private static final Uri BASE_URI = Uri.parse("https://www.steamgifts.com");

    private static final Pattern tdPattern = Pattern.compile("</td>([\\s\\r\\n]+)<td");
    private static final Pattern thPattern = Pattern.compile("</th>([\\s\\r\\n]+)<th");

    public static CharSequence fromHtml(@NonNull Context context, String source) {
        return fromHtml(context, source, true, null);
    }

    public static CharSequence fromHtml(@NonNull Context context, String source, boolean useCustomViewHandler, @Nullable Html.ImageGetter imageGetter) {
        if (TextUtils.isEmpty(source))
            return source;

        source = source
                .replace("\r\n", "\n")
                .replace("</tr>", "</tr><br/>");
        source = thPattern.matcher(tdPattern.matcher(source).replaceAll(" | </td><td")).replaceAll(" | </th><th");

        if (useCustomViewHandler) {
            try {
                CharSequence cs = fromHtml(source, imageGetter, new CustomHtmlTagHandler(context));
                cs = trim(cs, 0, cs.length());
                return addProperLinks(context, cs);
            } catch (Exception e) {
                Log.e(StringUtils.class.getSimpleName(), "Failed to parse HTML with custom parser", e);
            }
        }

        CharSequence cs = fromHtml(source, imageGetter, null);
        cs = trim(cs, 0, cs.length());
        return addProperLinks(context, cs);
    }

    /**
     * Wrapper around Html.fromHtml for deprecation.
     */
    private static CharSequence fromHtml(String source, Html.ImageGetter imageGetter, CustomHtmlTagHandler customHtmlTagHandler) {
        return Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY, imageGetter, customHtmlTagHandler);
    }

    private static CharSequence trim(CharSequence s, int start, int end) {
        while (start < end && Character.isWhitespace(s.charAt(start))) {
            start++;
        }

        while (end > start && Character.isWhitespace(s.charAt(end - 1))) {
            end--;
        }

        return s.subSequence(start, end);
    }

    /**
     * Convert all {@link URLSpan} (which uses the default browser) to use our custom {@link ClickableSpan} instead.
     *
     * @param context
     * @param charSequence
     * @return
     */
    private static CharSequence addProperLinks(@NonNull final Context context, CharSequence charSequence) {
        if (TextUtils.isEmpty(charSequence))
            return charSequence;

        SpannableStringBuilder stringBuilder = new SpannableStringBuilder(charSequence);
        URLSpan[] urls = stringBuilder.getSpans(0, charSequence.length(), URLSpan.class);
        for (URLSpan span : urls) {
            int start = stringBuilder.getSpanStart(span);
            int end = stringBuilder.getSpanEnd(span);
            int flags = stringBuilder.getSpanFlags(span);

            final String url = span.getURL();
            Uri uri = Uri.parse(span.getURL());

            // We only have a relative URL, relative to the base site.
            // This would ignore you if you'd reference ../somewhere or alike with the path not starting with /.
            if (uri.isRelative() && url.startsWith("/")) {
                uri = Uri.withAppendedPath(BASE_URI, url);
                Log.v(TAG, "Resolved relative URL " + url + " to " + uri);
            }

            if (uri.isAbsolute() && ("https".equals(uri.getScheme()) || "http".equals(uri.getScheme()))) {
                // Custom Span for clicking
                final Uri clickableUri = uri;
                stringBuilder.setSpan(new ClickableSpan() {
                    @Override
                    public void onClick(@NonNull View widget) {
                        // Do we have anything in the app we can open with that url?
                        UrlHandlingActivity.getIntentForUri(context, clickableUri, true).start((Activity) context);
                    }
                }, start, end, flags);
            } else {
                stringBuilder.setSpan(new ClickableSpan() {
                    @Override
                    public void onClick(@NonNull View widget) {
                        Toast.makeText(context, "Unable to open link " + url + ".", Toast.LENGTH_LONG).show();
                    }
                }, start, end, flags);
            }
            stringBuilder.removeSpan(span);
        }

        return stringBuilder;
    }

    public static void setBackgroundDrawable(Context context, View view, boolean highlighted) {
        setBackgroundDrawable(context, view, highlighted, R.attr.colorHighlightBackground);
    }

    public static void setBackgroundDrawable(Context context, View view, boolean highlighted, @AttrRes int attr) {
        if (highlighted) {
            int[] attrs = new int[]{attr};
            TypedArray ta = context.getTheme().obtainStyledAttributes(attrs);

            view.setBackground(ta.getDrawable(0));
            ta.recycle();
        } else {
            view.setBackgroundResource(R.color.colorTransparent);
        }
    }

    private StringUtils() {
    }
}
