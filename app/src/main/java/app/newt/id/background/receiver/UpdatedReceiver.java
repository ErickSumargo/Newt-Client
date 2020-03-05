package app.newt.id.background.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import app.newt.id.helper.Session;

/**
 * Created by Erick Sumargo on 2/24/2018.
 */

public class UpdatedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        if (intent.getDataString().contains(context.getPackageName()) && intent.getAction().equals(Intent.ACTION_PACKAGE_REPLACED)) {
            if (Session.with(context).isLogin()) {
                Session.with(context).startServices();
            }
        }
    }
}