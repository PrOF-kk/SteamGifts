package net.mabako.steamgifts.adapters.viewholder;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import net.mabako.steam.store.StoreSubFragment;
import net.mabako.steamgifts.core.R;
import net.mabako.steamgifts.data.Game;
import net.mabako.steamgifts.fragments.HiddenGamesFragment;

public class GameViewHolder extends RecyclerView.ViewHolder {
    private static final String TAG = GameViewHolder.class.getSimpleName();

    private final TextView gameName;
    private final ImageView image;

    private final Button removeGame;

    private final Fragment fragment;

    public GameViewHolder(View itemView, Fragment fragment) {
        super(itemView);
        this.fragment = fragment;

        gameName = itemView.findViewById(R.id.game_name);
        image = itemView.findViewById(R.id.game_image);

        removeGame = itemView.findViewById(R.id.remove_game);
    }

    public void setFrom(final Game game) {
        gameName.setText(game.getName());

        if (fragment instanceof HiddenGamesFragment && game.getInternalGameId() != Game.NO_APP_ID) {
            removeGame.setOnClickListener(v -> ((HiddenGamesFragment) fragment).requestShowGame(game.getInternalGameId(), game.getName()));
            removeGame.setVisibility(View.VISIBLE);
        } else if (fragment instanceof StoreSubFragment && game.getId() != Game.NO_APP_ID) {
            itemView.setOnClickListener(v -> ((StoreSubFragment) fragment).showDetails(game.getId()));
        }

        // giveaway_image
        if (game.getId() != Game.NO_APP_ID) {
            // Load capsule, fallback to header
            class ResizeImageOnSuccess implements Callback {
                @Override
                public void onSuccess() {
                    // We manually set the height of this image to fit the container.
                    ViewGroup.LayoutParams params = image.getLayoutParams();
                    params.height = itemView.getMeasuredHeight();
                }
                @Override
                public void onError(Exception e) { }
            }
            Picasso.get()
                    .load(game.getCdnUrl() + "/capsule_184x69.jpg")
                    .stableKey(game.getId() + "_capsule")
                    .into(image, new ResizeImageOnSuccess() {
                @Override
                public void onError(Exception e) {
                    // HTTP 404 is expected for delisted games and most bundles
                    if (!"HTTP 404".equals(e.getMessage())) {
                        Log.e(TAG, "Failed to load capsule image for game " + game.getId() + " (" + game.getName() + ")", e);
                        return;
                    }
                    // Fallback if capsule was 404
                    Picasso.get()
                            .load(game.getCdnUrl() + "/header.jpg")
                            .stableKey(game.getId() + "_capsule")
                            .resize(184, 69)
                            .into(image, new ResizeImageOnSuccess());
                }
            });
        } else {
            image.setImageResource(android.R.color.transparent);
        }
    }
}
