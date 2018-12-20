package com.click369.controlbp.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.click369.controlbp.activity.CPUSetView;

public class CPUSetFragment extends BaseFragment {

//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.fragment_cpu);
//        initView();
//        setTitle("CPU设置");
//    }
    public CPUSetFragment() {
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
//        View v = inflater.inflate(R.layout.fragment_cpu, container, false);
//        initView(v);
        CPUSetView v = new CPUSetView();
        return v.init(getContext());
    }


}
