package com.vikramsingh.seismocardiograph;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

public class ReplayActivity extends AppCompatActivity {

    //File Name
    private String fileName;

    //Graphs and Series
    private GraphView graphX;
    private GraphView graphY;
    private GraphView graphZ;
    private GraphView graphSCG;

    private LineGraphSeries<DataPoint> seriesX;
    private LineGraphSeries<DataPoint> seriesY;
    private LineGraphSeries<DataPoint> seriesZ;
    private LineGraphSeries<DataPoint> seriesSCG;

    //Heart Display Fragments
    private HeartDisplayFragment currentBPM;
    private HeartDisplayFragment maxBPM;
    private HeartDisplayFragment minBPM;
    private HeartDisplayFragment averageBPM;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_replay);

        //Intent i = getIntent();
        //fileName = i.getStringExtra("FILE_NAME");



    }

    public void init(){



    }

}
