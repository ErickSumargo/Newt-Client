package app.newt.id.background.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import app.newt.id.helper.Internet;
import app.newt.id.helper.Utils;
import app.newt.id.view.interfaces.NoInternet;

/**
 * Created by Erick Sumargo on 2/24/2018.
 */

public class NetworkReceiver extends BroadcastReceiver {
    private static NoInternet mNoInternetListener = null;

    @Override
    public void onReceive(final Context context, final Intent intent) {
        if (mNoInternetListener != null) {
            boolean connected = Internet.isConnected(context);
            if (connected) {
                Utils.with(context).saveServerTime();
            }
            mNoInternetListener.onInternetStatus(connected);
        }
    }

    public void setOnNoInternetListener(Context context) {
        mNoInternetListener = (NoInternet) context;
    }
}