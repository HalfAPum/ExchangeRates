package com.example.exchangerates;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RecyclerViewNationalBankAdapter extends RecyclerView.Adapter<RecyclerViewNationalBankAdapter.ViewHolder> {

    private List<ExchangeRateNationalBank> list;
    private LayoutInflater inflater;
    private int lastCheckedItem = -1;


    public RecyclerViewNationalBankAdapter(Context context, List<ExchangeRateNationalBank> list) {
            inflater = LayoutInflater.from(context);
            this.list = list;
    }

    public int getLastCheckedItem() {
        return lastCheckedItem;
    }

    public void setLastCheckedItem(int lastCheckedItem) {
        this.lastCheckedItem = lastCheckedItem;
    }

    @NonNull
    @Override
    public RecyclerViewNationalBankAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = inflater.inflate(R.layout.national_bank_recycler_view_item, parent, false);
            return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewNationalBankAdapter.ViewHolder holder, int position) {
        if(position == lastCheckedItem) {
            holder.itemView.setBackgroundColor(holder.itemView.getResources().getColor(R.color.colorHighlighted));
        } else if(position % 2 == 0) {
            holder.itemView.setBackgroundColor(holder.itemView.getResources().getColor(R.color.colorSecondItem));
        } else {
            holder.itemView.setBackgroundColor(Color.WHITE);
        }
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
            return list.size();
            }

    public void updateDataSet(List<ExchangeRateNationalBank> newList) {
            list.clear();
            list = newList;
            notifyDataSetChanged();
    }

    public int findItemPositionByCurrency(String currency) {
        for (ExchangeRateNationalBank exchangeRateNationalBank : list) {
            if(exchangeRateNationalBank.getCurrency().equals(currency)){
                return list.lastIndexOf(exchangeRateNationalBank);
            }
        }
        return -1;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView currencyName;
        TextView sellBuy;
        TextView currency;
        private final String [][] currencyNames = new String[][] {
                {"AZN","Азербайджанский манат"},
                {"BYN","Белорусский рубль"},
                {"CAD","Канадский доллар"},
                {"CHF","Швейцарский франк"},
                {"CNY","Китайский юань"},
                {"CZK","Чешская крона"},
                {"DKK","Датская крона"},
                {"EUR","Евро"},
                {"GBP","Фунт стерлингов"},
                {"HUF","Венгерский форинт"},
                {"ILS","Новый израильский шекель"},
                {"JPY","Японская иена"},
                {"KZT","Казахстанский тенге"},
                {"MDL","Лей"},
                {"NOK","Норвежская крона"},
                {"PLZ","Польский злотый"},
                {"RUB","Российский рубль"},
                {"SEK","Шведская крона"},
                {"SGD","Сингапурский доллар"},
                {"TMT","Туркменский манат"},
                {"TRY","Турецкая лира"},
                {"UAH","Украинская гривна"},
                {"USD","Доллар США"},
                {"UZS","Узбекский сум"},
                {"GEL","Грузинский лари"} };

        public ViewHolder(View view) {
            super(view);
            currencyName = view.findViewById(R.id.currencyName);
            sellBuy = view.findViewById(R.id.sellBuy);
            currency = view.findViewById(R.id.currency);
        }

        private void bind(int position) {
            ExchangeRateNationalBank exchangeRateNationalBank = list.get(position);
            for (String[] currency : currencyNames) {
                if(exchangeRateNationalBank.getCurrency().equals(currency[0])) {
                    currencyName.setText(currency[1]);
                    break;
                }
            }

            String sellBuyString = exchangeRateNationalBank.getBuySell() + "UAH";
            sellBuy.setText(sellBuyString);

            String currencyString = 1 + exchangeRateNationalBank.getCurrency();
            currency.setText(currencyString);
        }

        public void setChecked(int position) {
            setLastCheckedItem(position);
            itemView.setBackgroundColor(itemView.getResources().getColor(R.color.colorHighlighted));
        }

        public void setUnchecked() {
            if(lastCheckedItem % 2 == 0) {
                itemView.setBackgroundColor(itemView.getResources().getColor(R.color.colorSecondItem));
            } else {
                itemView.setBackgroundColor(Color.WHITE);
            }
        }
    }
}
