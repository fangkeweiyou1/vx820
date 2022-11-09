package com.jetinno.ecrofverifone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by zhangyuncai on 2021/10/8.
 */
public class CardCheckoutCast extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("CardCheckoutCast", "定时结账");
        EcrWrapper.getInstance().checkout();
    }
}
