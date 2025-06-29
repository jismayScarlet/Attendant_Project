package com.example.attendant_project.fragment_manager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.attendant_project.R;

public class FrontPageFragment extends Fragment {
    public FrontPageFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState){
        return inflater.inflate(R.layout.front_page,container,false);
    }
}
