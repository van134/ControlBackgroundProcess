package com.click369.controlbp.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.click369.controlbp.activity.BaseActivity;
import com.click369.controlbp.util.AppLoaderUtil;
import com.click369.controlbp.util.GCUtil;

/**
 * Created by 41856 on 2018/12/3.
 */

public class BaseFragment  extends Fragment {
    private Handler h = new Handler();
//    private BaseAdapter adapter;
    public AppLoaderUtil appLoader;
//    public void setAdapter(BaseAdapter adapter){
//        this.adapter = adapter;
//    }
    public void fresh(){
//        if(adapter!=null){
//            adapter.notifyDataSetChanged();
//        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        appLoader = AppLoaderUtil.getInstance(getActivity().getApplicationContext());
        return super.onCreateView(inflater, container, savedInstanceState);
    }

//    public void setlistenerScroll(int sortType,Class cls,ListView listView){
//        if(sortType==-1){
//            BaseActivity.scrollyTag = cls.getName();
//        }else{
//            BaseActivity.scrollyTag = "";
//        }
//    }
    public void loadY(final ListView listView,final Class cls,final int sortType){
        listView.setTag(this.getClass().getName());
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                int p = BaseActivity.mfirstVisibleItem;
                if((BaseActivity.scrollyTag.equals(RecentFragment.class.getName())||
                        BaseActivity.scrollyTag.equals(AppStartFragment.class.getName())||
                        BaseActivity.scrollyTag.equals(IceUnstallFragment.class.getName()))&&
                        (!cls.getName().equals(RecentFragment.class.getName())&&
                                !cls.getName().equals(AppStartFragment.class.getName())&&
                                !cls.getName().equals(IceUnstallFragment.class.getName()))){
                    if(p!=0){
                        p = p -1;
                    }
                }else if((cls.getName().equals(RecentFragment.class.getName())||
                        cls.getName().equals(AppStartFragment.class.getName())||
                        cls.getName().equals(IceUnstallFragment.class.getName()))&&
                        (!BaseActivity.scrollyTag.equals(RecentFragment.class.getName())&&
                                !BaseActivity.scrollyTag.equals(AppStartFragment.class.getName())&&
                                !BaseActivity.scrollyTag.equals(IceUnstallFragment.class.getName()))){
                    if(p!=0){
                        p = p +1;
                    }
                }
                if(sortType==-1){
                    listView.setSelection(p);
                    BaseActivity.scrollyTag = cls.getName();
                }else{
                    BaseActivity.scrollyTag = "";
                }
            }
        },300);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
//        View view = getView();
//        GCUtil.startGC(view,true);
//        GCUtil.unbindDrawables(view);
    }


}
