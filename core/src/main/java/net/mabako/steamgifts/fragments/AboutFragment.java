package net.mabako.steamgifts.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import net.mabako.steamgifts.ApplicationTemplate;
import net.mabako.steamgifts.core.R;

public class AboutFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.fragment_about, container, false);

        TextView versionText = layout.findViewById(R.id.version);
        versionText.setText(String.format("Version %s (%s)", ((ApplicationTemplate) getActivity().getApplication()).getAppVersionName(), ((ApplicationTemplate) getActivity().getApplication()).getFlavor()));
        versionText.setOnClickListener(v -> {
            Toast.makeText(getContext(), String.format("Build %d", ((ApplicationTemplate) getActivity().getApplication()).getAppVersionCode()), Toast.LENGTH_SHORT).show();
        });

        layout.findViewById(R.id.issues).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://github.com/PrOF-kk/SteamGifts/issues"));

            startActivity(intent);
        });

        layout.findViewById(R.id.source).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://github.com/PrOF-kk/SteamGifts"));

            startActivity(intent);
        });

        return layout;
    }
}
