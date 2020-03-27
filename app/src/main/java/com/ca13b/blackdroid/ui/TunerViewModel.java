package com.ca13b.blackdroid.ui;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class TunerViewModel extends ViewModel {
    private MutableLiveData<String> mText;

    public TunerViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is tuner fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}
