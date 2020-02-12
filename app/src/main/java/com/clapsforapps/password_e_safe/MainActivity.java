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

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioGroup;

import static com.clapsforapps.password_e_safe.SharedPrefs.isInApp;

/**
 * Created by User on 29/08/2015.
 */
public class MainActivity extends AppCompatActivity implements FragmentsListener, /*View.OnClickListener,*/ /*RecyclerViewAdapter.RecyclerViewOnItemClickListener,*/
        NavigationView.OnNavigationItemSelectedListener{

    //public static final int ACTIVITY_INSERT_OR_VIEW_INFO = 1000;
    public static final int ACTIVITY_PASSKEY = 1001;
    public static final int ACTIVITY_ABOUT = 1002;
    public static final int ACTIVITY_SETTINGS = 1003;
    public static final int ACTIVITY_PASS_GENERATOR = 1004;
    public static final int DB_UPDATED = 1100;
    public static final int CLOSE_ALL = 1101;
    public static final int UNLOCKED = 1102;

    private FragmentManager fragmentManager;
    private FragmentViewPassword fragmentViewPassword = new FragmentViewPassword();
    private FragmentMain fragmentMain = new FragmentMain();
    private FragmentInsert fragmentInsert = new FragmentInsert();

    private SharedPreferences settings;
    private DrawerLayout drawerLayout;
    private NavigationView nvMainActivity;
    private RadioGroup rgSort;
    private Database db = new Database(this);
    private int sortBy;
    private boolean arePasswordsLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        //Log.wtf("onCreate", "onCreate");

        //isSaved = false;
        //isCreated = true;

        settings = getApplicationContext().getSharedPreferences(SharedPrefs.PREFS_NAME, MODE_PRIVATE);
        sortBy = settings.getInt(SharedPrefs.PREFS_SORT_BY, SharedPrefs.SORT_NEWEST);

        drawerLayout = (DrawerLayout)findViewById(R.id.drawerLayout);
        nvMainActivity = (NavigationView)findViewById(R.id.nvMainActivity);
        nvMainActivity.setNavigationItemSelectedListener(this);

        fragmentManager = getSupportFragmentManager();

        if(savedInstanceState == null){
            fragmentViewPassword = new FragmentViewPassword();
            fragmentInsert = new FragmentInsert();
            fragmentMain = new FragmentMain();

            fragmentManager.beginTransaction().add(R.id.mainActivityContainer, fragmentInsert, "Insert").commit();
            fragmentManager.beginTransaction().detach(fragmentInsert).commit();
            fragmentManager.beginTransaction().add(R.id.mainActivityContainer, fragmentViewPassword, "ViewPassword").commit();
            fragmentManager.beginTransaction().detach(fragmentViewPassword).commit();
            fragmentManager.beginTransaction().add(R.id.mainActivityContainer, fragmentMain, "Main").commit();
        }
        else {
            fragmentViewPassword = (FragmentViewPassword)fragmentManager.findFragmentByTag("ViewPassword");
            fragmentInsert = (FragmentInsert) fragmentManager.findFragmentByTag("Insert");
            fragmentMain = (FragmentMain) fragmentManager.findFragmentByTag("Main");
            fragmentManager.beginTransaction().detach(fragmentInsert).commit();
            fragmentManager.beginTransaction().detach(fragmentViewPassword).commit();
            fragmentManager.beginTransaction().attach(fragmentMain).commit();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Log.wtf("onResume", "onResume");
        //if(settings.getBoolean(SharedPrefs.PREFS_IS_LOCKED, true) && settings.getBoolean(SharedPrefs.PREFS_KEY_BOOL, false))
        /*if(!(isSaved && isCreated)) {
            startActivityForResult(new Intent(MainActivity.this, PasskeyActivity.class), ACTIVITY_PASSKEY);
            return;
        }*/
        if(!isInApp && settings.getBoolean(SharedPrefs.PREFS_KEY_BOOL, false)) {
            startActivityForResult(new Intent(MainActivity.this, PasskeyActivity.class), ACTIVITY_PASSKEY);
            return;
        }
        if(!arePasswordsLoaded) {
            fragmentMain.getPasswords(sortBy);
            arePasswordsLoaded = true;
        }
        //isCreated = false;
        //isSaved = false;
        isInApp = false;
    }

    @Override
    public void onPasswordSelected(Password password) {
        fragmentManager.beginTransaction().detach(fragmentMain).commit();
        fragmentViewPassword.setPassword(password);
        fragmentManager.beginTransaction().attach(fragmentViewPassword).commit();
    }

    @Override
    public void onViewPasswordEnded(boolean isDataSetChanged, String snackBarMsg, boolean requiresRefresh) {
        fragmentManager.beginTransaction().detach(fragmentViewPassword).commit();
        fragmentManager.beginTransaction().attach(fragmentMain).commit();
        if(snackBarMsg != null)
            Snackbar.make(findViewById(R.id.mainActivityContainer), snackBarMsg, Snackbar.LENGTH_LONG).show();
        if(requiresRefresh)
            fragmentMain.getPasswords(sortBy);
        else if(isDataSetChanged)
            fragmentMain.notifyDataSetChanged();
    }

    @Override
    public void onPasswordAdded(String snackBarMsg) {
        fragmentInsert.clearTexts();
        fragmentManager.beginTransaction().detach(fragmentInsert).commit();
        fragmentManager.beginTransaction().attach(fragmentMain).commit();
        fragmentMain.getPasswords(sortBy);
        Snackbar.make(findViewById(R.id.mainActivityContainer), snackBarMsg, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onInsertEnded() {
        fragmentInsert.clearTexts();
        fragmentManager.beginTransaction().detach(fragmentInsert).commit();
        fragmentManager.beginTransaction().attach(fragmentMain).commit();
    }

    @Override
    public void onInsertRequested(int type) {
        fragmentManager.beginTransaction().detach(fragmentMain).commit();
        fragmentInsert.setType(type);
        fragmentManager.beginTransaction().attach(fragmentInsert).commit();
    }

    /*@Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isSaved", true);
        outState.putBoolean("arePasswordsLoaded", arePasswordsLoaded);
        //SharedPrefs.isInApp = true;
        //Log.wtf("OnCronSaveInstanceStateeate", "Test");
    }*/

    /*@Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        isSaved = savedInstanceState.getBoolean("isSaved");
        arePasswordsLoaded = savedInstanceState.getBoolean("arePasswordsLoaded");
        //Log.wtf("onRestoreInstanceState", "Test");
    }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.menu_fragment_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                break;
            case R.id.action_search:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_sort:
                drawerLayout.closeDrawer(GravityCompat.START);
                sort();
                break;
            case R.id.action_settings:
                drawerLayout.closeDrawer(GravityCompat.START);
                isInApp = true;
                startActivityForResult(new Intent(this, Settings.class), ACTIVITY_SETTINGS);
                break;
            case R.id.action_about:
                drawerLayout.closeDrawer(GravityCompat.START);
                isInApp = true;
                startActivityForResult(new Intent(this, About.class), ACTIVITY_ABOUT);
                break;
            case R.id.action_pass_generator:
                drawerLayout.closeDrawer(GravityCompat.START);
                isInApp = true;
                startActivityForResult(new Intent(this, PassGenActivity.class), ACTIVITY_PASS_GENERATOR);
                break;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START);
        else if(fragmentViewPassword.isVisible())
            fragmentViewPassword.onBackPressed();
        else if(fragmentInsert.isVisible())
            onInsertEnded();
        else
            super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == ACTIVITY_SETTINGS && resultCode == DB_UPDATED) {
            fragmentManager.beginTransaction().detach(fragmentViewPassword).commit();
            fragmentManager.beginTransaction().attach(fragmentMain).commit();
            fragmentMain.getPasswords(sortBy);
        }
        /*else if(requestCode == ACTIVITY_PASSKEY && resultCode == UNLOCKED) {
            isCreated = true;
            isSaved = true;
            Log.wtf("onActivityResult", "onActivityResult");
        }*/
        else if(resultCode == CLOSE_ALL)
            finish();
    }

    public Database getDb(){
        return db;
    }

    private void sort(){
        final AlertDialog.Builder sortBuilder = new AlertDialog.Builder(this);
        View sortView = getLayoutInflater().inflate(R.layout.sort_layout, null);

        rgSort = (RadioGroup) sortView.findViewById(R.id.rgSort);
        switch (sortBy){
            case SharedPrefs.SORT_NEWEST:
                rgSort.check(R.id.rbSortNewest);
                break;
            case SharedPrefs.SORT_OLDEST:
                rgSort.check(R.id.rbSortOldest);
                break;
            case SharedPrefs.SORT_TYPE:
                rgSort.check(R.id.rbSortType);
                break;
            case SharedPrefs.SORT_TITLE_AZ:
                rgSort.check(R.id.rbSortTitleAZ);
                break;
            case SharedPrefs.SORT_TITLE_ZA:
                rgSort.check(R.id.rbSortTitleZA);
                break;
        }
        sortBuilder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (rgSort.getCheckedRadioButtonId()){
                    case R.id.rbSortNewest:
                        sortBy = SharedPrefs.SORT_NEWEST;
                        break;
                    case R.id.rbSortOldest:
                        sortBy = SharedPrefs.SORT_OLDEST;
                        break;
                    case R.id.rbSortType:
                        sortBy = SharedPrefs.SORT_TYPE;
                        break;
                    case R.id.rbSortTitleAZ:
                        sortBy = SharedPrefs.SORT_TITLE_AZ;
                        break;
                    case R.id.rbSortTitleZA:
                        sortBy = SharedPrefs.SORT_TITLE_ZA;
                        break;
                }
                fragmentMain.getPasswords(sortBy);
                settings.edit().putInt(SharedPrefs.PREFS_SORT_BY, sortBy).commit();
                drawerLayout.closeDrawer(GravityCompat.START);
            }
        });

        sortBuilder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        sortBuilder.setTitle(R.string.sort_by);
        sortBuilder.setView(sortView);
        sortBuilder.show();
    }

}
