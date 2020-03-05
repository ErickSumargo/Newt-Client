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

import com.romainpiel.shimmer.Shimmer;
import com.romainpiel.shimmer.ShimmerTextView;
import com.squareup.picasso.Picasso;

import java.util.List;

import app.newt.id.R;
import app.newt.id.helper.Constant;
import app.newt.id.helper.Utils;
import app.newt.id.server.model.Challenge;
import app.newt.id.server.model.Challenger;
import app.newt.id.view.activity.QuestionActivity;
import app.newt.id.view.activity.RankActivity;
import app.newt.id.view.activity.RecordActivity;
import app.newt.id.view.custom.MathView;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Erick Sumargo on 8/31/2016.
 */
public class RankListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;

    private RecyclerView.ViewHolder mainHolder;
    private View itemView;

    private List<Challenger> challengers;

    public RankListAdapter(List<Challenger> challengers) {
        this.challengers = challengers;
    }

    public void clearData() {
        challengers.clear();
        notifyDataSetChanged();
    }

    private class ItemViewHolder extends RecyclerView.ViewHolder {
        private CardView item;
        private ShimmerTextView rank;
        private TextView ordinal;
        private CircleImageView photo;
        private TextView name, school;
        private TextView points;

        private ImageView icCoin;

        public ItemViewHolder(View view) {
            super(view);

            context = view.getContext();
            item = view.findViewById(R.id.item);
            rank = view.findViewById(R.id.rank);
            ordinal = view.findViewById(R.id.ordinal);
            photo = view.findViewById(R.id.photo);
            name = view.findViewById(R.id.name);
            school = view.findViewById(R.id.school);
            points = view.findViewById(R.id.points);

            icCoin = view.findViewById(R.id.ic_coin);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        itemView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.item_rank, parent, false);
        mainHolder = new ItemViewHolder(itemView);

        return mainHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final Challenger challenger = challengers.get(position);

        ItemViewHolder itemHolder = (ItemViewHolder) holder;

        itemHolder.rank.setText(String.valueOf(position + 1));
        if (position == 0) {
            itemHolder.rank.setTextColor(context.getResources().getColor(R.color.colorPoints));
            setShimmering(itemHolder.rank);

            itemHolder.ordinal.setText("st");
        } else if (position == 1) {
            itemHolder.rank.setTextColor(context.getResources().getColor(R.color.colorPrimary));
            setShimmering(itemHolder.rank);

            itemHolder.ordinal.setText("nd");
        } else if (position == 2) {
            itemHolder.rank.setTextColor(context.getResources().getColor(R.color.colorPrimary));
            setShimmering(itemHolder.rank);

            itemHolder.ordinal.setText("rd");
        } else {
            itemHolder.rank.setTextColor(context.getResources().getColor(R.color.holo_light));
            itemHolder.ordinal.setText("th");
        }

        Picasso.with(context).load(Utils.with(context).getURLMediaImage(challenger.getStudent().getPhoto(), Constant.STUDENT))
                .placeholder(R.drawable.avatar)
                .fit()
                .centerCrop()
                .into(itemHolder.photo);
        itemHolder.name.setText(challenger.getStudent().getName());
        if (!challenger.getStudent().getSchool().isEmpty()) {
            itemHolder.school.setText(challenger.getStudent().getSchool());
            itemHolder.school.setVisibility(View.VISIBLE);
        } else {
            itemHolder.school.setVisibility(View.GONE);
        }
        itemHolder.icCoin.setColorFilter(context.getResources().getColor(R.color.colorPoints));
        itemHolder.points.setText(challenger.getPoints() + " pts/" + challenger.getSolved());

        itemHolder.item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, RecordActivity.class);
                intent.putExtra("challenger_id", challenger.getId());
                intent.putExtra("lesson_id", ((RankActivity) context).lessonId);
                context.startActivity(intent);
            }
        });
    }

    private void setShimmering(ShimmerTextView rank) {
        Shimmer shimmer = new Shimmer();
        shimmer.setDuration(1500);
        shimmer.start(rank);
    }

    @Override
    public int getItemCount() {
        return challengers.size();
    }
}