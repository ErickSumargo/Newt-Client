package app.newt.id.view.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

import app.newt.id.BuildConfig;
import app.newt.id.Newt;
import app.newt.id.R;
import app.newt.id.helper.Constant;
import app.newt.id.helper.Internet;
import app.newt.id.helper.Session;
import app.newt.id.helper.Utils;
import app.newt.id.server.interfaces.Response;
import app.newt.id.server.model.User;
import app.newt.id.server.presenter.UserPresenter;
import app.newt.id.server.response.BaseResponse;
import app.newt.id.server.response.RatingResponse;
import app.newt.id.server.response.UserResponse;
import app.newt.id.view.custom.ProgressDialogCustom;
import app.newt.id.view.custom.imagepicker.PickerBuilder;
import app.newt.id.view.interfaces.ProfilePhotoUpdated;
import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;
import io.realm.Realm;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ProfileActivity extends AppCompatActivity implements Response {
    private Toolbar toolbar;

    private LinearLayout ratingContainer;
    private RelativeLayout subscriptionContainer;
    private ProgressBar progressBar;
    private TextView rating;

    private CoordinatorLayout parentLayout;
    private FloatingActionButton ask;

    private CircleImageView photo;
    private ImageView photoRemoval, gallery, camera, editPhone, renew;
    private TextInputLayout schoolFieldLayout;
    private EditText promoCodeField, nameField, phoneField, schoolField;
    private TextView promoNote, infoSubscribe, update;
    private Button apply;

    private LinearLayout promoCodeCont;

    private SwitchCompat notification, reply;
    private LinearLayout logout;
    private TextView version;

    private ProgressDialogCustom progressDialog;

    private static ProfilePhotoUpdated mProfilePhotoUpdated;

    private User user;
    private UserPresenter userPresenter;

    private String promoCode, name, school;
    private boolean photoExisted;
    private Uri uri;
    private int lesson, teacherId;
    private boolean applyingPromoCode = false, updatingProfile = false, fetchingRating = false;

    private String dateFormat = "dd MMMM yyyy, HH:mm";

    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        realm = ((Newt) getApplication()).realm;

        initView();
        loadData();
        setEvent();
    }

    private void initView() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ratingContainer = findViewById(R.id.rating_container);
        subscriptionContainer = findViewById(R.id.subscription_container);
        progressBar = findViewById(R.id.progress_bar);
        rating = findViewById(R.id.rating);

        parentLayout = findViewById(R.id.parent_layout);
        ask = findViewById(R.id.ask);

        photo = findViewById(R.id.photo);
        photoRemoval = findViewById(R.id.photo_removal);
        gallery = findViewById(R.id.gallery);
        camera = findViewById(R.id.camera);
        editPhone = findViewById(R.id.edit_phone);
        renew = findViewById(R.id.renew);

        promoCodeCont = findViewById(R.id.promo_code_container);

        promoCodeField = findViewById(R.id.promo_code_field);
        nameField = findViewById(R.id.name_field);
        phoneField = findViewById(R.id.phone_field);
        schoolFieldLayout = findViewById(R.id.school_field_layout);
        schoolField = findViewById(R.id.school_field);

        promoNote = findViewById(R.id.promo_note);
        infoSubscribe = findViewById(R.id.info_subscribe);
        renew = findViewById(R.id.renew);
        update = findViewById(R.id.update);
        apply = findViewById(R.id.apply);

        notification = findViewById(R.id.notification_switch);
        reply = findViewById(R.id.reply_switch);
        logout = findViewById(R.id.logout);

        version = findViewById(R.id.version);

        promoNote.setText(Html.fromHtml(getResources().getString(R.string.promo_note_2)));
        renew.startAnimation(addRotateAnimation());

        if (Build.VERSION.SDK_INT < 21) {
            Drawable pbBg = progressBar.getIndeterminateDrawable();
            pbBg = pbBg.mutate();
            pbBg.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_IN);
        }
        update.setTextColor(getResources().getColor(R.color.colorPrimary));
        renew.setColorFilter(getResources().getColor(R.color.colorPrimary));

        userPresenter = new UserPresenter(getApplicationContext(), this);
    }

    private void loadData() {
        user = Session.with(this).getUser();
        if (!user.getPhoto().isEmpty()) {
            Picasso.with(this).load(Utils.with(this).getURLMediaImage(user.getPhoto(), user.getType()))
                    .placeholder(R.drawable.avatar)
                    .fit()
                    .centerCrop()
                    .into(photo);
            photoExisted = true;
        } else {
            uri = null;
            photoExisted = false;
        }

        nameField.setText(user.getName());
        nameField.setSelection(nameField.getText().length());

        phoneField.setText(user.getPhone());

        notification.setChecked(Session.with(this).isNotificationEnabled());
        reply.setChecked(Session.with(this).isReplySoundEnabled());
        version.setText(BuildConfig.VERSION_NAME);

        if (Session.with(this).getUserType().equals(Constant.TEACHER)) {
            progressBar.setVisibility(View.VISIBLE);
            promoCodeCont.setVisibility(View.GONE);
            schoolFieldLayout.setVisibility(View.GONE);
            schoolField.setVisibility(View.GONE);
            subscriptionContainer.setVisibility(View.GONE);

            teacherId = user.getId();
            userPresenter.getAvgRating(teacherId);

            fetchingRating = true;
        } else {
            if (user.getSchool() != null) {
                schoolField.setText(user.getSchool());
            }

            if (!Utils.with(this).isExpired(user.getSubscription())) {
                infoSubscribe.setText(Utils.with(this).formatDate(user.getSubscription(), dateFormat) + " WIB");
            } else {
                infoSubscribe.setText("Habis");
                infoSubscribe.setTextColor(getResources().getColor(R.color.colorPrimary));
            }
        }
    }

    private void setEvent() {
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        ask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAskDialog();
            }
        });

        photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String fileName;
                if (photoExisted) {
                    if (uri != null) {
                        File file = new File(uri.getPath());
                        fileName = file.getName();
                    } else {
                        fileName = user.getPhoto();
                    }
                } else {
                    fileName = "";
                }
                Intent intent = new Intent(ProfileActivity.this, FullScreenImageActivity.class);
                intent.putExtra("image", fileName);
                intent.putExtra("category", user.getType());
                startActivity(intent);
            }
        });

        photoRemoval.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (photoExisted) {
                    photoExisted = false;
                    Toast.makeText(getApplicationContext(), "Foto terhapus", Toast.LENGTH_SHORT).show();
                }
                uri = null;
                photo.setImageResource(R.drawable.avatar);
            }
        });

        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PickerBuilder pickerBuilder = new PickerBuilder(ProfileActivity.this, PickerBuilder.SELECT_FROM_GALLERY);
                startImagePicker(pickerBuilder);
            }
        });

        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PickerBuilder pickerBuilder = new PickerBuilder(ProfileActivity.this, PickerBuilder.SELECT_FROM_CAMERA);
                startImagePicker(pickerBuilder);
            }
        });

        promoNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse("http://instagram.com/_u/newt.mobi");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                try {
                    intent.setPackage("com.instagram.android");
                    if (Utils.with(ProfileActivity.this).isIntentAvailable(getApplicationContext(), intent)) {
                        startActivity(intent);
                    }
                } catch (Exception e) {
                }
            }
        });

        apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validatePromoCode();
            }
        });

        editPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, PhoneResActivity.class);
                startActivityForResult(intent, 0);
            }
        });

        renew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfileActivity.this, PaymentActivity.class);
                startActivity(intent);
            }
        });

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validate();
            }
        });

        notification.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Utils.with(ProfileActivity.this).playDefSound();
                    Utils.with(ProfileActivity.this).vibrate();
                }
                Session.with(ProfileActivity.this).saveNotificationSetting(isChecked);
            }
        });

        reply.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Utils.with(ProfileActivity.this).playReplySound();
                }
                Session.with(ProfileActivity.this).saveReplySoundSetting(isChecked);
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
                builder.setTitle("Konfirmasi")
                        .setMessage("Keluar dari aplikasi?")
                        .setNegativeButton("Tutup", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .setPositiveButton("Keluar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Session.with(ProfileActivity.this).logout(0);

                                new Handler().post(new Runnable() {
                                    @Override
                                    public void run() {
                                        realm.close();
                                        try {
                                            Realm.deleteRealm(realm.getDefaultConfiguration());
                                        } catch (Exception e) {
                                        }

                                        Intent intent = new Intent();
                                        intent.setAction(Constant.REALM_RESTART);
                                        sendBroadcast(intent);
                                    }
                                });
                            }
                        })
                        .show();
            }
        });
    }

    private RotateAnimation addRotateAnimation() {
        RotateAnimation rotate = new RotateAnimation(0, 360,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(2000);
        rotate.setRepeatCount(Animation.INFINITE);
        rotate.setRepeatMode(Animation.INFINITE);
        rotate.setInterpolator(new LinearInterpolator());

        return rotate;
    }

    private void showAskDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final AlertDialog dialog = builder.create();
        dialog.setView(dialog.getLayoutInflater().inflate(R.layout.custom_ask_dialog, null));
        dialog.show();

        ImageView line = dialog.findViewById(R.id.line);
        ImageView whatsapp = dialog.findViewById(R.id.whatsapp);

        line.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                try {
                    intent.setData(Uri.parse("line://ti/p/~newt"));
                    startActivity(intent);
                } catch (Exception e) {
                }
                dialog.dismiss();
            }
        });

        whatsapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                String phone = "+6285371117293";
                try {
                    String url = "https://api.whatsapp.com/send?phone=" + phone + "&text=" + URLEncoder.encode("", "UTF-8");
                    intent.setPackage("com.whatsapp");
                    intent.setData(Uri.parse(url));
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                    }
                } catch (Exception e) {
                }
                dialog.dismiss();
            }
        });
    }

    private void validatePromoCode() {
        promoCode = promoCodeField.getText().toString().trim();
        if (promoCode.length() != 0) {
            attemptApply();
        }
    }

    private void validate() {
        name = nameField.getText().toString().trim();
        school = schoolField.getText().toString().trim();
        if (name.length() == 0) {
            showErrorDialog(getString(R.string.user_reg_error_1));
        } else if (!name.matches("^\\p{L}+[\\p{L}\\p{Z}\\p{P}]{0,}")) {
            showErrorDialog(getString(R.string.user_reg_error_2));
        } else if (school.length() == 0) {
            showErrorDialog(getString(R.string.user_error_5));
        } else {
            attemptUpdate();
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

    private void startImagePicker(PickerBuilder pickerBuilder) {
        final String fileName = "local-" + user.getCode() + "-" + System.currentTimeMillis();
        pickerBuilder.setOnImageReceivedListener(new PickerBuilder.onImageReceivedListener() {
            @Override
            public void onImageReceived(Uri imageUri) {
                Utils utils = Utils.with(ProfileActivity.this);

                File file = null;
                try {
                    file = new Compressor(ProfileActivity.this).compressToFile(new File(imageUri.getPath()));
                } catch (IOException e) {
                }

                if ((double) file.length() / 1000000 <= Constant.MAX_UPLOAD_FILE_SIZE) {
                    imageUri = Uri.fromFile(file);
                    Bitmap b = utils.adjustBitmap(utils.maintainBitmap(imageUri), imageUri);
                    utils.saveImage(b, fileName + ".jpg", Constant.DIR_PICTURES_INTERNAL, false);

                    uri = Uri.fromFile(utils.getFile(fileName + ".jpg", Constant.DIR_PICTURES_INTERNAL, false));
                    photo.setImageURI(uri);

                    photoExisted = true;
                } else {
                    Toast.makeText(getApplicationContext(), R.string.file_size_overflow, Toast.LENGTH_SHORT).show();
                }
            }
        })
                .setImageFolderName(getString(R.string.app_name) + File.separator + Constant.DIR_MEDIA_INTERNAL + File.separator + Constant.DIR_PICTURES_INTERNAL)
                .setImageName(fileName)
                .withTimeStamp(false)
                .setCropScreenColor(getResources().getColor(Utils.colorsPrimary[lesson]))
                .start();
    }

    private void attemptApply() {
        progressDialog = new ProgressDialogCustom(this);
        progressDialog.setMessage("Menerapkan kode promo...");

        if (Internet.isConnected(this)) {
            applyingPromoCode = true;
            userPresenter.applyPromoCode(promoCode);
        } else {
            progressDialog.dismiss();

            notifyNoInternet();
        }
    }

    private void attemptUpdate() {
        progressDialog = new ProgressDialogCustom(this);
        progressDialog.setMessage("Memperbarui...");

        if (Internet.isConnected(this)) {
            updatingProfile = true;
            if (uri != null) {
                Session.with(this).saveProfilePhotoChanged(true);

                userPresenter.updateProfile(name, school, uri.toString());
            } else if (!user.getPhoto().isEmpty() && !photoExisted) {
                Session.with(this).saveProfilePhotoChanged(true);

                userPresenter.updateProfile(name, school, "");
            } else {
                userPresenter.updateProfile(name, school, user.getPhoto());
            }
        } else {
            progressDialog.dismiss();

            notifyNoInternet();
        }
    }

    private void notifyNoInternet() {
        Snackbar.make(parentLayout, R.string.no_internet, Snackbar.LENGTH_SHORT).show();
    }

    public void setOnProfilePhotoUpdatedListener(Context context) {
        mProfilePhotoUpdated = (ProfilePhotoUpdated) context;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0 && resultCode == RESULT_OK) {
            String phone = data.getStringExtra("phone");
            phoneField.setText(phone);

            Snackbar.make(parentLayout, getString(R.string.phone_res_success), Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onSuccess(BaseResponse base) {
        if (applyingPromoCode) {
            if (progressDialog != null) {
                progressDialog.dismiss();
            }

            UserResponse r = (UserResponse) base;
            if (r.isSuccess()) {
                promoCodeField.getText().clear();
                Toast.makeText(getApplicationContext(), "Terima kasih kode promo anda berhasil di-apply", Toast.LENGTH_SHORT).show();
            } else {
                if (r.getError() == 0) {
                    showErrorDialog(getString(R.string.user_error_6));
                } else if (r.getError() == 1) {
                    showErrorDialog(getString(R.string.user_error_8));
                } else if (r.getError() == 2) {
                    showErrorDialog(getString(R.string.user_error_7));
                }
            }
            applyingPromoCode = false;
        } else if (updatingProfile) {
            UserResponse r = (UserResponse) base;

            Session.with(this).saveUser(r.getData().getUser());
            Session.with(this).saveToken(r.getData().getToken());
            Session.with(this).saveProfilePhotoChanged(false);

            if (mProfilePhotoUpdated != null) {
                mProfilePhotoUpdated.onPhotoUpdated(photo.getDrawable());
            }
            updatingProfile = false;
            finish();
        } else if (fetchingRating) {
            RatingResponse r = (RatingResponse) base;
            String avg = String.format("%,.2f", r.getData().getAvgRating());

            rating.setText(": " + avg + "/" + r.getData().getReviewers());
            ratingContainer.setVisibility(View.VISIBLE);

            progressBar.setVisibility(View.GONE);
            fetchingRating = false;
        }
    }

    @Override
    public void onFailure(String message) {
        if (applyingPromoCode) {
            if (progressDialog != null) {
                progressDialog.dismiss();
            }
            Toast.makeText(getApplicationContext(), getString(R.string.connection_error_try), Toast.LENGTH_SHORT).show();
        }
        if (updatingProfile) {
            if (progressDialog != null) {
                progressDialog.dismiss();
            }
            Toast.makeText(getApplicationContext(), getString(R.string.connection_error_try), Toast.LENGTH_SHORT).show();
        } else if (fetchingRating) {
            userPresenter.getAvgRating(teacherId);
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}