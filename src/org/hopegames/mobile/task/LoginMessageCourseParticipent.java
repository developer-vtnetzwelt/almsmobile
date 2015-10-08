package org.hopegames.mobile.task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.hopegames.mobile.activity.PrefsActivity;
import org.hopegames.mobile.application.MobileLearning;
import org.hopegames.mobile.learning.R;
import org.hopegames.mobile.listener.SubmitListener;
import org.hopegames.mobile.model.User;
import org.hopegames.mobile.utils.HTTPConnectionUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import com.bugsense.trace.BugSenseHandler;
import com.google.gson.Gson;

public class LoginMessageCourseParticipent extends AsyncTask<Payload, Object, Payload> {

	public static final String TAG = LoginTask.class.getSimpleName();

	private Context ctx;
	private SharedPreferences prefs;
	private SubmitListener mStateListener;
	
	public LoginMessageCourseParticipent(Context c) {
		this.ctx = c;
		prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
	}

	@Override
	protected Payload doInBackground(Payload... params) {

		Payload payload = params[0];
		User u = (User) payload.getData().get(0);
		HTTPConnectionUtils client = new HTTPConnectionUtils(ctx);

		//String url = "http://tmpmoodle.hopecybrary.org/webservice/rest/server.php?moodlewsrestformat=json";
		String url = prefs.getString(PrefsActivity.PREF_MOODLE_SERVER, ctx.getString(R.string.prefMoodleServerDefault)) + MobileLearning.SERVER_MOODLE_COMMON_URL_NAME;

		JSONObject json = new JSONObject();
		
		HttpPost httpPost = new HttpPost(url);
		try {
			// update progress dialog
			publishProgress(ctx.getString(R.string.login_process));
			// add post params
			List<NameValuePair> paramsPost = new ArrayList<NameValuePair>(2);
			paramsPost.add(new BasicNameValuePair("courseid", ""+u.getCourseid()));
			paramsPost.add(new BasicNameValuePair("options[0][name]","limitfrom"));
			paramsPost.add(new BasicNameValuePair("options[0][value]", "0"));
			paramsPost.add(new BasicNameValuePair("options[1][name]", "limitnumber"));
			paramsPost.add(new BasicNameValuePair("options[1][value]", "100"));
			paramsPost.add(new BasicNameValuePair("wsfunction", "core_enrol_get_enrolled_users"));
			paramsPost.add(new BasicNameValuePair("wstoken", u.getToken()));
			paramsPost.add(new BasicNameValuePair("moodlewssettingfilter", "true"));
			
			httpPost.setEntity(new UrlEncodedFormEntity(paramsPost, "UTF-8"));

			// make request
			HttpResponse response = client.execute(httpPost);

			// read response
			InputStream content = response.getEntity().getContent();
			BufferedReader buffer = new BufferedReader(new InputStreamReader(content), 4096);
			String responseStr = "";
			String s = "";

			while ((s = buffer.readLine()) != null) {
				responseStr += s;
			}
			
			// check status code
			switch (response.getStatusLine().getStatusCode()){
				case 400: // unauthorised
					payload.setResult(false);
					payload.setResultResponse(ctx.getString(R.string.error_login));
					break;
				case 200: // authorised
					List<CourseParticipant> courseList = new ArrayList<CourseParticipant>();
					JSONArray jsonResp1 = new JSONArray(responseStr);
					for(int i = 0;i<jsonResp1.length();i++){
						 Gson gson = new Gson();
						 CourseParticipant courseParticipant = gson.fromJson(jsonResp1.get(i).toString(), CourseParticipant.class);
						 
						 courseList.add(courseParticipant) ;
						
					}
					 
					
					payload.setCourseparticipant(courseList);
					payload.setResult(false);
				
					break;
				default:
					payload.setResult(false);
					payload.setResultResponse(ctx.getString(R.string.error_connection));
			}
			

		} catch (UnsupportedEncodingException e) {
			payload.setResult(false);
			payload.setResultResponse(ctx.getString(R.string.error_connection));
		} catch (ClientProtocolException e) {
			payload.setResult(false);
			payload.setResultResponse(ctx.getString(R.string.error_connection));
		} catch (IOException e) {
			payload.setResult(false);
			payload.setResultResponse(ctx.getString(R.string.error_connection));
		} catch (JSONException e) {
			BugSenseHandler.sendException(e);
			e.printStackTrace();
			payload.setResult(false);
			payload.setResultResponse(ctx.getString(R.string.error_processing_response));
		} finally {

		}
		return payload;
	}

	@Override
	protected void onPostExecute(Payload response) {
		synchronized (this) {
            if (mStateListener != null) {
               mStateListener.submitComplete(response);
            }
        }
	}
	
	public void setLoginListener(SubmitListener srl) {
        synchronized (this) {
            mStateListener = srl;
        }
    }
}

