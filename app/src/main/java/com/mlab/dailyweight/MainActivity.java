package com.mlab.dailyweight;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.util.Pair;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.LimitLine.LimitLabelPosition;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.XAxis.XAxisPosition;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.components.YAxis.YAxisLabelPosition;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.FillFormatter;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import com.mlab.dailyweight.DatabaseHelper;

import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends ActionBarActivity {

    private static final int INPUT_WEIGHT  = 1;
    private static final int INPUT_2  = 2;
    private static final int INPUT_3  = 3;

    protected LineChart mChart;
    public DatabaseHelper mDbHelper = null;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
        menu.add(0, INPUT_WEIGHT, 0, R.string.input_weight);
        menu.add(0, INPUT_2, 0, "test2");
        menu.add(0, INPUT_3, 0, "test3");
        getMenuInflater().inflate(R.menu.action_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

//    @Override
//    public boolean onPrepareOptionsMenu(Menu menu) {
//        return super.onPrepareOptionsMenu(menu);
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case INPUT_WEIGHT :
                dlgInputWeight();
                break;
            case INPUT_2 :
                ContentValues addRowValue = new ContentValues();
                addRowValue.put("user_id", "1");
                addRowValue.put("date", "1");
                addRowValue.put("weight", "50");
                mDbHelper.insert(DatabaseHelper.TABLE_WEIGHT, addRowValue);
                addEntry();
                break;
            case INPUT_3 :
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDbHelper = DatabaseHelper.getInstance(this);

        mChart = (LineChart) findViewById(R.id.chart);
        initChart();
    }

    @Override
    protected void onResume() {
        super.onResume();

        setChartByBMI();
        setChartByPastWeight();
    }

    protected void initChart() {
        mChart.setDrawGridBackground(false);
        mChart.setDescription("");
        mChart.setNoDataTextDescription("You need to provide data for the chart.");

        mChart.setTouchEnabled(true);
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setPinchZoom(true);
//        mChart.setVisibleXRangeMaximum(6);
        mChart.setVisibleYRangeMaximum(15, YAxis.AxisDependency.LEFT);

        XAxis xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxisPosition.BOTTOM);
        xAxis.setTextSize(6);

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setAxisMaxValue(110f);
        leftAxis.setAxisMinValue(30f);
        leftAxis.enableGridDashedLine(10f, 10f, 0f);
        leftAxis.setDrawZeroLine(false);
        xAxis.setTextSize(6);

        mChart.getAxisRight().setEnabled(false);
    }

    private ArrayList<Pair<String, Float>> getPastWeight() {

        ArrayList<Pair<String, Float>> weights = new ArrayList<Pair<String, Float>>();

        String[] columns = new String[] { "date", "weight" };
        Cursor c = mDbHelper.query(DatabaseHelper.TABLE_WEIGHT, columns,
                null, null, null, null, null);

        while (c.moveToNext()) {
            String date = c.getString(0);
            float weight = c.getFloat(1);
            weights.add(Pair.create(date, weight));
        }

        return weights;
    }

    private double getBMI(boolean bMax) {
        String[] columns = new String[] { "gender", "height" };
        Cursor c = mDbHelper.query(DatabaseHelper.TABLE_USERINFO, columns,
                null, null, null, null, null);
        c.moveToNext();
        if (c != null) {
            int gender = c.getInt(0);
            double height = c.getDouble(1) / 100f;

            if (bMax == false) {
                return height * height * 18.5f;
            } else {
                return height * height * 23f;
            }
        }

        return 0f;
    }

    private void setChartByBMI() {
        float upper = (float)getBMI(true);
        LimitLine maxBMI = new LimitLine(upper, "BMI (upper limit)");
        maxBMI.setLineWidth(2f);
        maxBMI.enableDashedLine(10f, 10f, 0f);
        maxBMI.setLabelPosition(LimitLabelPosition.RIGHT_TOP);
        maxBMI.setTextSize(10f);

        float lower = (float)getBMI(false);
        LimitLine lowBMI = new LimitLine(lower, "BMI (lower limit)");
        lowBMI.setLineWidth(2f);
        lowBMI.enableDashedLine(10f, 10f, 0f);
        lowBMI.setLabelPosition(LimitLabelPosition.RIGHT_TOP);
        lowBMI.setTextSize(10f);

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.removeAllLimitLines(); // reset all limit lines to avoid overlapping lines
        leftAxis.addLimitLine(maxBMI);
        leftAxis.addLimitLine(lowBMI);

        // limit lines are drawn behind data (and not on top)
        leftAxis.setDrawLimitLinesBehindData(true);
    }
    
    private void setChartByPastWeight() {
        ArrayList<Pair<String, Float>> weights = getPastWeight();
        int cnt = weights.size();

        ArrayList<String> xVals = new ArrayList<String>();
        for (int i = 0; i < cnt; i++) {
            xVals.add(weights.get(i).first);
        }

        ArrayList<Entry> yVals = new ArrayList<Entry>();
        for (int i = 0; i < cnt; i++) {
            yVals.add(new Entry((float)weights.get(i).second, i));
        }
        
//        LineDataSet set = createSet();
        LineDataSet set = new LineDataSet(yVals, "daily weight");
        set.enableDashedLine(10f, 5f, 0f);
        set.enableDashedHighlightLine(10f, 5f, 0f);
        set.setDrawCubic(true);
        set.setCubicIntensity(0.2f);
        set.setColor(Color.BLACK);
        set.setLineWidth(1f);
        set.setCircleColor(Color.BLACK);
        set.setCircleRadius(3f);
//        set.setDrawCircleHole(false);
        set.setValueTextSize(9f);
        set.setDrawFilled(true);
        set.setFillColor(Color.BLACK);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setFillAlpha(50);
        set.setDrawHorizontalHighlightIndicator(false);

        ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(set); // add the datasets
        
        LineData data = new LineData(xVals, dataSets);
        data.setValueTextSize(10f);
        
        mChart.setData(data);
        mChart.animateX(2000);
    }

    private void addEntry() {

        LineData data = mChart.getData();

        if (data != null) {

            ILineDataSet set = data.getDataSetByIndex(0);

            if (set == null) {
                return;
//                set = createSet();
//                data.addDataSet(set);
            }

            // add a new x-value first
            data.addXValue("addValue");
            data.addEntry(new Entry(50, set.getEntryCount()), data.getDataSetCount());

            // let the chart know it's data has changed
            mChart.notifyDataSetChanged();
            mChart.moveViewTo(data.getXValCount() - 7, 50f, YAxis.AxisDependency.LEFT);
        }
    }

//    private LineDataSet createSet() {
//        LineDataSet set = new LineDataSet(yVals, "daily weight");
//        set.enableDashedLine(10f, 5f, 0f);
//        set.enableDashedHighlightLine(10f, 5f, 0f);
//        set.setDrawCubic(true);
//        set.setCubicIntensity(0.2f);
//        set.setColor(Color.BLACK);
//        set.setLineWidth(1f);
//        set.setCircleColor(Color.BLACK);
//        set.setCircleRadius(3f);
////        set.setDrawCircleHole(false);
//        set.setValueTextSize(9f);
//        set.setDrawFilled(true);
//        set.setFillColor(Color.BLACK);
//        set.setAxisDependency(YAxis.AxisDependency.LEFT);
//        set.setFillAlpha(50);
//        set.setDrawHorizontalHighlightIndicator(false);
//
//        return set;
//    }

    private void dlgInputWeight() {
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int date = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                String date = year + "년 " + (monthOfYear + 1) + "월 " + dayOfMonth + "일";
            }
        };

        DatePickerDialog alert = new DatePickerDialog(this, mDateSetListener, year, month, date);
        alert.show();
    }
}
