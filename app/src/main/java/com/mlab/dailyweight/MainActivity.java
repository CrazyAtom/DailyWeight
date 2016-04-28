package com.mlab.dailyweight;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;

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
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import com.mlab.dailyweight.DatabaseHelper;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    protected LineChart mChart;
    public DatabaseHelper mDbHelper = null;

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

        XAxis xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxisPosition.BOTTOM);
        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setAxisMaxValue(110f);
        leftAxis.setAxisMinValue(0f);
        leftAxis.enableGridDashedLine(10f, 10f, 0f);
        leftAxis.setDrawZeroLine(false);

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
        
        LineDataSet set = new LineDataSet(yVals, "daily weight");
//        set.enableDashedLine(10f, 5f, 0f);
//        set.enableDashedHighlightLine(10f, 5f, 0f);
        set.setColor(Color.BLACK);
        set.setCircleColor(Color.BLACK);
        set.setLineWidth(1f);
        set.setCircleRadius(3f);
        set.setDrawCircleHole(false);
        set.setValueTextSize(9f);
        set.setDrawFilled(true);
        set.setFillColor(Color.BLACK);

        ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(set); // add the datasets
        
        LineData data = new LineData(xVals, dataSets);
        data.setValueTextSize(10f);
        
        mChart.setData(data);
    }
}
