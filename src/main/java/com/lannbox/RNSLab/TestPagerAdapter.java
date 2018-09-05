package com.lannbox.RNSLab;

import android.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.View;


public class TestPagerAdapter extends FragmentPagerAdapter {


    public TestPagerAdapter(FragmentManager fm) {
        super(fm);
    }


    @Override
    public Fragment getItem(int position){
        switch (position){
            case 0:
                return Temp.newInstance("t","m");
            case 1:
                return Humi.newInstance("t","m");
            case 2:
                return Dust.newInstance("t","m");
            case 3:
                return Tvoc.newInstance("t","m");
            case 4:
                return Co2.newInstance("t","m");
            case 5:
                return Irt.newInstance("t","m");
            default:
                return null;
        }
    }

    private static int PAGE_NUMBER = 6;
    @Override
    public int getCount(){
        return PAGE_NUMBER;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return false;
    }

    @Override
    public CharSequence getPageTitle(int position){
        switch (position){
            case 0:
                return "온도";
            case 1:
                return "습도";
            case 2:
                return "먼지";
            case 3:
                return "TVOC";
            case 4:
                return "CO2";
            case 5:
                return "IR Temp";
            default:
                return null;
        }
    }
}
