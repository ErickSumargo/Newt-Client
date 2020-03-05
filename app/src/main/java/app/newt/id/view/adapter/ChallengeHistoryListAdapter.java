package app.newt.id.view.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import app.newt.id.R;
import app.newt.id.helper.Constant;
import app.newt.id.helper.Session;
import app.newt.id.helper.Utils;
import app.newt.id.server.model.Challenge;
import app.newt.id.view.activity.ChallengeActivity;
import app.newt.id.view.activity.QuestionActivity;
import app.newt.id.view.custom.MathView;
import app.newt.id.view.interfaces.LoadMore;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Erick Sumargo on 8/31/2016.
 */
public class ChallengeHistoryListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;

    private RecyclerView.ViewHolder mainHolder;
    private RecyclerView listView;
    private LinearLayoutManager layoutManager;
    private View itemView;

    private static LoadMore mLoadMore;

    private final static int VIEW_ITEM = 0, VIEW_PROGRESS = 1;
    private int totalItemCount, lastVisibleItem, visibleThreshold = 1;
    private boolean loading = false;

    private List<Challenge> histories;

    public ChallengeHistoryListAdapter(RecyclerView listView, List<Challenge> histories) {
        this.listView = listView;
        this.histories = histories;

        layoutManager = (LinearLayoutManager) listView.getLayoutManager();
        setEvent();
    }

    private void setEvent() {
        listView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                totalItemCount = layoutManager.getItemCount();
                lastVisibleItem = layoutManager.findLastVisibleItemPosition();
                if (!loading && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                    loadMore();
                    loading = true;

                    histories.add(null);
                    notifyDataSetChanged();
                }

            }
        });
    }

    public void setOnLoadMoreListener(Context context) {
        mLoadMore = (LoadMore) context;
    }

    public void setLoaded() {
        loading = false;
    }

    public void updateList(List<Challenge> histories) {
        this.histories.clear();
        this.histories.addAll(histories);

        notifyDataSetChanged();
    }

    private void loadMore() {
        if (mLoadMore != null) {
            mLoadMore.onLoadMore();
        }
    }

    private class ItemViewHolder extends RecyclerView.ViewHolder {
        private CardView item;
        private ImageView icCoin;
        private TextView material, point;
        private MathView content;

        private CircleImageView presenterPhoto;
        private TextView presenterName;

        private LinearLayout detailCont;

        public ItemViewHolder(View view) {
            super(view);

            context = view.getContext();
            item = view.findViewById(R.id.item);
            icCoin = view.findViewById(R.id.ic_coin);
            material = view.findViewById(R.id.material);
            point = view.findViewById(R.id.point);
            content = view.findViewById(R.id.content);

            presenterPhoto = view.findViewById(R.id.presenter_photo);
            presenterName = view.findViewById(R.id.presenter_name);

            detailCont = view.findViewById(R.id.detail_container);

            content.setTextSize(15);
        }
    }

    private class ProgressViewHolder extends RecyclerView.ViewHolder {
        private ProgressBar progressBar;

        public ProgressViewHolder(View view) {
            super(view);
            progressBar = view.findViewById(R.id.progress_bar);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_ITEM) {
            itemView = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.item_challenge_history, parent, false);
            mainHolder = new ItemViewHolder(itemView);
        } else if (viewType == VIEW_PROGRESS) {
            itemView = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.custom_footer_loader, parent, false);
            mainHolder = new ProgressViewHolder(itemView);
        }
        return mainHolder;
    }

    @Override
    public int getItemViewType(int position) {
        if (histories.get(position) != null) {
            return VIEW_ITEM;
        } else {
            return VIEW_PROGRESS;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ItemViewHolder) {
            final Challenge challenge = histories.get(position);

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

            Picasso.with(context).load(Utils.with(context).getURLMediaImage(challenge.getQuestion().getTeacher().getPhoto(), "teacher"))
                    .placeholder(R.drawable.avatar)
                    .fit()
                    .centerCrop()
                    .into(itemHolder.presenterPhoto);
            itemHolder.presenterName.setText(challenge.getQuestion().getTeacher().getName());

            itemHolder.item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, QuestionActivity.class);
                    intent.putExtra("id", challenge.getQuestion().getId());
                    context.startActivity(intent);
                }
            });
        } else if (holder instanceof ProgressViewHolder) {
            ((ProgressViewHolder) holder).progressBar.setIndeterminate(true);
        }
    }

    @Override
    public int getItemCount() {
        return histories.size();
    }
}