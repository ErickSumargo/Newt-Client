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

import com.squareup.picasso.Picasso;

import java.util.List;

import app.newt.id.R;
import app.newt.id.helper.Constant;
import app.newt.id.helper.Session;
import app.newt.id.helper.Utils;
import app.newt.id.server.model.Available;
import app.newt.id.server.model.Package;
import app.newt.id.server.model.Teacher;
import app.newt.id.server.model.User;
import app.newt.id.view.activity.ChatActivity;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Erick Sumargo on 8/31/2016.
 */
public class PackageListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;

    private RecyclerView.ViewHolder mainHolder;
    private View itemView;

    private List<Package> packages;

    public PackageListAdapter(List<Package> packages) {
        this.packages = packages;
    }

    private class ItemViewHolder extends RecyclerView.ViewHolder {
        private TextView transaction, days;

        public ItemViewHolder(View view) {
            super(view);

            context = view.getContext();
            transaction = view.findViewById(R.id.transaction);
            days = view.findViewById(R.id.days);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        itemView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.item_package, parent, false);
        mainHolder = new ItemViewHolder(itemView);

        return mainHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final Package pack = packages.get(position);

        ItemViewHolder itemHolder = (ItemViewHolder) holder;
        itemHolder.transaction.setText("Rp. " + pack.getTransaction() + ",-");
        itemHolder.days.setText("/" + pack.getDays() + " Hari");
    }

    @Override
    public int getItemCount() {
        return packages.size();
    }
}