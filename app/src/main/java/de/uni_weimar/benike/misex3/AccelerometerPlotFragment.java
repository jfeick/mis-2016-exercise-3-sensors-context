
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


public class AccelerometerPlotFragment extends Fragment
        implements SensorEventListener,
                    SeekBar.OnSeekBarChangeListener{


    private static final String TAG = AccelerometerPlotFragment.class.getName();

    private static final int HISTORY_SIZE = 32;            // number of points to plot in history
    private SensorManager mSensorManager = null;
    private Sensor accelSensor = null;

    private XYPlot accelHistoryPlot = null;
    private SimpleXYSeries accelXHistorySeries = null;
    private SimpleXYSeries accelYHistorySeries = null;
    private SimpleXYSeries accelZHistorySeries = null;
    private SimpleXYSeries accelMHistorySeries = null;

    public static int SAMPLE_MIN_VALUE = 0;
    public static int SAMPLE_MAX_VALUE = 1000000;
    public static int SAMPLE_STEP = 10000;

    int interval = SAMPLE_MIN_VALUE;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        //FragmentActivity faActivity = (FragmentActivity) super.getActivity();
        View rootView = (LinearLayout) inflater.inflate(R.layout.fragment_accelerometer_plot, container, false);

        // setup the Accelerometer History plot:
        accelHistoryPlot = (XYPlot) rootView.findViewById(R.id.accelPlot);

        accelXHistorySeries = new SimpleXYSeries("X");
        accelXHistorySeries.useImplicitXVals();
        accelYHistorySeries = new SimpleXYSeries("Y");
        accelYHistorySeries.useImplicitXVals();
        accelZHistorySeries = new SimpleXYSeries("Z");
        accelZHistorySeries.useImplicitXVals();
        accelMHistorySeries = new SimpleXYSeries("M");
        accelMHistorySeries.useImplicitXVals();


        //accelHistoryPlot.setRangeBoundaries(-30, 30, BoundaryMode.FIXED);
        accelHistoryPlot.setDomainBoundaries(0, 30, BoundaryMode.FIXED);
        accelHistoryPlot.addSeries(accelXHistorySeries, new LineAndPointFormatter(Color.rgb(100, 100, 200), null, null, null));
        accelHistoryPlot.addSeries(accelYHistorySeries, new LineAndPointFormatter(Color.rgb(100, 200, 100), null, null, null));
        accelHistoryPlot.addSeries(accelZHistorySeries, new LineAndPointFormatter(Color.rgb(200, 100, 100), null, null, null));
        accelHistoryPlot.addSeries(accelMHistorySeries, new LineAndPointFormatter(Color.rgb(255, 255, 255), null, null, null));
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

        // register for accelerometer events:
        mSensorManager = (SensorManager) getActivity().getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
        for (Sensor sensor : mSensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER)) {
            if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                accelSensor = sensor;
            }
        }

        // if we can't access the accelerometer sensor then exit:
        if (accelSensor == null) {
            Log.e(TAG, "Failed to attach to accelSensor.");
            cleanup();
        }

        SeekBar seekBar = (SeekBar) rootView.findViewById(R.id.sampleRateSeekBar);

        seekBar.setMax( (SAMPLE_MAX_VALUE - SAMPLE_MIN_VALUE) / SAMPLE_STEP );
        seekBar.setOnSeekBarChangeListener(this);

        mSensorManager.registerListener(this, accelSensor, SensorManager.SENSOR_DELAY_UI);

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
        mSensorManager.registerListener(this, accelSensor,
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        int value = SAMPLE_MIN_VALUE + progress * SAMPLE_STEP;
        Log.d(TAG, "Sample value: " + value + "us");
        mSensorManager.unregisterListener(this);
        mSensorManager.registerListener(this, accelSensor,  value);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {}

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {}

    private void cleanup() {
        // unregister with the orientation sensor before exiting:
        mSensorManager.unregisterListener(this);
    }

    // Called whenever a new accelSensor reading is taken.
    @Override
    public synchronized void onSensorChanged(SensorEvent sensorEvent) {

        // update instantaneous data:
        Number[] series1Numbers = {sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2]};

        // get rid the oldest sample in history:
        if (accelZHistorySeries.size() > HISTORY_SIZE) {
            accelZHistorySeries.removeFirst();
            accelYHistorySeries.removeFirst();
            accelXHistorySeries.removeFirst();
            accelMHistorySeries.removeFirst();
        }

        // add the latest history sample:
        final float accelXdata = sensorEvent.values[0];
        final float accelYdata = sensorEvent.values[1];
        final float accelZdata = sensorEvent.values[2];
        accelXHistorySeries.addLast(null, accelXdata);
        accelYHistorySeries.addLast(null, accelYdata);
        accelZHistorySeries.addLast(null, accelZdata);
        accelMHistorySeries.addLast(null, Math.sqrt(accelXdata * accelXdata
                + accelYdata * accelYdata + accelZdata * accelZdata)
        );

        // redraw the Plots:
        accelHistoryPlot.redraw();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        // Not interested in this event
    }
}