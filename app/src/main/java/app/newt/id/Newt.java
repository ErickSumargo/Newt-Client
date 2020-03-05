package app.newt.id;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.support.multidex.MultiDexApplication;

import java.util.Locale;

import app.newt.id.background.receiver.NetworkReceiver;
import app.newt.id.helper.Constant;
import app.newt.id.server.interfaces.Response;
import app.newt.id.server.model.Migration;
import app.newt.id.server.model.Module;
import app.newt.id.server.presenter.BasePresenter;
import app.newt.id.server.response.BaseResponse;
import app.newt.id.view.interfaces.NoInternet;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by Erick Sumargo on 2/18/2018.
 */

public class Newt extends MultiDexApplication implements NoInternet, Response {
    public Realm realm;

    private BasePresenter basePresenter;

    @Override
    public void onCreate() {
        super.onCreate();

        setDefLanguage();
        setDefFont();

        initRealm();
        if (onRestartRealm != null) {
            IntentFilter intentFilter = new IntentFilter(Constant.REALM_RESTART);
            registerReceiver(onRestartRealm, intentFilter);
        }

        NetworkReceiver networkReceiver = new NetworkReceiver();
        networkReceiver.setOnNoInternetListener(this);
        registerReceiver(networkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        basePresenter = new BasePresenter(getApplicationContext(), this);
        basePresenter.estConnection();
    }

    private void initRealm() {
        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration.Builder()
                .name("newt.realm")
                .modules(new Module())
                .schemaVersion(3)
                .migration(new Migration())
                .build();
        Realm.setDefaultConfiguration(config);

        realm = Realm.getDefaultInstance();
    }

    private void setDefLanguage() {
        Locale locale = new Locale("id");
        Locale.setDefault(locale);

        Configuration configuration = new Configuration();
        configuration.locale = locale;

        getBaseContext().getResources().updateConfiguration(configuration, getBaseContext().getResources().getDisplayMetrics());
    }

    private void setDefFont() {
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/NotoSans-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );
    }

    private BroadcastReceiver onRestartRealm = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Constant.REALM_RESTART)) {
                realm = Realm.getDefaultInstance();
            }
        }
    };

    @Override
    public void onInternetStatus(boolean connected) {

    }

    @Override
    public void onSuccess(BaseResponse base) {
    }

    @Override
    public void onFailure(String message) {
        basePresenter.estConnection();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }
}