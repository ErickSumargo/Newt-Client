package app.newt.id.view.fragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import app.newt.id.R;
import app.newt.id.helper.Internet;
import app.newt.id.helper.Session;
import app.newt.id.server.interfaces.Response;
import app.newt.id.server.presenter.PasswordPresenter;
import app.newt.id.server.presenter.UserPresenter;
import app.newt.id.server.response.BaseResponse;
import app.newt.id.server.response.RegistrationResponse;
import app.newt.id.view.activity.PhoneResActivity;
import app.newt.id.view.activity.RegistrationActivity;
import app.newt.id.view.activity.PasswordResActivity;
import app.newt.id.view.custom.ProgressDialogCustom;

/**
 * Created by Erick Sumargo on 2/20/2018.
 */

public class PhoneRegFragment extends Fragment implements Response {
    private View view;

    private TextView headline, info;
    private EditText phoneField;
    private ProgressDialogCustom progressDialog;

    private UserPresenter userPresenter;
    private PasswordPresenter passwordPresenter;

    public static String phone, device, firebase;
    public static int regType;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_phone_reg, container, false);
        initView();

        return view;
    }

    private void initView() {
        headline = view.findViewById(R.id.headline);
        info = view.findViewById(R.id.info);
        phoneField = view.findViewById(R.id.phone_field);

        userPresenter = new UserPresenter(getContext(), this);
        passwordPresenter = new PasswordPresenter(getContext(), this);

        if (getActivity() instanceof RegistrationActivity) {
            regType = 0;
        } else if (getActivity() instanceof PasswordResActivity) {
            regType = 1;
        } else {
            regType = 2;
        }

        if (regType == 0) {
            headline.setText(getString(R.string.app_register));
            info.setText(getText(R.string.phone_reg_info));
        } else if (regType == 1) {
            headline.setText(getString(R.string.app_reset_password));
            info.setText(getText(R.string.phone_val_info));
        } else {
            headline.setText(getString(R.string.app_reset_phone));
            info.setText(getText(R.string.phone_res_info));
        }
    }

    public void validate() {
        phone = phoneField.getText().toString().trim();
        if (phone.length() == 0) {
            showErrorDialog(getString(R.string.phone_reg_error_1));
        } else if (phone.charAt(0) != '0' || phone.length() < 10) {
            showErrorDialog(getString(R.string.phone_reg_error_2));
        } else {
            showConfirmationDialog();
        }
    }

    private void showConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(phone)
                .setMessage(R.string.phone_reg_confirmation)
                .setNegativeButton("Tutup", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setPositiveButton("Lanjut", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        attemptReg();
                    }
                })
                .show();
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
        progressDialog.setMessage("Memeriksa...");

        if (Internet.isConnected(getContext())) {
            if (regType == 0) {
                device = Settings.Secure.getString(getActivity().getContentResolver(), Settings.Secure.ANDROID_ID);
                firebase = Session.with(getContext()).getFirebaseToken();

                userPresenter.registerPhone(phone, device);
            } else if (regType == 1) {
                passwordPresenter.validatePhone(phone);
            } else {
                userPresenter.reregisterPhone(phone);
            }
        } else {
            progressDialog.dismiss();

            if (regType == 0) {
                ((RegistrationActivity) getActivity()).notifyNoInternet();
            } else if (regType == 1) {
                ((PasswordResActivity) getActivity()).notifyNoInternet();
            } else {
                ((PhoneResActivity) getActivity()).notifyNoInternet();
            }
        }
    }

    private void loadError(int error) {
        if (error == 0) {
            if (regType == 0 || regType == 2) {
                showErrorDialog(getString(R.string.phone_reg_error_3));
            } else {
                showErrorDialog(getString(R.string.phone_val_error_1));
            }
        } else if (error == 1) {
            if (regType == 0) {
                showErrorDialog(getString(R.string.phone_reg_error_4));
            }
        }
    }

    @Override
    public void onSuccess(BaseResponse base) {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }

        RegistrationResponse r = (RegistrationResponse) base;
        if (r.isSuccess()) {
            if (!r.getData().isSkipped()) {
                if (regType == 0) {
                    ((RegistrationActivity) getActivity()).viewPager.setCurrentItem(1);

                    ((RegistrationActivity) getActivity()).previous.setText("KEMBALI");
                    ((RegistrationActivity) getActivity()).previous.setVisibility(View.VISIBLE);
                } else if (regType == 1) {
                    ((PasswordResActivity) getActivity()).viewPager.setCurrentItem(1);

                    ((PasswordResActivity) getActivity()).previous.setText("KEMBALI");
                    ((PasswordResActivity) getActivity()).previous.setVisibility(View.VISIBLE);
                } else {
                    ((PhoneResActivity) getActivity()).viewPager.setCurrentItem(1);

                    ((PhoneResActivity) getActivity()).previous.setText("KEMBALI");
                    ((PhoneResActivity) getActivity()).previous.setVisibility(View.VISIBLE);

                    ((PhoneResActivity) getActivity()).next.setText("VERIFIKASI");
                }
            } else {
                if (regType == 0) {
                    ((RegistrationActivity) getActivity()).viewPager.setCurrentItem(2);

                    ((RegistrationActivity) getActivity()).previous.setText("KEMBALI");
                    ((RegistrationActivity) getActivity()).previous.setVisibility(View.VISIBLE);

                    ((RegistrationActivity) getActivity()).next.setText("DAFTAR");
                } else if (regType == 1) {
                    ((PasswordResActivity) getActivity()).viewPager.setCurrentItem(2);

                    ((PasswordResActivity) getActivity()).previous.setText("KEMBALI");
                    ((PasswordResActivity) getActivity()).previous.setVisibility(View.VISIBLE);

                    ((PasswordResActivity) getActivity()).next.setText("RESET");
                }
            }
            phoneField.getText().clear();
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