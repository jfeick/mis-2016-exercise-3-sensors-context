
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

import java.util.Observable;
import java.util.Observer;


public class AccelerometerPlotFragment extends Fragment
        implements SeekBar.OnSeekBarChangeListener,
                    Observer {

    private static final String TAG = AccelerometerPlotFragment.class.getName();


    private XYPlot accelHistoryPlot = null;

    private static int SAMPLE_MIN_VALUE = 0;
    private static int SAMPLE_MAX_VALUE = 1000000;
    private static int SAMPLE_STEP = 10000;

    private int interval = SAMPLE_MIN_VALUE;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        //FragmentActivity faActivity = (FragmentActivity) super.getActivity();
        View rootView = (LinearLayout) inflater.inflate(R.layout.fragment_accelerometer_plot, container, false);

        // setup the Accelerometer History plot:
        accelHistoryPlot = (XYPlot) rootView.findViewById(R.id.accelPlot);

        //accelHistoryPlot.setRangeBoundaries(-30, 30, BoundaryMode.FIXED);
        accelHistoryPlot.setDomainBoundaries(0, 30, BoundaryMode.FIXED);
        AccelerometerSensor sensor = ((MainActivity) getActivity()).getSensor();
        sensor.addObserver(this);
        accelHistoryPlot.addSeries(sensor.getXSeries(), new LineAndPointFormatter(Color.rgb(100, 100, 200), null, null, null));
        accelHistoryPlot.addSeries(sensor.getYSeries(), new LineAndPointFormatter(Color.rgb(100, 200, 100), null, null, null));
        accelHistoryPlot.addSeries(sensor.getZSeries(), new LineAndPointFormatter(Color.rgb(200, 100, 100), null, null, null));
        accelHistoryPlot.addSeries(sensor.getMSeries(), new LineAndPointFormatter(Color.rgb(255, 255, 255), null, null, null));
        accelHistoryPlot.setDomainStepValue(5);
        accelHistoryPlot.setTicksPerRangeLabel(3);
        accelHistoryPlot.setDomainLabel("Sample Index");
        accelHistoryPlot.getDomainLabelWidget().pack();
        accelHistoryPlot.setRangeLabel("m/s^2");
        accelHistoryPlot.getRangeLabelWidget().pack();

        // setup checkboxes:
        final PlotStatistics histStats = new PlotStatistics(1000, false);
        accelHistoryPlot.addListener(histStats);

        accelHistoryPlot.setLayerType(View.LAYER_TYPE_NONE, null);
        //accelHistoryPlot.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        //histStats.setAnnotatePlotEnabled(true);

        SeekBar seekBar = (SeekBar) rootView.findViewById(R.id.sampleRateSeekBar);
        seekBar.setMax( (SAMPLE_MAX_VALUE - SAMPLE_MIN_VALUE) / SAMPLE_STEP );
        seekBar.setOnSeekBarChangeListener(this);

        //Log.d(TAG, "Sensor delay UI: " + SensorManager.SENSOR_DELAY_UI);
        //Log.d(TAG, "Sensor delay fastest: " + SensorManager.SENSOR_DELAY_FASTEST);
        //Log.d(TAG, "Sensor delay normal: " + SensorManager.SENSOR_DELAY_NORMAL);
        //Log.d(TAG, "Sensor delay game:" + SensorManager.SENSOR_DELAY_GAME);

        return rootView;
    }

    public static AccelerometerPlotFragment newInstance(int sectionNumber) {
        AccelerometerPlotFragment fragment = new AccelerometerPlotFragment();
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
        int value = SAMPLE_MIN_VALUE + progress * SAMPLE_STEP;
        Log.d(TAG, "Sample value: " + value + "us");

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {}

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {}

    @Override
    public void update(Observable observable, Object data) {
        accelHistoryPlot.redraw();
    }


}