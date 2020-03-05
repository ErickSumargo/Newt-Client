package app.newt.id.view.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.concurrent.ConcurrentSkipListMap;

import app.newt.id.R;
import app.newt.id.helper.AES;
import app.newt.id.helper.Constant;
import app.newt.id.helper.Internet;
import app.newt.id.helper.Session;
import app.newt.id.server.interfaces.Response;
import app.newt.id.server.presenter.UserPresenter;
import app.newt.id.server.response.BaseResponse;
import app.newt.id.server.response.UserResponse;
import app.newt.id.view.custom.ProgressDialogCustom;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class LoginActivity extends AppCompatActivity implements Response {
    private CoordinatorLayout parentLayout;
    private EditText phoneField, passwordField;
    private Button login;

    private TextView resetPassword, register;
    private ProgressDialogCustom progressDialog;

    private UserPresenter userPresenter;

    private String phone, password, device, firebase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initView();
        setEvent();
    }

    private void initView() {
        parentLayout = findViewById(R.id.parent_layout);

        phoneField = findViewById(R.id.phone_field);
        passwordField = findViewById(R.id.password_field);
        login = findViewById(R.id.login);

        resetPassword = findViewById(R.id.reset_password);
        register = findViewById(R.id.register);

        userPresenter = new UserPresenter(getApplicationContext(), this);

        if (Session.with(this).getPhone() != null) {
            phoneField.setText(Session.with(this).getPhone());
            passwordField.requestFocus();
        }
    }

    private void setEvent() {
        phoneField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    if (phoneField.length() == 0) {
                        phoneField.setHint("08xxxxxxxx");
                    }
                } else {
                    phoneField.setHint("No. HP");
                }
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validate();
            }
        });

        resetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, PasswordResActivity.class);
                startActivityForResult(intent, 0);
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegistrationActivity.class);
                startActivity(intent);
            }
        });
    }

    private void validate() {
        phone = phoneField.getText().toString().trim();
        password = passwordField.getText().toString().trim();

        if (phone.length() == 0) {
            showErrorDialog(getString(R.string.phone_reg_error_1));
        } else if (phone.charAt(0) != '0' || phone.length() < 10) {
            showErrorDialog(getString(R.string.phone_reg_error_2));
        } else if (password.length() == 0) {
            showErrorDialog(getString(R.string.user_error_3));
        } else if (password.length() < 6) {
            showErrorDialog(getString(R.string.user_error_4));
        } else {
            attemptLogin();
        }
    }

    private void showErrorDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Kesalahan")
                .setMessage(message)
                .setNegativeButton("Tutup", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .show();
    }

    private void attemptLogin() {
        progressDialog = new ProgressDialogCustom(this);
        progressDialog.setMessage("Masuk...");

        if (Internet.isConnected(this)) {
            device = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
            firebase = Session.with(this).getFirebaseToken();

            userPresenter.login(phone, AES.encrypt(password), device, firebase);
        } else {
            progressDialog.dismiss();

            notifyNoInternet();
        }
    }

    private void notifyNoInternet() {
        Snackbar.make(parentLayout, R.string.no_internet, Snackbar.LENGTH_SHORT).show();
    }

    private void loadError(int error) {
        if (error == 0) {
            showErrorDialog(getString(R.string.login_error_1));
        } else if (error == 1) {
            showErrorDialog(getString(R.string.login_error_2));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0 && resultCode == RESULT_OK) {
            Snackbar.make(parentLayout, getString(R.string.password_res_success), Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onSuccess(BaseResponse base) {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }

        UserResponse r = (UserResponse) base;
        if (r.isSuccess()) {
            Session.with(this).saveUser(r.getData().getUser());
            Session.with(this).saveToken(r.getData().getToken());

            Session.with(this).saveFirstLogin();
            Session.with(this).saveLogin();

            Intent intent = new Intent(this, HomeActivity.class);
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
        Toast.makeText(getApplicationContext(), getString(R.string.connection_error_try), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        userPresenter.disposables.clear();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}