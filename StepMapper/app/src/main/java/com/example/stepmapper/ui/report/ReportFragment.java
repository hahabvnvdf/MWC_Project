package com.example.stepmapper.ui.report;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.anychart.APIlib;
import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Cartesian;
import com.anychart.core.Chart;
import com.anychart.core.cartesian.series.Column;
import com.anychart.data.Mapping;
import com.anychart.data.Set;
import com.anychart.enums.Anchor;
import com.anychart.enums.HoverMode;
import com.anychart.enums.Position;
import com.anychart.enums.TooltipPositionMode;
import com.example.stepmapper.FirebaseDatabaseHelper;
import com.example.stepmapper.R;
import com.example.stepmapper.StepAppOpenHelper;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.firebase.database.FirebaseDatabase;

import android.os.Build;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ReportFragment extends Fragment {

    public Context context;
    AnyChartView anyChartView;
    AnyChartView anyChartView1;

    public static boolean isStepsByHourCheck() {
        return stepsByHourCheck;
    }

    public static void setStepsByHourCheck() {
        ReportFragment.stepsByHourCheck = true;
    }

    private static boolean stepsByHourCheck = false;

    Date cDate = new Date();
    String current_time = new SimpleDateFormat("yyyy-MM-dd").format(cDate);

    public static void setStepsByHour(Map<Integer, Integer> stepsByHour) {
        ReportFragment.stepsByHour.putAll(stepsByHour);
    }

    public static Map<Integer, Integer> getStepsByHour() {
        return stepsByHour;
    }

    public static Map<Integer, Integer> stepsByHour = new HashMap<>();

    public static Map<String, Integer> getStepsByDay() {
        return stepsByDay;
    }

    public static void setStepsByDay(Map<String, Integer> stepsByDay) {
        ReportFragment.stepsByDay.putAll(stepsByDay);
    }

    public static Map<String, Integer> stepsByDay = new HashMap<>();


    public View onCreateView(@NonNull LayoutInflater inflater,
                             final ViewGroup container, Bundle savedInstanceState) {
        if (container != null) {
            container.removeAllViews();
        }
        final View root = inflater.inflate(R.layout.fragment_report, container, false);
        FirebaseDatabaseHelper.loadStepsByDay();
        //set hourly chart as default when report fragment is loaded
        FirebaseDatabaseHelper.loadStepsByHour(current_time);
        anyChartView1 = root.findViewById(R.id.dayBarChart);
        anyChartView = root.findViewById(R.id.hourBarChart);
        APIlib.getInstance().setActiveAnyChartView(anyChartView);
        anyChartView.setProgressBar(root.findViewById(R.id.loadingBar));
        Cartesian cartesian = null;
        cartesian = createColumnChart();
        anyChartView.setChart(cartesian);
        anyChartView1.setVisibility(View.GONE);
        anyChartView.setBackgroundColor("#00000000");
        anyChartView.setVisibility(View.VISIBLE);


        final Button button_Hour = root.findViewById(R.id.button_Hourly);
        button_Hour.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v1) {
                APIlib.getInstance().setActiveAnyChartView(anyChartView);
                anyChartView.setProgressBar(root.findViewById(R.id.loadingBar));
                Cartesian cartesian = null;
                cartesian = createColumnChart();
                anyChartView.setChart(cartesian);
                anyChartView1.setVisibility(View.GONE);
                anyChartView.setBackgroundColor("#00000000");
                anyChartView.setVisibility(View.VISIBLE);
            }
        });

        Button button_Week = root.findViewById(R.id.button_Weekly);
        button_Week.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v1) {
                APIlib.getInstance().setActiveAnyChartView(anyChartView1);
                anyChartView1.setProgressBar(root.findViewById(R.id.loadingBar));
                Cartesian cartesian = null;
                try {
                    cartesian = createBarChart();

                } catch (ParseException e) {
                    e.printStackTrace();
                }
                anyChartView1.setChart(cartesian);
                anyChartView.setVisibility(View.GONE);
                anyChartView1.setBackgroundColor("#00000000");
                anyChartView1.setVisibility(View.VISIBLE);
            }

        });

        return root;
    }

    public Cartesian createColumnChart(){

        Map<Integer, Integer> graph_map = new TreeMap<>();
        int maxStep = 0;
        for(int i =0; i <24; i++){
            int newStep;
            if(stepsByHour.get(i) != null){
                if(i > 0 && stepsByHour.get(i) > 0) {
                    newStep = stepsByHour.get(i) - maxStep;
                    if(maxStep <= stepsByHour.get(i)){
                        maxStep = stepsByHour.get(i);
                    }
                    graph_map.put(i, newStep);
                }
            }else{
                graph_map.put(i,0);
            }
        }

        Cartesian cartesian = AnyChart.column();
        List<DataEntry> data = new ArrayList<>();

        for (Map.Entry<Integer,Integer> entry : graph_map.entrySet())
            data.add(new ValueDataEntry(entry.getKey(), entry.getValue()));

        Column column = cartesian.column(data);
        column.fill("#1258DC");
        column.stroke("#1258DC");

        column.tooltip()
                .titleFormat("At hour: {%X}")
                .format("{%Value}{groupsSeparator: } Steps")
                .anchor(Anchor.RIGHT_TOP);

        column.tooltip()
                .position(Position.RIGHT_TOP)
                .offsetX(0d)
                .offsetY(10);

        cartesian.tooltip().positionMode(TooltipPositionMode.POINT);
        cartesian.interactivity().hoverMode(HoverMode.BY_X);
        cartesian.yScale().minimum(0);
        cartesian.yAxis(0).title("Number of Steps");
        cartesian.xAxis(0).title("Hour");
        cartesian.background().fill("#00000000");
        cartesian.animation(true);
        return cartesian;
    }

    public Cartesian createBarChart() throws ParseException {
        Map<String, Integer> graph_map = new TreeMap<>();

        Date startDate = new Date(cDate.getTime() - 518400000L);
        String d = new SimpleDateFormat("yyyy-MM-dd").format(startDate);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c = Calendar.getInstance();
        for(int i = 1; i <7; i ++) {
            c.setTime(sdf.parse(d));
            c.add (Calendar.DATE, 1);  // number of days to add
            d = sdf.format(c.getTime());
            if(stepsByDay.containsKey(d)){
                Log.d("Heree: "+d, String.valueOf(stepsByDay.get(d)));
                graph_map.put(d,stepsByDay.get(d));
            }else{
                graph_map.put(d,0);
            }
        }
        // 1. Create and get the cartesian coordinate system for bar chart
        Cartesian cartesian1 = AnyChart.column();

        // 2. Create data entries for x and y axis of the graph
        List<DataEntry> data = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : graph_map.entrySet())
            data.add(new ValueDataEntry(entry.getKey(), entry.getValue()));

        Column column = cartesian1.column(data);

        column.fill("#1258DC");

        column.stroke("#1258DC");

        column.tooltip()
                .titleFormat("At day: {%X}")
                .format("{%Value}{groupsSeparator: } Steps")
                .anchor(Anchor.RIGHT_TOP);

        column.tooltip()
                .position(Position.RIGHT_TOP)
                .offsetX(0d)
                .offsetY(5);

        column.labels()
                .textOverflow();

        cartesian1.xAxis(0).labels().rotation(270);
        cartesian1.tooltip().positionMode(TooltipPositionMode.POINT);
        cartesian1.interactivity().hoverMode(HoverMode.BY_X);
        cartesian1.yScale().minimum(0);
        cartesian1.yAxis(0).title("Number of Steps");
        cartesian1.xAxis(0).title("Day");
        cartesian1.background().fill("#00000000");
        cartesian1.animation(true);
        return cartesian1;
    }
}