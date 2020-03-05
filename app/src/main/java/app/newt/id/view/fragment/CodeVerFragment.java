package app.newt.id.view.fragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import app.newt.id.R;
import app.newt.id.helper.Constant;
import app.newt.id.helper.Internet;
import app.newt.id.helper.Session;
import app.newt.id.server.interfaces.Response;
import app.newt.id.server.presenter.UserPresenter;
import app.newt.id.server.response.BaseResponse;
import app.newt.id.server.response.RegistrationResponse;
import app.newt.id.server.response.UserResponse;
import app.newt.id.view.activity.PhoneResActivity;
import app.newt.id.view.activity.RegistrationActivity;
import app.newt.id.view.activity.PasswordResActivity;
import app.newt.id.view.custom.ProgressDialogCustom;

/**
 * Created by Erick Sumargo on 2/20/2018.
 */

public class CodeVerFragment extends Fragment implements Response {
    private View view;

    private TextView headline;
    private EditText codeField1, codeField2, codeField3, codeField4;
    private TextView timerLabel, resendLabel;
    private ProgressDialogCustom progressDialog;

    private UserPresenter userPresenter;

    public CountDownTimer timer;
    public boolean isTimerRunning = false;

    private String phone, code;
    private int regType;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_code_ver, container, false);
        initView();
        setEvent();

        return view;
    }

    private void initView() {
        headline = view.findViewById(R.id.headline);

        codeField1 = view.findViewById(R.id.code_field_1);
        codeField2 = view.findViewById(R.id.code_field_2);
        codeField3 = view.findViewById(R.id.code_field_3);
        codeField4 = view.findViewById(R.id.code_field_4);

        timerLabel = view.findViewById(R.id.timer_label);
        resendLabel = view.findViewById(R.id.resend_label);

        userPresenter = new UserPresenter(getContext(), this);

        if (PhoneRegFragment.regType == 0) {
            headline.setText(getString(R.string.app_register));
        } else if (PhoneRegFragment.regType == 1) {
            headline.setText(getString(R.string.app_reset_password));
        } else {
            headline.setText(getString(R.string.app_reset_phone));
        }
    }

    private void setEvent() {
        resendLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptResendCode();

                Toast.makeText(getActivity().getApplicationContext(), "Kode verifikasi telah dikirimkan", Toast.LENGTH_SHORT).show();
                setTimer();
            }
        });

        codeField1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() > 0) {
                    codeField2.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        codeField2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() > 0) {
                    codeField3.requestFocus();
                } else {
                    codeField1.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        codeField3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() > 0) {
                    codeField4.requestFocus();
                } else {
                    codeField2.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        codeField4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() > 0) {
                    validate();
                } else {
                    codeField3.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    private void setTimer() {
        timerLabel.setVisibility(View.VISIBLE);

        resendLabel.setAlpha(0.5f);
        resendLabel.setEnabled(false);
        resendLabel.setTextColor(getResources().getColor(R.color.colorDisabled));

        timer = new CountDownTimer(60500, 1000) {
            int secondsLeft = 60;

            @Override
            public void onTick(long l) {
                secondsLeft--;
                if (secondsLeft < 10) {
                    timerLabel.setText("00:0" + secondsLeft);
                } else {
                    timerLabel.setText("00:" + secondsLeft);
                }
                isTimerRunning = true;
            }

            @Override
            public void onFinish() {
                resetTimer();
            }
        }
                .start();
    }

    public void resetTimer() {
        timerLabel.setVisibility(View.GONE);

        resendLabel.setAlpha(1f);
        resendLabel.setEnabled(true);
        resendLabel.setTextColor(getResources().getColor(R.color.colorPrimary));

        isTimerRunning = false;
    }

    public void validate() {
        code = codeField1.getText().toString().trim() +
                codeField2.getText().toString().trim() +
                codeField3.getText().toString().trim() +
                codeField4.getText().toString().trim();
        if (code.length() == 0 || code.length() < 4) {
            showErrorDialog(getString(R.string.code_ver_error_1));
        } else {
            attemptReg();
        }
    }

    private void showErrorDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Kesalahan")
                .setMessage(message)
                .setPositiveButton("Tutup", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .show();
    }

    private void attemptReg() {
        progressDialog = new ProgressDialogCustom(getContext());
        progressDialog.setMessage("Verifikasi...");

        if (Internet.isConnected(getContext())) {
            phone = PhoneRegFragment.phone;
            regType = PhoneRegFragment.regType;

            if (regType == 0 || regType == 1) {
                userPresenter.verifyCode(phone, code, regType);
            } else {
                userPresenter.resetPhone(phone, code);
            }
        } else {
            progressDialog.dismiss();

            if (getActivity() instanceof RegistrationActivity) {
                ((RegistrationActivity) getActivity()).notifyNoInternet();
            } else if (getActivity() instanceof PasswordResActivity) {
                ((PasswordResActivity) getActivity()).notifyNoInternet();
            } else {
                ((PhoneResActivity) getActivity()).notifyNoInternet();
            }
        }
    }

    private void attemptResendCode() {
        phone = PhoneRegFragment.phone;
        regType = PhoneRegFragment.regType;

        userPresenter.resendCode(phone, regType);
    }

    public void clearCodeField() {
        codeField1.getText().clear();
        codeField2.getText().clear();
        codeField3.getText().clear();
        codeField4.getText().clear();
    }

    private void loadError(int error) {
        if (error == 0) {
            showErrorDialog(getString(R.string.code_ver_error_2));
        }
    }

    @Override
    public void onSuccess(BaseResponse base) {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }

        if (base.getTag().equals(Constant.REQ_RESEND_CODE)) {
            RegistrationResponse r = (RegistrationResponse) base;
        } else {
            if (base.isSuccess()) {
                if (regType == 0) {
                    ((RegistrationActivity) getActivity()).viewPager.setCurrentItem(2);

                    ((RegistrationActivity) getActivity()).next.setText("DAFTAR");
                } else if (regType == 1) {
                    ((PasswordResActivity) getActivity()).viewPager.setCurrentItem(2);

                    ((PasswordResActivity) getActivity()).next.setText("RESET");
                } else {
                    UserResponse r = (UserResponse) base;
                    Session.with(getContext()).saveUser(r.getData().getUser());
                    Session.with(getContext()).saveToken(r.getData().getToken());

                    ((PhoneResActivity) getActivity()).phoneReset(phone);
                }

                if (isTimerRunning) {
                    timer.cancel();
                    resetTimer();
                }
                clearCodeField();
            } else {
                loadError(base.getError());
            }
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