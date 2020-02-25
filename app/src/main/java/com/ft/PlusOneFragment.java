package com.ft;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.google.android.gms.plus.PlusOneButton;

/**
 * A fragment with a Google +1 button.
 * Activities that contain this fragment must implement the
 * {@link PlusOneFragment} interface
 * to handle interaction events.
 * Use the {@link PlusOneFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PlusOneFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    // The request code must be 0 or greater.
    private static final int PLUS_ONE_REQUEST_CODE = 0;
    // The URL to +1.  Must be a valid URL.
    private final String PLUS_ONE_URL = "http://developer.android.com";
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private PlusOneButton mPlusOneButton;


    public PlusOneFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PlusOneFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PlusOneFragment newInstance(String param1, String param2) {
        PlusOneFragment fragment = new PlusOneFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("1--", "1=====onCreate");
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_plus_one, container, false);

        //Find the +1 button
        mPlusOneButton = (PlusOneButton) view.findViewById(R.id.plus_one_button);
        Log.d("1--", "1=====onCreateView");
        return view;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        Log.d("1--", "1=====onHiddenChanged=" + hidden);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("1--", "1=====onPause");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("1--", "1=====onDestroy");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("1--", "1=====onResume");

        // Refresh the state of the +1 button each time the activity receives focus.
        mPlusOneButton.initialize(PLUS_ONE_URL, PLUS_ONE_REQUEST_CODE);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        Log.d("1--", "1=====onDetach");
        super.onDetach();
    }


}
