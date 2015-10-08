package org.hopegames.mobile.activity;

import java.util.ArrayList;
import java.util.List;

import org.hopegames.mobile.adapter.MsgCourseListAdapter;
import org.hopegames.mobile.adapter.MsgMemberListAdapter;
import org.hopegames.mobile.adapter.PagerAdaptorMessageSection;
import org.hopegames.mobile.learning.R;
import org.hopegames.mobile.listener.SubmitListener;
import org.hopegames.mobile.model.User;
import org.hopegames.mobile.task.Courses;
import org.hopegames.mobile.task.LoginMessageTask;
import org.hopegames.mobile.task.LoginMessageUIDTask;
import org.hopegames.mobile.task.LoginMessageUsersTask;
import org.hopegames.mobile.task.Payload;
import org.hopegames.mobile.task.UserCourseTask;
import org.hopegames.mobile.task.MessageModel.UserMessageMain;
import org.hopegames.mobile.utils.ConnectionUtils;
import org.hopegames.mobile.utils.UIUtils;
import org.hopegames.mobile.utils.TabsView.SlidingTabLayout;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;

public class ActivityMessageMain extends AppActivity implements SubmitListener {

	private SlidingTabLayout mTabs;
	private ViewPager mViewpager;
	private SharedPreferences pref;
	private boolean HitCourseAPI = false;

	// message layout
	private ListView lst_messages;
	private RelativeLayout rl_message_progress;
	private Payload mPayLoad, updatedPayloadObject;

	// participent layout
	private ListView lst_participent;
	private RelativeLayout rl_participent_progress;
	private boolean swipe = false;
	private List<UserMessageMain> listPayload;

	// control flags

	boolean hideDialog = false;
	boolean hittingUI = false;
	boolean hittingMessageUsers = false;
	String mToken = null;

	private List<Courses> mCourses = new ArrayList<Courses>();
	private String mUserId;

	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		setContentView(R.layout.activity_msg_participent);

		pref = getApplicationContext().getSharedPreferences("MyMsgPref",
				MODE_PRIVATE);

		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);
		getActionBar().setTitle("Messages");

		lst_messages = (ListView) findViewById(R.id.lst_messages);
		rl_message_progress = (RelativeLayout) findViewById(R.id.rl_message_progress);

		lst_participent = (ListView) findViewById(R.id.lst_participent);
		rl_participent_progress = (RelativeLayout) findViewById(R.id.rl_participent_progress);

		// init the tab layout part

		mTabs = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
		mViewpager = (ViewPager) findViewById(R.id.pager);

		mTabs.setDistributeEvenly(true);
		mTabs.setBackgroundColor(getResources().getColor(
				R.color.background_dark));

		// set adaptor on pager
		mViewpager.setAdapter(new PagerAdaptorMessageSection(
				ActivityMessageMain.this));
		mTabs.setViewPager(mViewpager);

		getUserToxken();

		onClickListners();

		addheaderInPartcipantList();

	}

	private void addheaderInPartcipantList() {
		LayoutInflater inflater = getLayoutInflater();
		ViewGroup header = (ViewGroup) inflater.inflate(R.layout.header_course,
				lst_participent, false);
		lst_participent.addHeaderView(header, null, false);

	}

	private void onClickListners() {
		lst_messages.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent intent = new Intent(ActivityMessageMain.this,
						ActivityMsgConversation.class);
				if (updatedPayloadObject.getMessages()[position].getUseridto() == Integer
						.parseInt(mUserId)) {
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
					intent.putExtra("userFullName", updatedPayloadObject
							.getMessages()[position].getUserfromfullname());
				} else if (updatedPayloadObject.getMessages()[position]
						.getUseridfrom() == Integer.parseInt(mUserId)) {
					intent.putExtra(
							"fromMsgUserId",
							""
									+ updatedPayloadObject.getMessages()[position]
											.getUseridto());
					intent.putExtra(
							"toMsgUserId",
							""
									+ updatedPayloadObject.getMessages()[position]
											.getUseridfrom());
					intent.putExtra("userFullName", updatedPayloadObject
							.getMessages()[position].getUsertofullname());
				}
				intent.putExtra("toMsgUserToken", "" + mToken);

				startActivity(intent);

			}
		});

		lst_participent.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent intent = new Intent(ActivityMessageMain.this,
						ActivityCourseParticipants.class);
				intent.putExtra("toMsgUserToken", "" + mToken);
				intent.putExtra("course_id", mCourses.get(position - 1)
						.getCourseId());
				intent.putExtra("msg_user_id", mUserId);
				// intent.putExtra("msg_course_id", value)
				startActivity(intent);

			}
		});
	}

	private void getUserToxken() {
		if (!ConnectionUtils.isNetworkConnected(ActivityMessageMain.this)) {
			UIUtils.showAlert(ActivityMessageMain.this, R.string.warning,
					R.string.warning_wifi_required);
			return;
		}
		String username = pref.getString("user_signin", null);
		String password = pref.getString("user_pass", null);
		// check valid email address format
		if (username.length() == 0) {
			UIUtils.showAlert(ActivityMessageMain.this, R.string.error,
					R.string.error_no_username);
			return;
		}

		// show progress dialog
		// pDialog = new ProgressDialog(ActivityMessageMain.this);
		// pDialog.setTitle(R.string.title_messages);
		// pDialog.setMessage(this.getString(R.string.msg_process));
		// pDialog.setCancelable(false);
		// pDialog.show();
		rl_message_progress.setVisibility(View.VISIBLE);
		lst_messages.setVisibility(View.GONE);

		ArrayList<Object> users = new ArrayList<Object>();
		User u = new User();
		u.setUsername(username);
		u.setPassword(password);
		users.add(u);

		Payload p = new Payload(users);
		LoginMessageTask lt = new LoginMessageTask(ActivityMessageMain.this);
		lt.setLoginListener(this);
		lt.execute(p);

	}

	@Override
	public void submitComplete(Payload response) {
		if (response.getFromWhich().equalsIgnoreCase("course")) {

			rl_participent_progress.setVisibility(View.GONE);
			lst_participent.setVisibility(View.VISIBLE);
			if (response.getCourses() != null) {
				mCourses = response.getCourses();
				MsgCourseListAdapter adapter = new MsgCourseListAdapter(
						ActivityMessageMain.this, response);
				lst_participent.setAdapter(adapter);
				response.setFromWhich("");
			}
			return;
		}
		if (hideDialog) {
			try {
				rl_message_progress.setVisibility(View.GONE);
				lst_messages.setVisibility(View.VISIBLE);
			} catch (IllegalArgumentException iae) {
				//
			}
			if (response.getMessages() != null) {
				for (int i = 0; i < response.getMessages().length; i++) {
					listPayload.add(response.getMessages()[i]);
				}
			}
			UserMessageMain[] array = listPayload
					.toArray(new UserMessageMain[listPayload.size()]);
			response.setMessages(array);
			mPayLoad = response;
			updatedPayloadObject = payloadObjectForUserList(mPayLoad);
			updatedPayloadObject.setUserId(mUserId);

			if (mPayLoad.getMessages() != null
					&& mPayLoad.getMessages().length > 0) {
				MsgMemberListAdapter adapter = new MsgMemberListAdapter(
						ActivityMessageMain.this, updatedPayloadObject);
				lst_messages.setAdapter(adapter);
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


		if (!hideDialog) {
			if (!hittingUI) {
				if (!ConnectionUtils
						.isNetworkConnected(ActivityMessageMain.this)) {
					UIUtils.showAlert(ActivityMessageMain.this,
							R.string.warning, R.string.warning_wifi_required);
					try {
						rl_message_progress.setVisibility(View.GONE);
						lst_messages.setVisibility(View.VISIBLE);
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
						ActivityMessageMain.this);
				lt.setLoginListener(this);
				lt.execute(p);
			} else if (hittingUI && !hittingMessageUsers && !swipe) {
				if (!ConnectionUtils
						.isNetworkConnected(ActivityMessageMain.this)) {
					UIUtils.showAlert(ActivityMessageMain.this,
							R.string.warning, R.string.warning_wifi_required);
					try {
						rl_message_progress.setVisibility(View.GONE);
						lst_messages.setVisibility(View.VISIBLE);
					} catch (IllegalArgumentException iae) {
						//
					}
					return;
				}

				ArrayList<Object> users = new ArrayList<Object>();
				User u = new User();
				u.setMsgUserId(response.getUserId());

				if (!HitCourseAPI) {
					mUserId = response.getUserId();
					getCourseDetails(response.getUserId());
					HitCourseAPI = true;
				}

				u.setToken(mToken);
				u.setSwipe_0(mUserId);
				u.setMsgUserId("0");
				users.add(u);
				Log.e("userId----", "" + response.getUserId());

				hittingMessageUsers = true;

				swipe = true;
				Payload p = new Payload(users);
				LoginMessageUsersTask lt = new LoginMessageUsersTask(
						ActivityMessageMain.this);
				lt.setLoginListener(this);
				lt.execute(p);
			} else if (swipe && !hideDialog) {

				if (!ConnectionUtils
						.isNetworkConnected(ActivityMessageMain.this)) {
					UIUtils.showAlert(ActivityMessageMain.this,
							R.string.warning, R.string.warning_wifi_required);
					try {
						rl_message_progress.setVisibility(View.GONE);
						lst_messages.setVisibility(View.VISIBLE);
					} catch (IllegalArgumentException iae) {
						//
					}
					return;
				}
				listPayload = new ArrayList<UserMessageMain>();
				if (response.getMessages() != null) {
					for (int i = 0; i < response.getMessages().length; i++) {
						listPayload.add(response.getMessages()[i]);
					}
				}
				ArrayList<Object> users = new ArrayList<Object>();
				User u = new User();
				u.setMsgUserId(response.getUserId());

				if (!HitCourseAPI) {

					// getCourseDetails(response.getUserId());
					HitCourseAPI = true;
				}
				u.setToken(mToken);
				u.setSwipe_0("0");
				u.setMsgUserId(mUserId);
				users.add(u);
				Log.e("userId----", "" + response.getUserId());

				hittingMessageUsers = true;
				hideDialog = true;
				swipe = true;
				Payload p = new Payload(users);
				LoginMessageUsersTask lt = new LoginMessageUsersTask(
						ActivityMessageMain.this);
				lt.setLoginListener(this);
				lt.execute(p);
			}

		}

	}

	private void getCourseDetails(String userId) {

		if (!ConnectionUtils.isNetworkConnected(ActivityMessageMain.this)) {
			UIUtils.showAlert(ActivityMessageMain.this, R.string.warning,
					R.string.warning_wifi_required);
			return;
		}

		rl_participent_progress.setVisibility(View.VISIBLE);
		lst_participent.setVisibility(View.GONE);

		ArrayList<Object> users = new ArrayList<Object>();
		User u = new User();
		u.setMsgUserId(userId);

		u.setToken(mToken);
		u.setFromWhichHit("course");
		users.add(u);
		Payload p = new Payload(users);
		UserCourseTask lt = new UserCourseTask(ActivityMessageMain.this);
		lt.setLoginListener(this);
		lt.execute(p);

	}
	// get data from server to get user list.

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
								mPayLoadOut.getMessages()[i])) {
					userM = new UserMessageMain();
					userM.setUserfromfullname(mPayLoadOut.getMessages()[i]
							.getUserfromfullname());
					userM.setId(mPayLoadOut.getMessages()[i].getId());
					userM.setText(mPayLoadOut.getMessages()[i].getText().trim());
					userM.setUseridfrom(mPayLoadOut.getMessages()[i]
							.getUseridfrom());
					userM.setUseridto(mPayLoadOut.getMessages()[i]
							.getUseridto());

					userM.setUsertofullname(mPayLoadOut.getMessages()[i]
							.getUsertofullname());

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

	
	// logic to check the number of contacts msg to the current user or visa versa.
	
	private boolean alreadyNotInList(List<UserMessageMain> userMessageMainlist,
			UserMessageMain userMessageMainObject) {
		boolean notDuplicate = true;
		for (int i = 0; i < userMessageMainlist.size(); i++) {
// checks whether the user for duplicate entries
			if ((userMessageMainlist.get(i).getUseridto() == userMessageMainObject
					.getUseridto() && userMessageMainlist.get(i)
					.getUseridfrom() == userMessageMainObject.getUseridfrom())
					|| (userMessageMainlist.get(i).getUseridto() == userMessageMainObject
							.getUseridfrom() && userMessageMainlist.get(i)
							.getUseridfrom() == userMessageMainObject
							.getUseridto())) {
				notDuplicate = false;
			}

		}
		return notDuplicate;
	}

}