package cs371m.recall;

import android.content.Intent;
import android.graphics.Color;
import android.media.MediaRecorder;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.parceler.Parcels;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static cs371m.recall.MainActivity.currentRecording;

public class RecordingActivity extends AppCompatActivity {

    public EditText title;
    public TextView transcript;
    public Button keyword1;
    public Button keyword2;
    public Button keyword3;
    public Recording recording;
    private SpannableString displayTranscript;

//    public EditText transcriptPageNumber;
//    public TextView transcriptPageCount;
//    private int currentPageNumber;
//    private int pageCount;
//    final private int maxWordsPerPage = 60;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recording);

        title = (EditText) findViewById(R.id.title_edit);
        title.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (EditorInfo.IME_ACTION_DONE == actionId) {
                    String newTitle = title.getText().toString().trim();
                    if (!newTitle.isEmpty() && !newTitle.startsWith(".")) {
                        if (recording != null) {
                            recording.setTitle(newTitle);
                            MainActivity.currentRecording.setTitle(newTitle);
                            return true;
                        }
                    }
                }
                return false;
            }
        });

        transcript = (TextView) findViewById(R.id.transcript);
        keyword1 = (Button) findViewById(R.id.key1);
        keyword2 = (Button) findViewById(R.id.key2);
        keyword3 = (Button) findViewById(R.id.key3);

//        transcriptPageCount = (TextView) findViewById(R.id.transcript_page_count);
//        transcriptPageNumber = (EditText) findViewById(R.id.input_transcript_page_number);
//        transcriptPageNumber.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
//                if (EditorInfo.IME_ACTION_DONE == actionId) {
//                    String currentPageNumberText = transcriptPageNumber.getText().toString().trim();
//
//                    if (!currentPageNumberText.isEmpty()) {
//                        int newCurrentPageNumber = Integer.parseInt(currentPageNumberText);
//
//                        if (newCurrentPageNumber > 0 && newCurrentPageNumber <= pageCount) {
//                            currentPageNumber = newCurrentPageNumber;
//                            return true;
//                        }
//                    }
//
//                    transcriptPageNumber.setText(currentPageNumber + "");
//                }
//                return false;
//            }
//        });

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            Parcelable parcel = bundle.getParcelable("recording");
            recording = Parcels.unwrap(parcel);
            title.setText(recording.title);
            displayTranscript = new SpannableString(recording.transcript);
            transcript.setText(displayTranscript);

//            computePageCount();

            keyword1.setText(recording.keywords.get(0));
            keyword1.setOnClickListener(createClickAction(recording.keywords.get(0)));
            keyword2.setText(recording.keywords.get(1));
            keyword2.setOnClickListener(createClickAction(recording.keywords.get(1)));
            keyword3.setText(recording.keywords.get(2));
            keyword3.setOnClickListener(createClickAction(recording.keywords.get(2)));
        }

//        transcriptPageCount.setText(pageCount + "");
//        transcriptPageNumber.setText(currentPageNumber + "");
    }

    private View.OnClickListener createClickAction(final String word) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                highlightInTranscript(word);
            }
        };
    }

    private void highlightInTranscript(String word) {
        for (BackgroundColorSpan span : displayTranscript.getSpans(0, displayTranscript.length(),
                BackgroundColorSpan.class)) {
            displayTranscript.removeSpan(span);
        }
        List<Integer> indices = new ArrayList<>();
        String text = displayTranscript.toString();
        int index = 0;
        while (index < text.length()) {
            index = text.indexOf(word, index);
            if (index == -1) {
                break;
            }
            indices.add(index);
            index++;
        }
        for (Integer current : indices) {
            displayTranscript.setSpan(new BackgroundColorSpan(Color.rgb(255, 165, 0)), current,
                    current + word.length(), Spanned.SPAN_INTERMEDIATE);
        }
        transcript.setText(displayTranscript);
    }

//    private void computePageCount() {
//        if (recording == null || recording.getWordCount() == 0) {
//            pageCount = 0;
//            currentPageNumber = 0;
//            return;
//        }
//
//        pageCount = (recording.getWordCount() / maxWordsPerPage) + 1;
//        currentPageNumber = 1;
//    }
}
