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
    public boolean isDirectory;
    public long rawDate;
    public String audioPath;
    public String transcript;
//    private int wordCount;
    private String description;
    static SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
    public List<String> keywords;
    private boolean modified;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.modified = true;
        this.title = title;
    }

//    public int getWordCount() {
//        return this.wordCount;
//    }

    public String getDescription() {
        return this.description;
    }

    public static Recording create(String title, long rawDate, boolean isDirectory) {
        Recording result = new Recording(title, rawDate, isDirectory);
        return result;
    }

    @ParcelConstructor
    public Recording(String title, long rawDate, boolean isDirectory) {
        this.modified = true;
//        this.wordCount = 0;
        this.title = title;
        this.rawDate = rawDate;

        this.date = rawDate != 0L ? dateFormat.format(rawDate) : "(Previous)";
        this.isDirectory = isDirectory;
        this.description = isDirectory ? "(directory)" : "";
    }

    public Recording addAudioPath(String path) {
        this.modified = true;
        this.audioPath = path;
        return this;
    }

    public Recording addTranscript(String text) {
        this.modified = true;
        this.transcript = text;
        // http://stackoverflow.com/questions/5864159/count-words-in-a-string-method
//        this.wordCount = text.trim().split("\\s+").length;
        return this;
    }

    public Recording addKeywords(List<String> keywords) {
        this.modified = true;
        this.keywords = keywords;
        return this;
    }

    public Recording addKeyword(String word) {
        this.modified = true;
        this.keywords.add(word);
        return this;
    }

    @Override
    public int compareTo(Recording otherRecording) {
        // Previous directory link has highest priority
        if (this.isPreviousDir()) {
            return -1;
        } else if (otherRecording.isPreviousDir()) {
            return 1;
        }

        // Directories have priority over files
        if (this.isDirectory && !otherRecording.isDirectory) {
            return -1;
        } else if (!this.isDirectory && otherRecording.isDirectory) {
            return 1;
        } else {
            // At this stage, both recordings are directories or files
            // Either way, whichever is new has priority;
            if (this.rawDate > otherRecording.rawDate) {
                return -1;
            } else if (this.rawDate < otherRecording.rawDate) {
                return 1;
            } else {
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
