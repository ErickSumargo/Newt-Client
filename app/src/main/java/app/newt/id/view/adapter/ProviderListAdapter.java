package app.newt.id.view.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import app.newt.id.R;
import app.newt.id.helper.Constant;
import app.newt.id.server.model.Provider;

/**
 * Created by Erick Sumargo on 8/31/2016.
 */
public class ProviderListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;

    private RecyclerView.ViewHolder mainHolder;
    private View itemView;

    private List<Provider> providers;

    public ProviderListAdapter(List<Provider> providers) {
        this.providers = providers;
    }

    private class ItemViewHolder extends RecyclerView.ViewHolder {
        private ImageView logo;
        private TextView phone;

        public ItemViewHolder(View view) {
            super(view);

            context = view.getContext();
            logo = view.findViewById(R.id.logo);
            phone = view.findViewById(R.id.phone);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        itemView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.item_provider, parent, false);
        mainHolder = new ItemViewHolder(itemView);

        return mainHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final Provider provider = providers.get(position);

        ItemViewHolder itemHolder = (ItemViewHolder) holder;
        if (provider.getProvider().equals(Constant.TELKOMSEL)) {
            itemHolder.logo.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_telkomsel));
        } else if (provider.getProvider().equals(Constant.XL)) {
            itemHolder.logo.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_xl));
        } else if (provider.getProvider().equals(Constant.THREE)) {
            itemHolder.logo.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_3));
        } else if (provider.getProvider().equals(Constant.INDOSAT)) {
            itemHolder.logo.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_indosat));
        }
        itemHolder.phone.setText(provider.getPhone());
    }

    @Override
    public int getItemCount() {
        return providers.size();
    }
}