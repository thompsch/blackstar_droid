package com.ca13b.blackdroid.ui;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class EffectsViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public EffectsViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is effects fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}