package net.mabako.steamgifts.intro;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.squareup.picasso.Picasso;

import net.mabako.steamgifts.core.R;

import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

public class Slide extends Fragment {
    private SubView subview;

    public static Slide newInstance(SubView subview) {
        Bundle args = new Bundle();
        args.putSerializable("subview", subview);

        Slide fragment = new Slide();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        subview = (SubView) getArguments().getSerializable("subview");
        if (subview == null)
            throw new IllegalStateException("Not a slide given?");

        View view = inflater.inflate(subview.getLayout(), container, false);
        onCreateSubView(view.findViewById(R.id.intro_container));
        return view;
    }

    public void onCreateSubView(View view) {
        switch (subview) {
            case MAIN_WELCOME:
                ((TextView) view.getRootView().findViewById(R.id.welcome_text)).setText(getString(R.string.intro_giveaway_welcome_header, getText(R.string.app_name)));
                break;
            case MAIN_GIVEAWAY_1:
                // Giveaway
                View giveawayView = view.findViewById(R.id.giveaway);
                // Hide all indicators
                for (int id : new int[]{
                        R.id.separator, R.id.giveaway_list_indicator_group, R.id.giveaway_list_indicator_level_negative, R.id.giveaway_list_indicator_level_positive, R.id.giveaway_list_indicator_private, R.id.giveaway_list_indicator_whitelist, R.id.giveaway_list_indicator_region_restricted,
                        R.id.giveaway_list_indicator_cards, R.id.giveaway_list_indicator_dlc, R.id.giveaway_list_indicator_limited, R.id.giveaway_list_indicator_delisted
                }) {
                    giveawayView.findViewById(id).setVisibility(View.GONE);
                }

                // Comment
                View commentView = view.findViewById(R.id.comment);
                Picasso.get().load(R.drawable.default_avatar).placeholder(R.drawable.default_avatar_mask).transform(new RoundedCornersTransformation(20, 0)).into((ImageView) (commentView.findViewById(R.id.author_avatar)));
                commentView.findViewById(R.id.comment_indent).getLayoutParams().width = 0;
                break;

            case MAIN_GIVEAWAY_2:
                view.findViewById(R.id.separator).setVisibility(View.GONE);
                break;

            case MAIN_GIVEAWAY_3:
                view.findViewById(R.id.enter).setVisibility(View.VISIBLE);
                view.findViewById(R.id.login).setVisibility(View.GONE);
                view.findViewById(R.id.comment).setVisibility(View.VISIBLE);
                break;
        }
    }
}