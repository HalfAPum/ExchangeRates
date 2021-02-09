package com.example.exchangerates;

import java.math.BigDecimal;

public class ExchangeRateNationalBank {
    private String currency;
    private double buySell;

    public ExchangeRateNationalBank(String currency, double buySell) {
        this.currency = currency;
        this.buySell = roundWithScale(buySell);
    }

    public static double roundWithScale(double value) {
        BigDecimal bigDecimal = new BigDecimal(value);
        BigDecimal roundedWithScale =  bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP);
        return roundedWithScale.doubleValue();
    }

    public String getCurrency() {
        return currency;
    }

    public double getBuySell() {
        return buySell;
    }
}
