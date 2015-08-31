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

package org.hopegames.mobile.activity;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.hopegames.mobile.application.MobileLearning;
import org.hopegames.mobile.learning.R;
import org.hopegames.mobile.listener.APIRequestListener;
import org.hopegames.mobile.listener.InstallCourseListener;
import org.hopegames.mobile.listener.PostInstallListener;
import org.hopegames.mobile.listener.UpgradeListener;
import org.hopegames.mobile.model.DownloadProgress;
import org.hopegames.mobile.model.Tag;
import org.hopegames.mobile.task.APIRequestTask;
import org.hopegames.mobile.task.GetEssayAnswerStatusTask;
import org.hopegames.mobile.task.InstallDownloadedCoursesTask;
import org.hopegames.mobile.task.Payload;
import org.hopegames.mobile.task.PostInstallTask;
import org.hopegames.mobile.task.UpgradeManagerTask;
import org.hopegames.mobile.utils.storage.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TextView;

import com.bugsense.trace.BugSenseHandler;

public class StartUpActivity extends Activity implements UpgradeListener, PostInstallListener, InstallCourseListener{

	public final static String TAG = StartUpActivity.class.getSimpleName();
	private TextView tvProgress;
	private SharedPreferences prefs;
	private ProgressDialog progressDialog;
	private String url;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BugSenseHandler.initAndStartSession(this, MobileLearning.BUGSENSE_API_KEY);
        
        setContentView(R.layout.start_up);
        tvProgress = (TextView) this.findViewById(R.id.start_up_progress);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        BugSenseHandler.setUserIdentifier(prefs.getString(PrefsActivity.PREF_USER_NAME, "anon"));
        
        UpgradeManagerTask umt = new UpgradeManagerTask(this);
		umt.setUpgradeListener(this);
		ArrayList<Object> data = new ArrayList<Object>();
 		Payload p = new Payload(data);
		umt.execute(p);
		
		getEssayQuestionStatus();
 		
	}
	
	
    private void getEssayQuestionStatus() {
		
    	
    	//?format=json&quiz=36&user=1&username=admin&api_key=fc548cfafae206822431649e50d7ad68
    	
    	
    	int attempt_status = prefs.getInt("ESSAY_QUESTION_ATTEMPT_STATUS",0);
    	if(attempt_status==1){
    	String username = prefs.getString(PrefsActivity.PREF_USER_NAME, "anon");
    	String apikey = prefs.getString(PrefsActivity.PREF_API_KEY,"");
    	
    	int quiz_id = prefs.getInt("ACTIVITY_ESSAY_QUESTION_QUIZ_ID",-1);
    	String user_id_combine =  prefs.getString("user_id","");
    	String[] splitForuserId = user_id_combine.split("/");
    	String user_id = splitForuserId[4];
    	
    	String url_to_hit = this.getString(R.string.prefServerDefault) + MobileLearning.GET_ESSAY_ANSWER+"?format=json&quiz="+quiz_id+"&user="+user_id+"&username="+username+"&api_key="+apikey;
    	Log.e("url_to_hit",url_to_hit);
    	if(isOnline())
    	new JSONAsyncTask().execute(url_to_hit);
    	}
		
	}


	private void updateProgress(String text){
    	if(tvProgress != null){
    		tvProgress.setText(text);
    	}
    }
	
	private void endStartUpScreen() {
        // launch new activity and close splash screen
		if (!MobileLearning.isLoggedIn(this)) {
			startActivity(new Intent(StartUpActivity.this, WelcomeActivity.class));
			finish();
		} else {
			startActivity(new Intent(StartUpActivity.this, OppiaMobileActivity.class));
			finish();
		}
    }

	private void installCourses(){
		File dir = new File(FileUtils.getDownloadPath(this));
		String[] children = dir.list();
		if (children != null) {
			ArrayList<Object> data = new ArrayList<Object>();
     		Payload payload = new Payload(data);
			InstallDownloadedCoursesTask imTask = new InstallDownloadedCoursesTask(this);
			imTask.setInstallerListener(this);
			imTask.execute(payload);
		} else {
			endStartUpScreen();
		}
	}
	
	public void upgradeComplete(Payload p) {
		
		 // set up local dirs
 		if(!FileUtils.createDirs(this)){
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			builder.setCancelable(false);
 			builder.setTitle(R.string.error);
 			builder.setMessage(R.string.error_sdcard);
 			builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int which) {
 					StartUpActivity.this.finish();
 				}
 			});
 			builder.show();
 			return;
 		}
 		
		if(p.isResult()){
			Payload payload = new Payload();
			PostInstallTask piTask = new PostInstallTask(this);
			piTask.setPostInstallListener(this);
			piTask.execute(payload);
		} else {
			// now install any new courses
			this.installCourses();
		}
		
	}

	public void upgradeProgressUpdate(String s) {
		this.updateProgress(s);
	}

	public void postInstallComplete(Payload response) {
		this.installCourses();
	}

	public void downloadComplete(Payload p) {
		// do nothing
		
	}

	public void downloadProgressUpdate(DownloadProgress dp) {
		// do nothing
		
	}

	public void installComplete(Payload p) {
		if(p.getResponseData().size()>0){
			Editor e = prefs.edit();
			e.putLong(PrefsActivity.PREF_LAST_MEDIA_SCAN, 0);
			e.commit();
		}
		endStartUpScreen();	
	}

	public void installProgressUpdate(DownloadProgress dp) {
		this.updateProgress(dp.getMessage());
	}
	
	
	
	
	/*
	 * 
	 * 
	 */
	class JSONAsyncTask extends AsyncTask<String, Void, Boolean> {


		@Override
		protected void onPreExecute() {
		    super.onPreExecute();

		}

		@Override
		protected Boolean doInBackground(String... urls) {
		    try {

		        //------------------>>
		        HttpGet httppost = new HttpGet(urls[0]);
		        HttpClient httpclient = new DefaultHttpClient();
		        HttpResponse response = httpclient.execute(httppost);

		        // StatusLine stat = response.getStatusLine();
		        int status = response.getStatusLine().getStatusCode();

		        if (status == 200 || status == 201) {
		            HttpEntity entity = response.getEntity();
		            String data = EntityUtils.toString(entity);

		            Log.d("response",data);
		            
		            JSONObject jsonObjMain = new JSONObject(data);
		            
		            JSONArray arrayObjects = jsonObjMain.getJSONArray("objects");
		            
					JSONObject lastArrayObjects = arrayObjects.getJSONObject(arrayObjects.length()-1);
					
					String scoreMain = lastArrayObjects.getString("score");
					
					String quizIdCombine  = lastArrayObjects.getString("quiz");
					
					String[] splitForuserId = quizIdCombine.split("/");
					
			    	String quizId = splitForuserId[4];
					
					JSONArray arrayObjectResponse = lastArrayObjects.getJSONArray("responses");
					
					
					JSONObject arrayObjectResponseObject = arrayObjectResponse.getJSONObject(0);
					
					String feedback = arrayObjectResponseObject.getString("feedback");
					
					String score = arrayObjectResponseObject.getString("score");
					
					String userresponse = arrayObjectResponseObject.getString("text");
					
					Log.e("scoreMain",scoreMain);
					Log.e("feedback",feedback);
					Log.e("score",score);
					Log.e("userresponse",userresponse);
					Log.e("quizId",quizId);
					
					
					
					prefs.edit().putString("QUIZ_ID", quizId).commit();
					prefs.edit().putString("USER_RESPONSE", userresponse).commit();
					prefs.edit().putString("MAIN_SCORE", scoreMain).commit();
					prefs.edit().putString("FEEDBACK", feedback).commit();
					prefs.edit().putString("SCORE", score).commit();


		            return true;
		        }


		    } catch (IOException e) {
		        e.printStackTrace();
		    } catch (JSONException e) {

		        e.printStackTrace();
		    }
		    return false;
		}

		protected void onPostExecute(Boolean result) {
			
			
			
			 
		}
	}
	
	
	
	public boolean isOnline() {
	    ConnectivityManager cm =
	        (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo netInfo = cm.getActiveNetworkInfo();
	    return netInfo != null && netInfo.isConnectedOrConnecting();
	}
}
