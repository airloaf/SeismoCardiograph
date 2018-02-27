package com.vikramsingh.seismocardiograph;

import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

public class HeartDataFragment extends Fragment {

    //Graphs
    private GraphView graphX;
    private GraphView graphY;
    private GraphView graphZ;
    private GraphView graphS;

    //Series
    private LineGraphSeries<DataPoint> seriesX;
    private LineGraphSeries<DataPoint> seriesY;
    private LineGraphSeries<DataPoint> seriesZ;
    private LineGraphSeries<DataPoint> seriesS;

    //HeartDisplayFragments
    HeartDisplayFragment currentBPM;
    HeartDisplayFragment maxBPM;
    HeartDisplayFragment minBPM;
    HeartDisplayFragment averageBPM;

    private double totalTime;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){

        View view = inflater.inflate(R.layout.fragment_heart, container, false);

        //initialize variables
        totalTime = 0.0;

        //Get Graph references
        graphX = (GraphView) view.findViewById(R.id.gr_heart_data_x);
        graphY = (GraphView) view.findViewById(R.id.gr_heart_data_y);
        graphZ = (GraphView) view.findViewById(R.id.gr_heart_data_z);
        graphS = (GraphView) view.findViewById(R.id.gr_heart_data_scg);

        //Instantiates new series
        seriesX = new LineGraphSeries<>();
        seriesY = new LineGraphSeries<>();
        seriesZ = new LineGraphSeries<>();
        seriesS = new LineGraphSeries<>();

        //Get display fragment references
        currentBPM = (HeartDisplayFragment) getFragmentManager().findFragmentById(R.id.fr_heart_data_current_bpm);
        averageBPM = (HeartDisplayFragment) getFragmentManager().findFragmentById(R.id.fr_heart_data_average_bpm);
        minBPM = (HeartDisplayFragment) getFragmentManager().findFragmentById(R.id.fr_heart_data_min_bpm);
        maxBPM = (HeartDisplayFragment) getFragmentManager().findFragmentById(R.id.fr_heart_data_max_bpm);

        //setup graph properties

        //Removes numbers from the axis
        DefaultLabelFormatter noLabel = new DefaultLabelFormatter(){
            public String formatLabel(double value, boolean isValueX) {
                return "";
            }
        };

        //Set no label
        graphX.getGridLabelRenderer().setLabelFormatter(noLabel);
        graphY.getGridLabelRenderer().setLabelFormatter(noLabel);
        graphZ.getGridLabelRenderer().setLabelFormatter(noLabel);
        graphS.getGridLabelRenderer().setLabelFormatter(noLabel);

        //Set viewport settings
        graphX.getViewport().setXAxisBoundsManual(true);
        graphX.getViewport().setYAxisBoundsManual(true);
        float range = 0.5f;
        graphX.getViewport().setMaxY(range);
        graphX.getViewport().setMinY(-range);

        seriesX = new LineGraphSeries<>();
        graphX.addSeries(seriesX);

        graphY.getViewport().setXAxisBoundsManual(true);
        graphY.getViewport().setYAxisBoundsManual(true);
        graphY.getViewport().setMaxY(range);
        graphY.getViewport().setMinY(-range);

        seriesY = new LineGraphSeries<>();
        graphY.addSeries(seriesY);

        graphZ.getViewport().setXAxisBoundsManual(true);
        graphZ.getViewport().setYAxisBoundsManual(true);
        graphZ.getViewport().setMaxY(range);
        graphZ.getViewport().setMinY(-range);

        seriesZ = new LineGraphSeries<>();
        graphZ.addSeries(seriesZ);

        graphS.getViewport().setXAxisBoundsManual(true);
        graphS.getViewport().setYAxisBoundsManual(true);
        graphS.getViewport().setMaxY(0.15f);
        graphS.getViewport().setMinY(0.0f);

        seriesS = new LineGraphSeries<>();
        graphS.addSeries(seriesS);

        graphX.getViewport().setScrollable(true);
        graphY.getViewport().setScrollable(true);
        graphZ.getViewport().setScrollable(true);
        graphS.getViewport().setScrollable(true);
        graphS.getViewport().setScalable(true);
        graphS.getViewport().setScalableY(true);

        return view;
    }

    public void clearGraphs(){

        graphX.removeAllSeries();
        graphY.removeAllSeries();
        graphZ.removeAllSeries();
        graphS.removeAllSeries();

        seriesX = new LineGraphSeries<>();
        seriesY = new LineGraphSeries<>();
        seriesZ = new LineGraphSeries<>();
        seriesS = new LineGraphSeries<>();

    }

    public void newPoint(double x, double y, double z, double s, double dt){

        //add the change in time
        totalTime += dt;

        int squeeze = 20;

        graphX.getViewport().setMinX(totalTime - squeeze);
        graphY.getViewport().setMinX(totalTime - squeeze);
        graphZ.getViewport().setMinX(totalTime - squeeze);
        graphS.getViewport().setMinX(totalTime - squeeze);

        graphX.getViewport().setMaxX(totalTime);
        graphY.getViewport().setMaxX(totalTime);
        graphZ.getViewport().setMaxX(totalTime);
        graphS.getViewport().setMaxX(totalTime);

        seriesX.appendData(new DataPoint(totalTime, x), false, Integer.MAX_VALUE);
        seriesX.appendData(new DataPoint(totalTime, y), false, Integer.MAX_VALUE);
        seriesX.appendData(new DataPoint(totalTime, z), false, Integer.MAX_VALUE);
        seriesX.appendData(new DataPoint(totalTime, s), false, Integer.MAX_VALUE);

    }

}
