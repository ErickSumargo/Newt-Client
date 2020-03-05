package app.newt.id.view.custom;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import app.newt.id.R;

/**
 * Created by Erick Sumargo on 11/25/2016.
 */

public class ProgressDialogCustom extends AppCompatActivity {
    private AlertDialog.Builder builder;
    private AlertDialog alertDialog;

    private TextView message;

    public ProgressDialogCustom(Context context) {
        builder = new AlertDialog.Builder(context);

        alertDialog = builder.create();
        alertDialog.setView(alertDialog.getLayoutInflater().inflate(R.layout.custom_progress_dialog, null));
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    public void setMessage(String msg) {
        message = alertDialog.findViewById(R.id.message);
        message.setText(msg);
    }

    public void dismiss() {
        alertDialog.dismiss();
    }
}