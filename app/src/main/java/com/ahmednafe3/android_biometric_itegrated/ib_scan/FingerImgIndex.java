package com.ahmednafe3.android_biometric_itegrated.ib_scan;


import com.ahmednafe3.android_biometric_itegrated.R;

public enum FingerImgIndex {
    FINGER1("1", R.drawable.hand_1),
    FINGER2("2", R.drawable.hand_2),
    FINGER3("3", R.drawable.hand_3),
    FINGER4("4", R.drawable.hand_4),
    FINGER5("5", R.drawable.hand_5),
    FINGER6("6", R.drawable.hand_6),
    FINGER7("7", R.drawable.hand_7),
    FINGER8("8", R.drawable.hand_8),
    FINGER9("9", R.drawable.hand_9),
    FINGER10("10", R.drawable.hand_10);
    private String value;


    private int image;

    private FingerImgIndex(String value, int image) {

        this.value = value;
        this.image = image;

    }

    public String getValue() {
        return value;
    }

    public int getImage() {
        return image;
    }


}
