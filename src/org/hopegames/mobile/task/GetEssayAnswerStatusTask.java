package org.hopegames.mobile.task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.hopegames.mobile.learning.R;
import org.hopegames.mobile.listener.APIRequestListener;
import org.hopegames.mobile.utils.HTTPConnectionUtils;

import android.content.Context;
import android.os.AsyncTask;

public class GetEssayAnswerStatusTask extends AsyncTask<Payload, Object, Payload>{
	
	public static final String TAG = APIRequestTask.class.getSimpleName();
	protected Context ctx;
	private APIRequestListener requestListener;
	
	public GetEssayAnswerStatusTask(Context ctx) {
		this.ctx = ctx;
	}
	
	@Override
	protected Payload doInBackground(Payload... params){
		
		Payload payload = params[0];
		String responseStr = "";
		
		HTTPConnectionUtils client = new HTTPConnectionUtils(ctx);
		String url = client.getFullURL(payload.getUrl());
		HttpGet httpGet = new HttpGet(url);
		httpGet.addHeader(client.getAuthHeader());
		
		try {
			
			// make request
			HttpResponse response = client.execute(httpGet);
		
			// read response
			InputStream content = response.getEntity().getContent();
			BufferedReader buffer = new BufferedReader(new InputStreamReader(content), 1024);
			String s = "";
			while ((s = buffer.readLine()) != null) {
				responseStr += s;
			}
			
			
			switch (response.getStatusLine().getStatusCode()){
				// TODO check the unauthorised response code...
				case 400: // unauthorised
					payload.setResult(false);
					payload.setResultResponse(ctx.getString(R.string.error_login));
					break;
				case 200: 
					payload.setResult(true);
					payload.setResultResponse(responseStr);
					break;
				default:
					payload.setResult(false);
					payload.setResultResponse(ctx.getString(R.string.error_connection));
			}
			
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			payload.setResult(false);
			payload.setResultResponse(ctx.getString(R.string.error_connection));
		} catch (IOException e) {
			e.printStackTrace();
			payload.setResult(false);
			payload.setResultResponse(ctx.getString(R.string.error_connection));
		}
		return payload;
	}
	
	@Override
	protected void onPostExecute(Payload response) {
		synchronized (this) {
            if (requestListener != null) {
               requestListener.apiRequestComplete(response);
            }
        }
	}
	
	public void setAPIRequestListener(APIRequestListener srl) {
        synchronized (this) {
        	requestListener = srl;
        }
    }
}
