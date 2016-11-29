package cs371m.recall;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.app.AlertDialog;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.parceler.Parcels;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RecordingActivity extends AppCompatActivity {

    public EditText title;
    public TextView transcript;
    public Button keyword1;
    public Button keyword2;
    public Button keyword3;
    public Recording recording;
    private SpannableString displayTranscript;
    private ImageButton deleteRecordingButton;

    private MediaPlayer audioPlayer;
    private ImageButton audioPlayButton;
    private SeekBar audioSeekBar;
    private Handler handler = new Handler();

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
                            recording.save();
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

        // Media player UI
        audioPlayButton = (ImageButton) findViewById(R.id.audio_play_button);
        audioPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (audioPlayer != null) {
                    if (audioPlayer.isPlaying()) {
                        audioPlayButton.setBackgroundResource(R.drawable.ic_play);
                        audioPlayer.pause();
                    } else {
                        audioPlayButton.setBackgroundResource(R.drawable.ic_pause);
                        audioPlayer.start();
                    }
                }
            }
        });
        audioSeekBar = (SeekBar) findViewById(R.id.audio_seek_bar);
        // Add event handler for progress change in seek bar
        audioSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Only seek to a position in song if the user interacted with the seek bar (avoids feedback loop)
                if (fromUser) {
                    audioPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Stub
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Stub
            }
        });

        // Recording delete button
        deleteRecordingButton = (ImageButton) findViewById(R.id.delete_recording_button);
        deleteRecordingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // http://stackoverflow.com/questions/5127407/how-to-implement-a-confirmation-yes-no-dialogpreference
                new AlertDialog.Builder(view.getContext())
                        .setTitle("Please confirm")
                        .setMessage("Do you want to delete this recording?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setNegativeButton(android.R.string.no, null)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int whichButton) {
                                if (recording != null) {
                                    File recordingFile = new File(recording.getFilePath());
                                    if (recordingFile.exists()) {
                                        recordingFile.delete();

                                        File audioFile = new File(recording.audioPath);
                                        if (audioFile.exists()) {
                                            audioFile.delete();
                                        }

                                        finish();
                                    }
                                }
                            }
                        })
                        .show();
            }
        });

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            Parcelable parcel = bundle.getParcelable("recording");
            recording = Parcels.unwrap(parcel);

            if (recording != null) {
                title.setText(recording.title);
                displayTranscript = new SpannableString(recording.transcript);
                transcript.setText(displayTranscript);

                keyword1.setText(recording.keywords.get(0));
                keyword1.setOnClickListener(createClickAction(recording.keywords.get(0)));
                keyword2.setText(recording.keywords.get(1));
                keyword2.setOnClickListener(createClickAction(recording.keywords.get(1)));
                keyword3.setText(recording.keywords.get(2));
                keyword3.setOnClickListener(createClickAction(recording.keywords.get(2)));

                playRecording(recording);
            } else {
                Toast.makeText(this, "Cannot read recording file.", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
        } else {
            Toast.makeText(this, "Cannot read bundle.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Start seek bar updater thread
        Runnable audioSeekBarUpdate = new Runnable() {
            @Override
            public void run() {
                if (audioPlayer != null && audioPlayer.isPlaying()) {
                    audioSeekBar.setProgress(audioPlayer.getCurrentPosition());
                }
                handler.postDelayed(this, 200);
            }
        };
        handler.postDelayed(audioSeekBarUpdate, 200);
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

    public void playRecording(Recording recording) {
        try {
            if (audioPlayer == null) {
                audioPlayer = new MediaPlayer();
                audioPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        audioPlayButton.setBackgroundResource(R.drawable.ic_play);
                        audioSeekBar.setProgress(0);
                    }
                });
            }
            audioPlayer.reset();
            audioPlayer.setDataSource(recording.audioPath);
            audioPlayer.prepare();
            audioPlayer.start();

            // Update UI elements
            audioPlayButton.setBackgroundResource(R.drawable.ic_pause);
            audioSeekBar.setMax(audioPlayer.getDuration());
            audioSeekBar.setProgress(0);
        } catch (IOException ex) {
            Toast.makeText(this, "Unable to play recording.", Toast.LENGTH_LONG).show();
            audioPlayer = null;
            audioSeekBar.setProgress(0);
        }
    }

    @Override
    protected void onDestroy() {
        if (audioPlayer != null) {
            MediaPlayer oldAudioPlayer = audioPlayer;
            audioPlayer = null;
            oldAudioPlayer.release();
        }

        super.onDestroy();
    }
}
