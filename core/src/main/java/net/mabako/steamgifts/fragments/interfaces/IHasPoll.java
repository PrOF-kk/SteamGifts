package net.mabako.steamgifts.fragments.interfaces;

import androidx.annotation.NonNull;

import net.mabako.steamgifts.data.Poll;

public interface IHasPoll {
    void selectPollAnswer(@NonNull Poll.Answer answer);

    void onPollAnswerSelected(int answerId);
}
