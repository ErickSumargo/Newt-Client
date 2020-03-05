package app.newt.id.view.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.InterstitialAd;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Random;

import app.newt.id.R;
import app.newt.id.helper.AES;
import app.newt.id.helper.Constant;
import app.newt.id.helper.Session;
import app.newt.id.helper.Utils;
import app.newt.id.server.model.Chat;
import app.newt.id.server.model.Dialog;
import app.newt.id.server.model.User;
import app.newt.id.view.activity.ChatActivity;
import de.hdodenhof.circleimageview.CircleImageView;
import io.realm.RealmList;

/**
 * Created by Erick Sumargo on 8/31/2016.
 */
public class DialogListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;

    private RecyclerView.ViewHolder mainHolder;
    private View itemView;

    private InterstitialAd mInterstitialAd;

    private User user;
    private List<Dialog> dialogs;

    private int lesson;
    private String receiverCode, dialogId;

    public DialogListAdapter(InterstitialAd mInterstitialAd, User user, List<Dialog> dialogs, int lesson) {
        this.mInterstitialAd = mInterstitialAd;
        this.user = user;
        this.dialogs = dialogs;
        this.lesson = lesson;

//        mInterstitialAd.setAdListener(new AdListener() {
//            @Override
//            public void onAdClosed() {
//                openChat();
//            }
//        });
    }

    private class ItemViewHolder extends RecyclerView.ViewHolder {
        private View item;
        private CircleImageView photo;
        private View online;

        private TextView name, pro, message;
        private TextView date, counter;

        private LinearLayout mediaContainer;
        private ImageView mediaIc;
        private TextView mediaName;

        private View divider;

        public ItemViewHolder(View view) {
            super(view);

            context = view.getContext();
            item = view.findViewById(R.id.item);
            photo = view.findViewById(R.id.photo);
            online = view.findViewById(R.id.online);
            name = view.findViewById(R.id.name);
            pro = view.findViewById(R.id.pro);
            message = view.findViewById(R.id.message);

            date = view.findViewById(R.id.date);
            counter = view.findViewById(R.id.counter);

            mediaContainer = view.findViewById(R.id.media_container);
            mediaIc = view.findViewById(R.id.media_ic);
            mediaName = view.findViewById(R.id.media_name);

            divider = view.findViewById(R.id.divider);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        itemView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.item_dialog, parent, false);
        mainHolder = new ItemViewHolder(itemView);

        return mainHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final Dialog dialog = dialogs.get(position);
        RealmList<Chat> chats = dialog.getChats();
        String dateFormat = "dd/MM/yy";

        ItemViewHolder itemHolder = (ItemViewHolder) holder;

        if (chats.size() > 0) {
            itemHolder.item.setVisibility(View.VISIBLE);
            itemHolder.item.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));

            GradientDrawable gd = (GradientDrawable) itemHolder.counter.getBackground().getCurrent();
            gd.setColor(context.getResources().getColor(Utils.colorsPrimary[lesson]));

            int counter = 0;
            for (int i = 0; i < chats.size(); i++) {
                if (!chats.get(i).getSenderCode().equals(user.getCode()) && chats.get(i).getSent() < 2) {
                    counter++;
                }
            }

            Picasso.with(context).load(Utils.with(context).getURLMediaImage(dialog.getUser().getPhoto(),
                    Utils.with(context).getUserType(dialog.getUser().getCode())))
                    .placeholder(R.drawable.avatar)
                    .fit()
                    .centerCrop()
                    .into(itemHolder.photo);

            if (counter > 0) {
                itemHolder.name.setTypeface(null, Typeface.BOLD);
                itemHolder.message.setTextColor(context.getResources().getColor(Utils.colorsPrimaryDark[lesson]));
                itemHolder.message.setTypeface(null, Typeface.BOLD);

                itemHolder.counter.setText(String.valueOf(counter));
                itemHolder.counter.setVisibility(View.VISIBLE);
            } else {
                itemHolder.name.setTypeface(null, Typeface.NORMAL);
                itemHolder.message.setTextColor(context.getResources().getColor(R.color.black));
                itemHolder.message.setTypeface(null, Typeface.NORMAL);

                itemHolder.counter.setVisibility(View.GONE);
            }

            if (chats.get(chats.size() - 1).getContentType() == 0) {
                itemHolder.message.setText(AES.decrypt(chats.get(chats.size() - 1).getContent()));
                itemHolder.message.setVisibility(View.VISIBLE);

                itemHolder.mediaContainer.setVisibility(View.GONE);
            } else {
                itemHolder.message.setVisibility(View.GONE);

                if (chats.get(chats.size() - 1).getContentType() == 1) {
                    itemHolder.mediaIc.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_image));
                    itemHolder.mediaName.setText("Gambar");
                } else {
                    itemHolder.mediaIc.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_insert_drive_file));
                    itemHolder.mediaName.setText("Dokumen");
                }
                itemHolder.mediaIc.setColorFilter(context.getResources().getColor(R.color.grayDark));
                itemHolder.mediaContainer.setVisibility(View.VISIBLE);
            }
            itemHolder.online.setVisibility(dialog.getUser().isOnline() ? View.VISIBLE : View.GONE);
            itemHolder.name.setText(dialog.getUser().getName());
            itemHolder.pro.setVisibility(dialog.getUser().getPro() == 1 ? View.VISIBLE : View.GONE);
            itemHolder.date.setText(Utils.with(context).formatDate(chats.get(chats.size() - 1).getCreatedAt(), dateFormat));

            if (position != dialogs.size() - 1) {
                itemHolder.divider.setVisibility(View.VISIBLE);
            } else {
                if (user.getType().equals(Constant.STUDENT)) {
                    itemHolder.divider.setVisibility(View.GONE);
                } else {
                    itemHolder.divider.setVisibility(View.VISIBLE);
                }
            }

            itemHolder.item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    receiverCode = dialog.getUser().getCode();
                    dialogId = dialog.getId();

                    Session.with(context).saveEnvChat(1);
                    Session.with(context).saveCurrentDialog(dialogId);
                    Session.with(context).saveCurrentReceiverCode(receiverCode);
                    Session.with(context).saveCurrentReceiverType(Constant.USER);
                    Session.with(context).saveCurrentTheme(lesson);

//                    if (mInterstitialAd.isLoaded()) {
//                        Random rnd = new Random();
//                        if (rnd.nextInt(2) == 0) {
//                            mInterstitialAd.show();
//                        } else {
//                            openChat();
//                        }
//                    } else {
//                        openChat();
//                    }
                    openChat();
                }
            });
        } else {
            itemHolder.item.setVisibility(View.GONE);
            itemHolder.item.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
        }
    }

    private void openChat() {
        Intent intent = new Intent(context, ChatActivity.class);
        context.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return dialogs.size();
    }
}