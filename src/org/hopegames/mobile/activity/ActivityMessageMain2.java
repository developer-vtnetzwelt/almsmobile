package org.hopegames.mobile.activity;

import java.util.ArrayList;
import java.util.List;

import org.hopegames.mobile.adapter.MsgMemberListAdapter;
import org.hopegames.mobile.learning.R;
import org.hopegames.mobile.listener.SubmitListener;
import org.hopegames.mobile.model.User;
import org.hopegames.mobile.task.LoginMessageTask;
import org.hopegames.mobile.task.LoginMessageUIDTask;
import org.hopegames.mobile.task.LoginMessageUsersTask;
import org.hopegames.mobile.task.Payload;
import org.hopegames.mobile.task.MessageModel.UserMessageMain;
import org.hopegames.mobile.utils.ConnectionUtils;
import org.hopegames.mobile.utils.UIUtils;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class ActivityMessageMain2 extends AppActivity implements SubmitListener {
	private ProgressDialog pDialog;
	private ListView lst_msg_user_list;
	private SharedPreferences pref;
	private Payload mPayLoad, updatedPayloadObject;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_message_main);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);

		pref = getApplicationContext().getSharedPreferences("MyMsgPref",
				MODE_PRIVATE);

		lst_msg_user_list = (ListView) findViewById(R.id.lst_msg_user_list);

		getUserToxken();

		onClcikListners();

	}

	private void onClcikListners() {
		lst_msg_user_list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent intent = new Intent(ActivityMessageMain2.this,
						ActivityMsgConversation.class);
				intent.putExtra(
						"fromMsgUserId",
						""
								+ updatedPayloadObject.getMessages()[position]
										.getUseridfrom());
				intent.putExtra(
						"toMsgUserId",
						""
								+ updatedPayloadObject.getMessages()[position]
										.getUseridto());
				intent.putExtra("toMsgUserToken", "" + mToken);
				intent.putExtra("userFullName", updatedPayloadObject
						.getMessages()[position].getUserfromfullname());
				startActivity(intent);

			}
		});

	}

	private void getUserToxken() {
		 if(!ConnectionUtils.isNetworkConnected(ActivityMessageMain2.this)){
             UIUtils.showAlert(ActivityMessageMain2.this, R.string.warning, R.string.warning_wifi_required);
             return;
         }
		String username = pref.getString("user_signin", null);
		String password = pref.getString("user_pass", null);
		// check valid email address format
		if (username.length() == 0) {
			UIUtils.showAlert(ActivityMessageMain2.this, R.string.error,
					R.string.error_no_username);
			return;
		}

		// show progress dialog
		pDialog = new ProgressDialog(ActivityMessageMain2.this);
		pDialog.setTitle(R.string.title_messages);
		pDialog.setMessage(this.getString(R.string.msg_process));
		pDialog.setCancelable(false);
		pDialog.show();

		ArrayList<Object> users = new ArrayList<Object>();
		User u = new User();
		u.setUsername(username);
		u.setPassword(password);
		users.add(u);

		Payload p = new Payload(users);
		LoginMessageTask lt = new LoginMessageTask(ActivityMessageMain2.this);
		lt.setLoginListener(this);
		lt.execute(p);

	}

	boolean hideDialog = false;
	boolean hittingUI = false;
	boolean hittingMessageUsers = false;
	String mToken = null;

	@Override
	public void submitComplete(Payload response) {
		if (hideDialog) {
			try {
				pDialog.dismiss();
			} catch (IllegalArgumentException iae) {
				//
			}
			mPayLoad = response;
			updatedPayloadObject = payloadObjectForUserList(mPayLoad);

			if (mPayLoad.getMessages() != null
					&& mPayLoad.getMessages().length > 0) {
				MsgMemberListAdapter adapter = new MsgMemberListAdapter(
						ActivityMessageMain2.this, updatedPayloadObject);
				lst_msg_user_list.setAdapter(adapter);
			}
		}
		/******* Create SharedPreferences *******/
		if (response.getToken() != null
				&& !response.getToken().equalsIgnoreCase("")) {
			SharedPreferences pref = getApplicationContext()
					.getSharedPreferences("MyMsgPref", MODE_PRIVATE);
			Editor editor = pref.edit();
			editor.putString("msg_token", response.getToken()); // Saving string

			mToken = response.getToken();
			// Save the changes in SharedPreferences
			editor.commit(); // commit changes
		}

		// check valid email address format

		if (!hideDialog) {
			if (!hittingUI) {
				 if(!ConnectionUtils.isNetworkConnected(ActivityMessageMain2.this)){
		                UIUtils.showAlert(ActivityMessageMain2.this, R.string.warning, R.string.warning_wifi_required);
		                try {
		    				pDialog.dismiss();
		    			} catch (IllegalArgumentException iae) {
		    				//
		    			}
		                return;
		            }
		              
				ArrayList<Object> users = new ArrayList<Object>();
				User u = new User();
				u.setToken(response.getToken());
				users.add(u);
				hittingUI = true;
				Payload p = new Payload(users);
				LoginMessageUIDTask lt = new LoginMessageUIDTask(
						ActivityMessageMain2.this);
				lt.setLoginListener(this);
				lt.execute(p);
			} else if (hittingUI && !hittingMessageUsers) {
				 if(!ConnectionUtils.isNetworkConnected(ActivityMessageMain2.this)){
		                UIUtils.showAlert(ActivityMessageMain2.this, R.string.warning, R.string.warning_wifi_required);
		                try {
		    				pDialog.dismiss();
		    			} catch (IllegalArgumentException iae) {
		    				//
		    			}
		                return;
		            }
		               
			
				 
				ArrayList<Object> users = new ArrayList<Object>();
				User u = new User();
				u.setMsgUserId(response.getUserId());
				u.setToken(mToken);
				users.add(u);
				Log.e("Token----", "" + response.getUserId());

				hittingMessageUsers = true;
				hideDialog = true;
				Payload p = new Payload(users);
				LoginMessageUsersTask lt = new LoginMessageUsersTask(
						ActivityMessageMain2.this);
				lt.setLoginListener(this);
				lt.execute(p);
			}

		}

	}

	private Payload payloadObjectForUserList(Payload mPayLoadOut) {

		Payload payloadInner = new Payload();
		List<UserMessageMain> userMessageMainlist = new ArrayList<UserMessageMain>();
		UserMessageMain userM = null;
		UserMessageMain[] userMessage = null;
		if (mPayLoadOut.getMessages() != null
				& mPayLoadOut.getMessages().length > 0) {
			for (int i = 0; i < mPayLoadOut.getMessages().length; i++) {
				if (userMessageMainlist.size() == 0
						|| alreadyNotInList(userMessageMainlist,
								mPayLoadOut.getMessages()[i].getUseridfrom())) {
					userM = new UserMessageMain();
					userM.setUserfromfullname(mPayLoadOut.getMessages()[i]
							.getUserfromfullname());
					userM.setId(mPayLoadOut.getMessages()[i].getId());
					userM.setText(mPayLoadOut.getMessages()[i].getText().trim());
					userM.setUseridfrom(mPayLoadOut.getMessages()[i]
							.getUseridfrom());
					userM.setUseridto(mPayLoadOut.getMessages()[i]
							.getUseridto());

					userMessageMainlist.add(userM);
				}
			}
			if (userMessageMainlist.size() > 0) {
				userMessage = userMessageMainlist
						.toArray(new UserMessageMain[userMessageMainlist.size()]);
				payloadInner.setMessages(userMessage);
			}
		}
		return payloadInner;
	}

	private boolean alreadyNotInList(List<UserMessageMain> userMessageMainlist,
			int userId) {
		boolean notDuplicate = true;
		for (int i = 0; i < userMessageMainlist.size(); i++) {
			if (userMessageMainlist.get(i).getUseridfrom() == userId) {
				notDuplicate = false;
			}
		}
		return notDuplicate;
	}
}
