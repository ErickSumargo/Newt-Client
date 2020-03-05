package app.newt.id.view.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import app.newt.id.R;
import app.newt.id.helper.Constant;
import app.newt.id.helper.Session;
import app.newt.id.server.model.Challenge;
import app.newt.id.view.activity.QuestionActivity;
import app.newt.id.view.custom.MathView;

/**
 * Created by Erick Sumargo on 8/31/2016.
 */
public class RecordListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;

    private RecyclerView.ViewHolder mainHolder;
    private View itemView;

    private List<Challenge> challenges;

    public RecordListAdapter(List<Challenge> challenges) {
        this.challenges = challenges;
    }

    private class ItemViewHolder extends RecyclerView.ViewHolder {
        private CardView item;
        private ImageView icCoin;
        private TextView material, point;
        private MathView content;

        private LinearLayout detailCont;
        private TextView answer;
        private ImageView icStatus;

        public ItemViewHolder(View view) {
            super(view);

            context = view.getContext();

            item = view.findViewById(R.id.item);
            icCoin = view.findViewById(R.id.ic_coin);
            material = view.findViewById(R.id.material);
            point = view.findViewById(R.id.point);
            content = view.findViewById(R.id.content);

            detailCont = view.findViewById(R.id.detail_container);
            answer = view.findViewById(R.id.answer);
            icStatus = view.findViewById(R.id.ic_status);

            content.setTextSize(15);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        itemView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.item_record, parent, false);
        mainHolder = new ItemViewHolder(itemView);

        return mainHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final Challenge challenge = challenges.get(position);

        ItemViewHolder itemHolder = (ItemViewHolder) holder;
        itemHolder.material.setText(challenge.getQuestion().getMaterial());
        itemHolder.icCoin.setColorFilter(context.getResources().getColor(R.color.colorPoints));
        itemHolder.point.setText(challenge.getQuestion().getPoint() + " pts");

        String content = "";
        if (challenge.getQuestion().getContent().contains("$$")) {
            int index = challenge.getQuestion().getContent().indexOf("$$");
            int counter = 1;
            while (counter != 2) {
                index = challenge.getQuestion().getContent().indexOf("$$", index + 1);
                if (index != -1) {
                    counter++;
                }
            }

            String display = challenge.getQuestion().getContent().substring(0, index + 2);
            if (display.contains("\\rhd")) {
                index = challenge.getQuestion().getContent().indexOf("$$");
                content = challenge.getQuestion().getContent().substring(0, index) + "\n \\( \\cdots \\)";
            } else {
                content = display + "\n \\( \\cdots \\)";
            }
        } else {
            if (challenge.getQuestion().getContent().length() <= 84) {
                content = challenge.getQuestion().getContent();
            } else {
                int index = challenge.getQuestion().getContent().indexOf("\\)");
                while (challenge.getQuestion().getContent().substring(0, index + 2).length() <= 84) {
                    index = challenge.getQuestion().getContent().indexOf("\\)", index + 1);
                    if (challenge.getQuestion().getContent().indexOf("\\)", index + 1) == -1) {
                        break;
                    }
                }
                content = challenge.getQuestion().getContent().substring(0, index + 2) + " \n \\( \\cdots \\)";
            }
        }
        itemHolder.content.setDisplayText(content);

        if (Session.with(context).getUserType().equals(Constant.STUDENT)) {
            itemHolder.detailCont.setBackgroundColor(context.getResources().getColor(R.color.colorTrue));
            itemHolder.answer.setVisibility(View.GONE);
            itemHolder.icStatus.setVisibility(View.VISIBLE);
        } else {
            itemHolder.detailCont.setBackgroundColor(context.getResources().getColor(R.color.graySemiLight));
            itemHolder.answer.setVisibility(View.VISIBLE);
            itemHolder.icStatus.setVisibility(View.GONE);
        }

        itemHolder.item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, QuestionActivity.class);
                intent.putExtra("id", challenge.getQuestion().getId());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return challenges.size();
    }
}