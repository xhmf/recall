package cs371m.recall;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.renderscript.ScriptGroup;
import android.speech.RecognizerIntent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.Toast;

import com.ibm.watson.developer_cloud.alchemy.v1.AlchemyLanguage;
import com.ibm.watson.developer_cloud.alchemy.v1.model.Keywords;
import com.ibm.watson.developer_cloud.android.library.audio.MicrophoneInputStream;
import com.ibm.watson.developer_cloud.android.library.audio.utils.ContentType;
import com.ibm.watson.developer_cloud.http.ServiceCallback;
import com.ibm.watson.developer_cloud.speech_to_text.v1.SpeechToText;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.RecognizeOptions;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechResults;
import com.ibm.watson.developer_cloud.speech_to_text.v1.websocket.BaseRecognizeCallback;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private final String KEY = "22684d7c8acd5f0f2b8a1b19bc6aa6b73b2a7488";
    AlchemyLanguage service;
    private SpeechToText speechService;
    private RecyclerView recyclerView;
    private RecordingAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        List<Recording> recordings = new ArrayList<>();
        recordings.add(new Recording("first", "10/23"));
        recordings.add(new Recording("second", "22/32"));
        recordings.add(new Recording("third", "42/3"));
        recordings.add(new Recording("fourth", "123/232"));
        recyclerView = (RecyclerView) findViewById(R.id.recycler);
        adapter = new RecordingAdapter(recordings);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        service = new AlchemyLanguage();
        service.setApiKey(KEY);
        speechService = new SpeechToText();
        speechService.setEndPoint("https://stream.watsonplatform.net/speech-to-text/api");
        speechService.setApiKey(KEY);

        ImageButton viewTranscriptButton = (ImageButton) findViewById(R.id.open_transcript);
        viewTranscriptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent transcriptIntent = new Intent(getApplicationContext(), TranscriptViewer.class);
                startActivity(transcriptIntent);
            }
        });

        FloatingActionButton addRecordingButton = (FloatingActionButton) findViewById(R.id.add_recording);

        addRecordingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                service.getKeywords(makeRequest("here is some text to analyze")).enqueue(new ServiceCallback<Keywords>() {
                    @Override
                    public void onResponse(Keywords response) {
//                        System.out.println(response.getKeywords());
//                        System.out.println(response);

                        speechService.recognizeUsingWebSocket(new MicrophoneInputStream(),
                                getRecognizeOptions(), new BaseRecognizeCallback() {
                                    @Override
                                    public void onTranscription(SpeechResults speechResults) {
                                        String text = speechResults.getResults().get(0)
                                                .getAlternatives()
                                                .get(0).getTranscript();
                                        System.out.println(text);
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        System.out.println(e.getMessage());
                                    }

                                    @Override
                                    public void onDisconnected() {
                                        System.out.println("DISCONNECTED");
                                    }

                                });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        System.out.println(e.getMessage());
                    }
                });
            }
        });
    }

    private RecognizeOptions getRecognizeOptions() {
        RecognizeOptions options = new RecognizeOptions.Builder()
                .continuous(true)
                .contentType(ContentType.OPUS.toString())
                .model("en-US_BroadbandModel")
                .interimResults(true)
                .inactivityTimeout(2000)
                .build();
        return options;
    }

    public void recordSpeech() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Recording audio.");
        try {
            startActivityForResult(intent, 1);
        } catch (ActivityNotFoundException a) {
            System.out.println(a.getMessage());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data
                    .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            System.out.println(result);
        }
    }

    private Map<String, Object> makeRequest(String text) {
        Map<String, Object> result = new HashMap<>();
        result.put("text", text);
        return result;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
