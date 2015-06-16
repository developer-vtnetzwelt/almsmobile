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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.Callable;

import org.hopegames.mobile.adapter.DownloadCourseListAdapter;
import org.hopegames.mobile.application.DatabaseManager;
import org.hopegames.mobile.application.DbHelper;
import org.hopegames.mobile.application.MobileLearning;
import org.hopegames.mobile.learning.R;
import org.hopegames.mobile.listener.APIRequestListener;
import org.hopegames.mobile.listener.DownloadCompleteListener;
import org.hopegames.mobile.listener.ListInnerBtnOnClickListener;
import org.hopegames.mobile.model.Course;
import org.hopegames.mobile.model.Lang;
import org.hopegames.mobile.model.Tag;
import org.hopegames.mobile.task.APIRequestTask;
import org.hopegames.mobile.task.DownloadCourseTask;
import org.hopegames.mobile.task.DownloadTasksController;
import org.hopegames.mobile.task.Payload;
import org.hopegames.mobile.task.ScheduleUpdateTask;
import org.hopegames.mobile.utils.UIUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ListView;

import com.bugsense.trace.BugSenseHandler;

public class DownloadActivity extends AppActivity implements APIRequestListener, DownloadCompleteListener {
	
	public static final String TAG = DownloadActivity.class.getSimpleName();
	
	private SharedPreferences prefs;
	private ProgressDialog progressDialog;
	private JSONObject json;
	private DownloadCourseListAdapter dla;
	private String url;
	private ArrayList<Course> courses;
	private boolean showUpdatesOnly = false;

	private DownloadTasksController tasksController;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_download);
		getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        
		Bundle bundle = this.getIntent().getExtras(); 
        if(bundle != null) {
        	Tag t = (Tag) bundle.getSerializable(Tag.TAG);
        	this.url = MobileLearning.SERVER_TAG_PATH + String.valueOf(t.getId()) + File.separator;
        } else {
        	this.url = MobileLearning.SERVER_COURSES_PATH;
        	this.showUpdatesOnly = true;
        }

        courses = new ArrayList<Course>();
        dla = new DownloadCourseListAdapter(this, courses);
        dla.setOnClickListener(new CourseListListener());
        ListView listView = (ListView) findViewById(R.id.tag_list);
        listView.setAdapter(dla);
	}
	
	@Override
	public void onResume(){
		super.onResume();
		if(json == null){
            //The JSON download task has not started or been completed yet
			getCourseList();
		} else if ((courses != null) && courses.size()>0) {
            //We already have loaded JSON and courses (coming from orientationchange)
            dla.notifyDataSetChanged();
        }
        else{
            //The JSON is downloaded but course list is not
	        refreshCourseList();
		}

        if (tasksController == null){
            tasksController = new DownloadTasksController(this, prefs);
        }
        tasksController.setOnDownloadCompleteListener(this);
        tasksController.setCtx(this);
	}

	@Override
	public void onPause(){
		//Kill any open dialogs
		if (progressDialog != null){
            progressDialog.dismiss();
        }
		super.onPause();
	}

    @Override
    public void onDestroy(){
        tasksController.setOnDownloadCompleteListener(null);
        tasksController.setCtx(null);
        super.onDestroy();
    }
	
	@SuppressWarnings("unchecked")
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

	    try {
			this.json = new JSONObject(savedInstanceState.getString("json"));
            ArrayList<Course> savedCourses = (ArrayList<Course>) savedInstanceState.getSerializable("courses");
            this.courses.addAll(savedCourses);
		} catch (Exception e) {
            // error in the json so just get the list again
        }
        tasksController = (DownloadTasksController) savedInstanceState.getParcelable("tasksProgress");
	}

	@Override
	protected void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
            if (json != null){
                //Only save the instance if the request has been proccessed already
                savedInstanceState.putString("json", json.toString());
                savedInstanceState.putSerializable("courses", courses);

                if (tasksController != null){
                    tasksController.setOnDownloadCompleteListener(null);
                    savedInstanceState.putParcelable("tasksProgress", tasksController);
                }
            }

	}
	
	private void getCourseList() {
		// show progress dialog
		progressDialog = new ProgressDialog(this);
		progressDialog.setTitle(R.string.loading);
		progressDialog.setMessage(getString(R.string.loading));
		progressDialog.setCancelable(true);
		progressDialog.show();

		APIRequestTask task = new APIRequestTask(this);
		Payload p = new Payload(url);
		task.setAPIRequestListener(this);
		task.execute(p);
	}

	public void refreshCourseList() {
		// process the response and display on screen in listview
		// Create an array of courses, that will be put to our ListActivity

		DbHelper db = new DbHelper(this);
		try {
			this.courses.clear();
			
			for (int i = 0; i < (json.getJSONArray(MobileLearning.SERVER_COURSES_NAME).length()); i++) {
				JSONObject json_obj = (JSONObject) json.getJSONArray(MobileLearning.SERVER_COURSES_NAME).get(i);
				Course dc = new Course(prefs.getString(PrefsActivity.PREF_STORAGE_LOCATION, ""));
				
				ArrayList<Lang> titles = new ArrayList<Lang>();
				JSONObject jsonTitles = json_obj.getJSONObject("title");
				Iterator<?> keys = jsonTitles.keys();
		        while( keys.hasNext() ){
		            String key = (String) keys.next();
		            Lang l = new Lang(key,jsonTitles.getString(key));
					titles.add(l);
		        }
		        dc.setTitles(titles);
		        
		        ArrayList<Lang> descriptions = new ArrayList<Lang>();
		        if (json_obj.has("description") && !json_obj.isNull("description")){
		        	try {
						JSONObject jsonDescriptions = json_obj.getJSONObject("description");
						Iterator<?> dkeys = jsonDescriptions.keys();
				        while( dkeys.hasNext() ){
				            String key = (String) dkeys.next();
				            if (!jsonDescriptions.isNull(key)){
					            Lang l = new Lang(key,jsonDescriptions.getString(key));
					            descriptions.add(l);
				            }
				        }
				        dc.setDescriptions(descriptions);
		        	} catch (JSONException jsone){
		        		//do nothing
		        	}
		        }
		        
		        dc.setShortname(json_obj.getString("shortname"));
		        dc.setVersionId(json_obj.getDouble("version"));
		        dc.setDownloadUrl(json_obj.getString("url"));
		        try {
		        	dc.setDraft(json_obj.getBoolean("is_draft"));
		        }catch (JSONException je){
		        	dc.setDraft(false);
		        }
		        dc.setInstalled(db.isInstalled(dc.getShortname()));
		        dc.setToUpdate(db.toUpdate(dc.getShortname(), dc.getVersionId()));
				if (json_obj.has("schedule_uri")){
					dc.setScheduleVersionID(json_obj.getDouble("schedule"));
					dc.setScheduleURI(json_obj.getString("schedule_uri"));
					dc.setToUpdateSchedule(db.toUpdateSchedule(dc.getShortname(), dc.getScheduleVersionID()));
				}
				if (!this.showUpdatesOnly || dc.isToUpdate()){
					this.courses.add(dc);
				} 
			}

            dla.notifyDataSetChanged();

		} catch (Exception e) {
			db.close();
			BugSenseHandler.sendException(e);
			e.printStackTrace();
			UIUtils.showAlert(this, R.string.loading, R.string.error_processing_response);
		}
		DatabaseManager.getInstance().closeDatabase();
	}
	
	public void apiRequestComplete(Payload response) {
		// close dialog and process results
		progressDialog.dismiss();
	
		if(response.isResult()){
			try {
				json = new JSONObject(response.getResultResponse());
				refreshCourseList();
			} catch (JSONException e) {
				BugSenseHandler.sendException(e);
				e.printStackTrace();
				UIUtils.showAlert(this, R.string.loading, R.string.error_connection);
				
			}
		} else {
			UIUtils.showAlert(this, R.string.error, R.string.error_connection_required, new Callable<Boolean>() {
				public Boolean call() throws Exception {
					DownloadActivity.this.finish();
					return true;
				}
			});
		}
	}

    //@Override
    public void onComplete(Payload p) {
        refreshCourseList();
    }

    private class CourseListListener implements ListInnerBtnOnClickListener {
        //@Override
        public void onClick(int position) {
            Log.d("course-download", "Clicked " + position);
            Course courseSelected = courses.get(position);
            if (!tasksController.isTaskInProgress()){
                ArrayList<Object> data = new ArrayList<Object>();
                data.add(courseSelected);
                Payload p = new Payload(data);

                if(!courseSelected.isInstalled() || courseSelected.isToUpdate()){
                    tasksController.setTaskInProgress(true);
                    tasksController.showDialog();
                    DownloadCourseTask downloadTask = new DownloadCourseTask(DownloadActivity.this);
                    downloadTask.setInstallerListener(tasksController);
                    downloadTask.execute(p);
                }
                else if(courseSelected.isToUpdateSchedule()){
                    tasksController.setTaskInProgress(true);
                    tasksController.showDialog();
                    ScheduleUpdateTask updateTask = new ScheduleUpdateTask(DownloadActivity.this);
                    updateTask.setUpdateListener(tasksController);
                    updateTask.execute(p);
                }
            }
        }
    }

}
