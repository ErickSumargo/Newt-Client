package app.newt.id.view.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import app.newt.id.R;

/**
 * Created by Erick Sumargo on 2/20/2018.
 */

public class Intro3Fragment extends Fragment {
    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_intro_3, container, false);
        return view;
    }
}