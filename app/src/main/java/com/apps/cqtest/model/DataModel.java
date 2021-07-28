package com.apps.cqtest.model;

import android.graphics.Bitmap;

public class DataModel {
    private Bitmap bmPic;

    public DataModel(Bitmap bmPic) {
        this.bmPic = bmPic;
    }

    public Bitmap getBmPic() {
        return bmPic;
    }

    public void setBmPic(Bitmap bmPic) {
        this.bmPic = bmPic;
    }
}
