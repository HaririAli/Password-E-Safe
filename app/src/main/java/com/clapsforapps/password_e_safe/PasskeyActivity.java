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

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

/**
 * Created by ALI on 2/27/2016.
 */
public class PasskeyActivity extends AppCompatActivity implements View.OnTouchListener, View.OnClickListener, View.OnKeyListener{

    private EditText etPass;
    private ImageButton ibShowPasskey;
    private FloatingActionButton fabSubmitPass;
    private LinearLayout llPasskey;
    //private ImageButton ibClose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_passkey);
        setSupportActionBar((Toolbar)findViewById(R.id.tbPasskeyActivity));

        fabSubmitPass = (FloatingActionButton) findViewById(R.id.fab_submit_pass);
        etPass = (EditText) findViewById(R.id.etPass);
        ibShowPasskey = (ImageButton) findViewById(R.id.ibShowPasskey);
        //ibClose = (ImageButton) findViewById(R.id.ibClose);
        llPasskey = (LinearLayout)findViewById(R.id.llPasskey);

        fabSubmitPass.setOnClickListener(this);
        ibShowPasskey.setOnTouchListener(this);
        //ibClose.setOnClickListener(this);
        etPass.setOnKeyListener(this);
    }

    private void login(){
        Database db = new Database(this);
        String passkeyHash = db.getPasskey();
        db.close();
        try {
            if (Encryption.encryptMD5(etPass.getText().toString()).equals(passkeyHash)){
                SharedPrefs.passkey = etPass.getText().toString();
                SharedPrefs.isInApp = true;
                setResult(MainActivity.UNLOCKED);
                finish();
            }
            else {
                Snackbar.make(llPasskey, getResources().getString(R.string.wrong_passkey), Snackbar.LENGTH_LONG).show();
            }
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException  e) {
            e.printStackTrace();
            etPass.setText("");
            Snackbar.make(llPasskey, "Error!", Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (v.getId()){
            case R.id.ibShowPasskey:
                if(event.getAction() == MotionEvent.ACTION_UP) {
                    etPass.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    etPass.setSelection(etPass.getText().length());
                }
                else if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    etPass.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    etPass.setSelection(etPass.getText().length());
                }
                break;
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.fab_submit_pass:
                login();
                break;
            //case R.id.ibClose:
                //setResult(MainActivity.CLOSE_ALL);
                //finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            setResult(MainActivity.CLOSE_ALL);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (v.getId() == R.id.etPass && event.getAction() == KeyEvent.ACTION_UP) {
            switch (keyCode){
                case KeyEvent.KEYCODE_ENTER:
                    login();
                    break;
                case KeyEvent.KEYCODE_DEL:
                    if(etPass.getText().length() > 0) {
                        etPass.setText(etPass.getText().toString().substring(0, etPass.getText().toString().length() - 1));
                        etPass.setSelection(etPass.getText().length());
                    }
                    break;
            }
        }
        return true;
    }
}
