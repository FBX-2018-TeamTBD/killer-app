package com.example.cassandrakane.goalz.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HorizontalPagerAdapter extends FragmentPagerAdapter {

    private Context mContext;
    private List<Fragment> mPages;
    private Map<Integer, String> mFragmentTags;
    private FragmentManager mFragmentManager;

    public HorizontalPagerAdapter(FragmentManager fragmentManager, Context context, List<Fragment> pages) {
        super(fragmentManager);
        mContext = context;
        mPages = pages;
        mFragmentTags = new HashMap<Integer,String>();
        mFragmentManager = fragmentManager;
    }

     @Override
    public Fragment getItem(int position) {
         switch (position){
             case 0:
                 return mPages.get(0);
             case 1:
                 return mPages.get(1);
             case 2:
                 return mPages.get(2);
             default: return null;
         }
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Object obj = super.instantiateItem(container, position);
        if (obj instanceof Fragment) {
            // record the fragment tag here.
            Fragment f = (Fragment) obj;
            String tag = f.getTag();
            mFragmentTags.put(position, tag);
        }
        return obj;
    }

    public Fragment getFragment(int position) {
        String tag = mFragmentTags.get(position);
        if (tag == null)
            return null;
        return mFragmentManager.findFragmentByTag(tag);
    }

//    @Override
//    public int getItemPosition(@NonNull Object object) {
//        return POSITION_NONE;
//    }

}
