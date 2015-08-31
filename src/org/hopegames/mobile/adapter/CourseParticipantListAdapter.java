package org.hopegames.mobile.adapter;

import org.hopegames.mobile.learning.R;
import org.hopegames.mobile.task.Payload;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class CourseParticipantListAdapter extends BaseAdapter {

    Context mContext;
    LayoutInflater inflater;
    Payload mPayload;
    boolean misFromFragmentScan=false;




    public CourseParticipantListAdapter(Context context,
			Payload response) {
    	 mContext = context;
         inflater = LayoutInflater.from(mContext);
         mPayload = response;
	}



	@Override
    public int getCount() {
        return mPayload.getCourseparticipant().size();
    }

    @Override
    public Object getItem(int position) {
        return mPayload.getCourseparticipant().get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;

     
          

                convertView = inflater.inflate(R.layout.msg_user_list_item_view, null, false);

                holder = new ViewHolder();

                holder.msg_user_name = (TextView) convertView.findViewById(R.id.msg_user_name);
                holder.msg_user_latest_msg = (TextView) convertView.findViewById(R.id.msg_user_latest_msg);
                holder.msg_user_latest_msg.setVisibility(View.VISIBLE);
                
                //convertView.setTag(holder);


            
           //     else {
           //     holder = (ViewHolder) convertView.getTag();

            //}

            
            
                holder.msg_user_name.setText(mPayload.getCourseparticipant().get(position).getFullname());
                holder.msg_user_latest_msg.setText(mPayload.getCourseparticipant().get(position).getRoles()[0].getName());
             

        
        return convertView;
    }


    static class ViewHolder {
        TextView msg_user_name;
        TextView msg_user_latest_msg;
    }
}


