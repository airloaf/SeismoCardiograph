package com.vikramsingh.seismocardiograph;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SaveActivity extends AppCompatActivity {

    private String x;
    private String y;
    private String z;
    private String s;
    private String t;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save);

        //Get intent
        Intent intent = getIntent();

        //Get Strings
        x = intent.getStringExtra("DX_VALUES");
        y = intent.getStringExtra("DY_VALUES");
        z = intent.getStringExtra("DZ_VALUES");
        s = intent.getStringExtra("DS_VALUES");
        t = intent.getStringExtra("DT_VALUES");

        //Set on click listener
        findViewById(R.id.bt_save_save).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                saveClicked(v);
            }
        });

    }

    private void saveClicked(View v){

        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        Date d = new Date();

        String name;
        String description;
        String date = dateFormat.format(d);

        EditText nameText = (EditText) findViewById(R.id.et_save_name);
        EditText descriptionText = (EditText) findViewById(R.id.et_save_description);

        name = nameText.getText().toString();
        description = descriptionText.getText().toString();

        File file = new File(getFilesDir(), String.valueOf(System.nanoTime()) + ".dat");

        try {

            FileOutputStream outputStream = new FileOutputStream(file);

            outputStream.write((name + "\n").getBytes());
            outputStream.write((date + "\n").getBytes());
            outputStream.write((description + "\n").getBytes());
            outputStream.write((x.substring(0, x.length() - 1) + "\n").getBytes());
            outputStream.write((y.substring(0, y.length() - 1) + "\n").getBytes());
            outputStream.write((z.substring(0, z.length() - 1) + "\n").getBytes());
            outputStream.write((s.substring(0, s.length() - 1) + "\n").getBytes());
            outputStream.write((t.substring(0, t.length() - 1)).getBytes());

            outputStream.close();

            Toast.makeText(this, file.getAbsolutePath(), Toast.LENGTH_SHORT).show();

        }catch(Exception e){
            e.printStackTrace();
            Toast.makeText(this, "Unable to save", Toast.LENGTH_SHORT).show();
        }

        finish();

    }

}
