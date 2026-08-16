package net.mabako.steam.store.viewholder;

import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import net.mabako.steam.store.data.Text;
import net.mabako.steamgifts.adapters.viewholder.StringUtils;
import net.mabako.steamgifts.core.R;

public class TextViewHolder extends RecyclerView.ViewHolder {
    private final TextView textView;

    public TextViewHolder(View itemView) {
        super(itemView);

        textView = itemView.findViewById(R.id.text);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    public void setFrom(Text text) {
        if (TextUtils.isEmpty(text.text())) {
            textView.setText(null);
        } else if (text.html()) {
            var context = textView.getContext();
            textView.setText(StringUtils.fromHtml(context, text.text(), true, new StoreImageGetter(textView, context.getResources())));
            textView.setMovementMethod(LinkMovementMethod.getInstance());
        } else {
            textView.setText(text.text());
        }
    }
}
