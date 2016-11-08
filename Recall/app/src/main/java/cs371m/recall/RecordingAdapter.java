package cs371m.recall;


import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.parceler.Parcels;

import java.util.List;
import java.util.Map;

public class RecordingAdapter extends RecyclerView.Adapter<RecordingAdapter.RecordViewHolder> {

    private List<Recording> recordingList;

    public class RecordViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView title;
        public TextView date;
        public TextView duration;
        public View container;
        public Recording recording;

        public RecordViewHolder(View view) {
            super(view);
            container = view;
            title = (TextView) view.findViewById(R.id.title);
            date = (TextView) view.findViewById(R.id.date);
            duration = (TextView) view.findViewById(R.id.duration);
            container.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(v.getContext(), RecordingActivity.class);
            Bundle bundle = new Bundle();
            bundle.putParcelable("recording", Parcels.wrap(recording));
            intent.putExtras(bundle);
            v.getContext().startActivity(intent);
        }
    }

    public RecordingAdapter(List<Recording> recordingList) {
        this.recordingList = recordingList;
    }

    @Override
    public int getItemCount() {
        return recordingList.size();
    }

    @Override
    public void onBindViewHolder(RecordViewHolder holder, int position) {
        final Recording recording = recordingList.get(position);
        holder.title.setText(recording.title);
        if (recording.isDirectory) {
            holder.title.setTypeface(holder.title.getTypeface(), Typeface.BOLD);
        }
        holder.duration.setText(recording.duration);
        holder.duration.setTypeface(holder.duration.getTypeface(), Typeface.ITALIC);
        holder.date.setText(recording.date);

        // It might be better to use getAdapterPosition rather than updating the reference we keep
        holder.recording = recording;
    }

    @Override
    public RecordingAdapter.RecordViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.record, parent, false);
        return new RecordViewHolder(itemView);
    }
}

