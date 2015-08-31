package org.hopegames.mobile.adapter;

import org.hopegames.mobile.learning.R;
import org.hopegames.mobile.task.Payload;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class MsgCourseListAdapter  extends BaseAdapter {

	    Context mContext;
	    LayoutInflater inflater;
	    Payload mPayload;
	    boolean misFromFragmentScan=false;




	    public MsgCourseListAdapter(Context context,
				Payload response) {
	    	 mContext = context;
	         inflater = LayoutInflater.from(mContext);
	         mPayload = response;
		}



		@Override
	    public int getCount() {
	        return mPayload.getCourses().size();
	    }

	    @Override
	    public Object getItem(int position) {
	        return mPayload.getCourses().get(position);
	    }

	    @Override
	    public long getItemId(int position) {
	        return 0;
	    }


	    @Override
	    public View getView(int position, View convertView, ViewGroup parent) {
	        ViewHolder holder = null;
	        View view;

	     
	            if (convertView == null) {

	                convertView = inflater.inflate(R.layout.courses_user_list_item_view, null, false);

	                holder = new ViewHolder();

	                holder.msg_user_name = (TextView) convertView.findViewById(R.id.course_user_name);
	                
	                //convertView.setTag(holder);


	            } 
	           //     else {
	           //     holder = (ViewHolder) convertView.getTag();

	            //}

	            
	            
	                holder.msg_user_name.setText(mPayload.getCourses().get(position).getCourseName());
	             

	        
	        return convertView;
	    }


	    static class ViewHolder {
	        TextView msg_user_name;
	    }
	}


