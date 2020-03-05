package app.newt.id.view.fragment;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;

import app.newt.id.R;
import app.newt.id.helper.Constant;
import app.newt.id.helper.Session;
import app.newt.id.helper.Utils;
import app.newt.id.server.model.Package;
import app.newt.id.server.model.Provider;
import app.newt.id.server.model.User;
import app.newt.id.view.adapter.PackageListAdapter;
import app.newt.id.view.adapter.ProviderListAdapter;

/**
 * Created by Erick Sumargo on 2/20/2018.
 */

public class Payment1Fragment extends Fragment {
    private View view;

    private ScrollView contentView;
    private RecyclerView packageListView, providerListView;
    private TextView step1, step2, step3;
    private TextView noteStep1, noteStep2;

    private TextView p1s1, p1s2, p1s3, p1s4, p1s5;

    private ProgressBar loadingView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_payment_1, container, false);
        initView();

        return view;
    }

    private void initView() {
        contentView = view.findViewById(R.id.content_view);
        packageListView = view.findViewById(R.id.package_list_view);
        providerListView = view.findViewById(R.id.provider_list_view);
        loadingView = view.findViewById(R.id.loading_view);

        step1 = view.findViewById(R.id.step_1);
        step2 = view.findViewById(R.id.step_2);
        step3 = view.findViewById(R.id.step_3);

        noteStep1 = view.findViewById(R.id.note_step_1);
        noteStep2 = view.findViewById(R.id.note_step_2);

        p1s1 = view.findViewById(R.id.p1s1);
        p1s2 = view.findViewById(R.id.p1s2);
        p1s3 = view.findViewById(R.id.p1s3);
        p1s4 = view.findViewById(R.id.p1s4);
        p1s5 = view.findViewById(R.id.p1s5);

        packageListView.setLayoutManager(new LinearLayoutManager(getContext()));
        packageListView.setNestedScrollingEnabled(false);

        providerListView.setLayoutManager(new LinearLayoutManager(getContext()));
        providerListView.setNestedScrollingEnabled(false);

        step1.setText(Html.fromHtml(getResources().getString(R.string.payment_1_step_1)));
        step2.setText(Html.fromHtml(getResources().getString(R.string.payment_1_step_2)));
        step3.setText(Html.fromHtml(getResources().getString(R.string.payment_1_step_3)));

        noteStep1.setText(Html.fromHtml(getResources().getString(R.string.payment_1_note_step_1)));
        noteStep2.setText(Html.fromHtml(getResources().getString(R.string.payment_1_note_step_2)));

        p1s1.setText(Html.fromHtml(getResources().getString(R.string.example_payment_1_step_1)));
        p1s2.setText(Html.fromHtml(getResources().getString(R.string.example_payment_1_step_2)));
        p1s3.setText(Html.fromHtml(getResources().getString(R.string.example_payment_1_step_3)));
        p1s4.setText(Html.fromHtml(getResources().getString(R.string.example_payment_1_step_4)));
        p1s5.setText(Html.fromHtml(getResources().getString(R.string.example_payment_1_step_5)));
    }

    public void loadData(List<Package> packages, List<Provider> providers) {
        PackageListAdapter packageListAdpt = new PackageListAdapter(packages);
        packageListView.setAdapter(packageListAdpt);

        ProviderListAdapter providerListAdpt = new ProviderListAdapter(providers);
        providerListView.setAdapter(providerListAdpt);

        loadingView.setVisibility(View.GONE);
        contentView.setVisibility(View.VISIBLE);
    }
}