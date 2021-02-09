package com.example.exchangerates;

import java.math.BigDecimal;

public class ExchangeRatePrivatBank {
    private String currency;
    private double buy;
    private double sell;

    public ExchangeRatePrivatBank(String currency, double buy, double sell) {
        this.currency = currency;
        this.buy = ExchangeRateNationalBank.roundWithScale(buy);
        this.sell = ExchangeRateNationalBank.roundWithScale(sell);
    }

    public String getCurrency() {
        return currency;
    }

    public double getBuy() {
        return buy;
    }

    public double getSell() {
        return sell;
    }
}
