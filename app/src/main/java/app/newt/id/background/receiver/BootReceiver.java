package app.newt.id.background.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import app.newt.id.helper.Session;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            if (Session.with(context).isLogin()) {
                Session.with(context).startServices();
            }
        }
    }
}