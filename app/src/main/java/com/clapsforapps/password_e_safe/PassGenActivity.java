/*
 * Copyright (c) 2015, The Codefather. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 3 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 3 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 3 along with this work.
 */

package com.clapsforapps.password_e_safe;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSeekBar;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by ALI on 1/12/2017.
 */

public class PassGenActivity extends AppCompatActivity implements SensorEventListener, View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    private static final String CAPS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String SMALL   = "abcdefghijklmnopqrstuvwxyz";
    private static final String NUMBERS     = "0123456789";
    private static final String SYMBOLS   = "!@#$%^&*_=+-/";

    private Toolbar tbPassGen;
    private CardView cvResult;
    private TextView tvPassGenResult, tvPassLength;
    private ImageButton ibCopy;
    private FloatingActionButton fabAgain;
    private AppCompatSeekBar sbPassLength;
    private CheckBox chbCaps, chbSmall, chbNumbers, chbSymbols;
    private int passLength, nextChar;

    private SensorManager sensorManager;
    private float acceleration; // acceleration apart from gravity
    private float currentAcceleration; // current acceleration including gravity
    private float lastAcceleration; // last acceleration including gravity
    private float randomChar = 0;
    private boolean isShaking = false;
    private char[] password;
    private String charset;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pass_gen_activity);

        tbPassGen = (Toolbar)findViewById(R.id.tbPassGen);
        setSupportActionBar(tbPassGen);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        acceleration = 0.00f;
        currentAcceleration = SensorManager.GRAVITY_EARTH;
        lastAcceleration = SensorManager.GRAVITY_EARTH;

        tvPassGenResult = (TextView)findViewById(R.id.tvPassGenResult);
        tvPassLength = (TextView)findViewById(R.id.tvPassLength);
        sbPassLength = (AppCompatSeekBar)findViewById(R.id.sbPassLength);
        cvResult = (CardView)findViewById(R.id.cvResult);
        chbCaps = (CheckBox)findViewById(R.id.chbCaps);
        chbSmall = (CheckBox)findViewById(R.id.chbSmall);
        chbNumbers = (CheckBox)findViewById(R.id.chbNumbers);
        chbSymbols = (CheckBox)findViewById(R.id.chbSymbols);
        fabAgain = (FloatingActionButton)findViewById(R.id.fabAgain);
        ibCopy = (ImageButton) findViewById(R.id.ibCopy);

        ibCopy.setOnClickListener(this);
        fabAgain.setOnClickListener(this);

        sbPassLength.setOnSeekBarChangeListener(this);
        tvPassLength.append(" 8");
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home)
            onBackPressed();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.fabAgain:
                generate();
                break;
            case R.id.ibCopy:
                ((ClipboardManager) getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("Password", tvPassGenResult.getText()));
                Toast.makeText(this, R.string.copied, Toast.LENGTH_LONG).show();
                break;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        lastAcceleration = currentAcceleration;

        randomChar = event.values[0] * event.values[1] - event.values[0] * event.values[2] + event.values[1] * event.values[2];
        currentAcceleration = (float) Math.sqrt((double)(event.values[0] * event.values[0] + event.values[1] * event.values[1] + event.values[2] * event.values[2]));
        acceleration = acceleration * 0.9f + currentAcceleration - lastAcceleration; // perform low-cut filter
        if(acceleration > 6){
            isShaking = true;
            if(nextChar < passLength){
                randomChar = Math.abs(randomChar);
                password[nextChar] = charset.charAt((int)randomChar % charset.length());
                nextChar++;
            }
            else {
                if(chbCaps.isChecked()) {
                    randomChar = event.values[0] * event.values[1] * event.values[2];
                    randomChar = Math.abs(randomChar);
                    password[(int) randomChar % passLength] = CAPS.charAt((int) randomChar % CAPS.length());
                }
                if(chbSmall.isChecked()) {
                    randomChar = (event.values[2] - event.values[0]) * (event.values[1] - event.values[0]);
                    randomChar = Math.abs(randomChar);
                    password[(int) randomChar % passLength] = SMALL.charAt((int) randomChar % SMALL.length());
                }
                if(chbNumbers.isChecked()) {
                    randomChar = (event.values[0] - event.values[1]) * (event.values[2] - event.values[1]);
                    randomChar = Math.abs(randomChar);
                    password[(int) randomChar % passLength] = NUMBERS.charAt((int) randomChar % NUMBERS.length());
                }
                if(chbSymbols.isChecked()) {
                    randomChar = (event.values[1] - event.values[2]) * (event.values[0] - event.values[2]);
                    randomChar = Math.abs(randomChar);
                    password[(int) randomChar % passLength] = SYMBOLS.charAt((int) randomChar % SYMBOLS.length());
                }
                onPhoneShaken();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void onPhoneShaken(){
        ((Vibrator)getSystemService(Context.VIBRATOR_SERVICE)).vibrate(600);
        sensorManager.unregisterListener(this);

        tvPassGenResult.setText(new String(password));
        cvResult.setVisibility(View.VISIBLE);
        isShaking = false;
    }

    private void generate(){
        if(!isShaking) {
            charset = "";
            if (chbCaps.isChecked())
                charset += CAPS;
            if (chbSmall.isChecked())
                charset += SMALL;
            if (chbNumbers.isChecked())
                charset += NUMBERS;
            if (chbSymbols.isChecked())
                charset += SYMBOLS;

            if (charset.length() > 0) {
                cvResult.setVisibility(View.GONE);
                passLength = 8 + sbPassLength.getProgress();
                nextChar = 0;
                password = new char[passLength];

                acceleration = 0.00f;
                currentAcceleration = SensorManager.GRAVITY_EARTH;
                lastAcceleration = SensorManager.GRAVITY_EARTH;
                randomChar = 0;
                sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
            }
            else
                Snackbar.make(findViewById(R.id.clPassGen), getString(R.string.charsetEmpty), Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        tvPassLength.setText(getString(R.string.passLength) + " " + (8 + progress));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
