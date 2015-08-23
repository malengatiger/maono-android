package com.boha.monitor.library.fragments;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.boha.monitor.library.dto.MonitorDTO;
import com.boha.monitor.library.dto.PhotoUploadDTO;
import com.boha.monitor.library.util.ImageUtil;
import com.boha.monitor.library.util.SharedUtil;
import com.boha.platform.library.R;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MonitorProfileListener} interface
 * to handle interaction events.
 * Use the {@link MonitorProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MonitorProfileFragment extends Fragment implements PageFragment {


    private MonitorProfileListener mListener;
    MonitorDTO monitor;
    TextView txtName;
    ImageView backDrop, roundImage, iconCamera;
    EditText eFirst, eLast, eAddress, eID;
    RadioButton radioMale, radioFemale;
    Button btnSave;
    View view;


    public static MonitorProfileFragment newInstance(MonitorDTO monitor) {
        MonitorProfileFragment fragment = new MonitorProfileFragment();
        Bundle args = new Bundle();
        args.putSerializable("monitor", monitor);
        fragment.setArguments(args);
        return fragment;
    }

    public MonitorProfileFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            monitor = (MonitorDTO)getArguments().getSerializable("monitor");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_monitor_details, container, false);

        setFields();
        return view;
    }

    private void setFields() {
        txtName = (TextView)view.findViewById(R.id.FMP_name);
        btnSave = (Button)view.findViewById(R.id.FMP_btnSave);

        eFirst = (EditText)view.findViewById(R.id.FMP_editFirstName);
        eLast = (EditText)view.findViewById(R.id.FMP_editLastName);
        eID = (EditText)view.findViewById(R.id.FMP_editID);
        eAddress = (EditText)view.findViewById(R.id.FMP_editAddress);

        iconCamera = (ImageView)view.findViewById(R.id.FMP_iconCamera);
        roundImage = (ImageView)view.findViewById(R.id.FMP_personImage);
        backDrop = (ImageView)view.findViewById(R.id.FMP_backdrop);

        iconCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onMonitorPictureRequested(monitor);
            }
        });
        roundImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onMonitorPictureRequested(monitor);
            }
        });
        if (monitor != null) {
            txtName.setText(monitor.getFirstName() + " " + monitor.getLastName());
            eFirst.setText(monitor.getFirstName());
            eLast.setText(monitor.getLastName());
        }
        PhotoUploadDTO x = SharedUtil.getPhoto(getActivity());
        if (x != null) {
            setPicture(x);
        }
    }

    public void setPicture(PhotoUploadDTO photo) {

        if (photo.getThumbFilePath() == null) {
            if (photo.getUri() != null) {
                ImageLoader.getInstance().displayImage(photo.getUri(), backDrop);
                ImageLoader.getInstance().displayImage(photo.getUri(), roundImage);
            }

        } else {
            File f = new File(photo.getThumbFilePath());
            if (f.exists()) {
                try {
                    Bitmap bm = ImageUtil.getBitmapFromUri(getActivity(), Uri.fromFile(f));
                    backDrop.setImageBitmap(bm);
                    roundImage.setImageBitmap(bm);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (MonitorProfileListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement MonitorProfileListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void animateHeroHeight() {

    }
    String pageTitle;
    @Override
    public void setPageTitle(String title) {
        pageTitle = title;
    }

    @Override
    public String getPageTitle() {
        return pageTitle;
    }

    public interface MonitorProfileListener {
         void onProfileUpdated(MonitorDTO monitor);
         void onMonitorPictureRequested(MonitorDTO monitor);
    }

}