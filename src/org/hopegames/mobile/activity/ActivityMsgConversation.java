package org.hopegames.mobile.activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.hopegames.mobile.adapter.MsgConversationListAdapter;
import org.hopegames.mobile.learning.R;
import org.hopegames.mobile.listener.SubmitListener;
import org.hopegames.mobile.model.User;
import org.hopegames.mobile.task.MessageDetailTask;
import org.hopegames.mobile.task.Payload;
import org.hopegames.mobile.task.MessageModel.UserMessageMain;
import org.hopegames.mobile.utils.ConnectionUtils;
import org.hopegames.mobile.utils.UIUtils;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;

public class ActivityMsgConversation extends AppActivity implements
		SubmitListener {
	private SharedPreferences prefs;
	//private ProgressDialog pDialog;
	private ListView lst_msg_user_list;
	private Button lst_msg_conversation_send;
	private EditText lst_msg_conversation_edit_text;
	private MsgConversationListAdapter msgConversationListAdapter;
	private boolean pDialogFlag = false;
	private Payload mPayLoad;
	private RelativeLayout rl_msg_conversation_progress;

	private String mMsgUserIdFrom, mMsgUserIdTo, mMsgUserToken, mUserFullName;;

	List<UserMessageMain> msgCollection = new ArrayList<UserMessageMain>();

	private int conversationLimit = 50;
	private boolean readUnreadMsgFrom, readUnreadMsgTo, getAsTo = false;
	private List<UserMessageMain> tempElements;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_message_conversation_main);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);

		//get data from intent
		
		mMsgUserIdFrom = getIntent().getStringExtra("fromMsgUserId");
		mMsgUserIdTo = getIntent().getStringExtra("toMsgUserId");
		mMsgUserToken = getIntent().getStringExtra("toMsgUserToken");
		mUserFullName = getIntent().getStringExtra("userFullName");

		getActionBar().setTitle(mUserFullName);

		// init preferences
		
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		// init UI elements
		
		lst_msg_user_list = (ListView) findViewById(R.id.lst_msg_conversation_list);
		lst_msg_conversation_send = (Button) findViewById(R.id.lst_msg_conversation_send);
		lst_msg_conversation_edit_text = (EditText) findViewById(R.id.lst_msg_conversation_edit_text);
		rl_msg_conversation_progress = (RelativeLayout) findViewById(R.id.rl_msg_conversation_progress);
		
		//get conversation from server
		
		/* this method contains three perameters
		 * @ 0 is for getting all the unread messages
		 * @  conversation limit is by default 50 and will change on every hit with 50 - number of messages 
		 * @ boolen variable to swip the the user id to get the messages from both sender and receiver end
		*/
		
		getMsgConversation(0, conversationLimit, true);
		// true for getting all the unread messages
		readUnreadMsgFrom = true;

		// add click listner for sending the data to sever
		
		sendMsgOnServer();
	}

	private void sendMsgOnServer() {
		lst_msg_conversation_send.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (lst_msg_conversation_edit_text.getText().toString().trim()
						.length() > 0) {
					
					//sending data on server asyn without notify the user
					
					sendMsgToServerforparticulerUser();
				}
			}

		});

	}

	private void getMsgConversation(int readStatus, int limit, boolean swapeIDs) {
		// checking the internet connection
		 if(!ConnectionUtils.isNetworkConnected(ActivityMsgConversation.this)){
             UIUtils.showAlert(ActivityMsgConversation.this, R.string.warning, R.string.warning_wifi_required);
             try {
            	 rl_msg_conversation_progress.setVisibility(View.GONE);
            	 lst_msg_user_list.setVisibility(View.VISIBLE);
 				//pDialog.dismiss();
 			} catch (IllegalArgumentException iae) {
 				//
 			}
             return;
         }
		
		if (!pDialogFlag) {
			
		//unhide the progress dialog and hide the list view during hit
			
		rl_msg_conversation_progress.setVisibility(View.VISIBLE);
       	lst_msg_user_list.setVisibility(View.GONE);
       	// set the dialog flag for hiding unhiding the progress dialog
		pDialogFlag = true;
		}
		
		// create the user object with condition for swiping the userids
		
		ArrayList<Object> users = new ArrayList<Object>();
		User u = new User();
		if (!swapeIDs) {
			u.setMsgUserIdFrom(mMsgUserIdFrom);
			u.setMsgUserIdTo(mMsgUserIdTo);
		} else {
			u.setMsgUserIdFrom(mMsgUserIdTo);
			u.setMsgUserIdTo(mMsgUserIdFrom);
		}
		u.setToken(mMsgUserToken);
		u.setRead("" + readStatus);
		u.setLimitnum(limit);
		users.add(u);

		Payload p = new Payload(users);
		MessageDetailTask lt = new MessageDetailTask(
				ActivityMsgConversation.this);
		lt.setLoginListener(this);
		lt.execute(p);

	}
	
	
	// contain logic regarding conversation between users

	@Override
	public void submitComplete(Payload response) {
		lst_msg_conversation_edit_text.setText("");

		mPayLoad = response;
		if (response != null && response.getMessages() != null) {
			conversationLimit = conversationLimit
					- response.getMessages().length;
			for (int i = 0; i < response.getMessages().length; i++) {
				msgCollection.add(response.getMessages()[i]);
			}
			if (!readUnreadMsgTo && readUnreadMsgFrom && !getAsTo) {
				getMsgConversation(0, conversationLimit, false);
				readUnreadMsgFrom = true;
				readUnreadMsgTo = true;
			} else if (readUnreadMsgTo && readUnreadMsgFrom && !getAsTo) {
				getMsgConversation(1, conversationLimit, false);
				getAsTo = true;
				readUnreadMsgFrom = true;
				readUnreadMsgTo = true;
			} else if (readUnreadMsgTo && readUnreadMsgFrom && getAsTo) {
				getMsgConversation(1, conversationLimit, true);
				getAsTo = false;
				readUnreadMsgFrom = false;
				readUnreadMsgTo = false;
			} else {
				if (pDialogFlag) {
					try {
						rl_msg_conversation_progress.setVisibility(View.GONE);
		            	 lst_msg_user_list.setVisibility(View.VISIBLE);
						//pDialog.dismiss();
						pDialogFlag = false;
					} catch (IllegalArgumentException iae) {
						//
					}
				}
				//sortMsgListByIds(msgCollection
				//		.toArray(new UserMessageMain[msgCollection.size()]));
				
				
				//sort msg list by ids of msgs using selection sort
				
				tempElements = sortMsgListByIds(msgCollection
						.toArray(new UserMessageMain[msgCollection.size()]));
				
				
				
				//addDate as string in object
					for(int i = 0;i<tempElements.size();i++){
					 
					 long dateInMilisec = Long.valueOf(tempElements.get(i).getTimecreated())*1000;// its need to be in milisecond
					 Date dateFormat = new java.util.Date(dateInMilisec);
					 String dateInString = new SimpleDateFormat("hh:mma dd MM, yyyy").format(dateFormat);
					 Log.e("TS",""+dateInString);
					 tempElements.get(i).setDateInString(dateInString);
			    	  }
				
				
				
				Collections.reverse(tempElements);
			
				msgConversationListAdapter = new MsgConversationListAdapter(
						this, tempElements, mMsgUserIdTo);
				lst_msg_user_list.setAdapter(msgConversationListAdapter);
			}
		}

	}
	
	//logic for section sort applies on list in assending order

	private List<UserMessageMain> sortMsgListByIds(
			UserMessageMain[] arr) {
		
	       for (int i = 0; i < arr.length - 1; i++)
	        {
	            int index = i;
	            for (int j = i + 1; j < arr.length; j++)
	            	 if (arr[j].getId()>(arr[index].getId())) 
	                    index = j;
	      
	            UserMessageMain smallerNumber = arr[index];  
	            arr[index] = arr[i];
	            arr[i] = smallerNumber;
	        }
      
		return new ArrayList<UserMessageMain>(Arrays.asList(arr));

	}
	
	
	//send the data on server

	private void sendMsgToServerforparticulerUser() {

		 if(!ConnectionUtils.isNetworkConnected(ActivityMsgConversation.this)){
             UIUtils.showAlert(ActivityMsgConversation.this, R.string.warning, R.string.warning_wifi_required);
             try {
            	 rl_msg_conversation_progress.setVisibility(View.GONE);
            	 lst_msg_user_list.setVisibility(View.VISIBLE);
 				//pDialog.dismiss();
 			} catch (IllegalArgumentException iae) {
 				//
 			}
             return;
         }

		ArrayList<Object> users = new ArrayList<Object>();
		User u = new User();
		u.setMsgUserIdFrom(mMsgUserIdFrom);
		u.setMsgUserIdTo(mMsgUserIdTo);
		u.setToken(mMsgUserToken);
		u.setMsgUserMsg(lst_msg_conversation_edit_text.getText().toString()
				.trim().toString());
		users.add(u);

		Payload p = new Payload(users);

		MessagePostTask lt = new MessagePostTask(ActivityMsgConversation.this);
		lt.setLoginListener(this);
		lt.execute(p);
		
		addMessageInUIList(lst_msg_conversation_edit_text.getText().toString());
		
	}
	
	
	// msg sends on server asyn meanwhile the msg added in list for user for instent msging

	private void addMessageInUIList(String msg1) {
		UserMessageMain messageMain = new UserMessageMain();
		messageMain.setText(msg1);
		messageMain.setUseridfrom(Integer.parseInt(mMsgUserIdTo));
		messageMain.setUseridto(Integer.parseInt(mMsgUserIdFrom));
		messageMain.setDateInString("now");
		tempElements.add(messageMain);
		msgConversationListAdapter.notifyDataSetChanged();
		lst_msg_conversation_edit_text.setText("");
	}

}
