package cs371m.recall;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link NewFolderDialogFragment.OnNewFolderDialogFragmentListener} interface
 * to handle interaction events.
 * Use the {@link NewFolderDialogFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NewFolderDialogFragment extends DialogFragment implements TextView.OnEditorActionListener {
    protected EditText newFolderEditText;
    private OnNewFolderDialogFragmentListener listeningActivity;

    public NewFolderDialogFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment NewFolderDialogFragment.
     */
    public static NewFolderDialogFragment newInstance() {
        NewFolderDialogFragment fragment = new NewFolderDialogFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_new_folder_dialog, container, false);
        newFolderEditText = (EditText) view.findViewById(R.id.new_folder_edit_text);
        newFolderEditText.setOnEditorActionListener(this);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnNewFolderDialogFragmentListener) {
            listeningActivity = (OnNewFolderDialogFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnNewFolderDialogFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listeningActivity = null;
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (EditorInfo.IME_ACTION_DONE == actionId) {
            // Return input text to activity
            listeningActivity.onNewFolderDialogFragmentDone(newFolderEditText.getText().toString());
            this.dismiss();
            return true;
        }
        return false;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnNewFolderDialogFragmentListener {
        void onNewFolderDialogFragmentDone(String newFolderName);
    }
}
