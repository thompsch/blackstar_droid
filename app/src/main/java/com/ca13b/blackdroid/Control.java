package com.ca13b.blackdroid;

import androidx.annotation.Nullable;

public class Control {

    public String controlName;
    public Integer controlId;
    public Integer controlValue;
    public Integer minValue;
    public Integer maxValue;

    public Control(String name, Integer id, Integer minValue, Integer maxValue){
        this.controlId = id;
        this.controlName = name;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == this) return true;
        Control ctrl = (Control)obj;
        return controlId == ctrl.controlId;
    }
}
