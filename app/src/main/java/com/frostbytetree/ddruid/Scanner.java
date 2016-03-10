package com.frostbytetree.ddruid;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;

public class Scanner extends AppCompatActivity {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String CLASS_NAME = "Scanner";

    // TODO: Rename and change types of parameters
    //private String mParam1;
    //private String mParam2;

    //private IDataInflateListener mListener;

    private Camera mCamera;
    private CameraPreview mPreview;
    private Handler autoFocusHandler;

    //private Context context;
    ImageScanner scanner;
    AppLogic appLogic;
    Toolbar toolbar;

    private boolean barcodeScanned = false;
    private boolean previewing = true;

    Intent intent;

    public Scanner() {
        // Required empty public constructor
        Log.d(CLASS_NAME, "Constructor called");
    }


    static {
        System.loadLibrary("iconv");
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appLogic = AppLogic.getInstance();
        /*
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        */
        intent = getIntent();
        setContentView(R.layout.activity_scanner);
        toolbar = (Toolbar)findViewById(R.id.scanner_toolbar);
        String title = intent.getStringExtra("scan_label");
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTheme(appLogic.configFile.custom_color);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        autoFocusHandler = new Handler();

        if(mCamera != null)
            releaseCamera();
        mCamera = getCameraInstance();
        mCamera.setDisplayOrientation(90);
        mCamera.startPreview();
        previewing = true;
        Log.i(CLASS_NAME, "Camera loaded!");
        /* Instance barcode scanner */
        scanner = new ImageScanner();
        scanner.setConfig(0, Config.X_DENSITY, 3);
        scanner.setConfig(0, Config.Y_DENSITY, 3);

        mPreview = new CameraPreview(this, mCamera, previewCb, autoFocusCB);
        FrameLayout preview = (FrameLayout)findViewById(R.id.cameraPreview);
        preview.addView(mPreview);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void onPause() {
        super.onPause();
        releaseCamera();
        finish();
    }


    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open();
            Log.i(CLASS_NAME, "Camera instance: " + c.toString());
        } catch (Exception e){
            Log.i(CLASS_NAME, "Could not open camera: " + e);
        }
        return c;
    }

    public void releaseCamera() {
        if (mCamera != null) {
            previewing = false;
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
            mPreview = null;
        }
    }

    private Runnable doAutoFocus = new Runnable() {
        public void run() {
            if (previewing)
                mCamera.autoFocus(autoFocusCB);
        }
    };

    Camera.PreviewCallback previewCb = new Camera.PreviewCallback() {
        public void onPreviewFrame(byte[] data, Camera camera) {
            Camera.Parameters parameters = camera.getParameters();
            Camera.Size size = parameters.getPreviewSize();
            Image barcode = new Image(size.width, size.height, "Y800");
            barcode.setData(data);

            int result = scanner.scanImage(barcode);

            if (result != 0) {
                previewing = false;
                mCamera.setPreviewCallback(null);
                mCamera.stopPreview();

                SymbolSet syms = scanner.getResults();
                for (Symbol sym : syms) {
                    barcodeScanned = true;
                    intent.putExtra("scanned_code", sym.getData());
                    setResult(Activity.RESULT_OK, intent);
                    onBackPressed();
                    //scanText.setText("barcode result " + sym.getData());
                    //mListener.codeScanned(sym.getData());

                }
            }
        }
    };

    // Mimic continuous auto-focusing
    Camera.AutoFocusCallback autoFocusCB = new Camera.AutoFocusCallback() {
        public void onAutoFocus(boolean success, Camera camera) {
            autoFocusHandler.postDelayed(doAutoFocus, 1000);
        }
    };



    /* OLD FRAGMENT STUFF
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView  = inflater.inflate(R.layout.fragment_scanner, container, false);
        FrameLayout preview = (FrameLayout)rootView.findViewById(R.id.cameraPreview);
        preview.addView(mPreview);
        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        if (context instanceof IDataInflateListener) {
            mListener = (IDataInflateListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.i(CLASS_NAME, "On detach called!");
        mListener = null;
    }
    */

}
