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
import org.hopegames.mobile.learning.R;
import org.hopegames.mobile.listener.SubmitListener;
import org.hopegames.mobile.model.User;
import org.hopegames.mobile.utils.HTTPConnectionUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.bugsense.trace.BugSenseHandler;
import com.google.gson.Gson;

public class MessageDetailTask extends AsyncTask<Payload, Object, Payload> {

	public static final String TAG = LoginTask.class.getSimpleName();

	private Context ctx;
	private SharedPreferences prefs;
	private SubmitListener mStateListener;
	private String mUserIdFrom;
	
	public MessageDetailTask(Context c) {
		this.ctx = c;
		prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
	}

	@Override
	protected Payload doInBackground(Payload... params) {

		Payload payload = params[0];
		User u = (User) payload.getData().get(0);
		HTTPConnectionUtils client = new HTTPConnectionUtils(ctx);

		String url = "http://tmpmoodle.hopecybrary.org/webservice/rest/server.php?moodlewsrestformat=json";
		JSONObject json = new JSONObject();
		
		HttpPost httpPost = new HttpPost(url);
		try {
			// update progress dialog
			publishProgress(ctx.getString(R.string.login_process));
			// add post params
			List<NameValuePair> paramsPost = new ArrayList<NameValuePair>(2);
			paramsPost.add(new BasicNameValuePair("limitfrom","0"));
			paramsPost.add(new BasicNameValuePair("limitnum", ""+u.getLimitnum()));
			paramsPost.add(new BasicNameValuePair("moodlewssettingfilter", "true"));
			paramsPost.add(new BasicNameValuePair("newestfirst","1"));
			paramsPost.add(new BasicNameValuePair("read",""+u.getRead()));
			paramsPost.add(new BasicNameValuePair("type", "conversations"));
			paramsPost.add(new BasicNameValuePair("useridfrom",u.getMsgUserIdFrom() ));
			paramsPost.add(new BasicNameValuePair("useridto",""+u.getMsgUserIdTo()));
			Log.e("From", ""+u.getMsgUserIdFrom());
			Log.e("To", ""+u.getMsgUserIdTo());
			paramsPost.add(new BasicNameValuePair("wsfunction", "local_mobile_core_message_get_messages"));
			paramsPost.add(new BasicNameValuePair("wstoken", u.getToken()));
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
					JSONObject jsonResp1 = new JSONObject(responseStr);
					payload.setResult(false);
					Gson gson = new Gson();
					payload = gson.fromJson(responseStr, Payload.class);
				
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

