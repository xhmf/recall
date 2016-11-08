package cs371m.recall;

import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import org.parceler.Parcels;

import java.io.IOException;

public class RecordingActivity extends AppCompatActivity {

    MediaRecorder mediaRecorder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recording);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            Parcelable parcel = bundle.getParcelable("recording");
            Recording recording = Parcels.unwrap(parcel);
            TextView textView = (TextView) findViewById(R.id.current_track);
            textView.setText(recording.getTitle());
        }

    }
}
