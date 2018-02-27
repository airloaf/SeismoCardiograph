package com.vikramsingh.seismocardiograph;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

public class ViewActivity extends AppCompatActivity {

    //All the files
    private ArrayList<File> files;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view);

        files = new ArrayList<>();

        //Adds the files to the file array list
        addFiles();

        //Adds the files to the adapters
        addAdapter();


    }

    private void addFiles(){

        for(File f : getFilesDir().listFiles()){

            //Check if the file ends with dat
            if(f.getName().endsWith(".dat")){

                //Adds the file to the array list
                files.add(f);

            }

        }

    }

    private void addAdapter(){

        //array of the item's file names
        String items[] = new String[files.size()];

        //adds the files to an array
        for(int i =  0; i < files.size(); i++){

            items[i] = files.get(i).getName().toString();

        }

        //The adapter
        final ListAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items);

        //Get the view and add the adapter
        ListView lv = (ListView) findViewById(R.id.lv_view_files);
        lv.setAdapter(adapter);

        //sets the listeners
        lv.setOnItemClickListener(new OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String fileName = String.valueOf(parent.getItemAtPosition(position));
                openFile(fileName);
            }
        });

    }

    private void openFile(String fileName){

        Intent intent = new Intent(this, ReplayActivity.class);
        intent.putExtra("FILE_NAME", fileName);
        startActivity(intent);

    }

}
