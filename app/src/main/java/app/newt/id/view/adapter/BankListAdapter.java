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
import app.newt.id.server.model.Bank;
import app.newt.id.server.model.Package;

/**
 * Created by Erick Sumargo on 8/31/2016.
 */
public class BankListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;

    private RecyclerView.ViewHolder mainHolder;
    private View itemView;

    private List<Bank> banks;

    public BankListAdapter(List<Bank> banks) {
        this.banks = banks;
    }

    private class ItemViewHolder extends RecyclerView.ViewHolder {
        private ImageView logo;
        private TextView owner, account;

        public ItemViewHolder(View view) {
            super(view);

            context = view.getContext();
            logo = view.findViewById(R.id.logo);
            owner = view.findViewById(R.id.owner);
            account = view.findViewById(R.id.account);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        itemView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.item_bank, parent, false);
        mainHolder = new ItemViewHolder(itemView);

        return mainHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final Bank bank = banks.get(position);

        ItemViewHolder itemHolder = (ItemViewHolder) holder;
        if(bank.getBank().equals(Constant.BCA)) {
            itemHolder.logo.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_bca));
        } else if(bank.getBank().equals(Constant.BNI)) {
            itemHolder.logo.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_bni));
        }
        itemHolder.owner.setText(bank.getOwner());
        itemHolder.account.setText(bank.getAccount());
    }

    @Override
    public int getItemCount() {
        return banks.size();
    }
}