package org.hopegames.mobile.adapter;

import org.hopegames.mobile.learning.R;

import android.app.Activity;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;


/**
 * Created by Sameer
 * <p/>
 */
public class PagerAdaptorMessageSection extends PagerAdapter
{
    Activity mcontext;

    public PagerAdaptorMessageSection(Activity mcontext)
    {
        this.mcontext = mcontext;
    }

    @Override
    public int getCount()
    {
        return 2;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position)
    {
        int resId = 0;
        switch (position)
        {
            case 0:
                resId = R.id.frame_message;
                break;
            case 1:
                resId = R.id.frame_participent;
                break;
        }
        return mcontext.findViewById(resId);
    }

    @Override
    public boolean isViewFromObject(View view, Object object)
    {
        return view == (View) object;
    }

    @Override
    public CharSequence getPageTitle(int position)
    {
        String title = "";

        switch (position)
        {
            case 0:
                title = "MESSAGES";
                break;
            case 1:
                title = "PARTICIPANTS";
                break;

        }

        return title;
    }
}
