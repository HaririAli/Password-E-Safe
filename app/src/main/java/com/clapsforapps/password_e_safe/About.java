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

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by ALI on 9/26/2015.
 */
public class About extends AppCompatActivity {

    private SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);
        settings = getApplicationContext().getSharedPreferences(SharedPrefs.PREFS_NAME, MODE_PRIVATE);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        SharedPrefs.isInApp = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!SharedPrefs.isInApp && settings.getBoolean(SharedPrefs.PREFS_KEY_BOOL, false))
            startActivityForResult(new Intent(About.this, PasskeyActivity.class), MainActivity.ACTIVITY_PASSKEY);
        SharedPrefs.isInApp = false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == MainActivity.ACTIVITY_PASSKEY && resultCode == MainActivity.CLOSE_ALL) {
            setResult(MainActivity.CLOSE_ALL);
            finish();
        }
    }
}
