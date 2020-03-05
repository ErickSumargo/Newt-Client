package app.newt.id.view.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.List;

import app.newt.id.R;
import app.newt.id.helper.Constant;
import app.newt.id.helper.Session;
import app.newt.id.server.model.Bank;
import app.newt.id.server.model.Package;
import app.newt.id.server.model.User;
import app.newt.id.view.adapter.BankListAdapter;
import app.newt.id.view.adapter.PackageListAdapter;

/**
 * Created by Erick Sumargo on 2/20/2018.
 */

public class Payment2Fragment extends Fragment {
    private View view;

    private ScrollView contentView;
    private RecyclerView packageListView, bankListView;
    private TextView step1, step2, step3, step4;
    private TextView noteStep1, noteStep3;

    private ProgressBar loadingView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_payment_2, container, false);
        initView();

        return view;
    }

    private void initView() {
        contentView = view.findViewById(R.id.content_view);
        packageListView = view.findViewById(R.id.package_list_view);
        bankListView = view.findViewById(R.id.bank_list_view);
        loadingView = view.findViewById(R.id.loading_view);

        step1 = view.findViewById(R.id.step_1);
        step2 = view.findViewById(R.id.step_2);
        step3 = view.findViewById(R.id.step_3);
        step4 = view.findViewById(R.id.step_4);

        noteStep1 = view.findViewById(R.id.note_step_1);
        noteStep3 = view.findViewById(R.id.note_step_3);

        packageListView.setLayoutManager(new LinearLayoutManager(getContext()));
        packageListView.setNestedScrollingEnabled(false);

        bankListView.setLayoutManager(new LinearLayoutManager(getContext()));
        bankListView.setNestedScrollingEnabled(false);

        step1.setText(Html.fromHtml(getResources().getString(R.string.payment_2_step_1)));
        step2.setText(Html.fromHtml(getResources().getString(R.string.payment_2_step_2)));
        step3.setText(Html.fromHtml(getResources().getString(R.string.payment_2_step_3)));
        step4.setText(Html.fromHtml(getResources().getString(R.string.payment_2_step_4)));

        noteStep1.setText(Html.fromHtml(getResources().getString(R.string.payment_2_note_step_1)));
        noteStep3.setText(Html.fromHtml(getResources().getString(R.string.payment_2_note_step_3)));
    }

    public void loadData(List<Package> packages, List<Bank> banks) {
        PackageListAdapter packageListAdpt = new PackageListAdapter(packages);
        packageListView.setAdapter(packageListAdpt);

        BankListAdapter bankListAdpt = new BankListAdapter(banks);
        bankListView.setAdapter(bankListAdpt);

        loadingView.setVisibility(View.GONE);
        contentView.setVisibility(View.VISIBLE);
    }
}