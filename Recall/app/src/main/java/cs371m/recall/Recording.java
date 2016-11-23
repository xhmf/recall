package cs371m.recall;


import android.util.Log;

import org.parceler.Parcel;
import org.parceler.ParcelConstructor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Parcel
public class Recording implements Comparable<Recording>, Serializable {

    public String title;
    public String date;
    public String duration;
    public boolean isDirectory;
    public long rawDate;
    public String audioPath;
    public String transcript;
    static SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
    public List<String> keywords;
    private boolean modified;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public static Recording create(String title, long rawDate, String duration, boolean isDirectory) {
        Recording result = new Recording(title, rawDate, duration, isDirectory);
        return result;
    }

    @ParcelConstructor
    public Recording(String title, long rawDate, String duration, boolean isDirectory) {
        this.modified = true;
        this.title = title;
        this.rawDate = rawDate;

        if (rawDate != 0L) {
            this.date = dateFormat.format(rawDate);
        }
        else {
            this.date = "(Previous)";
        }
        this.isDirectory = isDirectory;
        this.duration = this.isDirectory ? "(directory)" : duration;
    }

    public Recording addAudioPath(String path) {
        this.audioPath = path;
        return this;
    }

    public Recording addTranscript(String text) {
        this.transcript = text;
        return this;
    }

    public Recording addKeywords(List<String> keywords) {
        this.keywords = keywords;
        return this;
    }

    public Recording addKeyword(String word) {
        this.keywords.add(word);
        return this;
    }

    @Override
    public int compareTo(Recording otherRecording) {
        // Previous directory link has highest priority
        if (this.isPreviousDir()) {
            return -1;
        }
        else if (otherRecording.isPreviousDir()) {
            return 1;
        }

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

    public boolean isPreviousDir() {
        return (this.isDirectory && this.rawDate == 0L);
    }

    public void save(String path) {
        if (!modified || isDirectory || title.equals("../")) {
            return;
        }
        try {
            this.modified = false; // So that after we read this file we don't rewrite again
            File file = new File(path, rawDate + ".recall");
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(this);
            oos.close();
            fos.close();
        } catch (IOException e) {
            Log.e(MainActivity.APP, e.getMessage());
        }
    }
}
