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

import java.util.ArrayList;

import org.hopegames.mobile.application.ScheduleReminders;
import org.hopegames.mobile.learning.R;

import android.support.v4.app.FragmentActivity;
import android.view.MenuItem;


public class AppActivity extends FragmentActivity {
	
	public static final String TAG = AppActivity.class.getSimpleName();
	
	private ScheduleReminders reminders;

	
	/**
	 * @param activities
	 */
	public void drawReminders(ArrayList<org.hopegames.mobile.model.Activity> activities){
		try {
			reminders = (ScheduleReminders) findViewById(R.id.schedule_reminders);
			reminders.initSheduleReminders(activities);
		} catch (NullPointerException npe) {
			// do nothing
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
			case android.R.id.home:
				this.finish();
				return true;
		}
		return true;
	}

}
