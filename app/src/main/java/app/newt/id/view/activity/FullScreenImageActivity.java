package app.newt.id.view.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.github.ybq.android.spinkit.SpinKitView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;

import app.newt.id.R;
import app.newt.id.helper.Constant;
import app.newt.id.helper.Session;
import app.newt.id.helper.Utils;
import app.newt.id.view.custom.ImageViewCustom;

public class FullScreenImageActivity extends AppCompatActivity {
    private RelativeLayout mainLayout;
    private ImageViewCustom image;
    private ImageView share;

    private SpinKitView attachingView;

    private Uri uri;
    private boolean uriGenerated;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image);

        initView();
        loadImage();
        setEvent();
    }

    private void initView() {
        mainLayout = findViewById(R.id.main_layout);

        image = findViewById(R.id.image);
        image.setZoom(0.99f);

        share = findViewById(R.id.share);
        attachingView = findViewById(R.id.attaching_view);
    }

    private void loadImage() {
        Bundle extras = getIntent().getExtras();
        String fileName = extras.getString("image");
        String category = extras.getString("category");

        if (!fileName.isEmpty()) {
            if (fileName.contains("local")) {
                mainLayout.setVisibility(View.VISIBLE);

                File imageFile = Utils.with(this).getFile(fileName, Constant.DIR_PICTURES_INTERNAL, false);
                image.setImageURI(Uri.fromFile(imageFile));
            } else {
                attachingView.setVisibility(View.VISIBLE);

                Picasso.with(this)
                        .load(Utils.with(this).getURLMediaImage(extras.getString("image"), category))
                        .into(image, new Callback() {
                            @Override
                            public void onSuccess() {
                                mainLayout.setVisibility(View.VISIBLE);
                                attachingView.setVisibility(View.GONE);
                            }

                            @Override
                            public void onError() {

                            }
                        });
            }
            uriGenerated = false;
        } else {
            mainLayout.setVisibility(View.VISIBLE);
        }
    }

    private void setEvent() {
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!uriGenerated) {
                    uri = getUri();
                }
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_STREAM, uri);

                startActivity(Intent.createChooser(intent, "Share Image"));
            }
        });
    }

    private Uri getUri() {
        Bitmap bitmap = ((BitmapDrawable) image.getDrawable()).getBitmap();
        String fileName = System.currentTimeMillis() + ".jpg";

        Utils.with(this).saveImage(bitmap, fileName, Constant.DIR_SHARE_EXTERNAL, true);
        File file = Utils.with(this).getFile(fileName, Constant.DIR_SHARE_EXTERNAL, true);

        uri = Uri.fromFile(file);
        uriGenerated = true;

        return uri;
    }
}