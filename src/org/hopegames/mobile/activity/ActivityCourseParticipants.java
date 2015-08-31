package org.hopegames.mobile.activity;

import java.util.ArrayList;

import org.hopegames.mobile.adapter.CourseParticipantListAdapter;
import org.hopegames.mobile.adapter.MsgCourseListAdapter;
import org.hopegames.mobile.learning.R;
import org.hopegames.mobile.listener.SubmitListener;
import org.hopegames.mobile.model.User;
import org.hopegames.mobile.task.LoginMessageCourseParticipent;
import org.hopegames.mobile.task.Payload;
import org.hopegames.mobile.utils.ConnectionUtils;
import org.hopegames.mobile.utils.UIUtils;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;

public class ActivityCourseParticipants extends AppActivity implements
		SubmitListener {

	private SharedPreferences pref;
	private ProgressDialog pDialog;

	// message layout
	private ListView lst_course_participent;
	private RelativeLayout rl_course_participent_progress;

	// control flags

	boolean hideDialog = false;
	boolean hittingUI = false;
	boolean hittingMessageUsers = false;

	String uToken, mUserId;
	int mCourseId;
	private Payload mPayLoad;

	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		setContentView(R.layout.activity_msg_course_participent);

		uToken = getIntent().getStringExtra("toMsgUserToken");
		mCourseId = getIntent().getIntExtra("course_id", 0);
		mUserId = getIntent().getStringExtra("msg_user_id");

		pref = getApplicationContext().getSharedPreferences("MyMsgPref",
				MODE_PRIVATE);

		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);
		getActionBar().setTitle("Course Particpants");

		lst_course_participent = (ListView) findViewById(R.id.lst_course_participent);
		rl_course_participent_progress = (RelativeLayout) findViewById(R.id.rl_course_participent_progress);

		getUserCouseUsers();

		onClickListners();

	}

	private void onClickListners() {
		lst_course_participent
				.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						Intent intent = new Intent(
								ActivityCourseParticipants.this,
								ActivityMsgConversation.class);
						intent.putExtra("fromMsgUserId", ""
								+ mPayLoad.getCourseparticipant().get(position)
										.getId());
						intent.putExtra("toMsgUserId", "" + mUserId);
						intent.putExtra("toMsgUserToken", "" + uToken);
						intent.putExtra("userFullName", mPayLoad
								.getCourseparticipant().get(position)
								.getFullname());
						startActivity(intent);

					}
				});

	}

	private void getUserCouseUsers() {
		if (!ConnectionUtils
				.isNetworkConnected(ActivityCourseParticipants.this)) {
			UIUtils.showAlert(ActivityCourseParticipants.this,
					R.string.warning, R.string.warning_wifi_required);
			return;
		}
		// check valid email address format

		rl_course_participent_progress.setVisibility(View.VISIBLE);
		lst_course_participent.setVisibility(View.GONE);

		ArrayList<Object> users = new ArrayList<Object>();
		User u = new User();
		u.setToken(uToken);
		u.setCourseid(mCourseId);
		users.add(u);

		Payload p = new Payload(users);
		LoginMessageCourseParticipent lt = new LoginMessageCourseParticipent(
				ActivityCourseParticipants.this);
		lt.setLoginListener(this);
		lt.execute(p);

	}

	@Override
	public void submitComplete(Payload response) {

		rl_course_participent_progress.setVisibility(View.GONE);
		lst_course_participent.setVisibility(View.VISIBLE);

		mPayLoad = response;

		if (mPayLoad.getCourseparticipant() != null) {
			if (mPayLoad.getCourseparticipant().size() > 0) {
				CourseParticipantListAdapter adapter = new CourseParticipantListAdapter(
						ActivityCourseParticipants.this, response);
				lst_course_participent.setAdapter(adapter);
			}
		}

	}

}