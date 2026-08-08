package net.mabako.steamgifts.backport.style;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.style.LineBackgroundSpan;

import androidx.annotation.ColorInt;
import androidx.annotation.Px;

import org.jspecify.annotations.NonNull;

/// Partial [LineBackgroundSpan.Standard] for older APIs
public class LineBackgroundSpanStandard implements LineBackgroundSpan {
    private final int mColor;

    /**
     * Constructor taking a color integer.
     *
     * @param color Color integer that defines the background color.
     */
    public LineBackgroundSpanStandard(@ColorInt int color) {
        mColor = color;
    }

    /**
     * @return the color of this span.
     * @see Standard#Standard(int)
     */
    @ColorInt
    public final int getColor() {
        return mColor;
    }

    @Override
    public void drawBackground(@NonNull Canvas canvas, @NonNull Paint paint,
                               @Px int left, @Px int right,
                               @Px int top, @Px int baseline, @Px int bottom,
                               @NonNull CharSequence text, int start, int end,
                               int lineNumber) {
        final int originColor = paint.getColor();
        paint.setColor(mColor);
        canvas.drawRect(left, top, right, bottom, paint);
        paint.setColor(originColor);
    }
}
