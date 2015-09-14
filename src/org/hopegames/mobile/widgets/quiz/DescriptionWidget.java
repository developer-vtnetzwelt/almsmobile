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

package org.hopegames.mobile.widgets.quiz;

import java.util.List;

import org.digitalcampus.mobile.quiz.model.Response;
import org.hopegames.mobile.learning.R;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class DescriptionWidget extends QuestionWidget{

	public static final String TAG = DescriptionWidget.class.getSimpleName();
	
	public DescriptionWidget(Activity activity,  View v, ViewGroup container) {
		LinearLayout ll = (LinearLayout) v.findViewById(R.id.quiz_response_widget);
		ll.removeAllViews();
	}
	
	@Override
	public void setQuestionResponses(List<Response> responses, List<String> currentAnswers) {
		// do nothing
		
	}

	@Override
	public List<String> getQuestionResponses(List<Response> responses) {
		// do nothing
		return null;
	}

}
