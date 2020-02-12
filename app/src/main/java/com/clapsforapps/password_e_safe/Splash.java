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
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.util.HashMap;

public class Splash extends AppCompatActivity implements View.OnClickListener, InitialConfig {

    private FragmentRestore fragmentRestore = new FragmentRestore();
    private FragmentComplete fragmentComplete = new FragmentComplete();
    private FragmentDailyBackup fragmentDailyBackup = new FragmentDailyBackup();
    private FragmentSetPasskey fragmentSetPasskey = new FragmentSetPasskey();
    private FragmentWelcome fragmentWelcome = new FragmentWelcome();
    private FragmentManager fragmentManager;
    private HashMap<Integer, Fragment> fragmentsHashMap = new HashMap<>();
    private SharedPreferences settings;
    private Button btNext;
    private int currentFragment = 0;
    private boolean  firstLogin = false, canRestore = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_wizard);

        Database db = new Database(this);
        db.getWritableDatabase(); // to be deleted
        db.close();
        settings = getApplicationContext().getSharedPreferences(SharedPrefs.PREFS_NAME, MODE_PRIVATE);
        firstLogin = settings.getBoolean(SharedPrefs.PREFS_FIRST_BOOL, true);
        boolean isPass = settings.getBoolean(SharedPrefs.PREFS_KEY_BOOL, false);

        if(firstLogin) {
            File backupDbFile = new File(Environment.getExternalStorageDirectory() + "/Android/data/PasswordBackup", Database.DATABASE_NAME);
            File file = new File(Environment.getExternalStorageDirectory() + "/Android/data/PasswordBackup", "copy_passwords.db");
            canRestore = (backupDbFile.exists() || file.exists());
            fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().add(R.id.llFragmentContainer, fragmentComplete).commit();
            fragmentManager.beginTransaction().detach(fragmentComplete).commit();
            fragmentManager.beginTransaction().add(R.id.llFragmentContainer, fragmentDailyBackup).commit();
            fragmentManager.beginTransaction().detach(fragmentDailyBackup).commit();
            fragmentManager.beginTransaction().add(R.id.llFragmentContainer, fragmentRestore).commit();
            fragmentManager.beginTransaction().detach(fragmentRestore).commit();
            fragmentManager.beginTransaction().add(R.id.llFragmentContainer, fragmentSetPasskey).commit();
            fragmentManager.beginTransaction().detach(fragmentSetPasskey).commit();
            fragmentManager.beginTransaction().add(R.id.llFragmentContainer, fragmentWelcome).commit();
            fragmentsHashMap.put(0, fragmentWelcome);
            fragmentsHashMap.put(1, canRestore? fragmentRestore : fragmentSetPasskey);
            fragmentsHashMap.put(2, fragmentDailyBackup);
            fragmentsHashMap.put(3, fragmentComplete);

            btNext = (Button)findViewById(R.id.btNext);
            btNext.setOnClickListener(this);
        }
        else if (isPass){
            startActivity(new Intent(Splash.this, MainActivity.class));
            finish();
        }
        else{
            SharedPrefs.isInApp = true;
            setContentView(R.layout.splash);
            Thread timer = new Thread(){
                @Override
                public void run() {
                    try {
                        Thread.sleep(1500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }finally {
                        startActivity(new Intent(Splash.this, MainActivity.class));
                        finish();
                    }
                }
            };
            timer.start();
        }
    }

    @Override
    public void onBackPressed() {
        if(!firstLogin || currentFragment == 0) {
            super.onBackPressed();
            finish();
        }
        else {
            fragmentManager.beginTransaction().detach(fragmentsHashMap.get(currentFragment)).commit();
            currentFragment--;
            btNext.setEnabled(true);
            fragmentManager.beginTransaction().attach(fragmentsHashMap.get(currentFragment)).commit();
        }
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btNext:
                fragmentManager.beginTransaction().detach(fragmentsHashMap.get(currentFragment)).commit();
                currentFragment++;
                fragmentManager.beginTransaction().attach(fragmentsHashMap.get(currentFragment)).commit();
                break;
        }
    }

    @Override
    public void skipRestore() {
        fragmentsHashMap.put(1, fragmentSetPasskey);
        fragmentManager.beginTransaction().detach(fragmentRestore).commit();
        fragmentManager.beginTransaction().attach(fragmentSetPasskey).commit();
    }

    @Override
    public void skipPasskey() {
        fragmentManager.beginTransaction().detach(fragmentsHashMap.get(currentFragment)).commit();
        currentFragment++;
        fragmentManager.beginTransaction().attach(fragmentsHashMap.get(currentFragment)).commit();
    }
}