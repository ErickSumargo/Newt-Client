package app.newt.id.view.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.romainpiel.shimmer.Shimmer;
import com.romainpiel.shimmer.ShimmerTextView;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.squareup.picasso.Picasso;

import java.util.List;

import app.newt.id.BuildConfig;
import app.newt.id.Newt;
import app.newt.id.R;
import app.newt.id.helper.Constant;
import app.newt.id.helper.Internet;
import app.newt.id.helper.Session;
import app.newt.id.helper.Utils;
import app.newt.id.server.interfaces.Response;
import app.newt.id.server.model.Lesson;
import app.newt.id.server.model.Tips;
import app.newt.id.server.model.User;
import app.newt.id.server.presenter.BasePresenter;
import app.newt.id.server.presenter.ChallengePresenter;
import app.newt.id.server.response.BaseResponse;
import app.newt.id.server.response.FeatureResponse;
import app.newt.id.server.response.UpdateResponse;
import app.newt.id.view.custom.MathView;
import app.newt.id.view.custom.ProgressDialogCustom;
import app.newt.id.view.interfaces.ProfilePhotoUpdated;
import de.hdodenhof.circleimageview.CircleImageView;
import io.realm.Realm;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class HomeActivity extends AppCompatActivity implements Response, ProfilePhotoUpdated {
    private AppBarLayout appBar;
    private CircleImageView profile;

    private SlidingUpPanelLayout slidingUpLayout;
    private CoordinatorLayout parentLayout;
    private LinearLayout challengeLogicCont, challengeMathematicsCont;

    private CardView chattingContainer;
    private ImageView mathematics, physics, chemistry;

    private CardView videoTutoringContainer;
    private ShimmerTextView betaLabel;

    private ShimmerTextView tipsLabel;
    private ScrollView tipsView;
    private MathView tips;

    private TextView logicUnsolved, mathematicsUnsolved;
    private TextView[] unsolveds;

    private LinearLayout loadingLayout;
    private ProgressDialogCustom progressDialog;

    private Realm realm;
    private User user;
    private int lessonId;
    private boolean updatingApp = false, registeringChallenger = false, loadingFeatures = false;

    private BasePresenter basePresenter;
    private ChallengePresenter challengePresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        realm = ((Newt) getApplication()).realm;

        init();
        updateApp();
        setEvent();
    }

    private void init() {
        appBar = findViewById(R.id.app_bar);
        appBar.setBackgroundResource(R.color.white);

        slidingUpLayout = findViewById(R.id.sliding_layout);
        parentLayout = findViewById(R.id.parent_layout);

        challengeLogicCont = findViewById(R.id.challenge_logic_container);
        challengeMathematicsCont = findViewById(R.id.challenge_mathematics_container);

        chattingContainer = findViewById(R.id.chatting_container);
        mathematics = findViewById(R.id.mathematics);
        physics = findViewById(R.id.physics);
        chemistry = findViewById(R.id.chemistry);

//        videoTutoringContainer = findViewById(R.id.video_tutoring_container);
//        betaLabel = findViewById(R.id.beta_label);

        mathematics.setColorFilter(getResources().getColor(R.color.mathematics));
        physics.setColorFilter(getResources().getColor(R.color.physics));
        chemistry.setColorFilter(getResources().getColor(R.color.chemistry));

        profile = findViewById(R.id.profile);

        tipsLabel = findViewById(R.id.tips_label);
        tipsView = findViewById(R.id.tips_view);
        tips = findViewById(R.id.tips);

        logicUnsolved = findViewById(R.id.logic_unsolved);
        mathematicsUnsolved = findViewById(R.id.mathematics_unsolved);
        unsolveds = new TextView[]{logicUnsolved, mathematicsUnsolved};

        Shimmer shimmer = new Shimmer();
        shimmer.setDuration(1500);
//        shimmer.start(betaLabel);
        shimmer.start(tipsLabel);

        loadingLayout = findViewById(R.id.loading_layout);

        tips.addJavascriptInterface(new JavascriptInterface(this), "Android");

        user = Session.with(this).getUser();
        Picasso.with(this).load(Utils.with(this).getURLMediaImage(user.getPhoto(), user.getType()))
                .placeholder(R.drawable.avatar)
                .fit()
                .centerCrop()
                .into(profile);

        ProfileActivity profAct = new ProfileActivity();
        profAct.setOnProfilePhotoUpdatedListener(this);

        basePresenter = new BasePresenter(getApplicationContext(), this);

        challengePresenter = new ChallengePresenter(getApplicationContext(), this);
    }

    private void setEvent() {
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });

        challengeLogicCont.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (user.getType().equals(Constant.STUDENT)) {
                    if (user.isChallenger()) {
                        lessonId = 1;
                        intentToChallenge();
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                        builder.setTitle("Konfirmasi")
                                .setMessage("Daftar sebagai Challenger?")
                                .setNegativeButton("Tutup", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                })
                                .setPositiveButton("Daftar", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        lessonId = 1;
                                        attemptRegister();
                                    }
                                })
                                .show();
                    }
                } else {
                    Intent intent = new Intent(HomeActivity.this, ChallengeActivity.class);
                    intent.putExtra("lesson_id", 1);
                    startActivity(intent);
                }
            }
        });

        challengeMathematicsCont.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (user.getType().equals(Constant.STUDENT)) {
                    if (user.isChallenger()) {
                        lessonId = 2;
                        intentToChallenge();
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                        builder.setTitle("Konfirmasi")
                                .setMessage("Daftar sebagai Challenger?")
                                .setNegativeButton("Tutup", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                })
                                .setPositiveButton("Daftar", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        lessonId = 2;
                                        attemptRegister();
                                    }
                                })
                                .show();
                    }
                } else {
                    Intent intent = new Intent(HomeActivity.this, ChallengeActivity.class);
                    intent.putExtra("lesson_id", 2);
                    startActivity(intent);
                }
            }
        });

        chattingContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Session.with(getApplicationContext()).saveChatSelected(true);
                if (!Session.with(getApplicationContext()).isBaseDataLoaded()) {
                    Session.with(getApplicationContext()).startServices();
                }

                Intent intent = new Intent(HomeActivity.this, DialogActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
    }

    private void updateApp() {
        if (Session.with(this).getVersionCode() == 0) {
            Session.with(this).saveVersionCode(BuildConfig.VERSION_CODE);

            loadingFeatures = true;
            basePresenter.loadFeatures();
        } else if (BuildConfig.VERSION_CODE > Session.with(this).getVersionCode()) {
            if (Internet.isConnected(this)) {
                progressDialog = new ProgressDialogCustom(this);
                progressDialog.setMessage("Memperbarui versi " + BuildConfig.VERSION_NAME + "...");

                updatingApp = true;
                basePresenter.updateApp();
            } else {
                showRetryUpdateAppDialog();
            }
        } else {
            loadingFeatures = true;
            basePresenter.loadFeatures();
        }
    }

    private void showRetryUpdateAppDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pembaruan Versi")
                .setMessage(getString(R.string.no_internet))
                .setPositiveButton("Coba Lagi", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        updateApp();
                    }
                })
                .setCancelable(false)
                .show();
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

    private void attemptRegister() {
        progressDialog = new ProgressDialogCustom(this);
        progressDialog.setMessage("Mendaftar...");

        if (Internet.isConnected(this)) {
            registeringChallenger = true;
            challengePresenter.registerChallenger();
        } else {
            progressDialog.dismiss();

            notifyNoInternet();
        }
    }

    @Override
    public void onPhotoUpdated(Drawable d) {
        user = Session.with(this).getUser();

        profile.setImageDrawable(d);
        Snackbar.make(parentLayout, R.string.profile_updated, Snackbar.LENGTH_SHORT).show();
    }

    private void notifyNoInternet() {
        Snackbar.make(parentLayout, R.string.no_internet, Snackbar.LENGTH_SHORT).show();
    }

    private void intentToChallenge() {
        Intent intent = new Intent(getApplicationContext(), ChallengeActivity.class);
        intent.putExtra("lesson_id", lessonId);
        startActivityForResult(intent, 0);
    }

    private void loadData(int versionCode, List<Integer> unsolved, Tips t) {
        if (versionCode > BuildConfig.VERSION_CODE) {
            promptUpdateDialog();
        }

        loadingLayout.setVisibility(View.GONE);
        if (t != null) {
            tips.setDisplayText(t.getContent());
        }
        tipsView.setVisibility(View.VISIBLE);

        for (int i = 0; i < unsolved.size(); i++) {
            if (unsolved.get(i) == 0) {
                unsolveds[i].setVisibility(View.GONE);
            } else {
                unsolveds[i].setText(String.valueOf(unsolved.get(i)));
                unsolveds[i].setVisibility(View.VISIBLE);
            }
        }
    }

    private void promptUpdateDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setNegativeButton("Tutup", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("market://details?id=app.newt.id"));
                startActivity(intent);
            }
        });
        AlertDialog dialog = builder.create();
        dialog.setView(dialog.getLayoutInflater().inflate(R.layout.custom_prompt_update_dialog, null));
        dialog.show();
    }

    private void loadError(int error) {
        if (registeringChallenger && error == 0) {
            showErrorDialog(getString(R.string.challenge_reg_error_1));
        }
    }

    public class JavascriptInterface {
        private Context context;

        JavascriptInterface(Context context) {
            this.context = context;
        }

        @android.webkit.JavascriptInterface
        public void onImageClicked(String filePath) {
            String[] parts = filePath.split("/");

            Intent intent = new Intent(context, FullScreenImageActivity.class);
            intent.putExtra("image", parts[parts.length - 1]);

            String category = "";
            for (int i = 0; i < parts.length - 1; i++) {
                category += parts[i];
                if (i < parts.length - 2) {
                    category += "/";
                }
            }
            intent.putExtra("category", category);
            startActivity(intent);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == RESULT_OK) {
            boolean ended = data.getBooleanExtra("ended", false);
            if (ended) {
                loadingFeatures = true;
                basePresenter.loadFeatures();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (slidingUpLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
            slidingUpLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        } else {
            finish();
        }
    }

    @Override
    public void onSuccess(BaseResponse base) {
        if (updatingApp) {
            updatingApp = false;

            UpdateResponse r = (UpdateResponse) base;
            final List<Lesson> lessons = r.getData().getLessons();

            realm.executeTransactionAsync(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    realm.copyToRealmOrUpdate(lessons);
                }
            }, new Realm.Transaction.OnSuccess() {
                @Override
                public void onSuccess() {
                    Session.with(HomeActivity.this).saveVersionCode(BuildConfig.VERSION_CODE);
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                    }

                    loadingFeatures = true;
                    basePresenter.loadFeatures();
                }
            });
        } else if (registeringChallenger) {
            registeringChallenger = false;
            if (base.isSuccess()) {
                realm.executeTransactionAsync(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        user.setChallenger(true);
                        Session.with(HomeActivity.this).saveUser(user);
                    }
                }, new Realm.Transaction.OnSuccess() {
                    @Override
                    public void onSuccess() {
                        if (progressDialog != null) {
                            progressDialog.dismiss();
                        }
                        intentToChallenge();
                    }
                });
            } else {
                if (progressDialog != null) {
                    progressDialog.dismiss();
                }
                loadError(base.getError());
            }
        } else if (loadingFeatures) {
            loadingFeatures = false;

            FeatureResponse r = (FeatureResponse) base;
            loadData(r.getData().getVersionCode(), r.getData().getUnsolved(), r.getData().getTips());
        }
    }

    @Override
    public void onFailure(String message) {
        if (updatingApp) {
            if (Internet.isConnected(this)) {
                basePresenter.updateApp();
            } else {
                if (progressDialog != null) {
                    progressDialog.dismiss();
                }
                showRetryUpdateAppDialog();
            }
        } else if (registeringChallenger) {
            registeringChallenger = false;

            if (progressDialog != null) {
                progressDialog.dismiss();
            }
            Toast.makeText(getApplicationContext(), getString(R.string.connection_error_try), Toast.LENGTH_SHORT).show();
        } else if (loadingFeatures) {
            if (Internet.isConnected(this)) {
                basePresenter.loadFeatures();
            }
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}