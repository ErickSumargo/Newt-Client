package app.newt.id.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import app.newt.id.R;
import app.newt.id.helper.Session;
import app.newt.id.helper.Utils;

public class FullScreenInputActivity extends AppCompatActivity {
    private Toolbar toolbar;

    private NestedScrollView mainLayout;
    private ImageView done;
    private EditText input;

    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int lesson = Session.with(this).getCurrentTheme();
        setTheme(Utils.styles[lesson]);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_input);

        initView();
        setEvent();
    }

    private void initView() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mainLayout = findViewById(R.id.main_layout);
        done = findViewById(R.id.done);
        input = findViewById(R.id.input);

        intent = getIntent();
        input.setText(intent.getStringExtra("content"));
        input.setSelection(input.getText().length());
    }

    private void setEvent() {
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        mainLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);

                return false;
            }
        });

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent.putExtra("content", input.getText().toString());
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }
}