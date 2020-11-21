package com.example.stepmapper.ui.report;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import com.example.stepmapper.R;
import com.example.stepmapper.StepAppOpenHelper;
import com.google.android.material.button.MaterialButtonToggleGroup;
import android.os.Build;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ReportFragment extends Fragment {
    public Context context;
    AnyChartView anyChartView;
    AnyChartView anyChartView1;

    Date cDate = new Date();
    String current_time = new SimpleDateFormat("yyyy-MM-dd").format(cDate);

    public Map<Integer, Integer> stepsByHour = null;
    public Map<String, Integer> stepsByDay = null;

    private Set set;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             final ViewGroup container, Bundle savedInstanceState) {
        if (container != null) {
            container.removeAllViews();
        }
        final View root = inflater.inflate(R.layout.fragment_report, container, false);
        anyChartView = root.findViewById(R.id.hourBarChart);

        Button button_Hour = root.findViewById(R.id.button_Hourly);
        button_Hour.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v1) {
                Toast.makeText(getContext(), "Hourly button clicked", Toast.LENGTH_SHORT).show();
                anyChartView.getDrawableState();
                anyChartView.setProgressBar(root.findViewById(R.id.loadingBar));
                Cartesian cartesian = createColumnChart();
                anyChartView.setBackgroundColor("#00000000");
                anyChartView.setChart(cartesian);
                //anyChartView.refreshDrawableState();
            }
        });

        Button button_Week = root.findViewById(R.id.button_Weekly);
        button_Week.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v1) {

                //APIlib.getInstance().setActiveAnyChartView(anyChartView1);
                Toast.makeText(getContext(), "Weekly button clicked", Toast.LENGTH_SHORT).show();
                anyChartView.setProgressBar(root.findViewById(R.id.loadingBar));
                anyChartView.getDrawableState();
                Cartesian cartesian = null;
                try {
                    cartesian = createBarChart();

                } catch (ParseException e) {
                    e.printStackTrace();
                }
                anyChartView.setBackgroundColor("#00000000");
                anyChartView.setChart(cartesian);
                //anyChartView.refreshDrawableState();

            }

        });

        return root;
    }

    public Cartesian createColumnChart(){
                stepsByHour = StepAppOpenHelper.loadStepsByHour(getContext(), current_time);
                Map<Integer, Integer> graph_map = new TreeMap<>();
                for(int i =0; i <24; i++){
                    graph_map.put(i,0);
                }
                graph_map.putAll(stepsByHour);
                Cartesian cartesian = AnyChart.column();
                List<DataEntry> data = new ArrayList<>();

                for (Map.Entry<Integer,Integer> entry : graph_map.entrySet())
                    data.add(new ValueDataEntry(entry.getKey(), entry.getValue()));

                Column column = cartesian.column(data);
                //column.fill("#55F2B9");
                column.fill("function() {" +
                        "            if (this.value < 20)" +
                        "                return 'yellow';" +
                        "            return '#55F2B9';" +
                        "        }");
                //column.stroke("#55F2B9");
                column.stroke("function() {" +
                        "            if (this.value < 20)" +
                        "                return 'yellow';" +
                        "            return '#55F2B9';" +
                        "        }");
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

        stepsByDay = StepAppOpenHelper.loadStepsByDay(getContext());
        Toast.makeText(getContext(), "create bar chat called", Toast.LENGTH_SHORT).show();
        Map<String, Integer> graph_map = new TreeMap<>();

        Date startDate = new Date(cDate.getTime() - 518400000L);
        String d = new SimpleDateFormat("yyyy-MM-dd").format(startDate);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c = Calendar.getInstance();
        for(int i = 1; i <7; i ++) {
            graph_map.put(d,0);
            c.setTime(sdf.parse(d));
            c.add (Calendar.DATE, 1);  // number of days to add
            d = sdf.format(c.getTime());
        }

        graph_map.putAll(stepsByDay);

        // 1. Create and get the cartesian coordinate system for bar chart
        Cartesian cartesian1 = AnyChart.column();

        // 2. Create data entries for x and y axis of the graph
        List<DataEntry> data = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : graph_map.entrySet())
            data.add(new ValueDataEntry(entry.getKey(), entry.getValue()));

        Column column = cartesian1.column(data);

        column.fill("function() {" +
                        "            if (this.value < 100)" +
                        "                return 'yellow';" +
                        "            return '#55F2B9';" +
                        "        }");

        column.stroke("function() {" +
                        "            if (this.value < 100)" +
                        "                return 'yellow';" +
                        "            return '#55F2B9';" +
                        "        }");

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