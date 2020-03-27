package com.ca13b.blackdroid.ui;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ControlsViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public ControlsViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is controls fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}