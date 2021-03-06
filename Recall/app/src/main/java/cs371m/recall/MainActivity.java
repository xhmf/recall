package cs371m.recall;

import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
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
import android.widget.ProgressBar;
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

import org.parceler.Parcels;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.security.Key;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;


public class MainActivity extends AppCompatActivity implements NewFolderDialogFragment
        .OnNewFolderDialogFragmentListener {

    static public final String APP = "recall";
    private final String KEY = "22684d7c8acd5f0f2b8a1b19bc6aa6b73b2a7488";
    private final String AUDIO_FILES = "audio";
    AlchemyLanguage service;
    private SpeechToText speechService;
    private RecyclerView recyclerView;
    private RecordingAdapter adapter;

    FileFilter fileFilter = new FileFilter() {
        @Override
        public boolean accept(File file) {
            return (file.isDirectory() || (file.getName().endsWith(".recall") && !file.getName()
                    .startsWith(".")));
        }
    };

    private List<Recording> recordings;
    static private String currentPath; // We make this static to preserve our location

    private Toast mainToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // We can only use this app if we have an SD card to write to...
        if (!isExternalStorageReadable() && !isExternalStorageWritable()) {
            displayToast("You need external storage for this app.");
            finish();
            return;
        }

        recordings = new ArrayList<>();

        File currentDir = (currentPath == null ? getRecordingDir() : new File(currentPath));
        updateCurrentDirectory(currentDir);

        recyclerView = (RecyclerView) findViewById(R.id.recycler);
        adapter = new RecordingAdapter(recordings, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        adapter.notifyDataSetChanged();

        service = new AlchemyLanguage();
        service.setApiKey(KEY);
        speechService = new SpeechToText();
        speechService.setEndPoint("https://stream.watsonplatform.net/speech-to-text/api");
        speechService.setApiKey(KEY);

        FloatingActionButton addRecordingButton = (FloatingActionButton) findViewById(R.id
                .add_recording);

        addRecordingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Only for testing. Switch to actual api call when done.
//                Keywords keywords = new Keywords();
//                List<Keyword> words = new ArrayList<>();
//                words.add(testKeyword("game"));
//                words.add(testKeyword("you"));
//                words.add(testKeyword("mad"));
//                keywords.setKeywords(words);
//                long timestamp = Calendar.getInstance().getTimeInMillis();
//                recordings.add(Recording.create(createKeywordList(keywords).toString(), currentPath, timestamp, false)
//                        .addAudioPath(getRecordingDir().getAbsolutePath() + File.separator + "Why you heff to be mad (Original).mp3")
//                        .addTranscript("It's only game. Why you have to be mad?")
//                        .addKeywords(new ArrayList<String>(Arrays.asList("game", "you", "mad"))));
//
//                Collections.sort(recordings);
//                adapter.notifyDataSetChanged();

                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");
                intent.putExtra("android.speech.extra.GET_AUDIO_FORMAT", "audio/AMR");
                intent.putExtra("android.speech.extra.GET_AUDIO", true);

                try {
                    startActivityForResult(intent, 1);
                } catch (ActivityNotFoundException a) {
                    Toast.makeText(getApplicationContext(),
                            "Your device doesn't support Speech to Text",
                            Toast.LENGTH_SHORT).show();
                }
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

    private boolean isExternalStorageReadable() {
        return isExternalStorageWritable() ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(
                        Environment.getExternalStorageState());
    }

    private boolean isExternalStorageWritable() {
        return Environment.MEDIA_MOUNTED.equals(
                Environment.getExternalStorageState());
    }

    private void saveRecordings() {
        for (Recording recording : recordings) {
            recording.save();
        }
    }

    private Recording dirToRecording(File dir) {
        return new Recording(dir.getName(), dir.getParentFile().getAbsolutePath(), dir.lastModified(), dir.isDirectory());
    }

    private Recording readRecording(File file) {
        try {
            FileInputStream fis = new FileInputStream(file);
            ObjectInputStream ois = new ObjectInputStream(fis);
            return (Recording) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            Log.e(APP, e.getMessage());
            return null;
        }
    }

    private void updateCurrentDirectory(File dir) {
        if (dir.isDirectory()) {
            currentPath = dir.getAbsolutePath();
            recordings.clear();
            if (!currentPath.equals(getRecordingDir().getAbsolutePath())) {
                recordings.add(Recording.create("../", currentPath, 0L, true));
            }
            for (File recording : dir.listFiles(fileFilter)) {
                if (recording.isDirectory()) {
                    recordings.add(dirToRecording(recording));
                } else if (recording.getName().endsWith(".recall")) {
                    Recording current = readRecording(recording);
                    if (current != null) {
                        recordings.add(current);
                    }
                }
            }
            Collections.sort(recordings);

            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        }
    }

    /*
     * Returns the directory where all audio recordings will be stored.
     */
    private String getAudioDir() {
        return getExternalFilesDir(getResources().getString(R.string.external_audio_dir)).getAbsolutePath();
    }
    private File getRecordingDir() {
        return getExternalFilesDir(getResources().getString(R.string.external_recording_dir));
    }

    public void gotoDirectory(String directoryName) {
        File newCurrentDir = new File(currentPath, directoryName);
        if (newCurrentDir.exists() && newCurrentDir.isDirectory()) {
            saveRecordings();
            updateCurrentDirectory(newCurrentDir);
        }
    }

    public void gotoPreviousDirectory() {
        // We don't want to leave the topmost file directory
        if (!currentPath.equals(getRecordingDir().getAbsolutePath())) {
            File parentDir = new File(currentPath).getParentFile();
            if (parentDir != null) {
                saveRecordings();
                updateCurrentDirectory(parentDir);
            }
        }
    }

    private void displayToast(String message) {
        // Display a message with a toast and cancel out old toasts
        if (mainToast != null) {
            mainToast.cancel();
        }
        mainToast = Toast.makeText(this, message, Toast.LENGTH_LONG);
        mainToast.show();
    }

    private List<String> createKeywordList(Keywords keywords) {
        List<String> result = new ArrayList<>();
        for(Keyword word : keywords.getKeywords()) {
            if (word.getRelevance() > .75) {
                result.add(word.getText());
            }
            result.add(word.getText());
        }
        return new ArrayList<>(new HashSet<>(result));
    }

    private void saveAudio(Uri audioUri, String fileName) {
        ContentResolver contentResolver = getContentResolver();
        try {
            InputStream filestream = contentResolver.openInputStream(audioUri);
            // Give file name and persist to disk.
            File file = new File(getAudioDir(), fileName + ".3pg");
            OutputStream out = new FileOutputStream(file);
            try {
                byte[] buffer = new byte[4 * 1024]; // or other buffer size
                int read;

                while ((read = filestream.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
                out.flush();
            } finally {
                out.close();
                filestream.close();
            }
        } catch (IOException e) {
            Log.e(APP, e.getMessage());
        }
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
            final String text = result.get(0);
            final long timestamp = Calendar.getInstance().getTimeInMillis();

            service.getKeywords(makeRequest(text)).enqueue(new ServiceCallback<Keywords>() {
                @Override
                public void onResponse(Keywords response) {
                    System.out.println(response);
                    List<String> keywords = createKeywordList(response);
                    Recording recording = Recording.create(keywords.toString(), currentPath, timestamp, false)
                            .addTranscript(text)
                            .addKeywords(keywords)
                            .addAudioPath(getAudioDir() + File.separator + Long.toString(timestamp) + ".3pg");
                    recordings.add(recording);
                    recording.save();
                    recyclerView.post(new Runnable() {
                        @Override
                        public void run() {
                            adapter.notifyDataSetChanged();
                        }
                    });
                    Toast.makeText(getBaseContext(),
                            response.getText(),
                            Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(Exception e) {
                    Log.e(APP, e.getMessage());
                }
            });

            Uri audioUri = data.getData();
            saveAudio(audioUri, Long.toString(timestamp));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCurrentDirectory(new File(currentPath));
    }

    @Override
    protected void onPause() {
        super.onPause();
        // save list of recordings
        saveRecordings();
    }

    @Override
    protected void onDestroy() {
        // Save any recordings with a default filename of the current time
        saveRecordings();
        super.onDestroy();
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
        if (id == R.id.action_new_folder) {
            // Create a subfolder within the current file view
            NewFolderDialogFragment.newInstance().show(getSupportFragmentManager(),
                    "fragment_new_folder_dialog");
            return true;
        } else if (id == R.id.action_exit) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onNewFolderDialogFragmentDone(String newFolderName) {
        // TODO: I need to do more error checking to make sure that we have a valid folder name
        if (!newFolderName.isEmpty() && !newFolderName.startsWith(".")) {
            File newDirectory = new File(currentPath, newFolderName);
            if (!newDirectory.exists()) {
                newDirectory.mkdir();
                updateCurrentDirectory(new File(currentPath));
            }
        }
    }
}
