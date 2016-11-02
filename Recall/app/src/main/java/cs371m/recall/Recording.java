package cs371m.recall;


import com.ibm.watson.developer_cloud.alchemy.v1.model.Keywords;

import org.parceler.Parcel;
import org.parceler.ParcelConstructor;

@Parcel
public class Recording {

    public String title;
    public String url;
    public String date;
    public String duration;
//    public Keywords keywords;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @ParcelConstructor
    public Recording(String title, String date) {
        this.title = title;
        this.date = date;
    }
}
