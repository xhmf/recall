package cs371m.recall;


import android.content.Intent;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.parceler.Parcels;
import org.w3c.dom.Text;

import java.util.List;
import java.util.Map;

public class RecordingAdapter extends RecyclerView.Adapter<RecordingAdapter.RecordViewHolder> {

    private List<Recording> recordingList;
    private MainActivity activity;

    public class RecordViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView title;
        public TextView date;
        public TextView description;
        public View container;
        public Recording recording;

        public RecordViewHolder(View view) {
            super(view);
            container = view;
            title = (TextView) view.findViewById(R.id.title);
            date = (TextView) view.findViewById(R.id.date);
            description = (TextView) view.findViewById(R.id.description);
            container.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                this.recording = recordingList.get(position);

                if (recording.isDirectory) {
                    if (recording.isPreviousDir()) {
                        activity.gotoPreviousDirectory();
                    }
                    else {
                        activity.gotoDirectory(recording.title);
                    }
                }
                else {
//                    Intent intent = new Intent(v.getContext(), RecordingActivity.class);
//                    Bundle bundle = new Bundle();
//                    bundle.putParcelable("recording", Parcels.wrap(recording));
//                    intent.putExtras(bundle);
//                    v.getContext().startActivity(intent);
                    activity.playRecording(recording);
                }
            }
        }
    }

    public RecordingAdapter(List<Recording> recordingList, MainActivity activity) {
        this.recordingList = recordingList;
        this.activity = activity;
    }

    @Override
    public int getItemCount() {
        return recordingList.size();
    }

    @Override
    public void onBindViewHolder(RecordViewHolder holder, int position) {
        final Recording recording = recordingList.get(position);
        holder.title.setText(recording.title);
        holder.description.setText(recording.getDescription());
        holder.description.setTypeface(holder.description.getTypeface(), Typeface.ITALIC);
        holder.date.setText(recording.date);
    }

    @Override
    public RecordingAdapter.RecordViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.record, parent, false);
        return new RecordViewHolder(itemView);
    }
}

