package app.newt.id.view.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import app.newt.id.R;
import app.newt.id.helper.AES;
import app.newt.id.helper.Internet;
import app.newt.id.helper.Session;
import app.newt.id.server.interfaces.Response;
import app.newt.id.server.presenter.UserPresenter;
import app.newt.id.server.response.BaseResponse;
import app.newt.id.server.response.UserResponse;
import app.newt.id.view.activity.DialogActivity;
import app.newt.id.view.activity.HomeActivity;
import app.newt.id.view.activity.PasswordResActivity;
import app.newt.id.view.activity.RegistrationActivity;
import app.newt.id.view.custom.ProgressDialogCustom;

/**
 * Created by Erick Sumargo on 2/20/2018.
 */

public class StudentRegFragment extends Fragment implements Response {
    private View view;

    private TextView headline;
    private EditText nameField, passwordField, schoolField, promoCodeField;
    private TextView promoNote;
    private ProgressDialogCustom progressDialog;

    private UserPresenter userPresenter;

    private String phone, device, firebase, name, password, school, promoCode;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_student_reg, container, false);
        initView();
        setEvent();

        return view;
    }

    private void initView() {
        headline = view.findViewById(R.id.headline);
        nameField = view.findViewById(R.id.name_field);
        passwordField = view.findViewById(R.id.password_field);
        schoolField = view.findViewById(R.id.school_field);
        promoCodeField = view.findViewById(R.id.promo_code_field);
        promoNote = view.findViewById(R.id.promo_note);

        userPresenter = new UserPresenter(getContext(), this);

        headline.setText(getString(R.string.app_register));
        promoNote.setText(Html.fromHtml(getContext().getResources().getString(R.string.promo_note)));
    }

    private void setEvent() {
        promoNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse("http://instagram.com/_u/newt.mobi");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                try {
                    intent.setPackage("com.instagram.android");
                    if (isIntentAvailable(getContext(), intent)) {
                        startActivity(intent);
                    }
                } catch (Exception e) {
                }
            }
        });
    }

    public void validate() {
        name = nameField.getText().toString().trim();
        password = passwordField.getText().toString().trim();
        school = schoolField.getText().toString().trim();
        promoCode = promoCodeField.getText().toString().trim();

        if (name.length() == 0) {
            showErrorDialog(getString(R.string.user_reg_error_1));
        } else if (!name.matches("^\\p{L}+[\\p{L}\\p{Z}\\p{P}]{0,}")) {
            showErrorDialog(getString(R.string.user_reg_error_2));
        } else if (password.length() == 0) {
            showErrorDialog(getString(R.string.user_error_3));
        } else if (password.length() < 6) {
            showErrorDialog(getString(R.string.user_error_4));
        } else if (school.length() == 0) {
            showErrorDialog(getString(R.string.user_error_5));
        } else {
            attemptReg();
        }
    }

    private void showErrorDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Kesalahan")
                .setMessage(message)
                .setNegativeButton("Tutup", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .show();
    }

    private void attemptReg() {
        progressDialog = new ProgressDialogCustom(getContext());
        progressDialog.setMessage("Membuat akun...");

        if (Internet.isConnected(getContext())) {
            phone = PhoneRegFragment.phone;
            device = PhoneRegFragment.device;
            firebase = PhoneRegFragment.firebase;

            userPresenter.registerStudent(name, AES.encrypt(password), school, promoCode, phone, device, firebase);
        } else {
            progressDialog.dismiss();

            if (getActivity() instanceof RegistrationActivity) {
                ((RegistrationActivity) getActivity()).notifyNoInternet();
            } else {
                ((PasswordResActivity) getActivity()).notifyNoInternet();
            }
        }
    }

    public void clearDataField() {
        nameField.getText().clear();
        passwordField.getText().clear();
    }

    private boolean isIntentAvailable(Context context, Intent intent) {
        final PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    private void loadError(int error) {
        if (error == 0) {

        } else if (error == 1) {

        } else if (error == 2) {
            showErrorDialog(getString(R.string.user_error_6));
        } else if (error == 3) {
            showErrorDialog(getString(R.string.user_error_8));
        }
    }

    @Override
    public void onSuccess(BaseResponse base) {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }

        UserResponse r = (UserResponse) base;
        if (r.isSuccess()) {
            Session.with(getContext()).saveUser(r.getData().getUser());
            Session.with(getContext()).saveToken(r.getData().getToken());

            Session.with(getContext()).saveFirstLogin();
            Session.with(getContext()).saveLogin();
            Session.with(getContext()).startServices();

            Intent intent = new Intent(getContext(), HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else {
            loadError(r.getError());
        }
    }

    @Override
    public void onFailure(String message) {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
        Toast.makeText(getActivity().getApplicationContext(), getContext().getString(R.string.connection_error_try), Toast.LENGTH_SHORT).show();
    }
}