
package de.uni_weimar.benike.misex3;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;

import com.androidplot.Plot;
import com.androidplot.util.PixelUtils;
import com.androidplot.xy.XYSeries;
import com.androidplot.xy.*;

import java.text.DecimalFormat;
import java.util.Observable;
import java.util.Observer;

public class MainActivity extends Activity implements SensorEventListener {

    @Override
    public void onSensorChanged(SensorEvent event) {
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    // redraws a plot whenever an update is received:
    private class MyPlotUpdater implements Observer {
        Plot plot;

        public MyPlotUpdater(Plot plot) {
            this.plot = plot;
        }

        @Override
        public void update(Observable o, Object arg) {
            plot.redraw();
        }
    }

    private XYPlot dynamicPlot;
    private MyPlotUpdater plotUpdater;
    private SensorManager mSensormanager;
    private Sensor mSensor;
    private Thread myThread;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        // android boilerplate stuff
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // get handles to our View defined in layout.xml:
        dynamicPlot = (XYPlot) findViewById(R.id.dynamicXYPlot);

        plotUpdater = new MyPlotUpdater(dynamicPlot);

        // only display whole numbers in domain labels
        dynamicPlot.getGraphWidget().setDomainValueFormat(new DecimalFormat("0"));

        // getInstance and position datasets:
        mSensormanager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensormanager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensormanager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_UI);
        //SampleDynamicSeries sine1Series = new SampleDynamicSeries(data, 0, "Sine 1");
        //SampleDynamicSeries sine2Series = new SampleDynamicSeries(data, 1, "Sine 2");

        LineAndPointFormatter formatter1 = new LineAndPointFormatter(
                Color.rgb(0, 0, 0), null, null, null);
        formatter1.getLinePaint().setStrokeJoin(Paint.Join.ROUND);
        formatter1.getLinePaint().setStrokeWidth(10);
        //dynamicPlot.addSeries(sine1Series,
        //        formatter1);

        LineAndPointFormatter formatter2 =
                new LineAndPointFormatter(Color.rgb(0, 0, 200), null, null, null);
        formatter2.getLinePaint().setStrokeWidth(10);
        formatter2.getLinePaint().setStrokeJoin(Paint.Join.ROUND);

        //formatter2.getFillPaint().setAlpha(220);
        //dynamicPlot.addSeries(sine2Series, formatter2);

        // hook up the plotUpdater to the data model:
        //data.addObserver(plotUpdater);

        // thin out domain tick labels so they dont overlap each other:
        dynamicPlot.setDomainStepMode(XYStepMode.INCREMENT_BY_VAL);
        dynamicPlot.setDomainStepValue(5);

        dynamicPlot.setRangeStepMode(XYStepMode.INCREMENT_BY_VAL);
        dynamicPlot.setRangeStepValue(10);

        dynamicPlot.setRangeValueFormat(new DecimalFormat("###.#"));

        // uncomment this line to freeze the range boundaries:
        dynamicPlot.setRangeBoundaries(-100, 100, BoundaryMode.FIXED);

        // create a dash effect for domain and range grid lines:
        DashPathEffect dashFx = new DashPathEffect(
                new float[] {PixelUtils.dpToPix(3), PixelUtils.dpToPix(3)}, 0);
        dynamicPlot.getGraphWidget().getDomainGridLinePaint().setPathEffect(dashFx);
        dynamicPlot.getGraphWidget().getRangeGridLinePaint().setPathEffect(dashFx);
    }
    @Override
    public void onResume() {
        // kick off the data generating thread:
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    class SampleDynamicSeries implements XYSeries {
        private int seriesIndex;
        private String title;
        private Sensor sensor;

        public SampleDynamicSeries(Sensor sensor, int seriesIndex, String title) {
            this.sensor = sensor;
            this.seriesIndex = seriesIndex;
            this.title = title;
        }

        @Override
        public String getTitle() {
            return title;
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public Number getX(int index) {
            return 0;
        }


        @Override
        public Number getY(int index) {
            return 0;
        }
    }
}