package app.newt.id.view.activity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.rd.PageIndicatorView;

import app.newt.id.R;
import app.newt.id.helper.Utils;
import app.newt.id.view.custom.ViewPagerCustom;
import app.newt.id.view.fragment.Intro1Fragment;
import app.newt.id.view.fragment.Intro2Fragment;
import app.newt.id.view.fragment.Intro3Fragment;
import app.newt.id.view.fragment.Intro4Fragment;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class IntroActivity extends AppCompatActivity {
    private SectionsPagerAdapter sectionsPagerAdapter;
    public ViewPagerCustom viewPager;

    private PageIndicatorView indicator;

    private static String[] PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
//            Manifest.permission.ACCESS_FINE_LOCATION
    };

    private Intro1Fragment intro1;
    private Intro2Fragment intro2;
    private Intro3Fragment intro3;
    private Intro4Fragment intro4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        setPermission();
        initView();
        setEvent();
    }

    private void initView() {
        sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        viewPager = findViewById(R.id.container);
        viewPager.setAdapter(sectionsPagerAdapter);
        viewPager.setPagingEnabled(true);

        indicator = findViewById(R.id.indicator);

        intro1 = new Intro1Fragment();
        intro2 = new Intro2Fragment();
        intro3 = new Intro3Fragment();
        intro4 = new Intro4Fragment();
    }

    private void setEvent(){
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                indicator.setSelectedColor(getResources().getColor(Utils.colorsIntro[position]));
                indicator.setSelection(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void setPermission() {
        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, 0);
        }
    }

    private static boolean hasPermissions(Context context, String... permissions) {
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        for (int i = 0; i < grantResults.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                finish();
            }
        }
    }

    private class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return intro1;
                case 1:
                    return intro2;
                case 2:
                    return intro3;
                case 3:
                    return intro4;
            }
            return null;
        }

        @Override
        public int getCount() {
            return 4;
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}