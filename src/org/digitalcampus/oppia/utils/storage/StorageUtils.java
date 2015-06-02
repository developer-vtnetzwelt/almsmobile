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

package org.digitalcampus.oppia.utils.storage;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.StringTokenizer;

// Code from StackOverflow: http://stackoverflow.com/questions/9340332/how-can-i-get-the-list-of-mounted-external-storage-of-android-device/19982338#19982338

public class StorageUtils {

    public static final String TAG = StorageUtils.class.getSimpleName();

    public static List<StorageLocationInfo> getStorageList() {

        List<StorageLocationInfo> list = new ArrayList<StorageLocationInfo>();
        String def_path = Environment.getExternalStorageDirectory().getPath();
        boolean def_path_removable = Environment.isExternalStorageRemovable();
        String def_path_state = Environment.getExternalStorageState();
        boolean def_path_available = def_path_state.equals(Environment.MEDIA_MOUNTED)
                                    || def_path_state.equals(Environment.MEDIA_MOUNTED_READ_ONLY);
        boolean def_path_readonly = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED_READ_ONLY);

        HashSet<String> paths = new HashSet<String>();
        int cur_removable_number = 1;

        if (def_path_available) {
            paths.add(def_path);
            list.add(0, new StorageLocationInfo(def_path, def_path_readonly, def_path_removable, def_path_removable ? cur_removable_number++ : -1));
        }

        BufferedReader buf_reader = null;
        try {
            buf_reader = new BufferedReader(new FileReader("/proc/mounts"));
            String line;
            Log.d(TAG, "/proc/mounts");
            while ((line = buf_reader.readLine()) != null) {
                //Log.d(TAG, line);
                if (line.contains("vfat") || line.contains("/mnt")) {
                    StringTokenizer tokens = new StringTokenizer(line, " ");
                    String unused = tokens.nextToken(); //device
                    String mount_point = tokens.nextToken(); //mount point
                    if (paths.contains(mount_point)) {
                        continue;
                    }
                    unused = tokens.nextToken(); //file system
                    List<String> flags = Arrays.asList(tokens.nextToken().split(",")); //flags
                    boolean readonly = flags.contains("ro");

                    if (line.contains("/dev/block/vold")) {
                        if (!line.contains("/mnt/secure")
                            && !line.contains("/mnt/asec")
                            && !line.contains("/mnt/obb")
                            && !line.contains("/dev/mapper")
                            && !line.contains("tmpfs")) {
                            paths.add(mount_point);
                            list.add(new StorageLocationInfo(mount_point, readonly, true, cur_removable_number++));
                        }
                    }
                }
            }

        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (buf_reader != null) {
                try {
                    buf_reader.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
        
        /*
         * START HORRIBLE HACK.... to get around fact that the above is not returning the right paths/mounts for some reason 
         */
        list = new ArrayList<StorageLocationInfo>();
       
        File sdcard0 = new File("/storage/sdcard0/");
        if(sdcard0.exists()){
        	Log.d(TAG,"sdcard0 exists");
        	list.add(new StorageLocationInfo("/storage/sdcard0/", false, false, cur_removable_number++));
        } else {
        	Log.d(TAG,"sdcard0 does NOT exist");
        }
        
        cur_removable_number = 1;
        File sdcard1 = new File("/storage/sdcard1/");
        if(sdcard1.exists()){
        	Log.d(TAG,"sdcard1 exists");
        	list.add(new StorageLocationInfo("/storage/sdcard1/", false, true, cur_removable_number++));
        } else {
        	Log.d(TAG,"sdcard1 does NOT exist");
        }
        
        File extSdCard = new File("/storage/extSdCard/");
        if(extSdCard.exists()){
        	Log.d(TAG,"extSdCard exists");
        	list.add(new StorageLocationInfo("/storage/extSdCard/", false, true, cur_removable_number++));
        } else {
        	Log.d(TAG,"extSdCard does NOT exist");
        }
        /*
         * END HORRIBLE HACK
         */
        return list;
    }
}
