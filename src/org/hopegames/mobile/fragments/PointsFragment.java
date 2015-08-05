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

package org.hopegames.mobile.fragments;

import java.util.ArrayList;

import org.hopegames.mobile.adapter.PointsListAdapter;
import org.hopegames.mobile.application.MobileLearning;
import org.hopegames.mobile.learning.R;
import org.hopegames.mobile.listener.APIRequestListener;
import org.hopegames.mobile.model.Points;
import org.hopegames.mobile.task.APIRequestTask;
import org.hopegames.mobile.task.Payload;
import org.hopegames.mobile.utils.ConnectionUtils;
import org.hopegames.mobile.utils.UIUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;

import com.bugsense.trace.BugSenseHandler;

public class PointsFragment extends Fragment implements APIRequestListener {

	public static final String TAG = PointsFragment.class.getSimpleName();
	private JSONObject json;
	
	public static PointsFragment newInstance() {
		PointsFragment myFragment = new PointsFragment();
	    return myFragment;
	}

	public PointsFragment(){
		
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View vv = super.getLayoutInflater(savedInstanceState).inflate(R.layout.fragment_points, null);
		LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		vv.setLayoutParams(lp);
		return vv;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getPoints();
	}
	
	private void getPoints(){
		if(!ConnectionUtils.isOffLineMode(super.getActivity())){
		APIRequestTask task = new APIRequestTask(super.getActivity());
		Payload p = new Payload(MobileLearning.SERVER_POINTS_PATH);
		task.setAPIRequestListener(this);
		task.execute(p);
		}
	}

	private void refreshPointsList() {
		try {
			ArrayList<Points> points = new ArrayList<Points>();
			
			for (int i = 0; i < (json.getJSONArray("objects").length()); i++) {
				JSONObject json_obj = (JSONObject) json.getJSONArray("objects").get(i);
				Points p = new Points();
				p.setDescription(json_obj.getString("description"));
				p.setDateTime(json_obj.getString("date"));
				p.setPoints(json_obj.getInt("points"));

				points.add(p);
			}
			TextView tv = (TextView) super.getActivity().findViewById(R.id.fragment_points_title);
			tv.setVisibility(View.GONE);
			
			PointsListAdapter pla = new PointsListAdapter(super.getActivity(), points);
			ListView listView = (ListView) super.getActivity().findViewById(R.id.points_list);
			listView.setAdapter(pla);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	public void apiRequestComplete(Payload response) {
		if(response.isResult()){
			try {
				json = new JSONObject(response.getResultResponse());
				refreshPointsList();
			} catch (JSONException e) {
				BugSenseHandler.sendException(e);
				UIUtils.showAlert(super.getActivity(), R.string.loading, R.string.error_connection);
				e.printStackTrace();
			}
		} else {
			TextView tv = (TextView) super.getActivity().findViewById(R.id.fragment_points_title);
			tv.setVisibility(View.VISIBLE);
			tv.setText(R.string.error_connection_required);
		}
	}


}
