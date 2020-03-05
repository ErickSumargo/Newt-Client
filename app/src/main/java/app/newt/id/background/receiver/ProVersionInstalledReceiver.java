package app.newt.id.background.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import app.newt.id.Newt;
import app.newt.id.helper.Constant;
import app.newt.id.helper.Session;
import app.newt.id.helper.Utils;
import io.realm.Realm;

/**
 * Created by Erick Sumargo on 2/24/2018.
 */

public class ProVersionInstalledReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        if (Utils.with(context).proVersionInstalled()) {
            if (Session.with(context).isLogin()) {
                Session.with(context).logout(1);

                final Realm realm = ((Newt) context.getApplicationContext()).realm;
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        realm.close();
                        try {
                            Realm.deleteRealm(realm.getDefaultConfiguration());
                        } catch (Exception e) {
                        }

                        Intent intent = new Intent();
                        intent.setAction(Constant.REALM_RESTART);
                        context.sendBroadcast(intent);
                    }
                });
            }
        }
    }
}