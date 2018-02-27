package com.vikramsingh.seismocardiograph;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

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

    //Boolean to keep track if recording or not
    private boolean recording;

    //Previous x, y, z values
    private float prevXValue = 0.0f;
    private float prevYValue = 0.0f;
    private float prevZValue = 0.0f;

    //The string values for the x, y, z, s and time values
    private StringBuilder dxValues;
    private StringBuilder dyValues;
    private StringBuilder dzValues;
    private StringBuilder dsValues;
    private StringBuilder dtValues;

    //Time for the graph's x-axis
    private long previousTime;
    private float totalTime;

    //Time for the beat detections
    private float beatDetectTime;
    private float beatDetectDelay = 0.3f;

    //Peak Detection variables
    double lPeak;
    double mPeak;
    double rPeak;

    double lastPeak;
    double curPeak;
    double nextPeak;

    int numBeats;

    private enum PhoneOrientation{ flat, landscape, portrait };
    private PhoneOrientation orientation = PhoneOrientation.flat;

    //HeartBeat handler
    Handler heartBeatHandler;

    //Filter class
    private Filter filter;

    public void onRequestPermissionsResult(int resultCode, @NonNull String permissions[], @NonNull int[] grantResults){

        switch(resultCode){

            case 44:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

                }
                break;

        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initialize variables
        previousTime = System.currentTimeMillis();
        totalTime = 0;
        numBeats = 0;
        lPeak = rPeak = mPeak = 0.0f;
        lastPeak = curPeak = nextPeak = 0.0f;
        beatDetectTime = 0.0f;
        recording = false;

        dxValues = new StringBuilder("");
        dyValues = new StringBuilder("");
        dzValues = new StringBuilder("");
        dsValues = new StringBuilder("");
        dtValues = new StringBuilder("");

        filter = new Filter();

        heartBeatHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                int beats = msg.arg1;

                currentBPM.setNumber(beats);

            }
        };

        //Request permissions
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){

            if(!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)){

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 44);

            }

        }

        //Initialize accelerometer
        SensorManager sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor accelerometer = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sm.registerListener(this, accelerometer, 20000);

        //Register onClick Listeners
        findViewById(R.id.bt_main_record).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                recordPressed(v);
            }
        });
        findViewById(R.id.bt_main_save).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                savePressed();
            }
        });
        findViewById(R.id.bt_main_view).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                viewPressed();
            }
        });
        findViewById(R.id.bt_main_phone_flat).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                flatPressed();
            }
        });
        findViewById(R.id.bt_main_phone_portrait).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                portraitPressed();
            }
        });
        findViewById(R.id.bt_main_phone_landscape).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                landscapePressed();
            }
        });

        //Start app in portrait
        portraitPressed();

        //Hide Save Button (Nothing to save)
        Button save = (Button) findViewById(R.id.bt_main_save);
        save.setVisibility(View.INVISIBLE);

        //Setup fragments
        currentBPM = (HeartDisplayFragment) getFragmentManager().findFragmentById(R.id.fr_heart_data_current_bpm);
        maxBPM = (HeartDisplayFragment) getFragmentManager().findFragmentById(R.id.fr_main_max_bpm);
        minBPM = (HeartDisplayFragment) getFragmentManager().findFragmentById(R.id.fr_heart_data_min_bpm);
        averageBPM = (HeartDisplayFragment) getFragmentManager().findFragmentById(R.id.fr_main_average_bpm);

        //Set the descriptions of the fragments
        currentBPM.setDescription("CURRENT");
        maxBPM.setDescription("MAX");
        minBPM.setDescription("MIN");
        averageBPM.setDescription("AVERAGE");

        //Set the icons of the fragments
        currentBPM.setIcon(HeartDisplayFragment.ICON_HEART_CURRENT);
        maxBPM.setIcon(HeartDisplayFragment.ICON_HEART_MAX);
        minBPM.setIcon(HeartDisplayFragment.ICON_HEART_MIN);
        averageBPM.setIcon(HeartDisplayFragment.ICON_HEART_AVERAGE);

        //Sets a TEMPORARY number for each of the heart values
        currentBPM.setNumber(0);
        maxBPM.setNumber(0);
        minBPM.setNumber(0);
        averageBPM.setNumber(0);

        //Setup Graph
        graphX = (GraphView) findViewById(R.id.gr_heart_data_x);
        graphY = (GraphView) findViewById(R.id.gr_heart_data_y);
        graphZ = (GraphView) findViewById(R.id.gr_heart_data_z);
        graphSCG = (GraphView) findViewById(R.id.gr_main_scg);

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
        graphSCG.getGridLabelRenderer().setLabelFormatter(noLabel);

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

        graphSCG.getViewport().setXAxisBoundsManual(true);
        graphSCG.getViewport().setYAxisBoundsManual(true);
        graphSCG.getViewport().setMaxY(0.3f);
        graphSCG.getViewport().setMinY(-0.3f);

        seriesSCG = new LineGraphSeries<>();
        graphSCG.addSeries(seriesSCG);

        graphX.getViewport().setScrollable(true);
        graphY.getViewport().setScrollable(true);
        graphZ.getViewport().setScrollable(true);
        graphSCG.getViewport().setScrollable(true);
        graphSCG.getViewport().setScalable(true);
        graphSCG.getViewport().setScalableY(true);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //offload the series
        graphX.removeAllSeries();
        graphY.removeAllSeries();
        graphZ.removeAllSeries();
        graphSCG.removeAllSeries();

    }

    private void recordPressed(View v){

        Button recordBtn = (Button) v;
        Button saveBtn = (Button) findViewById(R.id.bt_main_save);

        //Swap status
        recording = !recording;

        if(recording){

            //Reset String values
            dxValues = new StringBuilder("");
            dyValues = new StringBuilder("");
            dzValues = new StringBuilder("");
            dsValues = new StringBuilder("");
            dtValues = new StringBuilder("");

            //Reset Previous Time
            previousTime = System.currentTimeMillis();
            totalTime = 0;

            //Reset heart rate
            numBeats = 0;
            currentBPM.setNumber(numBeats);
            maxBPM.setNumber(numBeats);
            minBPM.setNumber(numBeats);
            averageBPM.setNumber(numBeats);

            //Reset peaks
            lPeak = rPeak = mPeak = 0.0f;
            lastPeak = curPeak = nextPeak = 0.0f;

            //Beat detection
            beatDetectTime = 0.0f;

            //Set button's text to "Stop"
            recordBtn.setText(R.string.bt_activity_main_recordText_alternate);

            clearGraphs();

            //Hide Save button
            saveBtn.setVisibility(View.INVISIBLE);
        }else{

            //Set button's text to "Recording"
            recordBtn.setText(R.string.bt_activity_main_recordText);

            //Set the save button visible
            saveBtn.setVisibility(View.VISIBLE);

        }

    }

    private void savePressed(){

        //Check if some data was even recorded
        if(dxValues.length() > 0){

            //Save fileString content = "hello world";
            String state = Environment.getExternalStorageState();
            if (Environment.MEDIA_MOUNTED.equals(state)) {

                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "heartdata.dat");

                try {

                    if(!file.exists())
                        file.createNewFile();

                    FileOutputStream outputStream = new FileOutputStream(file);

                    outputStream.write((dxValues.toString().substring(0, dxValues.toString().length()-1) + "\n").getBytes());
                    outputStream.write((dyValues.toString().substring(0, dyValues.toString().length()-1) + "\n").getBytes());
                    outputStream.write((dzValues.toString().substring(0, dzValues.toString().length()-1) + "\n").getBytes());
                    outputStream.write((dsValues.toString().substring(0, dsValues.toString().length()-1) + "\n").getBytes());
                    outputStream.write((dtValues.toString().substring(0, dtValues.toString().length()-1)).getBytes());

                    outputStream.close();

                    Toast.makeText(this, "Saved Text to File", Toast.LENGTH_SHORT).show();

                    Log.d("SAVED FILE TO: ", file.getAbsolutePath());

                } catch (IOException e) {
                    e.printStackTrace();
                }

                /*
                Intent intent = new Intent(this, SaveActivity.class);
                intent.putExtra("DX_VALUES", dxValues.toString());
                intent.putExtra("DY_VALUES", dyValues.toString());
                intent.putExtra("DZ_VALUES", dzValues.toString());
                intent.putExtra("DS_VALUES", dsValues.toString());
                intent.putExtra("DT_VALUES", dtValues.toString());
                startActivity(intent);
                */

            }else{

                Toast.makeText(this, "Cannot save no permission", Toast.LENGTH_SHORT).show();

            }

        }

    }

    private void viewPressed(){

        Intent intent = new Intent(this, ViewActivity.class);
        startActivity(intent);

    }

    private void flatPressed(){
        orientation = PhoneOrientation.flat;
    }

    private void portraitPressed(){
        orientation = PhoneOrientation.portrait;
    }

    private void landscapePressed(){
        orientation = PhoneOrientation.landscape;
    }

    private void clearGraphs(){

        //Clear the graphs
        graphX.removeAllSeries();
        graphY.removeAllSeries();
        graphZ.removeAllSeries();
        graphSCG.removeAllSeries();

        //Clear the series data
        seriesX = new LineGraphSeries<>();
        seriesY = new LineGraphSeries<>();
        seriesZ = new LineGraphSeries<>();
        seriesSCG = new LineGraphSeries<>();

        //Set series colors
        seriesX.setColor(Color.argb(255, 57, 255, 20));
        seriesY.setColor(Color.argb(255, 57, 255, 20));
        seriesZ.setColor(Color.argb(255, 57, 255, 20));
        seriesSCG.setColor(Color.CYAN);

        //Create new series for the graphs
        graphX.addSeries(seriesX);
        graphY.addSeries(seriesY);
        graphZ.addSeries(seriesZ);
        graphSCG.addSeries(seriesSCG);

        graphX.getViewport().setMinX(0);
        graphY.getViewport().setMinX(0);
        graphZ.getViewport().setMinX(0);
        graphSCG.getViewport().setMinX(0);

    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        //If recording, plot the data
        if(recording) {

            //Gets values
            float xVal = event.values[0];
            float yVal = event.values[1];
            float zVal = event.values[2];

            //Changes in values;
            float dX = xVal - prevXValue;
            float dY = yVal - prevYValue;
            float dZ = zVal - prevZValue;

            //Get current time
            long currentTime = System.currentTimeMillis();

            float dt = (currentTime - previousTime) / 1000.0f;

            totalTime += dt;
            beatDetectTime += dt;
            previousTime = currentTime;

            //How many seconds on the x-axis
            int squeeze = 5;

            //Adjust viewport of the graphs
            graphX.getViewport().setMinX(totalTime - squeeze);
            graphY.getViewport().setMinX(totalTime - squeeze);
            graphZ.getViewport().setMinX(totalTime - squeeze);
            graphSCG.getViewport().setMinX(totalTime - squeeze);

            graphX.getViewport().setMaxX(totalTime);
            graphY.getViewport().setMaxX(totalTime);
            graphZ.getViewport().setMaxX(totalTime);
            graphSCG.getViewport().setMaxX(totalTime);

            //The filtered value
            //BigDecimal dS = filter.getFilteredValue(dSum);

            //Set the peaks values
            //lPeak = mPeak;
            //mPeak = rPeak;
            //rPeak = dS.doubleValue();


            //int timeDelay = 5;

            /*
            //Check if the middle of the peak is greater than its sides
            if ((mPeak > lPeak && mPeak > rPeak) && totalTime > timeDelay) {

                numBeats++;

                Message message = new Message();
                message.arg1 = (int) ((numBeats / (totalTime - timeDelay)) * 60);

                heartBeatHandler.sendMessage(message);

            }

            */

                float cutoff = 0.1f;

                if(Math.abs(dX) < cutoff || orientation != PhoneOrientation.landscape) {

                    dX = 0;

                }else{
                    prevXValue = xVal;
                }

                if(Math.abs(dY) < cutoff || orientation != PhoneOrientation.portrait) {

                    dY = 0;

                }else{
                    prevYValue = yVal;
                }

                if(Math.abs(dZ) < cutoff || orientation != PhoneOrientation.flat) {

                    dZ = 0;

                }else{
                    prevZValue = zVal;
                }

                double dSum = dX + dY + dZ;
                BigDecimal dS = filter.getFilteredValue(dSum);

                //BigDecimal dS = new BigDecimal(dSum);

                lPeak = mPeak;
                mPeak = rPeak;
                rPeak = dS.doubleValue();

                //Check if there is a peak
                if(mPeak > lPeak && mPeak > rPeak && beatDetectTime > beatDetectDelay){


                    lastPeak = curPeak;
                    curPeak = nextPeak;
                    nextPeak = mPeak;

                    if(curPeak > lastPeak && curPeak > nextPeak){

                        //Peak detected
                        beatDetectTime = 0.0f;
                        numBeats++;
                        Message message = new Message();
                        message.arg1 = (int) (numBeats / totalTime * 60);
                        heartBeatHandler.sendMessage(message);

                    }

                }

                //Append the new changes in acceleration
                seriesX.appendData(new DataPoint(totalTime, dX), false, Integer.MAX_VALUE);
                seriesY.appendData(new DataPoint(totalTime, dY), false, Integer.MAX_VALUE);
                seriesZ.appendData(new DataPoint(totalTime, dZ), false, Integer.MAX_VALUE);
                seriesSCG.appendData(new DataPoint(totalTime, dS.doubleValue()), false, Integer.MAX_VALUE);

                //Append the values to the strings
                dxValues.append(dX).append(",");
                dyValues.append(dY).append(",");
                dzValues.append(dZ).append(",");
                dsValues.append(dS.doubleValue()).append(",");
                dtValues.append(totalTime).append(",");

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

}