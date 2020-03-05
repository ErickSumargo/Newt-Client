package app.newt.id.view.fragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import app.newt.id.R;
import app.newt.id.helper.AES;
import app.newt.id.helper.Internet;
import app.newt.id.server.interfaces.Response;
import app.newt.id.server.presenter.PasswordPresenter;
import app.newt.id.server.response.BaseResponse;
import app.newt.id.server.response.RegistrationResponse;
import app.newt.id.view.activity.PasswordResActivity;
import app.newt.id.view.custom.ProgressDialogCustom;

/**
 * Created by Erick Sumargo on 2/20/2018.
 */

public class PasswordResFragment extends Fragment implements Response {
    private View view;

    private TextView headline;
    private EditText passwordField, passwordConfField;
    private ProgressDialogCustom progressDialog;

    private PasswordPresenter passwordPresenter;

    private String phone, password, passwordConf;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_password_res, container, false);
        initView();

        return view;
    }

    private void initView() {
        headline = view.findViewById(R.id.headline);
        passwordField = view.findViewById(R.id.password_field);
        passwordConfField = view.findViewById(R.id.password_conf_field);

        passwordPresenter = new PasswordPresenter(getContext(), this);

        headline.setText(getString(R.string.app_reset_password));
    }

    public void validate() {
        password = passwordField.getText().toString().trim();
        passwordConf = passwordConfField.getText().toString().trim();

        if (password.length() == 0) {
            showErrorDialog(getString(R.string.user_error_3));
        } else if (password.length() < 6) {
            showErrorDialog(getString(R.string.user_error_4));
        } else if (passwordConf.length() == 0) {
            showErrorDialog(getString(R.string.password_res_error_1));
        } else if (!password.equals(passwordConf)) {
            showErrorDialog(getString(R.string.password_res_error_2));
        } else {
            attemptResetPassword();
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

    private void attemptResetPassword() {
        progressDialog = new ProgressDialogCustom(getContext());
        progressDialog.setMessage("Me-reset password...");

        if (Internet.isConnected(getContext())) {
            phone = PhoneRegFragment.phone;
            passwordPresenter.resetPassword(phone, AES.encrypt(password));
        } else {
            progressDialog.dismiss();

            ((PasswordResActivity) getActivity()).notifyNoInternet();
        }
    }

    public void clearPasswordField() {
        passwordField.getText().clear();
        passwordConfField.getText().clear();
    }

    private void loadError(int error) {
        if (error == 0) {

        }
    }

    @Override
    public void onSuccess(BaseResponse base) {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }

        RegistrationResponse r = (RegistrationResponse) base;
        if (r.isSuccess()) {
            ((PasswordResActivity) getActivity()).passwordReset();
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