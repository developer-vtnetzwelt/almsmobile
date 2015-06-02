/* 
 * This file is part of OppiaMobile - https://digital-campus.org/
 * 
 * OppiaMobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * OppiaMobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with OppiaMobile. If not, see <http://www.gnu.org/licenses/>.
 */

package org.digitalcampus.oppia.adapter;

import java.util.ArrayList;
import java.util.Locale;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.CourseActivity;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.Section;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SectionListAdapter extends ArrayAdapter<Section> {

	public static final String TAG = SectionListAdapter.class.getSimpleName();
	public static final String TAG_PLACEHOLDER = "placeholder";
	
	private final Context ctx;
	private final ArrayList<Section> sectionList;
	private SharedPreferences prefs;
	private Course course;

	public SectionListAdapter(Activity context, Course course, ArrayList<Section> sectionList) {
		super(context, R.layout.section_list_row, sectionList);
		this.ctx = context;
		this.sectionList = sectionList;
		this.course = course;
		prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {
	    LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    
	    View rowView = inflater.inflate(R.layout.section_list_row, parent, false);
	    TextView sectionTitle = (TextView) rowView.findViewById(R.id.section_title);
	    
	    Section s = sectionList.get(position);
	    String title = "";
	    if(prefs.getBoolean(PrefsActivity.PREF_SHOW_SECTION_NOS, false)){
	    	title += String.valueOf(s.getOrder()) + ". ";
	    }
	    title += s.getTitle(prefs.getString(PrefsActivity.PREF_LANGUAGE, Locale.getDefault().getLanguage()));
	    sectionTitle.setText(title);
	    
	    rowView.setTag(sectionList.get(position));
	   
	    
	    // now set up the horizontal activity list
	    LinearLayout ll = (LinearLayout) rowView.findViewById(R.id.section_activities);
	    for(int i=0 ; i<s.getActivities().size(); i++){
		    View horizRowItem = inflater.inflate(R.layout.section_horizonal_item, parent, false);
		    
		    ll.addView(horizRowItem);
		    
		    TextView tv = (TextView) horizRowItem.findViewById(R.id.activity_title);
		    tv.setText(s.getActivities().get(i).getTitle(prefs.getString(PrefsActivity.PREF_LANGUAGE, Locale.getDefault().getLanguage())));
		    
		    // set image
		    ImageView iv = (ImageView) horizRowItem.findViewById(R.id.activity_image);
	    	if(!s.getActivities().get(i).hasCustomImage()){
	    		iv.setScaleType(ImageView.ScaleType.CENTER);
	    	}
	    	iv.setImageDrawable(s.getActivities().get(i).getImageFile(course.getLocation(), ctx.getResources()));
	    	
	    	
	    	LinearLayout activityObject = (LinearLayout) horizRowItem.findViewById(R.id.activity_object);
	    	
	    	// highlight if completed
	    	if(s.getActivities().get(i).getCompleted() && prefs.getBoolean(PrefsActivity.PREF_HIGHLIGHT_COMPLETED, MobileLearning.DEFAULT_DISPLAY_COMPLETED)){
	    		activityObject.setBackgroundResource(R.drawable.activity_background_completed);
	    	}
	    	
	    	activityObject.setTag(R.id.TAG_SECTION_ID, s);
	    	activityObject.setTag(R.id.TAG_PLACEHOLDER_ID, i);
		    // set clicker
	    	activityObject.setClickable(true);
	    	activityObject.setSelected(true);
	    	activityObject.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {
					Section s = (Section) v.getTag(R.id.TAG_SECTION_ID); 
					int placeholder = (Integer) v.getTag(R.id.TAG_PLACEHOLDER_ID);
					Intent i = new Intent(ctx, CourseActivity.class);
					Bundle tb = new Bundle();
					tb.putSerializable(Section.TAG, (Section) s);
					tb.putSerializable(Course.TAG, (Course) course);
					tb.putSerializable(SectionListAdapter.TAG_PLACEHOLDER, (Integer) placeholder);
					i.putExtras(tb);
	         		ctx.startActivity(i);
				}
		    });
	    }
	    
	    return rowView;
	}
	
}

