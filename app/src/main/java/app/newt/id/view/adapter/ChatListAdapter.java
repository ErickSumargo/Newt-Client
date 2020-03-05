package app.newt.id.view.adapter;

import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

import app.newt.id.BuildConfig;
import app.newt.id.R;
import app.newt.id.helper.AES;
import app.newt.id.helper.Constant;
import app.newt.id.helper.Internet;
import app.newt.id.helper.Utils;
import app.newt.id.server.interfaces.Response;
import app.newt.id.server.model.Chat;
import app.newt.id.server.model.User;
import app.newt.id.server.presenter.ChatPresenter;
import app.newt.id.server.response.BaseResponse;
import app.newt.id.view.activity.FullScreenImageActivity;
import app.newt.id.view.interfaces.LoadMore;
import io.realm.Realm;

/**
 * Created by Erick Sumargo on 8/31/2016.
 */
public class ChatListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Response {
    private Context context;

    private RecyclerView mainView;
    private LinearLayoutManager layoutManager;

    private User sender;
    private List<Chat> chats;

    private final int VIEW_TYPE_MESSAGE_DATE = 0;
    private final int VIEW_TYPE_MESSAGE_SENT = 1;
    private final int VIEW_TYPE_MESSAGE_RECEIVED = 2;

    private int lesson;
    private String timeFormat = "HH:mm";

    private int threshold = 2;
    public boolean isLoading = false, isAllLoaded = false;

    private static LoadMore mLoadMoreListener;

    public ChatListAdapter(RecyclerView mainView, User sender, List<Chat> chats, int lesson) {
        this.mainView = mainView;
        this.sender = sender;
        this.chats = chats;
        this.lesson = lesson;

        initView();
        setEvent();
    }

    private void initView() {
        layoutManager = (LinearLayoutManager) mainView.getLayoutManager();
    }

    private void setEvent() {
        mainView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int firstVisibleItem = layoutManager.findFirstVisibleItemPosition();
                if (!isLoading && !isAllLoaded && getItemCount() >= Constant.MAX_HISTORIES && firstVisibleItem <= threshold) {
                    isLoading = true;
                    if (mLoadMoreListener != null) {
                        mLoadMoreListener.onLoadMore();
                    }
                }
            }
        });
    }

    public void setOnLoadMoreListener(LoadMore mLoadMoreListener) {
        this.mLoadMoreListener = mLoadMoreListener;
    }

    private void refreshRemovalView(int position) {
        chats.remove(position);
        notifyItemRemoved(position);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        Chat chat = chats.get(position);
        if (chat.getSenderCode() == null && chat.getReceiverCode() == null) {
            return VIEW_TYPE_MESSAGE_DATE;
        } else if (chat.getSenderCode().equals(sender.getCode())) {
            return VIEW_TYPE_MESSAGE_SENT;
        } else {
            return VIEW_TYPE_MESSAGE_RECEIVED;
        }
    }

    @Override
    public long getItemId(int position) {
        return chats.get(position).getId();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_MESSAGE_DATE) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_date, parent, false);
            return new DateViewHolder(view);
        } else if (viewType == VIEW_TYPE_MESSAGE_SENT) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_sent, parent, false);
            return new SentViewHolder(view);
        } else {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_received, parent, false);
            return new ReceivedViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final Chat chat = chats.get(position);
        if (holder.getItemViewType() == VIEW_TYPE_MESSAGE_DATE) {
            ((DateViewHolder) holder).bind(chat, position);
        } else if (holder.getItemViewType() == VIEW_TYPE_MESSAGE_SENT) {
            ((SentViewHolder) holder).bind(chat, position);
        } else {
            ((ReceivedViewHolder) holder).bind(chat, position);
        }
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }

    public class DateViewHolder extends RecyclerView.ViewHolder {
        private TextView date;

        public DateViewHolder(View view) {
            super(view);
            context = view.getContext();

            date = view.findViewById(R.id.date);
            date.setTextColor(context.getResources().getColor(Utils.colorsPrimary[lesson]));
        }

        public void bind(final Chat chat, final int position) {
            date.setText(Utils.with(context).isToday(chat.getCreatedAt()) ?
                    "Hari ini" : Utils.with(context).getCompleteFormatDate(chat.getCreatedAt()));
        }
    }

    public class SentViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout bg;

        private TextView content, time;
        private ImageView image;
        private TextView status;

        public SentViewHolder(View view) {
            super(view);
            context = view.getContext();

            bg = view.findViewById(R.id.bg);
            content = view.findViewById(R.id.content);
            time = view.findViewById(R.id.time);
            image = view.findViewById(R.id.image);
            status = view.findViewById(R.id.status);

            bg.setBackground(ContextCompat.getDrawable(context, Utils.bubbles[lesson]));
            status.setTextColor(context.getResources().getColor(Utils.colorsPrimary[lesson]));
        }

        public void bind(final Chat chat, final int position) {
            if (position == chats.size() - 1) {
                mainView.smoothScrollToPosition(chats.size());
            }

            final String plain = AES.decrypt(chat.getContent());
            if (chat.getContentType() == 0) {
                content.setText(plain);
                content.setPaintFlags(Paint.LINEAR_TEXT_FLAG);

                content.setVisibility(View.VISIBLE);
                image.setVisibility(View.GONE);
            } else if (chat.getContentType() == 1) {
                content.setVisibility(View.GONE);

                if (plain.contains("local")) {
                    File imageFile = Utils.with(context).getFile(plain, Constant.DIR_PICTURES_INTERNAL, false);
                    if (imageFile.exists()) {
                        image.setImageURI(Uri.fromFile(imageFile));
                    } else {
                        image.setImageDrawable(context.getResources().getDrawable(R.drawable.placeholder_blurry));
                    }
                } else {
                    Picasso.with(context)
                            .load(Utils.with(context).getURLMediaImage(plain, "chat"))
                            .placeholder(R.drawable.placeholder)
                            .fit()
                            .centerCrop()
                            .into(image);
                }
                image.setVisibility(View.VISIBLE);
            } else if (chat.getContentType() == 2) {
                String[] parts = plain.split("-");
                if (plain.contains("local")) {
                    if (parts.length == 3) {
                        content.setText(plain.split("-")[2]);
                    } else {
                        String fileName = "";
                        for (int i = 2; i < parts.length; i++) {
                            fileName += parts[i];
                            if (i != parts.length - 1) {
                                fileName += "-";
                            }
                        }
                        content.setText(fileName);
                    }
                } else {
                    String fileName = "";
                    for (int i = 1; i < parts.length; i++) {
                        fileName += parts[i];
                        if (i != parts.length - 1) {
                            fileName += "-";
                        }
                    }
                    content.setText(fileName);
                }
                content.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);

                content.setVisibility(View.VISIBLE);
                image.setVisibility(View.GONE);
            }

            time.setText(Utils.with(context).formatDate(chat.getCreatedAt(), timeFormat) + " WIB");
            if (chat.getSent() == 0) {
                status.setText("");
            } else if (chat.getSent() == 1) {
                status.setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/ionicons.ttf"), Typeface.BOLD);
                status.setText(context.getString(R.string.chat_delay));
            } else {
                status.setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/ionicons.ttf"), Typeface.NORMAL);
                status.setText(context.getString(R.string.chat_read));
            }

            bg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (chat.getContentType() == 1) {
                        if (plain.contains("local")) {
                            File imageFile = Utils.with(context).getFile(plain, Constant.DIR_PICTURES_INTERNAL, false);
                            if (imageFile.exists()) {
                                Intent intent = new Intent(context, FullScreenImageActivity.class);
                                intent.putExtra("image", plain);
                                intent.putExtra("category", "chat");
                                context.startActivity(intent);
                            } else {
                                Toast.makeText(context.getApplicationContext(), "Gambar tidak tersedia", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Intent intent = new Intent(context, FullScreenImageActivity.class);
                            intent.putExtra("image", plain);
                            intent.putExtra("category", "chat");
                            context.startActivity(intent);
                        }
                    } else if (chat.getContentType() == 2) {
                        File file = Utils.with(context).getFile(plain, Constant.DIR_DOCUMENTS_INTERNAL, false);
                        if (plain.contains("local")) {
                            if (file.exists()) {
                                Uri uri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file);
                                try {
                                    openSupportedApp(uri, file);
                                } catch (ActivityNotFoundException e) {
                                    Toast.makeText(context, "Tidak ada aplikasi untuk membuka dokumen ini", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(context.getApplicationContext(), "Dokumen tidak tersedia", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            if (file.exists()) {
                                Uri uri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file);
                                try {
                                    openSupportedApp(uri, file);
                                } catch (ActivityNotFoundException e) {
                                    Toast.makeText(context, "Tidak ada aplikasi untuk membuka dokumen ini", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                if (Internet.isConnected(context)) {
                                    ChatPresenter chatPresenter = new ChatPresenter(context, ChatListAdapter.this);
                                    chatPresenter.downloadDocument(plain);
                                } else {
                                    Toast.makeText(context.getApplicationContext(), context.getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }
                }
            });

            bg.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (chat.getContentType() == 0 || chat.getContentType() == 1) {
                        showContextDialog(chat, position, SentViewHolder.this);
                    }
                    return false;
                }
            });

        }
    }

    public class ReceivedViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout bg;

        private TextView content, time;
        private ImageView image;

        public ReceivedViewHolder(View view) {
            super(view);
            context = view.getContext();

            bg = view.findViewById(R.id.bg);
            content = view.findViewById(R.id.content);
            time = view.findViewById(R.id.time);
            image = view.findViewById(R.id.image);

            bg.setBackground(ContextCompat.getDrawable(context, R.drawable.custom_bubble_default));
        }

        public void bind(final Chat chat, final int position) {
            if (position == chats.size() - 1) {
                mainView.smoothScrollToPosition(chats.size());
            }

            final String plain = AES.decrypt(chat.getContent());
            if (chat.getContentType() == 0) {
                content.setText(plain);
                content.setPaintFlags(Paint.LINEAR_TEXT_FLAG);

                content.setVisibility(View.VISIBLE);
                image.setVisibility(View.GONE);
            } else if (chat.getContentType() == 1) {
                Picasso.with(context)
                        .load(Utils.with(context).getURLMediaImage(plain, "chat"))
                        .placeholder(R.drawable.placeholder)
                        .fit()
                        .centerCrop()
                        .into(image);

                content.setVisibility(View.GONE);
                image.setVisibility(View.VISIBLE);
            } else if (chat.getContentType() == 2) {
                String[] parts = plain.split("-");
                String fileName = "";
                for (int i = 1; i < parts.length; i++) {
                    fileName += parts[i];
                    if (i != parts.length - 1) {
                        fileName += "-";
                    }
                }
                content.setText(fileName);
                content.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);

                content.setVisibility(View.VISIBLE);
                image.setVisibility(View.GONE);
            }
            time.setText(Utils.with(context).formatDate(chat.getCreatedAt(), timeFormat) + " WIB");

            bg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (chat.getContentType() == 1) {
                        Intent intent = new Intent(context, FullScreenImageActivity.class);
                        intent.putExtra("image", plain);
                        intent.putExtra("category", "chat");

                        context.startActivity(intent);
                    } else if (chat.getContentType() == 2) {
                        File file = Utils.with(context).getFile(plain, Constant.DIR_DOCUMENTS_INTERNAL, false);
                        if (file.exists()) {
                            Uri uri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file);
                            try {
                                openSupportedApp(uri, file);
                            } catch (ActivityNotFoundException e) {
                                Toast.makeText(context, "Tidak ada aplikasi untuk membuka dokumen ini", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            if (Internet.isConnected(context)) {
                                ChatPresenter chatPresenter = new ChatPresenter(context, ChatListAdapter.this);
                                chatPresenter.downloadDocument(plain);
                            } else {
                                Toast.makeText(context.getApplicationContext(), context.getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
            });

            bg.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (chat.getContentType() == 0 || chat.getContentType() == 1) {
                        showContextDialog(chat, position, ReceivedViewHolder.this);
                    }
                    return false;
                }
            });
        }
    }

    private void showContextDialog(final Chat chat, final int position, final RecyclerView.ViewHolder holder) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final AlertDialog dialog = builder.create();
        dialog.setView(dialog.getLayoutInflater().inflate(R.layout.custom_message_context, null));
        dialog.show();

        LinearLayout copy = dialog.findViewById(R.id.copy);
        LinearLayout save = dialog.findViewById(R.id.save);

        if (chat.getContentType() == 0) {
            copy.setVisibility(View.VISIBLE);
        } else if (chat.getContentType() == 1) {
            save.setVisibility(View.VISIBLE);
        }

        final String plain = AES.decrypt(chat.getContent());
        copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipData clipData = ClipData.newPlainText("message", plain);
                ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                clipboardManager.setPrimaryClip(clipData);

                dialog.dismiss();
                Toast.makeText(context.getApplicationContext(), "Pesan tersalin", Toast.LENGTH_SHORT).show();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageView image = null;
                if (holder.getItemViewType() == VIEW_TYPE_MESSAGE_SENT) {
                    final String plain = AES.decrypt(chat.getContent());
                    if (plain.contains("local")) {
                        File imageFile = Utils.with(context).getFile(plain, Constant.DIR_PICTURES_INTERNAL, false);
                        if (imageFile.exists()) {
                            image = ((SentViewHolder) holder).image;
                        } else {
                            dialog.dismiss();
                            Toast.makeText(context.getApplicationContext(), "Gambar tidak tersedia", Toast.LENGTH_SHORT).show();

                            return;
                        }
                    } else {
                        image = ((SentViewHolder) holder).image;
                    }
                } else {
                    image = ((ReceivedViewHolder) holder).image;
                }
                Bitmap bitmap = ((BitmapDrawable) image.getDrawable()).getBitmap();
                String fileName = System.currentTimeMillis() + ".jpg";
                Utils.with(context).saveImage(bitmap, fileName, Constant.DIR_DOWNLOAD_EXTERNAL, true);

                dialog.dismiss();
                Toast.makeText(context.getApplicationContext(), "Gambar tersimpan", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openSupportedApp(Uri uri, File file) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        if (file.toString().contains(".doc") || file.toString().contains(".docx")) {
            intent.setDataAndType(uri, "application/msword");
        } else if (file.toString().contains(".xls") || file.toString().contains(".xlsx")) {
            intent.setDataAndType(uri, "application/vnd.ms-excel");
        } else if (file.toString().contains(".ppt") || file.toString().contains(".pptx")) {
            intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
        } else if (file.toString().contains(".pdf")) {
            intent.setDataAndType(uri, "application/pdf");
        } else if (file.toString().contains(".txt")) {
            intent.setDataAndType(uri, "text/plain");
        } else {
            intent.setDataAndType(uri, "*/*");
        }
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(intent);
    }

    @Override
    public void onSuccess(BaseResponse base) {
        Toast.makeText(context.getApplicationContext(), "Dokumen berhasil diunduh", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onFailure(String message) {
        Toast.makeText(context.getApplicationContext(), "Unduhan gagal, silahkan coba lagi", Toast.LENGTH_SHORT).show();
    }
}