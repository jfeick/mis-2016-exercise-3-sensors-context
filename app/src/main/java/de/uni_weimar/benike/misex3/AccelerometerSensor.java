package de.uni_weimar.benike.misex3;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import com.androidplot.xy.SimpleXYSeries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Observable;
import java.util.Observer;

public final class AccelerometerSensor
        extends Observable
        implements SensorEventListener {


    private static final String TAG = AccelerometerSensor.class.getName();

    private static final int HISTORY_SIZE = 32;            // number of points to plot in history
    private SensorManager mSensorManager = null;
    private Sensor accelSensor = null;
    private Context mContext = null;

    private int interval = 0;
    private int counter = 0;
    private boolean windowFull = false;

    private SimpleXYSeries accelXHistorySeries = null;
    private SimpleXYSeries accelYHistorySeries = null;
    private SimpleXYSeries accelZHistorySeries = null;
    private SimpleXYSeries accelMHistorySeries = null;

    double re[] = new double[HISTORY_SIZE];
    double im[] = new double[HISTORY_SIZE];
    FFT fft = new FFT(HISTORY_SIZE);

    public AccelerometerSensor(Context context) {
        mContext = context;

        accelXHistorySeries = new SimpleXYSeries("X");
        accelXHistorySeries.useImplicitXVals();
        accelYHistorySeries = new SimpleXYSeries("Y");
        accelYHistorySeries.useImplicitXVals();
        accelZHistorySeries = new SimpleXYSeries("Z");
        accelZHistorySeries.useImplicitXVals();
        accelMHistorySeries = new SimpleXYSeries("M");
        accelMHistorySeries.useImplicitXVals();

        // register for accelerometer events:
        mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        for (Sensor sensor : mSensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER)) {
            if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                accelSensor = sensor;
            }
        }

        // if we can't access the accelerometer sensor then exit:
        if (accelSensor == null) {
            Log.e(TAG, "Failed to attach to Accelerator Sensor.");
            cleanup();
        }

        mSensorManager.registerListener(this, accelSensor, SensorManager.SENSOR_DELAY_UI);
    }

    public SimpleXYSeries getXSeries() {
        return accelXHistorySeries;
    }

    public SimpleXYSeries getYSeries() {
        return accelYHistorySeries;
    }

    public SimpleXYSeries getZSeries() {
        return accelZHistorySeries;
    }

    public SimpleXYSeries getMSeries() {
        return accelMHistorySeries;
    }


    public void start() {

    }

    public void ChangeSampleRate(int us) {
        mSensorManager.unregisterListener(this);
        mSensorManager.registerListener(this, accelSensor,  us);
    }

    private void cleanup() {
        // unregister with the orientation sensor before exiting:
        mSensorManager.unregisterListener(this);
    }

    private Double[] fftMagnitude(double[] re, double[] im) {
        if (re.length != im.length) return null;
        Double[] fftMag = new Double[re.length];
        for (int i = 0; i < re.length; i++) {
            fftMag[i] = Math.pow(re[i], 2) + Math.pow(im[i], 2);
        }
        return fftMag;
    }

    void performFFT() {
        for(int i = 0; i < accelMHistorySeries.size(); ++i) {
            re[i] = accelMHistorySeries.getY(i).doubleValue();
        }
        fft.fft(re, im);
        Double mag[] = fftMagnitude(re, im);
    }

    // Called whenever a new accelSensor reading is taken.
    @Override
    public synchronized void onSensorChanged(SensorEvent sensorEvent) {

        // update instantaneous data:
        Number[] series1Numbers = {sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2]};


        // get rid the oldest sample in history:
        if (accelZHistorySeries.size() > HISTORY_SIZE - 1) {
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
        //accelHistoryPlot.redraw();
        // notify observer for new data

        counter = (counter + 1 ) % HISTORY_SIZE;
        if (counter == 0)
        {
            performFFT();
        }

        setChanged();
        notifyObservers();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        // Not interested in this event
    }

    @Override
    public void notifyObservers() {
        super.notifyObservers();
    }

    @Override
    public void addObserver(Observer observer) {
        super.addObserver(observer);

    }
}
