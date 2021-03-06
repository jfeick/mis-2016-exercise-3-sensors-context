package de.uni_weimar.benike.misex3;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Process;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.androidplot.util.PlotStatistics;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYStepMode;

import java.util.ArrayList;
import java.util.Arrays;

/* The following code snippets have been used as a reference:
    http://androidplot.com/docs/a-dynamic-xy-plot/
    https://developer.android.com/guide/topics/sensors/sensors_motion.html#sensors-motion-accel
    icons from Google under CC-BY 4.0 (https://creativecommons.org/licenses/by/4.0/#):
    https://design.google.com/icons/
 */


public class MainActivity extends Activity
        implements SeekBar.OnSeekBarChangeListener, SensorEventListener

{
    private static final String TAG = MainActivity.class.getName();

    private XYPlot mAccelerometerPlot = null;
    private XYPlot mFftPlot = null;

    private static int SAMPLE_MIN_VALUE = 1;
    private static int SAMPLE_MAX_VALUE = 250;
    private static int SAMPLE_STEP = 10;

    private static int WINDOW_MIN_VALUE = 5;
    private static int WINDOW_MAX_VALUE = 10;
    private static int WINDOW_STEP = 1;

    private int interval = SAMPLE_MIN_VALUE;


    private SimpleXYSeries mAccelerometerXSeries = null;
    private SimpleXYSeries mAccelerometerYSeries = null;
    private SimpleXYSeries mAccelerometerZSeries = null;
    private SimpleXYSeries mAccelerometerMSeries = null;

    private ArrayList<Double> mFftAverages = new ArrayList<>();
    private static final int LEN_AVERAGES = 128;

    private SimpleXYSeries mFftSeries = null;

    private int mWindowSize = 32;            // number of points to plot in history
    private SensorManager mSensorManager = null;
    private Sensor mAccelerometerSensor = null;

    private SeekBar mSampleRateSeekBar;
    private SeekBar mFftWindowSeekBar;

    private FFT mFft;
    private Thread mFftThread;
    private Thread mDetectActivityThread;

    private static final int mNotificationId = 42;

    private NotificationManager mNotificationManager = null;
    private NotificationCompat.Builder mNotificationBuilder = null;

    private static final double ACTIVITY_THRESHOLD_WALKING = 14.5;
    private static final double ACTIVITY_THRESHOLD_RUNNING = 19.7;

    private TextView mWindowSizeTextView;

    public enum ActivityState { UNSURE, STANDING, WALKING, RUNNING };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // register for accelerometer events:
        mSensorManager = (SensorManager) getApplicationContext()
                .getSystemService(Context.SENSOR_SERVICE);
        for (Sensor sensor : mSensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER)) {
            if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                mAccelerometerSensor = sensor;
            }
        }

        // if we can't access the accelerometer sensor then exit:
        if (mAccelerometerSensor == null) {
            Log.e(TAG, "Failed to attach to Accelerator Sensor.");
            Toast.makeText(this, "Error! Failed to create accelerometer sensor!", Toast.LENGTH_LONG)
                    .show();
            cleanup();
        }

        mSensorManager.registerListener(this, mAccelerometerSensor, SensorManager.SENSOR_DELAY_UI);


        // setup the Accelerometer History plot:
        mAccelerometerPlot = (XYPlot) findViewById(R.id.accelerometerPlot);
        mFftPlot = (XYPlot) findViewById(R.id.fftPlot);

        mAccelerometerPlot.setRangeBoundaries(-25, 25, BoundaryMode.FIXED);
        mAccelerometerPlot.setDomainBoundaries(0, mWindowSize - 1, BoundaryMode.FIXED);

        mFftPlot.setRangeBoundaries(0, 250, BoundaryMode.FIXED);
        mFftPlot.setDomainBoundaries(0, mWindowSize / 2, BoundaryMode.AUTO);
        mFftPlot.setDomainStep(XYStepMode.SUBDIVIDE, 10);
        //mFftPlot.setRangeStep(XYStepMode.INCREMENT_BY_VAL, 50);

        mAccelerometerXSeries = new SimpleXYSeries("X");
        mAccelerometerXSeries.useImplicitXVals();
        mAccelerometerYSeries = new SimpleXYSeries("Y");
        mAccelerometerYSeries.useImplicitXVals();
        mAccelerometerZSeries = new SimpleXYSeries("Z");
        mAccelerometerZSeries.useImplicitXVals();
        mAccelerometerMSeries = new SimpleXYSeries("magnitude");
        mAccelerometerMSeries.useImplicitXVals();

        mFftSeries = new SimpleXYSeries("FFT");
        mFftSeries.useImplicitXVals();

        mFftPlot.addSeries(mFftSeries,
                new LineAndPointFormatter(Color.rgb(0, 0, 0), null, null, null));

        mAccelerometerPlot.addSeries(mAccelerometerXSeries,
                new LineAndPointFormatter(Color.rgb(100, 100, 200), null, null, null));
        mAccelerometerPlot.addSeries(mAccelerometerYSeries,
                new LineAndPointFormatter(Color.rgb(100, 200, 100), null, null, null));
        mAccelerometerPlot.addSeries(mAccelerometerZSeries,
                new LineAndPointFormatter(Color.rgb(200, 100, 100), null, null, null));
        mAccelerometerPlot.addSeries(mAccelerometerMSeries,
                new LineAndPointFormatter(Color.rgb(0, 0, 0), null, null, null));
        mAccelerometerPlot.setDomainStepValue(5);
        mAccelerometerPlot.setTicksPerRangeLabel(3);
        mAccelerometerPlot.setDomainLabel("Sample Index");
        mAccelerometerPlot.getDomainLabelWidget().pack();
        mAccelerometerPlot.setRangeLabel("m/s^2");
        mAccelerometerPlot.getRangeLabelWidget().pack();

        final PlotStatistics histStats = new PlotStatistics(1000, false);
        mAccelerometerPlot.addListener(histStats);

        // perform hardware accelerated rendering of the plots
        mAccelerometerPlot.setLayerType(View.LAYER_TYPE_NONE, null);
        mFftPlot.setLayerType(View.LAYER_TYPE_NONE, null);

        mFftPlot.setTicksPerRangeLabel(5);
        mFftPlot.setTicksPerDomainLabel(1);

        mSampleRateSeekBar = (SeekBar) findViewById(R.id.sampleRateSeekBar);
        mSampleRateSeekBar.setMax((SAMPLE_MAX_VALUE - SAMPLE_MIN_VALUE) / SAMPLE_STEP);
        mSampleRateSeekBar.setOnSeekBarChangeListener(this);

        mFftWindowSeekBar = (SeekBar) findViewById(R.id.fftWindowSeekBar);
        mFftWindowSeekBar.setMax((WINDOW_MAX_VALUE - WINDOW_MIN_VALUE) / WINDOW_STEP);
        mFftWindowSeekBar.setOnSeekBarChangeListener(this);

        mWindowSizeTextView = (TextView) findViewById(R.id.windowSizeTextView);

        // Perform FFT calculations in background thread
        mFft = new FFT(mWindowSize);
        Runnable r = new PerformFft();
        mFftThread = new Thread(r);
        mFftThread.start();

        mNotificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_help_outline_black_24dp)
                .setContentTitle("MIS Ex3 Activity Recognizer")
                .setContentText("Trying to guess your activity")
                .setOngoing(true);

        Intent resultIntent = new Intent(this, MainActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mNotificationBuilder.setContentIntent(resultPendingIntent);

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(mNotificationId, mNotificationBuilder.build());

        Runnable detectActivity = new DetectActivity();
        mDetectActivityThread = new Thread(detectActivity);
        mDetectActivityThread.start();
    }

    private void resetSeries() {

        while (mAccelerometerMSeries.size() > mWindowSize) {
            mAccelerometerXSeries.removeFirst();
            mAccelerometerYSeries.removeFirst();
            mAccelerometerZSeries.removeFirst();
            mAccelerometerMSeries.removeFirst();
        }


        mAccelerometerPlot.redraw();
    }

    @Override
    public void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometerSensor,
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (seekBar == mSampleRateSeekBar) {
            Log.d(TAG, "Progress: " + progress);
            progress = seekBar.getMax() - progress;
            int value = SAMPLE_MIN_VALUE + progress * SAMPLE_STEP;
            Log.d(TAG, "Samplesize SeekBar value: " + value);
            ChangeSampleRate(value * 2000);
        } else if (seekBar == mFftWindowSeekBar) {
            int value = (int) Math.pow(2, WINDOW_MIN_VALUE + progress * WINDOW_STEP);
            Log.d(TAG, "Windowsize SeekBar value: " + value);

            mWindowSize = value;
            mAccelerometerPlot.setDomainBoundaries(0, mWindowSize - 1, BoundaryMode.FIXED);

            mFft = new FFT(mWindowSize);
            resetSeries();
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }


    public void ChangeSampleRate(int us) {
        Log.d(TAG, "Samplerate value: " + us);
        mSensorManager.unregisterListener(this);
        mSensorManager.registerListener(this, mAccelerometerSensor, us);
    }

    private void cleanup() {
        // unregister with the orientation sensor before exiting:
        mSensorManager.unregisterListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cleanup();
        mNotificationManager.cancel(mNotificationId);
    }

    // Called whenever a new accelSensor reading is taken.
    @Override
    public synchronized void onSensorChanged(SensorEvent sensorEvent) {

        // update instantaneous data:
        Number[] series1Numbers = {sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2]};


        // get rid the oldest sample in history:
        if (mAccelerometerXSeries.size() > mWindowSize - 1) {
            mAccelerometerXSeries.removeFirst();
            mAccelerometerYSeries.removeFirst();
            mAccelerometerZSeries.removeFirst();
            mAccelerometerMSeries.removeFirst();
        }

        // add the latest history sample:
        final float accelXdata = sensorEvent.values[0];
        final float accelYdata = sensorEvent.values[1];
        final float accelZdata = sensorEvent.values[2];
        mAccelerometerXSeries.addLast(null, accelXdata);
        mAccelerometerYSeries.addLast(null, accelYdata);
        mAccelerometerZSeries.addLast(null, accelZdata);
        mAccelerometerMSeries.addLast(null, Math.sqrt(accelXdata * accelXdata
                + accelYdata * accelYdata + accelZdata * accelZdata) /* - 9.81 */
        );

        //Log.d(TAG, "Sample added. Size of m series: " + mAccelerometerMSeries.size());

        // redraw the Plots
        mAccelerometerPlot.redraw();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        // Not interested in this event
    }

    private void updateNotification(ActivityState activityState) {


        if (activityState == ActivityState.STANDING) {
            mNotificationBuilder
                    .setSmallIcon(R.drawable.ic_accessibility_black_24dp)
                    .setContentText("You are standing/sitting");
        } else if (activityState == ActivityState.WALKING) {
            mNotificationBuilder
                    .setSmallIcon(R.drawable.ic_directions_walk_black_24dp)
                    .setContentText("You are walking");
        } else if (activityState == ActivityState.RUNNING) {
            mNotificationBuilder
                    .setSmallIcon(R.drawable.ic_accessibility_black_24dp)
                    .setContentText("You are running");
        } else { // unsure
            mNotificationBuilder
                    .setSmallIcon(R.drawable.ic_help_outline_black_24dp)
                    .setContentText("Could not determine your activity...");
        }

        mNotificationManager.notify(
                mNotificationId,
                mNotificationBuilder.build());
    }

    private class PerformFft implements Runnable {

        private Handler mFftHandler = new Handler();

        @Override
        public void run() {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
            if (mAccelerometerMSeries.size() == mWindowSize) {
                double re[] = new double[mWindowSize];
                double im[] = new double[mWindowSize];
                for (int i = 0; i < mAccelerometerMSeries.size(); ++i) {
                    re[i] = (double) mAccelerometerMSeries.getY(i);
                    im[i] = 0.0;
                }
                mFft.fft(re, im);

                final Number magnitude[] = new Number[mWindowSize / 2];
                for (int i = 0; i < mWindowSize / 2; ++i) {
                    magnitude[i] = Math.sqrt(re[i] * re[i] + im[i] * im[i]);
                    //magnitude[i] = //re[i] * re[i] + im[i] * im[i];
                }

                // Plot our magnitude on
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mFftSeries.setModel(Arrays.asList(magnitude), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY);
                        mFftPlot.redraw();
                    }
                });

                Double sum = 0.0;
                for (Number n : magnitude) sum += (Double) n;
                Double average = sum / magnitude.length;

                if (mFftAverages.size() > LEN_AVERAGES - 1) {
                    mFftAverages.remove(0);
                }
                mFftAverages.add(average);
            }
            mFftHandler.post(this);
        }
    }

    private class DetectActivity implements Runnable {

        private Handler mActivityHandler = new Handler();

        @Override
        public void run() {
            android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            if (mFftAverages.size() == LEN_AVERAGES) {
                // calculate complete average
                Double sum = 0.0;
                for (Double d : mFftAverages) sum += d;
                Double average = sum / LEN_AVERAGES;
                // Log.i(TAG, "Activity fft freq average: " + average);
                // we used adb wifi debugging to watch the logcat to determine thresholds for
                // different walking speeds (a bit problematic is the shift with different samplerates)
                if (average < ACTIVITY_THRESHOLD_WALKING) {
                    Log.d(TAG, "Activity detected: sitting/standing");
                    updateNotification(ActivityState.STANDING);
                }
                else if (average > ACTIVITY_THRESHOLD_WALKING
                        && average < ACTIVITY_THRESHOLD_RUNNING) {
                    Log.d(TAG, "Activity detected: walking");
                    updateNotification(ActivityState.WALKING);
                }
                else if (average > ACTIVITY_THRESHOLD_RUNNING) {
                    Log.d(TAG, "Activity detected: running");
                    updateNotification(ActivityState.RUNNING);
                }
                else {
                    updateNotification(ActivityState.UNSURE);
                }
                mFftAverages.clear();
            }
            mActivityHandler.postDelayed(this, 5000);
        }
    }
}
