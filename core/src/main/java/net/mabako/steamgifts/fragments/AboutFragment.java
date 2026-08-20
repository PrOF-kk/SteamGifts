package net.mabako.steamgifts.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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

        layout.findViewById(R.id.issues).setOnClickListener(v -> open("https://github.com/PrOF-kk/SteamGifts/issues"));
        layout.findViewById(R.id.source).setOnClickListener(v -> open("https://github.com/PrOF-kk/SteamGifts"));
        layout.findViewById(R.id.faq).setOnClickListener(v -> open("https://www.steamgifts.com/about/faq"));
        layout.findViewById(R.id.guidelines).setOnClickListener(v -> open("https://www.steamgifts.com/about/guidelines"));
        layout.findViewById(R.id.comment_formatting).setOnClickListener(v -> open("https://www.steamgifts.com/about/comment-formatting"));
        layout.findViewById(R.id.privacy_policy).setOnClickListener(v -> open("https://www.steamgifts.com/legal/privacy-policy"));
        layout.findViewById(R.id.cookie_policy).setOnClickListener(v -> open("https://www.steamgifts.com/legal/cookie-policy"));
        layout.findViewById(R.id.terms_of_service).setOnClickListener(v -> open("https://www.steamgifts.com/legal/terms-of-service"));

        return layout;
    }

    private void open(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }
}
