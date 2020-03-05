package app.newt.id.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import app.newt.id.R;
import app.newt.id.view.custom.ViewPagerCustom;
import app.newt.id.view.fragment.CodeVerFragment;
import app.newt.id.view.fragment.PasswordResFragment;
import app.newt.id.view.fragment.PhoneRegFragment;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class PasswordResActivity extends AppCompatActivity {
    private CoordinatorLayout parentLayout;
    private RelativeLayout childLayout;

    private SectionsPagerAdapter sectionsPagerAdapter;
    public ViewPagerCustom viewPager;

    public TextView previous, next;
    private Snackbar snackbar;

    private PhoneRegFragment phoneReg;
    private CodeVerFragment codeVer;
    private PasswordResFragment passRes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_res);

        initView();
        setEvent();
    }

    private void initView() {
        parentLayout = findViewById(R.id.parent_layout);
        childLayout = findViewById(R.id.child_layout);

        sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        viewPager = findViewById(R.id.container);
        viewPager.setAdapter(sectionsPagerAdapter);

        previous = findViewById(R.id.previous);
        next = findViewById(R.id.next);

        next.setText("LANJUT");

        phoneReg = new PhoneRegFragment();
        codeVer = new CodeVerFragment();
        passRes = new PasswordResFragment();
    }

    private void setEvent() {
        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (viewPager.getCurrentItem() == 1) {
                    viewPager.setCurrentItem(0);
                    previous.setVisibility(View.GONE);

                    if (codeVer.isTimerRunning) {
                        codeVer.timer.cancel();
                        codeVer.resetTimer();
                    }
                    codeVer.clearCodeField();
                } else {
                    viewPager.setCurrentItem(0);
                    previous.setVisibility(View.GONE);
                    next.setText("LANJUT");

                    passRes.clearPasswordField();
                }
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (viewPager.getCurrentItem() == 0) {
                    phoneReg.validate();
                } else if (viewPager.getCurrentItem() == 1) {
                    codeVer.validate();
                } else {
                    passRes.validate();
                }
            }
        });
    }

    public void notifyNoInternet() {
        snackbar = Snackbar.make(parentLayout, R.string.no_internet, Snackbar.LENGTH_SHORT);
        snackbar.addCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar snackbar, int event) {
                super.onDismissed(snackbar, event);
                childLayout.setTranslationY(0.0f);
            }
        });
        snackbar.show();
    }

    public void passwordReset() {
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
    }

    private class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return phoneReg;
                case 1:
                    return codeVer;
                case 2:
                    return passRes;
            }
            return null;
        }

        @Override
        public int getCount() {
            return 3;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (viewPager.getCurrentItem() == 0) {
                onBackPressed();
            } else if (viewPager.getCurrentItem() == 1) {
                viewPager.setCurrentItem(0);
                previous.setVisibility(View.GONE);

                if (codeVer.isTimerRunning) {
                    codeVer.timer.cancel();
                    codeVer.resetTimer();
                }
                codeVer.clearCodeField();
            } else {
                viewPager.setCurrentItem(0);
                previous.setVisibility(View.GONE);
                next.setText("LANJUT");

                passRes.clearPasswordField();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        if (codeVer.isTimerRunning) {
            codeVer.timer.cancel();
            codeVer.resetTimer();
        }
        super.onDestroy();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}