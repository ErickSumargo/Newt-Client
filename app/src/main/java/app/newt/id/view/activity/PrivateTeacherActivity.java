package app.newt.id.view.activity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;

import app.newt.id.R;
import app.newt.id.helper.Constant;
import app.newt.id.helper.GPSTracker;
import app.newt.id.helper.Internet;
import app.newt.id.helper.Session;
import app.newt.id.helper.Utils;
import app.newt.id.server.interfaces.Response;
import app.newt.id.server.model.PrivateTeacher;
import app.newt.id.server.presenter.BasePresenter;
import app.newt.id.server.response.BaseResponse;
import app.newt.id.server.response.PrivateTeacherResponse;
import app.newt.id.view.custom.ViewPagerCustom;
import app.newt.id.view.fragment.ExternalPrivateFragment;
import app.newt.id.view.fragment.InternalPrivateFragment;
import io.realm.Realm;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class PrivateTeacherActivity extends AppCompatActivity implements Response {
    private AppBarLayout appBar;
    private Toolbar toolbar;

    private CoordinatorLayout parentLayout;
    private SectionsPagerAdapter sectionsPagerAdapter;
    private ViewPagerCustom viewPager;
    private TabLayout tabLayout;

    private Snackbar snackbar;

    private ExternalPrivateFragment externalPrivateFragment;
    private InternalPrivateFragment internalPrivateFragment;

    private int gradeId;
    private double latitude, longitude;

    private BasePresenter basePresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        gradeId = getIntent().getIntExtra("grade", 1);
        setTheme(Utils.privateStyles[gradeId - 1]);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_private_teacher);

        init();
        setEvent();
    }

    private void init() {
        appBar = findViewById(R.id.app_bar);
        appBar.setBackgroundResource(R.color.white);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        parentLayout = findViewById(R.id.parent_layout);
        sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        viewPager = findViewById(R.id.container);
        viewPager.setOffscreenPageLimit(sectionsPagerAdapter.getCount() - 1);
        viewPager.setAdapter(sectionsPagerAdapter);
        viewPager.setPagingEnabled(true);

        tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        for (int i = 0; i < sectionsPagerAdapter.getCount(); i++) {
            tabLayout.getTabAt(i).setCustomView(R.layout.custom_tab);

            View tabLesson = tabLayout.getTabAt(i).getCustomView();
            initTab(tabLesson, i);
        }

        if (gradeId == 1) {
            getSupportActionBar().setTitle("SD");
        } else if (gradeId == 2) {
            getSupportActionBar().setTitle("SMP");
        } else if (gradeId == 3) {
            getSupportActionBar().setTitle("SMA");
        } else if (gradeId == 4) {
            getSupportActionBar().setTitle("English");
        } else if (gradeId == 5) {
            getSupportActionBar().setTitle("Mandarin");
        } else if (gradeId == 6) {
            getSupportActionBar().setTitle("Programming");
        }
        externalPrivateFragment = new ExternalPrivateFragment();
        internalPrivateFragment = new InternalPrivateFragment();

        basePresenter = new BasePresenter(getApplicationContext(), this);
    }

    private void setEvent() {
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void initTab(View tab, int pos) {
        ImageView tabIcon = tab.findViewById(R.id.icon);
        TextView tabName = tab.findViewById(R.id.name);
        TextView tabCounter = tab.findViewById(R.id.counter);

        tabName.setText(sectionsPagerAdapter.getPageTitle(pos));
        tabName.setTextColor(getResources().getColor(R.color.black));

        tabIcon.setVisibility(View.GONE);
        tabCounter.setVisibility(View.GONE);
    }

    private void setPermission() {
        if (!hasPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION})) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        } else {
            if (!isLocationEnabled()) {
                showEnableLocationDialog();
            } else {
                loadPrivateTeachers();
            }
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
            } else {
                if (!isLocationEnabled()) {
                    showEnableLocationDialog();
                }
            }
        }
    }

    private boolean isLocationEnabled() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private void showEnableLocationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(PrivateTeacherActivity.this);
        builder.setTitle("Konfirmasi")
                .setMessage("Untuk menggunakan layanan ini, aktifkan layanan Google's location")
                .setNegativeButton("Tutup", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setPositiveButton("Aktifkan", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .show();
    }

    private void loadPrivateTeachers() {
        if (Internet.isConnected(this)) {
            while ((int) latitude == 0 && (int) longitude == 0) {
                GPSTracker gps = new GPSTracker(this);
                latitude = gps.getLatitude();
                longitude = gps.getLongitude();
            }
            basePresenter.loadPrivateTeachers(gradeId);
        } else {
            snackbar = Snackbar.make(parentLayout, getResources().getString(R.string.no_internet), Snackbar.LENGTH_INDEFINITE);
            snackbar.setAction("Coba Lagi", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    snackbar.dismiss();
                    loadPrivateTeachers();
                }
            });
            snackbar.show();
        }
    }

    private class SectionsPagerAdapter extends FragmentStatePagerAdapter {

        private SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return externalPrivateFragment;
                case 1:
                    return internalPrivateFragment;
            }
            return null;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "LES PRIVAT";
                case 1:
                    return "BIMBEL";
            }
            return null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        setPermission();
    }

    @Override
    protected void onPause() {
        externalPrivateFragment.stopTimer();
        internalPrivateFragment.stopTimer();

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        externalPrivateFragment.stopTimer();
        internalPrivateFragment.stopTimer();

        super.onDestroy();
    }

    @Override
    public void onSuccess(BaseResponse base) {
        PrivateTeacherResponse r = (PrivateTeacherResponse) base;
        List<String> privatePromos = r.getData().getPrivatePromos();
        List<String> tuitionPromos = r.getData().getTuitionPromos();
        List<PrivateTeacher> privateTeachers = r.getData().getPrivateTeachers();
        List<PrivateTeacher> tuitionTeachers = r.getData().getTuitionTeachers();

        externalPrivateFragment.loadData(privatePromos, privateTeachers);
        internalPrivateFragment.loadData(tuitionPromos, tuitionTeachers);
    }

    @Override
    public void onFailure(String message) {
        basePresenter.loadPrivateTeachers(gradeId);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}