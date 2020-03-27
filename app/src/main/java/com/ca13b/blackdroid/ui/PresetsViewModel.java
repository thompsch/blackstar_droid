package com.ca13b.blackdroid.ui;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class PresetsViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public PresetsViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is presets fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}