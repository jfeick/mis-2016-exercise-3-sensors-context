package de.uni_weimar.benike.misex3;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import com.androidplot.util.PlotStatistics;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;

import java.util.Arrays;
import java.util.Observable;
import java.util.Observer;

/*
public class FFTPlotFragment extends Fragment
        implements SeekBar.OnSeekBarChangeListener,
        Observer {

    private static final String TAG = FFTPlotFragment.class.getName();
    private XYPlot FFTPlot = null;
    private SimpleXYSeries FFTSeries = null;
    private FFT fft = null;




    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        //FragmentActivity faActivity = (FragmentActivity) super.getActivity();
        View rootView = (LinearLayout) inflater.inflate(R.layout. fragment_fft_plot, container, false);

        // setup the Accelerometer History plot:
        FFTPlot = (XYPlot) rootView.findViewById(R.id.fftPlot);

        //accelHistoryPlot.setRangeBoundaries(-30, 30, BoundaryMode.FIXED);
        FFTPlot.setDomainBoundaries(0, 30, BoundaryMode.FIXED);
        AccelerometerSensor sensor = ((MainActivity) getActivity()).getSensor();

        FFTSeries = new SimpleXYSeries("fft");
        FFTSeries.useImplicitXVals();
        fft = new FFT(32);

        sensor.addObserver(this);

        FFTPlot.addSeries(FFTSeries, new LineAndPointFormatter(Color.rgb(100, 100, 200), null, null, null));
        FFTPlot.setTicksPerRangeLabel(3);
        FFTPlot.setDomainStepValue(5);
        FFTPlot.setDomainLabel("Sample Index");
        FFTPlot.getDomainLabelWidget().pack();
        FFTPlot.setRangeLabel("m/s^2");
        FFTPlot.getRangeLabelWidget().pack();

        // setup checkboxes:
        final PlotStatistics histStats = new PlotStatistics(1000, false);
        FFTPlot.addListener(histStats);

        FFTPlot.setLayerType(View.LAYER_TYPE_NONE, null);
        //accelHistoryPlot.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        //histStats.setAnnotatePlotEnabled(true);

        //SeekBar seekBar = (SeekBar) rootView.findViewById(R.id.sampleRateSeekBar);
        //seekBar.setMax( (SAMPLE_MAX_VALUE - SAMPLE_MIN_VALUE) / SAMPLE_STEP );
        //seekBar.setOnSeekBarChangeListener(this);

        //Log.d(TAG, "Sensor delay UI: " + SensorManager.SENSOR_DELAY_UI);
        //Log.d(TAG, "Sensor delay fastest: " + SensorManager.SENSOR_DELAY_FASTEST);
        //Log.d(TAG, "Sensor delay normal: " + SensorManager.SENSOR_DELAY_NORMAL);
        //Log.d(TAG, "Sensor delay game:" + SensorManager.SENSOR_DELAY_GAME);

        return rootView;
    }

    public static FFTPlotFragment newInstance() {
        FFTPlotFragment fragment = new FFTPlotFragment();
        //Bundle args = new Bundle();
        //fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        //mSensorManager.registerListener(this, accelSensor,
        //        SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        //int value = SAMPLE_MIN_VALUE + progress * SAMPLE_STEP;
        //Log.d(TAG, "Sample value: " + value + "us");

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {}

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {}



    @Override
    public void update(Observable observable, Object data) {

    }


}

*/