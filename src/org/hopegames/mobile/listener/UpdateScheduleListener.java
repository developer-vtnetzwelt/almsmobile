package org.hopegames.mobile.listener;

import org.hopegames.mobile.model.DownloadProgress;
import org.hopegames.mobile.task.Payload;

public interface UpdateScheduleListener {
	
	void updateComplete(Payload p);
    void updateProgressUpdate(DownloadProgress dp);

}
