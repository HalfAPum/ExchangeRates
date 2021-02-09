package com.example.exchangerates;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements RecyclerViewPrivatBankAdapter.OnItemClickListener{

    private static final String API_LINK = "https://api.privatbank.ua/p24api/exchange_rates?date=";
    private static final int ARCHIVE_STORAGE_TIME = 40000;
    private final Calendar privatBankCalendar = Calendar.getInstance();
    private final Calendar nationalBankCalendar = Calendar.getInstance();

    TextView privatBankDate;
    TextView nationalBankDate;
    RecyclerView privatBankExchangeRates;
    RecyclerView nationalBankExchangeRates;
    RecyclerViewPrivatBankAdapter privatBankAdapter;
    RecyclerViewNationalBankAdapter nationalBankAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        privatBankDate = findViewById(R.id.privatBankDate);
        nationalBankDate = findViewById(R.id.nationalBankDate);
        privatBankExchangeRates = findViewById(R.id.privatBankExchangeRates);
        nationalBankExchangeRates = findViewById(R.id.nationalBankExchangeRates);

        privatBankAdapter = new RecyclerViewPrivatBankAdapter(this, new ArrayList<>());
        nationalBankAdapter = new RecyclerViewNationalBankAdapter(this, new ArrayList<>());

        privatBankExchangeRates.setAdapter(privatBankAdapter);
        nationalBankExchangeRates.setAdapter(nationalBankAdapter);

        int day = privatBankCalendar.get(Calendar.DAY_OF_MONTH);
        int month = privatBankCalendar.get(Calendar.MONTH);
        int year = privatBankCalendar.get(Calendar.YEAR);
        String date = assembleDateAsString(day, month, year);

        privatBankDate.setText(date);
        nationalBankDate.setText(date);

        changePrivatBankDataByDate(date);
        changeNationalBankDataByDate(date);
    }

    private String assembleDateAsString(int day, int month, int year) {
        StringBuilder sb = new StringBuilder();

        if(day<=9){
            sb.append(0);
        }

        sb.append(day);
        sb.append(".");

        if(month<=9){
            sb.append(0);
        }

        sb.append(month);
        sb.append(".");
        sb.append(year);

        return sb.toString();
    }

    private int assembleDateAsInt(int day, int month, int year) {
        int date = year * 100 + month;
        date = date * 100 + day;
        return date;
    }

    public boolean correctDate(int day, int month, int year) {
        int date = assembleDateAsInt(day, month, year);

        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        int currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        int currentMonth = Calendar.getInstance().get(Calendar.MONTH);
        int currentDate = assembleDateAsInt(currentDay, currentMonth, currentYear);

        if(date > currentDate) {
            makeToast("Выбранная дата превышает текущую");
            return false;
        }
        if(date < currentDate - ARCHIVE_STORAGE_TIME){
            makeToast("Выбранная дата меньше допустимой(последние 4 года)");
            return false;
        }
        return true;
    }

    public void makeToast(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    public void privatBankCalendarClick(View v) {
        int day = privatBankCalendar.get(Calendar.DAY_OF_MONTH);
        int month = privatBankCalendar.get(Calendar.MONTH);
        int year = privatBankCalendar.get(Calendar.YEAR);
        DatePickerDialog picker = new DatePickerDialog(
                this,
                android.R.style.Theme_Holo_Light_Dialog_NoActionBar,
                dateSetListenerPB,
                year,
                month,
                day);
        picker.show();
    }

    public void nationalBankCalendarClick(View v) {
        int day = nationalBankCalendar.get(Calendar.DAY_OF_MONTH);
        int month = nationalBankCalendar.get(Calendar.MONTH);
        int year = nationalBankCalendar.get(Calendar.YEAR);
        DatePickerDialog picker = new DatePickerDialog(
                this,
                android.R.style.Theme_Holo_Light_Dialog_NoActionBar,
                dateSetListenerNB,
                year,
                month,
                day);
        picker.show();
    }

    public void changePrivatBankDataByDate(String date) {
        Observable<List<ExchangeRatePrivatBank>> observable = Observable.fromCallable(new Callable<List<ExchangeRatePrivatBank>>() {
            @Override
            public List<ExchangeRatePrivatBank> call() throws Exception{
                XmlPullParser parser = connectToPrivatBankArchive(API_LINK + date);
                ArrayList<ExchangeRatePrivatBank> changePBCourseList = new ArrayList<>();

                int eventType = parser.getEventType();
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if(eventType == XmlPullParser.START_TAG){
                        if(parser.getAttributeCount() == 6){
                            String currency = parser.getAttributeValue(1);
                            double sell = Double.parseDouble(parser.getAttributeValue(4));
                            double buy = Double.parseDouble(parser.getAttributeValue(5));

                            ExchangeRatePrivatBank exchangeRatePrivatBank = new ExchangeRatePrivatBank(currency, buy, sell);
                            changePBCourseList.add(exchangeRatePrivatBank);
                        }
                    }
                    eventType = parser.next();
                }

                return changePBCourseList;
            }})
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
        Disposable disposable = observable.subscribe((changeCourses -> {
            privatBankAdapter.updateDataSet(changeCourses);
            privatBankAdapter.notifyDataSetChanged();
        }));
    }

    public void changeNationalBankDataByDate(String date) {
        Observable<List<ExchangeRateNationalBank>> observable = Observable.fromCallable(new Callable<List<ExchangeRateNationalBank>>() {
            @Override
            public List<ExchangeRateNationalBank> call() throws Exception{
                XmlPullParser parser = connectToPrivatBankArchive(API_LINK + date);
                ArrayList<ExchangeRateNationalBank> exchangeRatesNationalBank = new ArrayList<>();

                int eventType = parser.getEventType();
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if(eventType == XmlPullParser.START_TAG){
                        if(parser.getAttributeCount() >= 4 && !parser.getAttributeValue(3).equals("UAH")){
                            String currency = parser.getAttributeValue(1);
                            double buySell = Double.parseDouble(parser.getAttributeValue(2));

                            ExchangeRateNationalBank exchangeRateNationalBank = new ExchangeRateNationalBank(currency, buySell);
                            exchangeRatesNationalBank.add(exchangeRateNationalBank);
                        }
                    }
                    eventType = parser.next();
                }

                return exchangeRatesNationalBank;
            }})
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
        Disposable disposable = observable.subscribe((changeCourses -> {
            nationalBankAdapter.updateDataSet(changeCourses);
            nationalBankAdapter.notifyDataSetChanged();
        }));
    }

    public XmlPullParser connectToPrivatBankArchive(String link) throws Exception {

        URL url = new URL(link);
        HttpURLConnection http = (HttpURLConnection) url.openConnection();
        http.setDoInput(true);
        http.connect();
        InputStream is = http.getInputStream();

        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser parser = factory.newPullParser();
        parser.setInput(is, null);
        return parser;
    }

    DatePickerDialog.OnDateSetListener dateSetListenerPB = new DatePickerDialog.OnDateSetListener(){
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            if(correctDate(dayOfMonth, month, year)) {
                privatBankCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                privatBankCalendar.set(Calendar.MONTH, month);
                privatBankCalendar.set(Calendar.YEAR, year);
                String date = assembleDateAsString(dayOfMonth, month, year);
                privatBankDate.setText(date);
                changePrivatBankDataByDate(date);
            }
        }
    };

    DatePickerDialog.OnDateSetListener dateSetListenerNB = new DatePickerDialog.OnDateSetListener(){
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            if(correctDate(dayOfMonth, month, year)) {
                nationalBankCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                nationalBankCalendar.set(Calendar.MONTH, month);
                nationalBankCalendar.set(Calendar.YEAR, year);
                String date = assembleDateAsString(dayOfMonth, month, year);
                nationalBankDate.setText(date);
                changeNationalBankDataByDate(date);
            }
        }
    };

    @Override
    public void onItemClick(String currency) {
        int position = nationalBankAdapter.findItemPositionByCurrency(currency);
        if(position == -1)
            return;

        RecyclerViewNationalBankAdapter.ViewHolder lastCheckedItemHolder =
                (RecyclerViewNationalBankAdapter.ViewHolder)nationalBankExchangeRates.findViewHolderForAdapterPosition(nationalBankAdapter.getLastCheckedItem());
        if(lastCheckedItemHolder != null){

            lastCheckedItemHolder.setUnchecked();
        }
        nationalBankAdapter.setLastCheckedItem(position);
        nationalBankExchangeRates.scrollToPosition(position);
        RecyclerViewNationalBankAdapter.ViewHolder holder =
                (RecyclerViewNationalBankAdapter.ViewHolder)nationalBankExchangeRates.findViewHolderForAdapterPosition(position);
        if(holder != null)
            holder.setChecked(position);
    }
}
