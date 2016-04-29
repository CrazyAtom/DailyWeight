package com.mlab.dailyweight;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.util.Pair;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

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
    protected LineChart mChart;
    public DatabaseHelper mDbHelper = null;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_actions, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.input_weight:
                dlgInputWeight();
                break;
//            case R.id.action_settings:
//                ContentValues addRowValue = new ContentValues();
//                addRowValue.put("user_id", "1");
//                addRowValue.put("date", "1");
//                addRowValue.put("weight", "50");
//                mDbHelper.insert(DatabaseHelper.TABLE_WEIGHT, addRowValue);
//                addEntry();
//                break;
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

        String[] columns = new String[]{"date", "weight"};
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
        String[] columns = new String[]{"gender", "height"};
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
        float upper = (float) getBMI(true);
        LimitLine maxBMI = new LimitLine(upper, "BMI (upper limit)");
        maxBMI.setLineWidth(2f);
        maxBMI.enableDashedLine(10f, 10f, 0f);
        maxBMI.setLabelPosition(LimitLabelPosition.RIGHT_TOP);
        maxBMI.setTextSize(10f);

        float lower = (float) getBMI(false);
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
            yVals.add(new Entry((float) weights.get(i).second, i));
        }

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
            }

            // add a new x-value first
            data.addXValue("addValue");
            data.addEntry(new Entry(50, set.getEntryCount()), data.getDataSetCount());

            // let the chart know it's data has changed
            mChart.notifyDataSetChanged();
            mChart.moveViewTo(data.getXValCount() - 7, 50f, YAxis.AxisDependency.LEFT);
        }
    }

    private void dlgInputWeight() {
        // 레이아웃 xml 리소스 파일을 view 객체로 변환 시켜주는 inflater객체
        LayoutInflater inflater = getLayoutInflater();

        // xml 파일을 이용하여 view 객체 생성
        final View dlgView = inflater.inflate(R.layout.input_weight, null);

        //AlertDialog.Builder 객체 생성
        AlertDialog.Builder buider = new AlertDialog.Builder(this);
        buider.setTitle("Input Weight");    // 제목
        buider.setIcon(android.R.drawable.ic_menu_add); // 제목 표시줄 아이콘
        buider.setView(dlgView);

        // 버튼 설정
        buider.setPositiveButton("INPUT", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getApplicationContext(), "Input", Toast.LENGTH_SHORT).show();

                DatePicker datePicker = (DatePicker) dlgView.findViewById(R.id.datepicker);
                int year = datePicker.getYear();
                int month = datePicker.getMonth() + 1;
                int day = datePicker.getDayOfMonth();
                String date = year + "-" + month + "-" + day;

                EditText editWeight = (EditText) dlgView.findViewById(R.id.editWeight);
                String weigh = editWeight.getText().toString();

                ContentValues addRowValue = new ContentValues();
                addRowValue.put("user_id", "1");
                addRowValue.put("date", date);
                addRowValue.put("weight", weigh);
                mDbHelper.insert(DatabaseHelper.TABLE_WEIGHT, addRowValue);

                setChartByPastWeight();
            }
        });
        buider.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getApplicationContext(), "Cancel", Toast.LENGTH_SHORT).show();
            }
        });

        // 다이얼로그 생성
        AlertDialog dlg = buider.create();
        dlg.setCanceledOnTouchOutside(false);   // 다이얼로그 밖을 선택했을때 다이얼로그가 사라지지 않도록
        dlg.show();
    }
}

