package app.newt.id.view.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.InterstitialAd;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Random;

import app.newt.id.R;
import app.newt.id.helper.Constant;
import app.newt.id.helper.Session;
import app.newt.id.helper.Utils;
import app.newt.id.server.model.Available;
import app.newt.id.server.model.Teacher;
import app.newt.id.server.model.User;
import app.newt.id.view.activity.ChatActivity;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Erick Sumargo on 8/31/2016.
 */
public class TeacherListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;

    private RecyclerView.ViewHolder mainHolder;
    private View itemView;

    private InterstitialAd mInterstitialAd;

    private User user;
    private List<Teacher> teachers;

    private int lesson;
    private String dialogId, receiverCode;

    public TeacherListAdapter(InterstitialAd mInterstitialAd, User user, List<Teacher> teachers, int lesson) {
        this.mInterstitialAd = mInterstitialAd;
        this.user = user;
        this.teachers = teachers;
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
        private TextView name, pro, active, off, available, date, newMember;

        public ItemViewHolder(View view) {
            super(view);

            context = view.getContext();
            item = view.findViewById(R.id.item);
            photo = view.findViewById(R.id.photo);
            online = view.findViewById(R.id.online);
            name = view.findViewById(R.id.name);
            pro = view.findViewById(R.id.pro);
            active = view.findViewById(R.id.active);
            off = view.findViewById(R.id.off);
            available = view.findViewById(R.id.available);
            date = view.findViewById(R.id.date);
            newMember = view.findViewById(R.id.new_member);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        itemView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.item_teacher, parent, false);
        mainHolder = new ItemViewHolder(itemView);

        return mainHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final Teacher teacher = teachers.get(position);
        String dateFormat = "dd/MM/yy";

        ItemViewHolder itemHolder = (ItemViewHolder) holder;

        GradientDrawable gd = (GradientDrawable) itemHolder.newMember.getBackground().getCurrent();
        gd.setColor(context.getResources().getColor(Utils.colorsPrimary[lesson]));

        Picasso.with(context).load(Utils.with(context).getURLMediaImage(teacher.getUser().getPhoto(), Constant.TEACHER))
                .placeholder(R.drawable.avatar)
                .fit()
                .centerCrop()
                .into(itemHolder.photo);

        itemHolder.online.setVisibility(teacher.getUser().isOnline() ? View.VISIBLE : View.GONE);
        itemHolder.name.setText(teacher.getUser().getName());
        itemHolder.pro.setVisibility(teacher.getUser().getPro() == 1 ? View.VISIBLE : View.GONE);

        if (teacher.getAvailables().size() > 0) {
            showTeacher(itemHolder);
            if (!Utils.with(context).isTodayOff(teacher.getDays_off())) {
                showAvailableData(itemHolder, teacher);
            } else {
                hideAvailableData(itemHolder);
            }
        } else {
            hideTeacher(itemHolder);
        }

        itemHolder.date.setText(Utils.with(context).formatDate(teacher.getUser().getCreatedAt(), dateFormat));
        if (Utils.with(context).getDifferentDays(teacher.getUser().getCreatedAt(), 0) <= 7) {
            itemHolder.newMember.setVisibility(View.VISIBLE);
        } else {
            itemHolder.newMember.setVisibility(View.GONE);
        }

        itemHolder.item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                receiverCode = teacher.getUser().getCode();
                dialogId = Utils.with(context).getDialogId(user.getCode(), receiverCode, lesson + 1);

                Session.with(context).saveEnvChat(1);
                Session.with(context).saveCurrentDialog(dialogId);
                Session.with(context).saveCurrentReceiverCode(receiverCode);
                Session.with(context).saveCurrentReceiverType(Constant.TEACHER);
                Session.with(context).saveCurrentTheme(lesson);

//                if (mInterstitialAd.isLoaded()) {
//                    Random rnd = new Random();
//                    if (rnd.nextInt(2) == 0) {
//                        mInterstitialAd.show();
//                    } else {
//                        openChat();
//                    }
//                } else {
//                    openChat();
//                }
                openChat();
            }
        });
    }

    private void showTeacher(ItemViewHolder itemHolder) {
        itemHolder.item.setVisibility(View.VISIBLE);
        itemHolder.item.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
    }

    private void hideTeacher(ItemViewHolder itemHolder) {
        itemHolder.item.setVisibility(View.GONE);
        itemHolder.item.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
    }

    private void showAvailableData(ItemViewHolder itemHolder, Teacher teacher) {
        String available = "";
        for (int i = 0; i < teacher.getAvailables().size(); i++) {
            Available a = teacher.getAvailables().get(i);
            available += formatTime(a.getStart()) + " - " + formatTime(a.getEnd()) + " WIB";
            if (i < teacher.getAvailables().size() - 1) {
                available += "\n";
            }
        }
        itemHolder.active.setVisibility(View.VISIBLE);
        itemHolder.off.setVisibility(View.GONE);

        itemHolder.available.setText(available);
        itemHolder.available.setVisibility(View.VISIBLE);
    }

    private void hideAvailableData(ItemViewHolder itemHolder) {
        itemHolder.active.setVisibility(View.GONE);
        itemHolder.off.setVisibility(View.VISIBLE);

        itemHolder.available.setVisibility(View.GONE);
    }

    private String formatTime(String time) {
        String[] parts = time.split(":");
        return parts[0] + ":" + parts[1];
    }

    private void openChat() {
        Intent intent = new Intent(context, ChatActivity.class);
        context.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return teachers.size();
    }
}