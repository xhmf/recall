package cs371m.recall;


import com.ibm.watson.developer_cloud.alchemy.v1.model.Keywords;

public class Recording {

    public String title;
    public String url;
    public String date;
    public String duration;
    public Keywords keywords;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Recording(String title, String date) {
        this.title = title;
        this.date = date;
    }
}
