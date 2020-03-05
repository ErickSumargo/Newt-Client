package app.newt.id.view.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.net.URLEncoder;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import app.newt.id.R;
import app.newt.id.helper.Constant;
import app.newt.id.helper.Internet;
import app.newt.id.helper.Session;
import app.newt.id.helper.Utils;
import app.newt.id.server.interfaces.Response;
import app.newt.id.server.model.Attempt;
import app.newt.id.server.model.Challenge;
import app.newt.id.server.model.User;
import app.newt.id.server.presenter.ChallengePresenter;
import app.newt.id.server.response.BaseResponse;
import app.newt.id.server.response.ChallengeResponse;
import app.newt.id.view.custom.MathView;
import app.newt.id.view.custom.ProgressDialogCustom;
import cn.pedant.SweetAlert.SweetAlertDialog;
import de.hdodenhof.circleimageview.CircleImageView;
import io.fabric.sdk.android.services.settings.SessionSettingsData;
import okhttp3.internal.Util;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class QuestionActivity extends AppCompatActivity implements Response {
    private Toolbar toolbar;
    private TextView point;

    private ProgressBar loadingView;
    private CoordinatorLayout parentLayout;
    private NestedScrollView contentView;

    private LinearLayout infoCont;
    private TextSwitcher info;

    private CircleImageView presenterPhoto;
    private TextView presenterName;
    private LinearLayout socialLinksCont;

    private MathView question;
    private RelativeLayout answerCont;
    private EditText answerField;
    private Button submit;
    private TextView attempt;

    private LinearLayout solutionCont;
    private MathView solution;

    private ProgressDialogCustom progressDialog;

    private Timer timer;
    private int counter = 0;

    private User user;
    private Challenge challenge;
    private List<String> infos;

    private int questionId;
    private int attemptValue;
    private String answer;
    private boolean loadingQuestion = true, submittingAnswer = false, ended = false;

    private ChallengePresenter challengePresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);

        initView();
        setEvent();
    }

    private void initView() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Materi");

        loadingView = findViewById(R.id.loading_view);
        parentLayout = findViewById(R.id.parent_layout);
        contentView = findViewById(R.id.content_view);

        point = findViewById(R.id.point);
        infoCont = findViewById(R.id.info_container);
        info = findViewById(R.id.info);

        presenterPhoto = findViewById(R.id.presenter_photo);
        presenterName = findViewById(R.id.presenter_name);
        socialLinksCont = findViewById(R.id.presenter_social_link_container);

        question = findViewById(R.id.question);
        answerCont = findViewById(R.id.answer_container);
        answerField = findViewById(R.id.answer_field);
        submit = findViewById(R.id.submit);
        attempt = findViewById(R.id.attempt);

        solutionCont = findViewById(R.id.solution_container);
        solution = findViewById(R.id.solution);

        info.setInAnimation(this, R.anim.slide_in_left);
        info.setOutAnimation(this, R.anim.slide_out_right);

        question.addJavascriptInterface(new JavascriptInterface(this), "Android");
        solution.addJavascriptInterface(new JavascriptInterface(this), "Android");

        user = Session.with(this).getUser();
        if (user.getType().equals(Constant.TEACHER)) {
            answerCont.setVisibility(View.GONE);
        }
        questionId = getIntent().getIntExtra("id", 1);

        challengePresenter = new ChallengePresenter(getApplicationContext(), this);
        challengePresenter.loadQuestionDetail(questionId);
    }

    private void setEvent() {
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validate();
            }
        });
    }

    private void showCorrectDialog() {
        new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText("Benar!")
                .setConfirmText("Tutup")
                .show();
    }

    private void showIncorrectDialog(int attempt) {
        SweetAlertDialog dialog = new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                .setTitleText("Wrong Answer :(")
                .setConfirmText(attempt > 0 ? "Coba Lagi" : "Tutup");
        if (attempt == 0) {
            dialog.setContentText("Pembahasan akan ditampilkan pada periode berikutnya");
        }
        dialog.show();
    }

    private void loadData() {
        getSupportActionBar().setTitle(challenge.getQuestion().getMaterial());
        point.setText(challenge.getQuestion().getPoint() + " pts");

        Picasso.with(this).load(Utils.with(this).getURLMediaImage(challenge.getQuestion().getTeacher().getPhoto(), "teacher"))
                .placeholder(R.drawable.avatar)
                .fit()
                .centerCrop()
                .into(presenterPhoto);

        presenterName.setText(challenge.getQuestion().getTeacher().getName());
        if (!challenge.getQuestion().getTeacher().getSocialLinks().isEmpty()) {
            String socialLinks = challenge.getQuestion().getTeacher().getSocialLinks();
            String[] links = socialLinks.split(";");
            for (String link : links) {
                ImageView ic = new ImageView(this);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams((int) getResources().getDimension(R.dimen.social_link_radius), (int) getResources().getDimension(R.dimen.social_link_radius));
                lp.setMargins(0, 0, (int) getResources().getDimension(R.dimen.social_link_margin), 0);
                ic.setLayoutParams(lp);

                final String[] parts = link.split("=>");
                if (parts[0].equals(Constant.INSTAGRAM)) {
                    ic.setImageResource(R.drawable.instagram);
                    ic.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Uri uri = Uri.parse("http://instagram.com/_u/" + parts[1]);
                            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                            try {
                                intent.setPackage("com.instagram.android");
                                if (Utils.with(QuestionActivity.this).isIntentAvailable(getApplicationContext(), intent)) {
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(QuestionActivity.this, getResources().getString(R.string.no_instagram), Toast.LENGTH_SHORT).show();
                                }
                            } catch (Exception e) {
                            }
                        }
                    });
                } else if (parts[0].equals(Constant.WHATSAPP)) {
                    ic.setImageResource(R.drawable.whatsapp);
                    ic.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            try {
                                String url = "https://api.whatsapp.com/send?phone=" + parts[1] + "&text=" + URLEncoder.encode("", "UTF-8");
                                intent.setPackage("com.whatsapp");
                                intent.setData(Uri.parse(url));
                                if (intent.resolveActivity(getPackageManager()) != null) {
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(QuestionActivity.this, getResources().getString(R.string.no_whatsapp), Toast.LENGTH_SHORT).show();
                                }
                            } catch (Exception e) {
                            }
                        }
                    });
                } else if (parts[0].equals(Constant.LINE)) {
                    ic.setImageResource(R.drawable.line);
                    ic.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            try {
                                intent.setPackage("jp.naver.line.android");
                                if (intent.resolveActivity(getPackageManager()) != null) {
                                    intent.setData(Uri.parse("line://ti/p/~" + parts[1]));
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(QuestionActivity.this, getResources().getString(R.string.no_line), Toast.LENGTH_SHORT).show();
                                }
                            } catch (Exception e) {
                            }
                        }
                    });
                }
                socialLinksCont.addView(ic);
                socialLinksCont.setVisibility(View.VISIBLE);
            }
        }

        question.setDisplayText(challenge.getQuestion().getContent());

        if (challenge.getQuestion().getStatus() == 1) {
            if (challenge.getAttempt() > 0) {
                if (challenge.getStatus() == 1) {
                    answerCont.setVisibility(View.GONE);
                } else {
                    attempt.setText("Sisa " + challenge.getAttempt() + "x percobaan");
                }
            } else if (challenge.getAttempt() == -1) {
                attempt.setText("Sisa " + challenge.getQuestion().getAttempt() + "x percobaan");
            } else {
                answerCont.setVisibility(View.GONE);
            }
        } else {
            answerCont.setVisibility(View.GONE);

            solution.setDisplayText(challenge.getSolution().getContent());
            solutionCont.setVisibility(View.VISIBLE);
        }

        loadingView.setVisibility(View.GONE);
        contentView.setVisibility(View.VISIBLE);
    }

    private void validate() {
        answer = answerField.getText().toString().trim();
        if (answer.length() == 0) {
            showErrorDialog(getString(R.string.challenge_submit_answer_error_4));
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(QuestionActivity.this);
            builder.setTitle("Konfirmasi")
                    .setMessage(attemptValue > 1 ? Html.fromHtml(getResources().getString(R.string.submit_confirmation)) : Html.fromHtml(getResources().getString(R.string.submit_extra_confirmation)))
                    .setNegativeButton("Tutup", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .setPositiveButton("Kirim", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            attemptSubmit();
                        }
                    })
                    .show();
        }
    }

    private void attemptSubmit() {
        progressDialog = new ProgressDialogCustom(this);
        progressDialog.setMessage("Memeriksa...");

        if (Internet.isConnected(this)) {
            submittingAnswer = true;
            challengePresenter.submitAnswer(challenge.getQuestion().getId(), answer);
        } else {
            progressDialog.dismiss();

            notifyNoInternet();
        }
    }

    private void notifyNoInternet() {
        Snackbar.make(parentLayout, R.string.no_internet, Snackbar.LENGTH_SHORT).show();
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

    private void loadError(int error) {
        if (error == 0) {
            showErrorDialog(getString(R.string.challenge_submit_answer_error_1));
        } else if (error == 1) {
            showErrorDialog(getString(R.string.challenge_submit_answer_error_2));
        } else if (error == 2) {
            showErrorDialog(getString(R.string.challenge_submit_answer_error_3));
        }
    }

    @Override
    public void onSuccess(BaseResponse base) {
        if (loadingQuestion) {
            loadingQuestion = false;

            ChallengeResponse r = (ChallengeResponse) base;
            this.infos = r.getData().getInfos();
            if (infos.size() > 0) {
                infoCont.setVisibility(View.VISIBLE);
                startTimer();
            }

            challenge = r.getData().getChallenge();
            attemptValue = challenge.getAttempt();
            if (attemptValue == -1) {
                attemptValue = challenge.getQuestion().getAttempt();
            }
            loadData();
        } else if (submittingAnswer) {
            submittingAnswer = false;

            if (progressDialog != null) {
                progressDialog.dismiss();
            }
            ChallengeResponse r = (ChallengeResponse) base;
            if (r.isSuccess()) {
                answerField.getText().clear();

                Attempt challengeAttempt = r.getData().getChallengeAttempt();
                attemptValue = challengeAttempt.getAttempt();
                if (challengeAttempt.getStatus() == 1) {
                    Utils.with(this).playCorrectSound();
                    showCorrectDialog();

                    infos.clear();
                    infos.add("Jawaban Benar");
                    infos.add("Solusi lengkap akan ditampilkan pada periode berikutnya");
                    counter = 0;

                    infoCont.setVisibility(View.VISIBLE);
                    answerCont.setVisibility(View.GONE);

                    ended = true;
                } else {
                    showIncorrectDialog(challengeAttempt.getAttempt());
                    if (challengeAttempt.getAttempt() > 0) {
                        attempt.setText("Sisa " + challengeAttempt.getAttempt() + "x percobaan");
                    } else {
                        infos.clear();
                        infos.add("Jawaban Salah");
                        infos.add("Solusi lengkap akan ditampilkan pada periode berikutnya");
                        counter = 0;

                        infoCont.setVisibility(View.VISIBLE);
                        answerCont.setVisibility(View.GONE);

                        ended = true;
                    }
                }
            } else {
                loadError(base.getError());
            }
        }
    }

    private void startTimer() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (infos.get(counter).equals("Jawaban Benar") || infos.get(counter).equals("May the Newt be with You :)")) {
                            ((TextView) info.getNextView()).setTextColor(getResources().getColor(R.color.colorPrimary));
                        } else {
                            ((TextView) info.getNextView()).setTextColor(getResources().getColor(R.color.black));
                        }
                        info.setText(infos.get(counter));
                        if (counter == infos.size() - 1) {
                            counter = 0;
                        } else {
                            counter++;
                        }
                    }
                });
            }
        }, 0, 3000);
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
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("ended", ended);
        setResult(RESULT_OK, intent);

        finish();
    }

    @Override
    public void onFailure(String message) {
        if (loadingQuestion) {
            challengePresenter.loadQuestionDetail(questionId);
        } else if (submittingAnswer) {
            submittingAnswer = false;

            if (progressDialog != null) {
                progressDialog.dismiss();
            }
            Toast.makeText(getApplicationContext(), getString(R.string.connection_error_try), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}