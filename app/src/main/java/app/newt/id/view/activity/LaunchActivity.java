package app.newt.id.view.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import app.newt.id.BuildConfig;
import app.newt.id.helper.Constant;
import app.newt.id.helper.Session;
import app.newt.id.helper.Utils;
import app.newt.id.server.model.Challenge;

public class LaunchActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent;
        if (!Session.with(this).isFirstLogin()) {
            intent = new Intent(this, IntroActivity.class);
        } else if (Session.with(this).isFirstLogin() && !Session.with(this).isLogin()) {
            Utils.with(this).createAppDir();
            intent = new Intent(this, LoginActivity.class);
        } else {
            Utils.with(this).createAppDir();
            if (Session.with(this).getVersionCode() > 0 && BuildConfig.VERSION_CODE > Session.with(this).getVersionCode()) {
                Session.with(this).saveChatSelected(false);
            }
            if (!Session.with(this).isChatSelected()) {
                intent = new Intent(this, HomeActivity.class);
            } else {
                intent = new Intent(this, DialogActivity.class);
            }
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}