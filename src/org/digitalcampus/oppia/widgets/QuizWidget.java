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

package org.digitalcampus.oppia.widgets;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.quiz.InvalidQuizException;
import org.digitalcampus.mobile.quiz.Quiz;
import org.digitalcampus.mobile.quiz.model.QuizQuestion;
import org.digitalcampus.mobile.quiz.model.questiontypes.Description;
import org.digitalcampus.mobile.quiz.model.questiontypes.Essay;
import org.digitalcampus.mobile.quiz.model.questiontypes.Matching;
import org.digitalcampus.mobile.quiz.model.questiontypes.MultiChoice;
import org.digitalcampus.mobile.quiz.model.questiontypes.MultiSelect;
import org.digitalcampus.mobile.quiz.model.questiontypes.Numerical;
import org.digitalcampus.mobile.quiz.model.questiontypes.ShortAnswer;
import org.digitalcampus.oppia.activity.CourseActivity;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.adapter.QuizFeedbackAdapter;
import org.digitalcampus.oppia.application.DatabaseManager;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.application.Tracker;
import org.digitalcampus.oppia.model.Activity;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.QuizFeedback;
import org.digitalcampus.oppia.utils.resources.ExternalResourceOpener;
import org.digitalcampus.oppia.utils.storage.FileUtils;
import org.digitalcampus.oppia.utils.MetaDataUtils;
import org.digitalcampus.oppia.utils.mediaplayer.VideoPlayerActivity;
import org.digitalcampus.oppia.widgets.quiz.DescriptionWidget;
import org.digitalcampus.oppia.widgets.quiz.EssayWidget;
import org.digitalcampus.oppia.widgets.quiz.MatchingWidget;
import org.digitalcampus.oppia.widgets.quiz.MultiChoiceWidget;
import org.digitalcampus.oppia.widgets.quiz.MultiSelectWidget;
import org.digitalcampus.oppia.widgets.quiz.NumericalWidget;
import org.digitalcampus.oppia.widgets.quiz.QuestionWidget;
import org.digitalcampus.oppia.widgets.quiz.ShortAnswerWidget;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class QuizWidget extends WidgetFactory {

	public static final String TAG = QuizWidget.class.getSimpleName();
	private Quiz quiz;
	private QuestionWidget qw;
	public Button prevBtn;
	public Button nextBtn;
	private TextView qText;
	private String quizContent;
	private LinearLayout questionImage;
	private boolean isOnResultsPage = false;
	private ViewGroup container;
	QuizQuestion q = null;
	
	boolean clickedOnSendForEvaluation = false;
	
	private String PREF_QUIZ_ID;
	private String PREF_USER_RESPONSE;
	//private String PREF_MAIN_SCORE;
	private String PREF_FEEDBACK;
	private String PREF_SCORE;
	
	private String localQuizId="none";

	private boolean showingResultForEssayQuestion = false;

	public static QuizWidget newInstance(Activity activity, Course course,
			boolean isBaseline) {
		QuizWidget myFragment = new QuizWidget();

		Bundle args = new Bundle();
		args.putSerializable(Activity.TAG, activity);
		args.putSerializable(Course.TAG, course);
		args.putBoolean(CourseActivity.BASELINE_TAG, isBaseline);
		myFragment.setArguments(args);
		
		
		

		return myFragment;
	}

	public QuizWidget() {

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}

	@SuppressWarnings("unchecked")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		prefs = PreferenceManager.getDefaultSharedPreferences(super
				.getActivity());
		

		
		PREF_QUIZ_ID=prefs.getString("QUIZ_ID", null);
		PREF_USER_RESPONSE=prefs.getString("USER_RESPONSE", null);
		//PREF_MAIN_SCORE=prefs.getString("MAIN_SCORE", null);
		PREF_FEEDBACK=prefs.getString("FEEDBACK", null);
		PREF_SCORE=prefs.getString("SCORE", null);
		
		
		
		View vv = super.getLayoutInflater(savedInstanceState).inflate(
				R.layout.widget_quiz, null);
		this.container = container;
		course = (Course) getArguments().getSerializable(Course.TAG);
		activity = ((Activity) getArguments().getSerializable(Activity.TAG));
		this.setIsBaseline(getArguments().getBoolean(
				CourseActivity.BASELINE_TAG));
		quizContent = ((Activity) getArguments().getSerializable(Activity.TAG))
				.getContents(prefs.getString(PrefsActivity.PREF_LANGUAGE,
						Locale.getDefault().getLanguage()));

		LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);
		vv.setLayoutParams(lp);
		vv.setId(activity.getActId());
		if ((savedInstanceState != null)
				&& (savedInstanceState.getSerializable("widget_config") != null)) {
			setWidgetConfig((HashMap<String, Object>) savedInstanceState
					.getSerializable("widget_config"));
		}

		return vv;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable("widget_config", getWidgetConfig());
		outState.putBoolean("key_showingResultForEssayQuestion",
				showingResultForEssayQuestion);
		outState.putString("score_save", PREF_SCORE);
		outState.putString("local_quiz_id", localQuizId);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		prevBtn = (Button) getView().findViewById(R.id.mquiz_prev_btn);
		nextBtn = (Button) getView().findViewById(R.id.mquiz_next_btn);
		qText = (TextView) getView().findViewById(R.id.question_text);
		questionImage = (LinearLayout) getView().findViewById(
				R.id.question_image);

		if (savedInstanceState != null) {
			showingResultForEssayQuestion = savedInstanceState
					.getBoolean("key_showingResultForEssayQuestion");
			PREF_SCORE=savedInstanceState
					.getString("score_save");
			localQuizId=savedInstanceState
					.getString("local_quiz_id");
		}

		//loadQuiz();
	}

	@Override
	public void onResume() {
		super.onResume();
		loadQuiz();
	}

	public void loadQuiz() {
		if (this.quiz == null) {
			this.quiz = new Quiz();
			this.quiz.load(quizContent, prefs.getString(
					PrefsActivity.PREF_LANGUAGE, Locale.getDefault()
							.getLanguage()));
			Log.e("quiz id ******",""+quiz.getID());
			localQuizId=String.valueOf(quiz.getID());
		}
		if (this.isOnResultsPage) {
			if (q instanceof Essay || showingResultForEssayQuestion) {
				// log the activity as complete

				if(localQuizId.equalsIgnoreCase(PREF_QUIZ_ID)){
					if(PREF_SCORE.equalsIgnoreCase("0.00")){
						showSentForEvaluationScreen(-0.1,null);
					}else{
						showSentForEvaluationScreen(Double.valueOf(PREF_SCORE),PREF_FEEDBACK);
					}
				} if(PREF_QUIZ_ID==null){
					showSentForEvaluationScreen(-0.1,null);
				}
				
				

			} else {
				this.showResults();
			}
		} else {
			// determine availability
			if (this.quiz.getAvailability() == Quiz.AVAILABILITY_ALWAYS) {
				this.showQuestion();
			} else if (this.quiz.getAvailability() == Quiz.AVAILABILITY_SECTION) {

				// check to see if all previous section activities have been
				// completed
				DbHelper db = new DbHelper(getView().getContext());
				long userId = db.getUserId(prefs.getString(
						PrefsActivity.PREF_USER_NAME, ""));
				boolean completed = db.isPreviousSectionActivitiesCompleted(
						course, activity, userId);
				DatabaseManager.getInstance().closeDatabase();

				if (completed) {
					this.showQuestion();
				} else {
					ViewGroup vg = (ViewGroup) getView().findViewById(
							activity.getActId());
					vg.removeAllViews();
					vg.addView(View.inflate(getView().getContext(),
							R.layout.widget_quiz_unavailable, null));

					TextView tv = (TextView) getView().findViewById(
							R.id.quiz_unavailable);
					tv.setText(R.string.widget_quiz_unavailable_section);
				}
			} else if (this.quiz.getAvailability() == Quiz.AVAILABILITY_COURSE) {
				// check to see if all previous course activities have been
				// completed
				DbHelper db = new DbHelper(getView().getContext());
				long userId = db.getUserId(prefs.getString(
						PrefsActivity.PREF_USER_NAME, ""));
				boolean completed = db.isPreviousCourseActivitiesCompleted(
						course, activity, userId);
				DatabaseManager.getInstance().closeDatabase();

				if (completed) {
					this.showQuestion();
				} else {
					ViewGroup vg = (ViewGroup) getView().findViewById(
							activity.getActId());
					vg.removeAllViews();
					vg.addView(View.inflate(getView().getContext(),
							R.layout.widget_quiz_unavailable, null));

					TextView tv = (TextView) getView().findViewById(
							R.id.quiz_unavailable);
					tv.setText(R.string.widget_quiz_unavailable_course);
				}
			}
		}
	}

	public void showQuestion() {

		try {
			q = this.quiz.getCurrentQuestion();
		} catch (InvalidQuizException e) {
			Toast.makeText(
					super.getActivity(),
					super.getActivity().getString(
							R.string.error_quiz_no_questions),
					Toast.LENGTH_LONG).show();
			e.printStackTrace();
			return;
		}
		qText.setVisibility(View.VISIBLE);
		// convert in case has any html special chars
		qText.setText(Html.fromHtml(
				q.getTitle(prefs.getString(PrefsActivity.PREF_LANGUAGE, Locale
						.getDefault().getLanguage()))).toString());

		if (q.getProp("image") == null) {
			questionImage.setVisibility(View.GONE);
		} else {
			String fileUrl = course.getLocation() + q.getProp("image");
			// File file = new File(fileUrl);
			Bitmap myBitmap = BitmapFactory.decodeFile(fileUrl);
			File file = new File(fileUrl);
			ImageView iv = (ImageView) getView().findViewById(
					R.id.question_image_image);
			iv.setImageBitmap(myBitmap);
			iv.setTag(file);
			if (q.getProp("media") == null) {
				OnImageClickListener oicl = new OnImageClickListener(
						super.getActivity(), "image/*");
				iv.setOnClickListener(oicl);
				TextView tv = (TextView) getView().findViewById(
						R.id.question_image_caption);
				tv.setText(R.string.widget_quiz_image_caption);
				questionImage.setVisibility(View.VISIBLE);
			} else {
				TextView tv = (TextView) getView().findViewById(
						R.id.question_image_caption);
				tv.setText(R.string.widget_quiz_media_caption);
				OnMediaClickListener omcl = new OnMediaClickListener(
						q.getProp("media"));
				iv.setOnClickListener(omcl);
				questionImage.setVisibility(View.VISIBLE);
			}

		}
		

		if (q instanceof MultiChoice) {
			qw = new MultiChoiceWidget(super.getActivity(), getView(),
					container);
		} else if (q instanceof MultiSelect) {
			qw = new MultiSelectWidget(super.getActivity(), getView(),
					container);
		} else if (q instanceof ShortAnswer) {
			qw = new ShortAnswerWidget(super.getActivity(), getView(),
					container);
		} else if (q instanceof Matching) {
			qw = new MatchingWidget(super.getActivity(), getView(), container);
		} else if (q instanceof Numerical) {
			qw = new NumericalWidget(super.getActivity(), getView(), container);
		} else if (q instanceof Description) {
			qw = new DescriptionWidget(super.getActivity(), getView(),
					container);
		} else if (q instanceof Essay) {
			if (questionAttemptStatus()) {
				showingResultForEssayQuestion = true;
				qw = new EssayWidget(super.getActivity(), getView(), container);
			} else {

			}
		} else {
			return;
		}
		if (qw != null) {
			qw.setQuestionResponses(q.getResponseOptions(),
					q.getUserResponses());
			Log.e("*******qw***********", "" + qw);
			this.setProgress();
			this.setNav();
		}

	}

	public boolean questionAttemptStatus() {
		// TODO Auto-generated method stub
		if(prefs.getString("prefUsername", null).equalsIgnoreCase(prefs.getString("UNAME_ESSAY_QUESTION_ATTEMPT_STATUS", null))){
			if(prefs.getInt("COURSE_NAME_ESSAY_QUESTION_ATTEMPT_STATUS", -1)==(course.getCourseId())){
				if(prefs.getInt("ACTIVITY_ESSAY_QUESTION_ATTEMPT_STATUS", -1)==(activity.getActId())){
					if(prefs.getInt("ESSAY_QUESTION_ATTEMPT_STATUS", 0)==0){
						return true;
						}else if(prefs.getInt("ESSAY_QUESTION_ATTEMPT_STATUS", 0)==1){
							if(localQuizId.equalsIgnoreCase(PREF_QUIZ_ID)){
								
								if(PREF_SCORE.equalsIgnoreCase("0.00")){
									showSentForEvaluationScreen(-0.1,null);
								}else{
									showSentForEvaluationScreen(Double.valueOf(PREF_SCORE),PREF_FEEDBACK);
								}
							} else if(PREF_QUIZ_ID==null){
								showSentForEvaluationScreen(-0.1,null);
							}
							return false;
							}
				}
				
			}
		
		}
		
		
		return true;
	}

	private void setNav() {
		nextBtn.setVisibility(View.VISIBLE);
		prevBtn.setVisibility(View.VISIBLE);

		if (this.quiz.hasPrevious()) {
			prevBtn.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					// save answer
					saveAnswer();

					if (QuizWidget.this.quiz.hasPrevious()) {
						QuizWidget.this.quiz.movePrevious();
						showQuestion();
					}
				}
			});
			prevBtn.setEnabled(true);
		} else {
			prevBtn.setEnabled(false);
		}

		nextBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// save answer
				if (saveAnswer()) {
					String feedback = "";
					try {
						feedback = QuizWidget.this.quiz.getCurrentQuestion()
								.getFeedback(
										prefs.getString(
												PrefsActivity.PREF_LANGUAGE,
												Locale.getDefault()
														.getLanguage()));

						if (!feedback.equals("")
								&& quiz.getShowFeedback() == Quiz.SHOW_FEEDBACK_ALWAYS
								&& !QuizWidget.this.quiz.getCurrentQuestion()
										.getFeedbackDisplayed()) {
							// We hide the keyboard before showing the dialog
							InputMethodManager imm = (InputMethodManager) QuizWidget.super
									.getActivity().getSystemService(
											Context.INPUT_METHOD_SERVICE);
							imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
							showFeedback(feedback);
						} else if (QuizWidget.this.quiz.hasNext()) {
							QuizWidget.this.quiz.moveNext();
							showQuestion();
						} else {

							// have to change over here after checking if it is
							// a essay question.

							if (q instanceof Essay
									|| showingResultForEssayQuestion) {
								// log the activity as complete

								if(localQuizId.equalsIgnoreCase(PREF_QUIZ_ID)){
									if(PREF_SCORE.equalsIgnoreCase("0.00")){
										showSentForEvaluationScreen(-0.1,null);
									}else{
										showSentForEvaluationScreen(Double.valueOf(PREF_SCORE),PREF_FEEDBACK);
									}
								}else {
									showSentForEvaluationScreen(-0.1,null);
									
								}
							} else {

								showResults();
							}
						}
					} catch (InvalidQuizException e) {
						e.printStackTrace();
					}
				} else {
					CharSequence text = QuizWidget.super.getActivity()
							.getString(R.string.widget_quiz_noanswergiven);
					int duration = Toast.LENGTH_SHORT;
					Toast toast = Toast.makeText(
							QuizWidget.super.getActivity(), text, duration);
					toast.show();
				}
			}
		});

		// set label on next button
		if (quiz.hasNext()) {
			nextBtn.setText(super.getActivity().getString(
					R.string.widget_quiz_next));
		} else {
			if (q instanceof Essay) {
				nextBtn.setText(super.getActivity().getString(
						R.string.widget_quiz_sendforevaluation));
				clickedOnSendForEvaluation = true;
			} else {
				nextBtn.setText(super.getActivity().getString(
						R.string.widget_quiz_getresults));
			}
		}
	}

	protected void showSentForEvaluationScreen(Double score,String feedback) {
		
//if(score>0.00){
		//if(score>69){
		//feedback="You did really great job on the essay question!"; 
		//}else{
		//	feedback="You should go back and study more and retake the essay."; 
		//}
//}
		prefs.edit().putInt("ESSAY_QUESTION_ATTEMPT_STATUS", 1).commit();
		prefs.edit()
				.putString("UNAME_ESSAY_QUESTION_ATTEMPT_STATUS",
						prefs.getString("prefUsername", null)).commit();
		prefs.edit()
				.putInt("COURSE_NAME_ESSAY_QUESTION_ATTEMPT_STATUS",
						course.getCourseId()).commit();
		prefs.edit()
				.putInt("ACTIVITY_ESSAY_QUESTION_ATTEMPT_STATUS",
						activity.getActId()).commit();
		prefs.edit()
		.putInt("ACTIVITY_ESSAY_QUESTION_QUIZ_ID",
				quiz.getID()).commit();
		
		

		isOnResultsPage = true;

		showingResultForEssayQuestion = true;
		
		if(clickedOnSendForEvaluation==true){

		// save results ready to send back to the quiz server
		String data = quiz.getResultObject().toString();
		DbHelper db = new DbHelper(getActivity());
		db.insertQuizResult(data, course.getCourseId());
		DatabaseManager.getInstance().closeDatabase();
		clickedOnSendForEvaluation=false;
		}

		// Check if quiz results layout is already loaded
		View quizResultsLayout = getView().findViewById(
				R.id.widget_quiz_results);
		if(score==-0.1){
		if (quizResultsLayout == null) {
			// load new layout
			View C = getView().findViewById(R.id.quiz_progress);
			ViewGroup parent = (ViewGroup) C.getParent();
			int index = parent.indexOfChild(C);
			parent.removeView(C);
			C = super.getActivity().getLayoutInflater()
					.inflate(R.layout.widget_quiz_results, parent, false);
			parent.addView(C, index);
		}

		LinearLayout layout = (LinearLayout) getView().findViewById(
				R.id.quiz_results_top_control_bar);
		layout.setVisibility(View.GONE);

		LinearLayout layoutAwait = (LinearLayout) getView().findViewById(
				R.id.quiz_results_result_await_rl);
		layoutAwait.setVisibility(View.VISIBLE);
		ListView listViewGone = (ListView) getView().findViewById(
				R.id.quiz_results_feedback);
		listViewGone.setVisibility(View.GONE);

		TextView textViewquiz_results_result_await_msg1 = (TextView) getView()
				.findViewById(R.id.quiz_results_result_await_msg1);
		TextView textViewquiz_results_result_await_msg2 = (TextView) getView()
				.findViewById(R.id.quiz_results_result_await_msg2);
		TextView textViewquiz_results__result_await_question_header = (TextView) getView()
				.findViewById(R.id.quiz_results__result_await_question_header);
		TextView textViewquiz_results__result_await_question_title = (TextView) getView()
				.findViewById(R.id.quiz_results__result_await_question_title);

		textViewquiz_results_result_await_msg1.setText(super.getActivity()
				.getString(R.string.widget_quiz_result_await_msg1));
		textViewquiz_results_result_await_msg2.setText(super.getActivity()
				.getString(R.string.widget_quiz_result_await_msg2));
		textViewquiz_results__result_await_question_header.setText(super
				.getActivity()
				.getString(R.string.widget_quiz_result_await_msg3));
		List<QuizQuestion> questions = this.quiz.getQuestions();
		QuizQuestion qmain = null;
		for (QuizQuestion q : questions) {
			if (!(q instanceof Description)) {
				q.getTitle(prefs.getString(PrefsActivity.PREF_LANGUAGE, Locale
						.getDefault().getLanguage()));
				qmain = q;
			}
		}
		if (qmain != null) {
			textViewquiz_results__result_await_question_title.setText(qmain
					.getTitle(prefs.getString(PrefsActivity.PREF_LANGUAGE,
							Locale.getDefault().getLanguage())));
		}
		}else{
			if (quizResultsLayout == null) {
				// load new layout
				View C = getView().findViewById(R.id.quiz_progress);
				ViewGroup parent = (ViewGroup) C.getParent();
				int index = parent.indexOfChild(C);
				parent.removeView(C);
				C = super.getActivity().getLayoutInflater()
						.inflate(R.layout.widget_quiz_results, parent, false);
				parent.addView(C, index);
			}

			LinearLayout layout = (LinearLayout) getView().findViewById(
					R.id.quiz_results_top_control_bar);
			layout.setVisibility(View.GONE);

			RelativeLayout layoutAwait = (RelativeLayout) getView().findViewById(
					R.id.quiz_results_result_scorecard_ll);
			layoutAwait.setVisibility(View.VISIBLE);

			TextView quiz_results_result_scorecard_percentage = (TextView) getView()
					.findViewById(R.id.quiz_results_result_scorecard_percentage);
			TextView quiz_results__result_scorecard_question_title = (TextView) getView()
					.findViewById(R.id.quiz_results__result_scorecard_question_title);
			TextView quiz_results__result_scorecard_feedback_title = (TextView) getView()
					.findViewById(R.id.quiz_results__result_scorecard_feedback_title);
			ImageView quiz_results_result_percentage_image_indecator = (ImageView) getView().findViewById(R.id.quiz_results_result_percentage_image_indecator);
			Resources res = getResources(); // need this to fetch the drawable
			if(score>0.69){
				Drawable draw = res.getDrawable( R.drawable.quiz_tick );
				quiz_results_result_percentage_image_indecator.setImageDrawable(draw);
			}else{
				Drawable draw = res.getDrawable( R.drawable.quiz_cross );
				quiz_results_result_percentage_image_indecator.setImageDrawable(draw);
			}

			quiz_results_result_scorecard_percentage.setText("You Scored "+score+"%");
			quiz_results__result_scorecard_feedback_title.setText(feedback);
			List<QuizQuestion> questions = this.quiz.getQuestions();
			QuizQuestion qmain = null;
			for (QuizQuestion q : questions) {
				if (!(q instanceof Description)) {
					q.getTitle(prefs.getString(PrefsActivity.PREF_LANGUAGE, Locale
							.getDefault().getLanguage()));
					qmain = q;
				}
			}
			if (qmain != null) {
				quiz_results__result_scorecard_question_title.setText(qmain
						.getTitle(prefs.getString(PrefsActivity.PREF_LANGUAGE,
								Locale.getDefault().getLanguage())));
			}
		}

		// Show restart or continue button
		Button restartBtn = (Button) getView().findViewById(
				R.id.quiz_results_button);

		if (this.isBaseline) {
			restartBtn.setText(super.getActivity().getString(
					R.string.widget_quiz_baseline_goto_course));
			restartBtn.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					clearEssayQuizPref();
					QuizWidget.this.getActivity().finish();
				}
			});
		} else {
			restartBtn.setText(super.getActivity().getString(
					R.string.widget_quiz_results_restart));
			restartBtn.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					clearEssayQuizPref();
					QuizWidget.this.restart();
				}

		
			});
		}

	}
	
	public void clearEssayQuizPref() {
		// TODO Auto-generated method stub
		
		
		PREF_QUIZ_ID=prefs.getString("QUIZ_ID", null);
		PREF_USER_RESPONSE=prefs.getString("USER_RESPONSE", null);
		//PREF_MAIN_SCORE=prefs.getString("MAIN_SCORE", null);
		PREF_FEEDBACK=prefs.getString("FEEDBACK", null);
		PREF_SCORE=prefs.getString("SCORE", null);
		
		prefs.edit().putInt("ESSAY_QUESTION_ATTEMPT_STATUS", 0)
				.commit();
		
		
		
		prefs.edit().putString("QUIZ_ID", null)
		.commit();
		prefs.edit().putString("USER_RESPONSE", null)
		.commit();
		prefs.edit().putString("FEEDBACK", null)
		.commit();
		prefs.edit().putString("SCORE", null)
		.commit();
		
		PREF_SCORE="-0.1";
		
		prefs.edit()
				.putString("UNAME_ESSAY_QUESTION_ATTEMPT_STATUS",
						prefs.getString("prefUsername", null))
				.commit();
		prefs.edit()
				.putInt("COURSE_NAME_ESSAY_QUESTION_ATTEMPT_STATUS",
						course.getCourseId()).commit();
		prefs.edit()
				.putInt("ACTIVITY_ESSAY_QUESTION_ATTEMPT_STATUS",
						activity.getActId()).commit();

	}

	private void setProgress() {
		TextView progress = (TextView) getView().findViewById(
				R.id.quiz_progress);
		try {
			if (quiz.getCurrentQuestion().responseExpected()) {
				progress.setText(super.getActivity()
						.getString(R.string.widget_quiz_progress,
								quiz.getCurrentQuestionNo(),
								quiz.getTotalNoQuestions()));
			} else {
				progress.setText("");
			}
		} catch (InvalidQuizException e) {
			e.printStackTrace();
		}

	}

	private boolean saveAnswer() {
		try {
			List<String> answers = qw.getQuestionResponses(quiz
					.getCurrentQuestion().getResponseOptions());
			if (answers != null) {
				quiz.getCurrentQuestion().setUserResponses(answers);
				return true;
			}
			if (!quiz.getCurrentQuestion().responseExpected()) {
				return true;
			}
		} catch (InvalidQuizException e) {
			e.printStackTrace();
		}
		return false;
	}

	private void showFeedback(String msg) {
		AlertDialog.Builder builder = new AlertDialog.Builder(
				super.getActivity());
		builder.setTitle(super.getActivity().getString(R.string.feedback));
		builder.setMessage(msg);
		try {
			if (this.quiz.getCurrentQuestion().getScoreAsPercent() >= Quiz.QUIZ_QUESTION_PASS_THRESHOLD) {
				builder.setIcon(R.drawable.quiz_tick);
			} else {
				builder.setIcon(R.drawable.quiz_cross);
			}
		} catch (InvalidQuizException e) {
			e.printStackTrace();
		}
		builder.setPositiveButton(R.string.ok,
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface arg0, int arg1) {
						if (QuizWidget.this.quiz.hasNext()) {
							QuizWidget.this.quiz.moveNext();
							showQuestion();
						} else {
							showResults();
						}
					}
				});
		builder.show();
		try {
			this.quiz.getCurrentQuestion().setFeedbackDisplayed(true);
		} catch (InvalidQuizException e) {
			e.printStackTrace();
		}
	}

	public void showResults() {
		// log the activity as complete
		isOnResultsPage = true;

		// save results ready to send back to the quiz server
		String data = quiz.getResultObject().toString();
		DbHelper db = new DbHelper(super.getActivity());
		db.insertQuizResult(data, course.getCourseId());
		DatabaseManager.getInstance().closeDatabase();

		// Check if quiz results layout is already loaded
		View quizResultsLayout = getView().findViewById(
				R.id.widget_quiz_results);
		if (quizResultsLayout == null) {
			// load new layout
			View C = getView().findViewById(R.id.quiz_progress);
			ViewGroup parent = (ViewGroup) C.getParent();
			int index = parent.indexOfChild(C);
			parent.removeView(C);
			C = super.getActivity().getLayoutInflater()
					.inflate(R.layout.widget_quiz_results, parent, false);
			parent.addView(C, index);
		}

		TextView title = (TextView) getView().findViewById(
				R.id.quiz_results_score);
		title.setText(super.getActivity().getString(
				R.string.widget_quiz_results_score, this.getPercent()));

		if (this.isBaseline) {
			TextView baselineExtro = (TextView) getView().findViewById(
					R.id.quiz_results_baseline);
			baselineExtro.setVisibility(View.VISIBLE);
			baselineExtro.setText(super.getActivity().getString(
					R.string.widget_quiz_baseline_completed));
		}

		// TODO add TextView here to give overall feedback if it's in the quiz

		// Show the detail of which questions were right/wrong
		if (quiz.getShowFeedback() == Quiz.SHOW_FEEDBACK_ALWAYS
				|| quiz.getShowFeedback() == Quiz.SHOW_FEEDBACK_ATEND) {
			ListView questionFeedbackLV = (ListView) getView().findViewById(
					R.id.quiz_results_feedback);
			ArrayList<QuizFeedback> quizFeedback = new ArrayList<QuizFeedback>();
			List<QuizQuestion> questions = this.quiz.getQuestions();
			for (QuizQuestion q : questions) {
				if (!(q instanceof Description)) {
					QuizFeedback qf = new QuizFeedback();
					qf.setScore(q.getScoreAsPercent());
					qf.setQuestionText(q.getTitle(prefs.getString(
							PrefsActivity.PREF_LANGUAGE, Locale.getDefault()
									.getLanguage())));
					qf.setUserResponse(q.getUserResponses());
					qf.setFeedbackText(q.getFeedback(prefs.getString(
							PrefsActivity.PREF_LANGUAGE, Locale.getDefault()
									.getLanguage())));
					quizFeedback.add(qf);
				}
			}
			QuizFeedbackAdapter qfa = new QuizFeedbackAdapter(
					super.getActivity(), quizFeedback);
			questionFeedbackLV.setAdapter(qfa);
			questionFeedbackLV.setVisibility(View.VISIBLE);
		}

		// Show restart or continue button
		Button restartBtn = (Button) getView().findViewById(
				R.id.quiz_results_button);

		if (this.isBaseline) {
			restartBtn.setText(super.getActivity().getString(
					R.string.widget_quiz_baseline_goto_course));
			restartBtn.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					QuizWidget.this.getActivity().finish();
				}
			});
		} else {
			restartBtn.setText(super.getActivity().getString(
					R.string.widget_quiz_results_restart));
			restartBtn.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					QuizWidget.this.restart();
				}
			});
		}
	}

	private void restart() {
		this.setStartTime(System.currentTimeMillis() / 1000);

		this.quiz = new Quiz();
		this.quiz
				.load(quizContent, prefs.getString(PrefsActivity.PREF_LANGUAGE,
						Locale.getDefault().getLanguage()));
		this.isOnResultsPage = false;

		// reload quiz layout
		View C = getView().findViewById(R.id.widget_quiz_results);
		ViewGroup parent = (ViewGroup) C.getParent();
		int index = parent.indexOfChild(C);
		parent.removeView(C);
		C = super.getActivity().getLayoutInflater()
				.inflate(R.layout.widget_quiz, parent, false);
		parent.addView(C, index);

		this.prevBtn = (Button) getView().findViewById(R.id.mquiz_prev_btn);
		this.nextBtn = (Button) getView().findViewById(R.id.mquiz_next_btn);
		this.qText = (TextView) getView().findViewById(R.id.question_text);
		this.questionImage = (LinearLayout) getView().findViewById(
				R.id.question_image);
		this.questionImage.setVisibility(View.GONE);
		this.showQuestion();
	}

	@Override
	protected boolean getActivityCompleted() {
		int passThreshold;
		Log.d(TAG, "Threshold:" + quiz.getPassThreshold());
		if (quiz.getPassThreshold() != 0) {
			passThreshold = quiz.getPassThreshold();
		} else {
			passThreshold = Quiz.QUIZ_DEFAULT_PASS_THRESHOLD;
		}
		Log.d(TAG, "Percent:" + this.getPercent());
		if (isOnResultsPage && this.getPercent() >= passThreshold) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void saveTracker() {
		long timetaken = System.currentTimeMillis() / 1000
				- this.getStartTime();
		Tracker t = new Tracker(super.getActivity());
		JSONObject obj = new JSONObject();
		if (!isOnResultsPage) {
			return;
		}
		// add in extra meta-data
		try {
			MetaDataUtils mdu = new MetaDataUtils(super.getActivity());
			obj.put("timetaken", timetaken);
			obj = mdu.getMetaData(obj);
			String lang = prefs.getString(PrefsActivity.PREF_LANGUAGE, Locale
					.getDefault().getLanguage());
			obj.put("lang", lang);
			obj.put("quiz_id", quiz.getID());
			obj.put("instance_id", quiz.getInstanceID());
			obj.put("score", this.getPercent());
			// if it's a baseline activity then assume completed
			if (this.isBaseline) {
				t.saveTracker(course.getCourseId(), activity.getDigest(), obj,
						true);
			} else {
				t.saveTracker(course.getCourseId(), activity.getDigest(), obj,
						this.getActivityCompleted());
			}
		} catch (JSONException e) {
			// Do nothing
		} catch (NullPointerException npe) {
			// do nothing
		}

	}

	@Override
	public HashMap<String, Object> getWidgetConfig() {
		HashMap<String, Object> config = new HashMap<String, Object>();
		// this.saveAnswer();
		config.put("quiz", this.quiz);
		config.put("Activity_StartTime", this.getStartTime());
		config.put("OnResultsPage", this.isOnResultsPage);
		return config;
	}

	@Override
	public void setWidgetConfig(HashMap<String, Object> config) {
		if (config.containsKey("quiz")) {
			this.quiz = (Quiz) config.get("quiz");
		}
		if (config.containsKey("Activity_StartTime")) {
			this.setStartTime((Long) config.get("Activity_StartTime"));
		}
		if (config.containsKey("OnResultsPage")) {
			this.isOnResultsPage = (Boolean) config.get("OnResultsPage");
		}
	}

	@Override
	public String getContentToRead() {
		// Get the current question text
		String toRead = "";
		try {
			toRead = quiz.getCurrentQuestion().getTitle(
					prefs.getString(PrefsActivity.PREF_LANGUAGE, Locale
							.getDefault().getLanguage()));
		} catch (InvalidQuizException e) {
			e.printStackTrace();
		}
		return toRead;
	}

	private float getPercent() {
		quiz.mark(prefs.getString(PrefsActivity.PREF_LANGUAGE, Locale
				.getDefault().getLanguage()));
		float percent = quiz.getUserscore() * 100 / quiz.getMaxscore();
		return percent;
	}

	private class OnImageClickListener implements OnClickListener {

		private Context ctx;
		private String type;

		public OnImageClickListener(Context ctx, String type) {
			this.ctx = ctx;
			this.type = type;
		}

		public void onClick(View v) {
			File file = (File) v.getTag();
			// check the file is on the file system (should be but just in case)
			if (!file.exists()) {
				Toast.makeText(
						this.ctx,
						this.ctx.getString(R.string.error_resource_not_found,
								file.getName()), Toast.LENGTH_LONG).show();
				return;
			}
			Uri targetUri = Uri.fromFile(file);
			// check there is actually an app installed to open this filetype
			Intent intent = ExternalResourceOpener.getIntentToOpenResource(ctx,
					targetUri, type);
			if (intent != null) {
				this.ctx.startActivity(intent);
			} else {
				Toast.makeText(
						this.ctx,
						this.ctx.getString(
								R.string.error_resource_app_not_found,
								file.getName()), Toast.LENGTH_LONG).show();
			}
		}

	}

	private class OnMediaClickListener implements OnClickListener {

		private String mediaFileName;

		public OnMediaClickListener(String mediaFileName) {
			this.mediaFileName = mediaFileName;
		}

		public void onClick(View v) {
			// check video file exists
			boolean exists = FileUtils.mediaFileExists(
					QuizWidget.super.getActivity(), mediaFileName);
			if (!exists) {
				Toast.makeText(
						QuizWidget.super.getActivity(),
						QuizWidget.super.getActivity().getString(
								R.string.error_media_not_found, mediaFileName),
						Toast.LENGTH_LONG).show();
				return;
			}

			String mimeType = FileUtils.getMimeType(FileUtils
					.getMediaPath(QuizWidget.super.getActivity())
					+ mediaFileName);
			if (!FileUtils.supportedMediafileType(mimeType)) {
				Toast.makeText(
						QuizWidget.super.getActivity(),
						QuizWidget.super.getActivity()
								.getString(R.string.error_media_unsupported,
										mediaFileName), Toast.LENGTH_LONG)
						.show();
				return;
			}

			Intent intent = new Intent(QuizWidget.super.getActivity(),
					VideoPlayerActivity.class);
			Bundle tb = new Bundle();
			tb.putSerializable(VideoPlayerActivity.MEDIA_TAG, mediaFileName);
			tb.putSerializable(Activity.TAG, activity);
			tb.putSerializable(Course.TAG, course);
			intent.putExtras(tb);
			startActivity(intent);
		}

	}

}
