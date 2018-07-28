package com.example.kazuki.ryukyusound;

import android.content.Context;
import android.content.res.TypedArray;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager mSensorManager;
    private Sensor mMagField;
    private Sensor mAccelerometer;
    private static final int AZIMUTH_THRESHOLD = 15;

    private static final int MATRIX_SIZE = 16;
    private float[] mgValues = new float[3];

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this, mAccelerometer);
        mSensorManager.unregisterListener(this, mMagField);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, 100000);
        mSensorManager.registerListener(this, mMagField, 100000);

        TypedArray notes = getResources().obtainTypedArray(R.array.notes);
        mplayer = new MediaPlayer[notes.length()];
        for (int i = 0; i < notes.length(); i++) {
            mplayer[i] = MediaPlayer.create(this, notes.getResourceId(i, -1));
        }
    }

    private float[] acValues = new float[3];

    private int nowScale = 0;
    private int oldScale = 9;
    private int nowAzimuth = 0;
    private int oldAzimuth = 0;

    private MediaPlayer[] mplayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagField = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        float[] inR = new float[MATRIX_SIZE];
        float[] outR = new float[MATRIX_SIZE];
        float[] I = new float[MATRIX_SIZE];
        float[] orValues = new float[3];
        TextView txt01 = findViewById(R.id.txt01);

        switch (sensorEvent.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                acValues = sensorEvent.values.clone();
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                mgValues = sensorEvent.values.clone();
                break;
        }

        if (mgValues != null && acValues != null) {
            SensorManager.getRotationMatrix(inR, I, acValues, mgValues);

            SensorManager.remapCoordinateSystem(inR, SensorManager.AXIS_X, SensorManager.AXIS_Z, outR);
            SensorManager.getOrientation(outR, orValues);

            StringBuilder strBuild = new StringBuilder();
            strBuild.append("方向角（アジマス）:");
            strBuild.append(rad2Deg(orValues[0]));
            strBuild.append("\n");
            strBuild.append("傾斜角（ピッチ）:");
            strBuild.append(rad2Deg(orValues[1]));
            strBuild.append("\n");
            nowScale = rad2Deg(orValues[1]) / 10;
            strBuild.append("index:" + nowScale);
            nowAzimuth = rad2Deg(orValues[0]);
            txt01.setText(strBuild.toString());

            if (nowScale != oldScale) {
                playSound(nowScale);
                oldScale = nowScale;
                oldAzimuth = nowAzimuth;
            } else if (Math.abs(oldAzimuth - nowAzimuth) > AZIMUTH_THRESHOLD) {
                playSound(nowScale);
                oldAzimuth = nowAzimuth;
            }
        }
    }

    private void playSound(int scale) {
        mplayer[scale].seekTo(0);
        mplayer[scale].start();
    }

    private int rad2Deg(float rad) {
        return (int) Math.floor(Math.abs(Math.toDegrees(rad)));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
