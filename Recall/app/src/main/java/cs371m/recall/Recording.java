package cs371m.recall;


import com.ibm.watson.developer_cloud.alchemy.v1.model.Keywords;

import org.parceler.Parcel;
import org.parceler.ParcelConstructor;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

@Parcel
public class Recording implements Comparable<Recording> {

    public String title;
//    public String url;
    public String date;
    public String duration;
    public boolean isDirectory;
    public long rawDate;
    static SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
//    public Keywords keywords;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @ParcelConstructor
    public Recording(String title, long rawDate, String duration, boolean isDirectory) {
        this.title = title;
        this.rawDate = rawDate;
        this.date = dateFormat.format(rawDate);
        this.isDirectory = isDirectory;
        this.duration = this.isDirectory ? "(directory)" : duration;
    }

    @Override
    public int compareTo(Recording otherRecording) {
        // Directories have priority over files
        if (this.isDirectory && !otherRecording.isDirectory) {
            return -1;
        }
        else if (!this.isDirectory && otherRecording.isDirectory) {
            return 1;
        }
        else {
            // At this stage, both recordings are directories or files
            // Either way, whichever is new has priority;
            if (this.rawDate > otherRecording.rawDate) {
                return -1;
            }
            else if (this.rawDate < otherRecording.rawDate) {
                return 1;
            }
            else {
                return 0;
            }
        }
    }
}
