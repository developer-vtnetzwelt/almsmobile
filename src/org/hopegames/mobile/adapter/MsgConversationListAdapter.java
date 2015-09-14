package org.hopegames.mobile.adapter;

import java.util.List;

import org.hopegames.mobile.learning.R;
import org.hopegames.mobile.task.MessageModel.UserMessageMain;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class MsgConversationListAdapter extends BaseAdapter {

	Context mContext;
	LayoutInflater inflater;
	List<UserMessageMain> mUserMessageMain;
	boolean misFromFragmentScan = false;
	String myId;

	public MsgConversationListAdapter(Context context,
			List<UserMessageMain> msgCollection, String mMsgUserIdTo) {
		mContext = context;
		inflater = LayoutInflater.from(mContext);
		mUserMessageMain = msgCollection;
		myId = mMsgUserIdTo;
	}

	@Override
	public int getCount() {
		return mUserMessageMain.size();
	}

	@Override
	public Object getItem(int position) {
		return mUserMessageMain.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TextView msg_user_latest_msg = null;
		TextView txt_msg_time = null;
		if (myId.equalsIgnoreCase(""
				+ mUserMessageMain.get(position).getUseridfrom())) {
			convertView = inflater.inflate(R.layout.msg_conversation_right,
					null, false);
		} else {
			convertView = inflater.inflate(R.layout.msg_conversation_left,
					null, false);
		}

		msg_user_latest_msg = (TextView) convertView.findViewById(R.id.txt_msg);
		txt_msg_time = (TextView) convertView.findViewById(R.id.txt_msg_time);

		msg_user_latest_msg.setText(""
				+ mUserMessageMain.get(position).getText());
		txt_msg_time.setText(""+mUserMessageMain.get(position).getDateInString());

		return convertView;
	}

}
