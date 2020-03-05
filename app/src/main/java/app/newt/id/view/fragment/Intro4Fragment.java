package app.newt.id.view.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import app.newt.id.R;
import app.newt.id.helper.Utils;
import app.newt.id.view.activity.LoginActivity;

/**
 * Created by Erick Sumargo on 2/20/2018.
 */

public class Intro4Fragment extends Fragment {
    private View view;

    private Button login;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_intro_4, container, false);
        initView();
        setEvent();

        return view;
    }

    private void initView() {
        login = view.findViewById(R.id.login);
    }

    private void setEvent() {
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!Utils.with(getContext()).proVersionInstalled()) {
                    Utils.with(getContext()).createAppDir();

                    Intent intent = new Intent(getContext(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } else {
                    Toast.makeText(getContext().getApplicationContext(), "Silahkan menggunakan versi Pro yang telah terinstall", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}