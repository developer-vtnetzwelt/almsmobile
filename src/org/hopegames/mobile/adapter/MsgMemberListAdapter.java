package org.hopegames.mobile.adapter;

import org.hopegames.mobile.learning.R;
import org.hopegames.mobile.task.Payload;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Mini PC 5 on 01-04-2015.
 */
public class MsgMemberListAdapter extends BaseAdapter {

	Context mContext;
	LayoutInflater inflater;
	Payload mPayload;
	boolean misFromFragmentScan = false;

	public MsgMemberListAdapter(Context context, Payload response) {
		mContext = context;
		inflater = LayoutInflater.from(mContext);
		mPayload = response;
	}

	@Override
	public int getCount() {
		return mPayload.getMessages().length;
	}

	@Override
	public Object getItem(int position) {
		return mPayload.getMessages()[position];
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		View view;

		convertView = inflater.inflate(R.layout.msg_user_only_list_item_view,
				null, false);

		holder = new ViewHolder();

		holder.msg_user_name = (TextView) convertView
				.findViewById(R.id.msg_user_only_name);

		if (mPayload.getMessages()[position].getUseridfrom() == Integer
				.parseInt(mPayload.getUserId())) {

			holder.msg_user_name.setText(mPayload.getMessages()[position]
					.getUsertofullname());

		} else {
			holder.msg_user_name.setText(mPayload.getMessages()[position]
					.getUserfromfullname());
		}
		return convertView;
	}

	static class ViewHolder {
		TextView msg_user_latest_msg, msg_user_name, msg_user_date;
		ImageView msg_user_img;
	}
}
