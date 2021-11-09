package com.ahmednafe3.android_biometric_itegrated.ib_scan;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.Arrays;

public class IBScanViewModel extends ViewModel {
    public ArrayList<Integer> arrList = new ArrayList<>(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9));
    public int fingerPrintIndex = 1;
    public String deviceSerialNumber = "CD1108B-10103034";

    public MutableLiveData<String> validatedFingerPrintImage = new MutableLiveData<>();
    public MutableLiveData<String> FingerPrintError = new MutableLiveData<>();
    public MutableLiveData<String> FingerPrintRetryTrue = new MutableLiveData<>();
    public MutableLiveData<String> FingerPrintRetryFlagFalse = new MutableLiveData<>();
    public MutableLiveData<String> FingerPrintErrorSimVerified = new MutableLiveData<>();
    public MutableLiveData<String> FingerPrintAgentNotVerified = new MutableLiveData<>();


    public MutableLiveData<Boolean> onCustomerFingerPrintSuccess = new MutableLiveData<>();
    public MutableLiveData<String> onAgentFingerPrintSuccess = new MutableLiveData<>();


}
