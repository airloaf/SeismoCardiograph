package com.vikramsingh.seismocardiograph;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class HeartDisplayFragment extends Fragment {

    //Icons
    public static final int ICON_HEART_CURRENT = R.mipmap.heart_bpm;
    public static final int ICON_HEART_MAX = R.mipmap.heart_reg_max;
    public static final int ICON_HEART_MIN = R.mipmap.heart_reg_min;
    public static final int ICON_HEART_AVERAGE = R.mipmap.heart_bpm_average;

    //references to the views
    private ImageView icon;

    private TextView description;
    private TextView number;

    //Create the view
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){

        View view = inflater.inflate(R.layout.fragment_heart, container, false);

        icon = (ImageView) view.findViewById(R.id.iv_fragment_heart_icon);

        description = (TextView) view.findViewById(R.id.tv_fragment_heart_description);
        number = (TextView) view.findViewById(R.id.tv_fragment_heart_number);

        return view;
    }

    public void setDescription(String desc){

        description.setText(desc);

    }

    public void setIcon(int image){

        icon.setImageResource(image);

    }

    public void setNumber(int num){

        number.setText(String.valueOf(num));

    }

}
