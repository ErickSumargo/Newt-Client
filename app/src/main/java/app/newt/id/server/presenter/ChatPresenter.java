package app.newt.id.server.presenter;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import app.newt.id.R;
import app.newt.id.helper.AES;
import app.newt.id.helper.API;
import app.newt.id.helper.Constant;
import app.newt.id.helper.Session;
import app.newt.id.helper.Utils;
import app.newt.id.server.interfaces.Response;
import app.newt.id.server.model.Chat;
import app.newt.id.server.response.BaseResponse;
import app.newt.id.server.response.ChatResponse;
import app.newt.id.server.response.DialogsResponse;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

public class ChatPresenter {
    private Context context;
    private Response response;

    public CompositeDisposable disposables;

    public ChatPresenter(Context context, Response response) {
        this.context = context;
        this.response = response;
        disposables = new CompositeDisposable();
    }

    public void add(Chat chat, int sent) {
        Map<String, RequestBody> data = new HashMap<>();
        data.put(Constant.REQ_SENDER_CODE, Utils.with(context).convertToRequestBody(chat.getSenderCode()));
        data.put(Constant.REQ_RECEIVER_CODE, Utils.with(context).convertToRequestBody(chat.getReceiverCode()));
        data.put(Constant.REQ_CONTENT_TYPE, Utils.with(context).convertToRequestBody(String.valueOf(chat.getContentType())));
        data.put(Constant.REQ_LESSON_ID, Utils.with(context).convertToRequestBody(String.valueOf(chat.getLesson_id())));
        data.put(Constant.REQ_SENT, Utils.with(context).convertToRequestBody(String.valueOf(sent)));

        if(chat.getContentType() == 0){
            data.put(Constant.REQ_CONTENT, Utils.with(context).convertToRequestBody(chat.getContent()));
        }
        else if (chat.getContentType() == 1) {
            String fileName = AES.decrypt(chat.getContent());
            if (fileName.contains("local")) {
                File file = Utils.with(context).getFile(fileName, Constant.DIR_PICTURES_INTERNAL, false);
                data.put(Constant.REQ_IMAGE + "." + Utils.with(context).getFileExtension(Uri.fromFile(file)) + "\"", Utils.with(context).convertToRequestBody(file));

                String[] parts = fileName.split("-");
                data.put(Constant.REQ_CONTENT, Utils.with(context).convertToRequestBody(AES.encrypt(parts[1] + "-" + parts[2])));
            }
        } else if (chat.getContentType() == 2) {
            String fileName = AES.decrypt(chat.getContent());
            if (fileName.contains("local")) {
                File file = Utils.with(context).getFile(fileName, Constant.DIR_DOCUMENTS_INTERNAL, false);

                String fName;
                String[] parts = fileName.split("-");
                if (parts.length == 3) {
                    fName = "\"" + parts[2] + "\"";
                } else {
                    fName = "\"";
                    for (int i = 2; i < parts.length; i++) {
                        fName += parts[i];
                        if (i != parts.length - 1) {
                            fName += "-";
                        }
                    }
                    fName += "\"";
                }
                data.put(Constant.REQ_DOCUMENT + fName, Utils.with(context).convertToRequestBody(file));
            }
        }

        API.with(context).getRest().addChat(data)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<retrofit2.Response<ChatResponse>>() {
                               @Override
                               public void onSubscribe(Disposable d) {
                                   disposables.add(d);
                               }

                               @Override
                               public void onSuccess(retrofit2.Response<ChatResponse> value) {
                                   if (value.code() == 200) {
                                       response.onSuccess(value.body());
                                   } else {
                                       response.onFailure(value.message());
                                   }
                               }

                               @Override
                               public void onError(Throwable e) {
                                   response.onFailure(e.getMessage());
                               }
                           }
                );
    }

    public void loadQueue(){
        API.with(context).getRest().loadQueue()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<retrofit2.Response<DialogsResponse>>() {
                               @Override
                               public void onSubscribe(Disposable d) {
                                   disposables.add(d);
                               }

                               @Override
                               public void onSuccess(retrofit2.Response<DialogsResponse> value) {
                                   if (value.code() == 200) {
                                       response.onSuccess(value.body());
                                   } else {
                                       response.onFailure(value.message());
                                   }
                               }

                               @Override
                               public void onError(Throwable e) {
                                   response.onFailure(e.getMessage());
                               }
                           }
                );
    }

    public void markQueue(){
        API.with(context).getRest().markQueue()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<retrofit2.Response<BaseResponse>>() {
                               @Override
                               public void onSubscribe(Disposable d) {
                                   disposables.add(d);
                               }

                               @Override
                               public void onSuccess(retrofit2.Response<BaseResponse> value) {
                                   if (value.code() == 200) {
                                       response.onSuccess(value.body());
                                   } else {
                                       response.onFailure(value.message());
                                   }
                               }

                               @Override
                               public void onError(Throwable e) {
                                   response.onFailure(e.getMessage());
                               }
                           }
                );
    }

    public void downloadDocument(final String fileName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.setView(dialog.getLayoutInflater().inflate(R.layout.custom_downloading_view, null));
        dialog.show();

        TextView fName = dialog.findViewById(R.id.file_name);
        final ProgressBar progress = dialog.findViewById(R.id.progress);
        final TextView percentage = dialog.findViewById(R.id.percentage);

        progress.setMax(100);
        progress.getProgressDrawable().setColorFilter(Utils.colorsHex[Session.with(context).getCurrentTheme()], android.graphics.PorterDuff.Mode.SRC_IN);

        percentage.setText("0%");

        String[] parts = fileName.split("-");
        String name = "";
        for (int i = 1; i < parts.length; i++) {
            name += parts[i];
            if (i != parts.length - 1) {
                name += "-";
            }
        }
        fName.setText(name);

        String url = Constant.URL_MEDIA_DOCUMENT + "chat/" + fileName;
        API.with(context).getRest().downloadDocument(url)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<retrofit2.Response<ResponseBody>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(retrofit2.Response<ResponseBody> value) {
                        if (value.isSuccessful()) {
                            new DownloadFile(dialog, progress, percentage, fileName, value.body()).execute();
                        } else {
                            dialog.dismiss();
                            response.onFailure(value.message());
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        dialog.dismiss();
                        response.onFailure(e.getMessage());
                    }
                });
    }

    class DownloadFile extends AsyncTask<Void, Integer, Void> {
        private AlertDialog dialog;
        private ProgressBar progress;
        private TextView percentage;

        private ResponseBody body;
        private String fName;
        private long fileSize;

        private DownloadFile(AlertDialog dialog, ProgressBar progress, TextView percentage, String fName, ResponseBody body) {
            this.dialog = dialog;
            this.progress = progress;
            this.percentage = percentage;
            this.fName = fName;
            this.body = body;
        }

        @Override
        protected void onPreExecute() {
            fileSize = body.contentLength();
        }

        @Override
        protected Void doInBackground(Void... params) {
            File file = Utils.with(context).getFile(fName, Constant.DIR_DOCUMENTS_INTERNAL, false);
            try {
                FileOutputStream out = new FileOutputStream(file);
                InputStream in = body.byteStream();

                byte[] fileReader = new byte[8192];
                long fileSizeDownloaded = 0;

                while (true) {
                    int read = in.read(fileReader);
                    if (read == -1) {
                        break;
                    }
                    out.write(fileReader, 0, read);

                    fileSizeDownloaded += read;
                    publishProgress((int) ((double) fileSizeDownloaded / fileSize * 100));
                }
                out.flush();
                dialog.dismiss();
            } catch (IOException e) {
                dialog.dismiss();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            progress.setProgress(values[0]);
            percentage.setText(values[0] + "%");
        }

        @Override
        protected void onPostExecute(Void result) {
            response.onSuccess(new BaseResponse());
        }
    }
}