package com.example.exchangerates;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RecyclerViewPrivatBankAdapter extends RecyclerView.Adapter<RecyclerViewPrivatBankAdapter.ViewHolder> {

    private List<ExchangeRatePrivatBank> list;
    private LayoutInflater inflater;
    private OnItemClickListener mListener;

    public RecyclerViewPrivatBankAdapter(Context context, List<ExchangeRatePrivatBank> list) {
        inflater = LayoutInflater.from(context);
        this.list = list;
        if(context instanceof OnItemClickListener) {
            mListener = (OnItemClickListener) context;
        }
    }

    @NonNull
    @Override
    public RecyclerViewPrivatBankAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.privat_bank_recycler_view_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewPrivatBankAdapter.ViewHolder holder, int position) {
        holder.bind(position);
        holder.setListener(mListener, position);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void updateDataSet(List<ExchangeRatePrivatBank> newList) {
        list.clear();
        list = newList;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView currency;
        TextView buy;
        TextView sell;

        public ViewHolder(View view) {
            super(view);
            currency = view.findViewById(R.id.currency);
            buy = view.findViewById(R.id.buy);
            sell = view.findViewById(R.id.sell);
        }

        public void bind(int position) {
            ExchangeRatePrivatBank exchangeRatePrivatBank = list.get(position);
            currency.setText(exchangeRatePrivatBank.getCurrency());
            buy.setText(String.valueOf(exchangeRatePrivatBank.getBuy()));
            sell.setText(String.valueOf(exchangeRatePrivatBank.getSell()));
        }

        public void setListener(OnItemClickListener listener, int position){
            itemView.setOnClickListener((view)->{
                String currency = list.get(position).getCurrency();
                listener.onItemClick(currency);
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(String currency);
    }
}
