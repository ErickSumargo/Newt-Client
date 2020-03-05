package app.newt.id.view.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import app.newt.id.R;
import app.newt.id.helper.Utils;
import app.newt.id.server.interfaces.Response;
import app.newt.id.server.model.Bank;
import app.newt.id.server.model.Package;
import app.newt.id.server.model.Provider;
import app.newt.id.server.presenter.TransactionPresenter;
import app.newt.id.server.response.BaseResponse;
import app.newt.id.server.response.TransactionResponse;
import app.newt.id.view.custom.ViewPagerCustom;
import app.newt.id.view.fragment.Payment1Fragment;
import app.newt.id.view.fragment.Payment2Fragment;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class PaymentActivity extends AppCompatActivity implements Response {
    private Toolbar toolbar;
    private TabLayout tabLayout;

    private SectionsPagerAdapter sectionsPagerAdapter;
    private ViewPagerCustom viewPager;

    private Payment1Fragment payment1Fragment;
    private Payment2Fragment payment2Fragment;

    private TransactionPresenter transactionPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        initView();
        setEvent();
    }

    private void initView() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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

        payment1Fragment = new Payment1Fragment();
        payment2Fragment = new Payment2Fragment();

        transactionPresenter = new TransactionPresenter(getApplicationContext(), this);
        transactionPresenter.getTransactionDatas();
    }

    private void initTab(View tab, int pos) {
        ImageView tabIcon = tab.findViewById(R.id.icon);
        TextView tabName = tab.findViewById(R.id.name);
        TextView tabCounter = tab.findViewById(R.id.counter);

        tabName.setText(sectionsPagerAdapter.getPageTitle(pos));
        tabIcon.setVisibility(View.GONE);
        tabCounter.setVisibility(View.GONE);
    }

    private void setEvent() {
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private class SectionsPagerAdapter extends FragmentStatePagerAdapter {

        private SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return payment1Fragment;
                case 1:
                    return payment2Fragment;
            }
            return null;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return getString(Utils.payments[position]).toUpperCase();
        }
    }

    @Override
    public void onSuccess(BaseResponse base) {
        TransactionResponse r = (TransactionResponse) base;
        final List<Package> packages = r.getData().getPackages();
        final List<Provider> providers = r.getData().getProviders();
        final List<Bank> banks = r.getData().getBanks();

        payment1Fragment.loadData(packages, providers);
        payment2Fragment.loadData(packages, banks);
    }

    @Override
    public void onFailure(String message) {
        transactionPresenter.getTransactionDatas();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}