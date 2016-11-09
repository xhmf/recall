package cs371m.recall;

import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.renderscript.ScriptGroup;
import android.speech.RecognizerIntent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.Toast;

import com.ibm.watson.developer_cloud.alchemy.v1.AlchemyLanguage;
import com.ibm.watson.developer_cloud.alchemy.v1.model.Keyword;
import com.ibm.watson.developer_cloud.alchemy.v1.model.Keywords;
import com.ibm.watson.developer_cloud.android.library.audio.MicrophoneInputStream;
import com.ibm.watson.developer_cloud.android.library.audio.utils.ContentType;
import com.ibm.watson.developer_cloud.http.ServiceCallback;
import com.ibm.watson.developer_cloud.speech_to_text.v1.SpeechToText;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.RecognizeOptions;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechResults;
import com.ibm.watson.developer_cloud.speech_to_text.v1.websocket.BaseRecognizeCallback;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;


public class MainActivity extends AppCompatActivity implements NewFolderDialogFragment.OnNewFolderDialogFragmentListener {

    private final String KEY = "22684d7c8acd5f0f2b8a1b19bc6aa6b73b2a7488";
    AlchemyLanguage service;
    private SpeechToText speechService;
    private RecyclerView recyclerView;
    private RecordingAdapter adapter;

    FileFilter fileFilter = new FileFilter() {
        @Override
        public boolean accept(File file) {
            return (file.isDirectory() || (file.getName().endsWith(".mp3") && !file.getName().startsWith(".")));
        }
    };

    List<Recording> recordings = new ArrayList<>();
    String currentPath = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // We can only use this app if we have an SD card to write to...
        if (isExternalStorageReadable()) {
            File externalDataDir = getExternalFilesDir(getResources().getString(R.string.external_recording_dir));
            updateCurrentDirectory(externalDataDir);
        }
        else {
            Toast.makeText(this, "You need external storage for this app!", Toast.LENGTH_LONG).show();
            finish();
        }

        recyclerView = (RecyclerView) findViewById(R.id.recycler);
        adapter = new RecordingAdapter(recordings, this);
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
                Intent transcriptIntent = new Intent(getApplicationContext(),
                        TranscriptViewer.class);
                startActivity(transcriptIntent);
            }
        });

        FloatingActionButton addRecordingButton = (FloatingActionButton) findViewById(R.id
                .add_recording);

        addRecordingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Only for testing. Switch to actual api call when done.
                Keywords keywords = new Keywords();
                List<Keyword> words = new ArrayList<>();
                words.add(testKeyword("first"));
                words.add(testKeyword("second"));
                words.add(testKeyword("third"));
                keywords.setKeywords(words);
                recordings.add(new Recording(keywords.toString(), Calendar.getInstance().getTimeInMillis(), "00:10:00", false));
                Collections.sort(recordings);
                adapter.notifyDataSetChanged();
//
//
//                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
//
//                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");
//                intent.putExtra("android.speech.extra.GET_AUDIO_FORMAT", "audio/AMR");
//                intent.putExtra("android.speech.extra.GET_AUDIO", true);
//
//                try {
//                    startActivityForResult(intent, 1);
//                } catch (ActivityNotFoundException a) {
//                    Toast.makeText(getApplicationContext(),
//                            "Your device doesn't support Speech to Text",
//                            Toast.LENGTH_SHORT).show();
//                }
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

    @Override
    protected void onActivityResult(final int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data
                    .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result.size() == 0) {
                return;
            }
            String text = result.get(0);

            // TODO Comment back in API call for actual usage.
//            service.getKeywords(makeRequest(text)).enqueue(new ServiceCallback<Keywords>() {
//                @Override
//                public void onResponse(Keywords response) {
//                    System.out.println(response);
//                    recordings.add(new Recording(response.toString(), "1"));
//                    adapter.notifyDataSetChanged();
//                    Toast.makeText(getBaseContext(),
//                            response.getText(),
//                            Toast.LENGTH_SHORT).show();
//                }
//
//                @Override
//                public void onFailure(Exception e) {
//                    Log.e("", e.getMessage());
//                }
//            });

            Uri audioUri = data.getData();
            ContentResolver contentResolver = getContentResolver();
            try {
                InputStream filestream = contentResolver.openInputStream(audioUri);
                // Give file name and persist to disk.
            } catch (FileNotFoundException e) {
                Log.e("SAVING", e.getMessage());
            }
        }
    }

    // Only for testing purposes.
    private Keyword testKeyword(String text) {
        Keyword word = new Keyword();
        word.setText(text);
        return word;
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
        else if (id == R.id.action_new_folder) {
            // Create a subfolder within the current file view
            NewFolderDialogFragment.newInstance().show(getSupportFragmentManager(), "fragment_new_folder_dialog");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean isExternalStorageReadable() {
        return isExternalStorageWritable() ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(
                        Environment.getExternalStorageState());
    }

    public boolean isExternalStorageWritable() {
        return Environment.MEDIA_MOUNTED.equals(
                Environment.getExternalStorageState());
    }

    private void updateCurrentDirectory(File dir) {
        if (dir.isDirectory()) {
            currentPath = dir.getAbsolutePath();
            recordings.clear();
            if (!currentPath.equals(getExternalFilesDir("Recall").getAbsolutePath())) {
                recordings.add(new Recording("../", 0L, "--:--:--", true));
            }
            for (File recording : dir.listFiles(fileFilter)) {
                String name = recording.getName();
                long rawDate = recording.lastModified();
                boolean isDirectory = recording.isDirectory();
                recordings.add(new Recording(name, rawDate, "--:--:--", isDirectory));
            }
            Collections.sort(recordings);

            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        }
    }

    public void gotoDirectory(String directoryName) {
        File newCurrentDir = new File(currentPath, directoryName);
        if (newCurrentDir.exists() && newCurrentDir.isDirectory()) {
            updateCurrentDirectory(newCurrentDir);
        }
    }

    public void gotoPreviousDirectory() {
        // We don't want to leave the topmost file directory
        if (!currentPath.equals(getExternalFilesDir("Recall").getAbsolutePath())) {
            File parentDir = new File(currentPath).getParentFile();
            if (parentDir != null) {
                updateCurrentDirectory(parentDir);
            }
        }
    }

    @Override
    public void onNewFolderDialogFragmentDone(String newFolderName) {
        // I need to do more error checking to make sure that we have a valid folder name
        if (!newFolderName.isEmpty() && !newFolderName.startsWith(".")) {
            File newDirectory = new File(currentPath, newFolderName);
            if (!newDirectory.exists()) {
                newDirectory.mkdir();
                updateCurrentDirectory(new File(currentPath));
            }
        }
    }
}
