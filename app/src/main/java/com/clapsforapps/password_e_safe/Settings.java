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

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import javax.crypto.spec.SecretKeySpec;

import static com.clapsforapps.password_e_safe.SharedPrefs.isInApp;

public class Settings extends AppCompatActivity implements View.OnClickListener, ImportDbListener {

    private SwitchCompat swDailyBackup;
    private Toolbar settingsToolbar;
    private EditText etOldPasskey, etNewPasskey, etConfirm;
    private TextView tvOldPass, tvMatch, tvRestore,tvBackup,tvChangePasskey,tvRemovePasskey;
    private SharedPreferences settings;
    private Database db = new Database(this);
    private AlertDialog.Builder dialogBuilder;
    private boolean match = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        settingsToolbar = (Toolbar)findViewById(R.id.settingsToolbar);
        setSupportActionBar(settingsToolbar);

        settings = this.getSharedPreferences(SharedPrefs.PREFS_NAME,MODE_PRIVATE);

        tvRestore = (TextView) findViewById(R.id.tvRestore);
        tvBackup = (TextView) findViewById(R.id.tvBackup);
        tvChangePasskey = (TextView) findViewById(R.id.tvChangePasskey);
        tvRemovePasskey = (TextView) findViewById(R.id.tvRemovePasskey);
        swDailyBackup = (SwitchCompat) findViewById(R.id.swDailyBackup);

        settings = this.getSharedPreferences(SharedPrefs.PREFS_NAME, MODE_PRIVATE);
        swDailyBackup.setChecked(settings.getBoolean(SharedPrefs.PREFS_AUTO_BACKUP, false));

        tvRestore.setOnClickListener(this);
        tvBackup.setOnClickListener(this);
        tvChangePasskey.setOnClickListener(this);
        tvRemovePasskey.setOnClickListener(this);
        swDailyBackup.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.equals(swDailyBackup)) {
                    if (isChecked) {
                        try {
                            backup();
                            settings.edit().putBoolean(SharedPrefs.PREFS_AUTO_BACKUP, true).commit();
                            Snackbar.make(findViewById(R.id.clSettings), R.string.auto_backup_activated, Snackbar.LENGTH_SHORT).show();
                        } catch (IOException e) {
                            e.printStackTrace();
                            settings.edit().putBoolean(SharedPrefs.PREFS_AUTO_BACKUP, false).commit();
                            Snackbar.make(findViewById(R.id.clSettings), R.string.backup_fail, Snackbar.LENGTH_LONG).show();
                        }
                    } else {
                        settings.edit().putBoolean(SharedPrefs.PREFS_AUTO_BACKUP, false).commit();
                        Snackbar.make(findViewById(R.id.clSettings), R.string.auto_backup_deactivated, Snackbar.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tvBackup:
                try {
                    backup();
                }
                catch (IOException ex){
                    Snackbar.make(findViewById(R.id.clSettings), R.string.backup_fail, Snackbar.LENGTH_LONG).show();
                }
                break;
            case R.id.tvRestore:
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                    SharedPrefs.importDB(this, this);
                else
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, SharedPrefs.REQUEST_READ_EXTERNAL_STORAGE);
                break;
            case R.id.tvChangePasskey:
                updateOrAddPasskey();
                break;
            case R.id.tvRemovePasskey:
                removePasskey();
                break;
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                isInApp = true;
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        isInApp = true;
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //if(settings.getBoolean(SharedPrefs.PREFS_IS_LOCKED, true) && settings.getBoolean(SharedPrefs.PREFS_KEY_BOOL, false))
        if(!isInApp && settings.getBoolean(SharedPrefs.PREFS_KEY_BOOL, false))
            startActivityForResult(new Intent(Settings.this, PasskeyActivity.class), MainActivity.ACTIVITY_PASSKEY);
        isInApp = false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == MainActivity.ACTIVITY_PASSKEY && resultCode == MainActivity.CLOSE_ALL) {
            setResult(MainActivity.CLOSE_ALL);
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case SharedPrefs.REQUEST_READ_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    SharedPrefs.importDB(this, this);
                } else
                    Snackbar.make(findViewById(R.id.clSettings), R.string.cant_restore, Snackbar.LENGTH_LONG).show();
                return;
            }
            case SharedPrefs.REQUEST_WRTIE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    try {
                        backup();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(this, getResources().getString(R.string.backup_fail), Toast.LENGTH_LONG).show();
                    }
                } else
                    Toast.makeText(this, getResources().getString(R.string.backup_fail), Toast.LENGTH_LONG).show();
                return;
            }
        }
    }

    private void backup() throws IOException {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            SharedPrefs.exportDB(this);
            Snackbar.make(findViewById(R.id.clSettings), R.string.backup_successful, Snackbar.LENGTH_LONG).show();
        }
        else
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, SharedPrefs.REQUEST_WRTIE_EXTERNAL_STORAGE);
    }

    private void updateOrAddPasskey(){
        final boolean isNew;
        dialogBuilder = new AlertDialog.Builder(Settings.this);
        View changePasskeyView = LayoutInflater.from(this).inflate(R.layout.change_passkey, null);
        etOldPasskey = (EditText) changePasskeyView.findViewById(R.id.etOldPasskey);
        etNewPasskey = (EditText) changePasskeyView.findViewById(R.id.etnewPasskey);
        etConfirm = (EditText) changePasskeyView.findViewById(R.id.etConfirm);
        tvOldPass = (TextView) changePasskeyView.findViewById(R.id.tvOldPass);
        tvMatch = (TextView) changePasskeyView.findViewById(R.id.tvMatch);

        isNew = !settings.getBoolean(SharedPrefs.PREFS_KEY_BOOL, false);
        if(isNew){
            etOldPasskey.setVisibility(View.GONE);
            tvOldPass.setVisibility(View.GONE);
        }
        dialogBuilder.setTitle(getResources().getString(R.string.change_passkey_title));
        dialogBuilder.setView(changePasskeyView);

        etConfirm.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (etConfirm.getText().toString().equals(etNewPasskey.getText().toString())) {
                    tvMatch.setTextColor(Color.GREEN);
                    tvMatch.setText(getResources().getString(R.string.match));
                    match = true;
                } else {
                    tvMatch.setTextColor(Color.RED);
                    tvMatch.setText(getResources().getString(R.string.mismatch));
                }
            }
        });

        dialogBuilder.setPositiveButton(getResources().getString(R.string.change_passkey_title), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int sumthin) {
                if (match) {
                    //SQLiteDatabase sQLite = db.open();
                    String newPasskey = etNewPasskey.getText().toString();
                    if (etOldPasskey.getText().toString().equals(SharedPrefs.passkey) || isNew) {
                        try {
                            db.reEncryptPasswords(db.getWritableDatabase(),
                                    isNew? new SecretKeySpec(Encryption.DEFAULT_KEY_VALUE, Encryption.AES_ALGORITHM) :
                                            Encryption.encryptSHA1(SharedPrefs.passkey),
                                    Encryption.encryptSHA1(newPasskey));
                            db.updatePasskey(Encryption.encryptMD5(newPasskey));
                            SharedPrefs.passkey = newPasskey;
                            settings.edit().putBoolean(SharedPrefs.PREFS_KEY_BOOL, true).commit();
                            setResult(MainActivity.DB_UPDATED);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        Snackbar.make(findViewById(R.id.clSettings), R.string.wrong_passkey, Snackbar.LENGTH_LONG).show();
                    }
                    db.close();
                }
                else {
                    Snackbar.make(findViewById(R.id.clSettings), R.string.paskey_confirm_error, Snackbar.LENGTH_LONG).show();
                }
                dialog.dismiss();
            }
        });

        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int sumthin) {
                dialog.dismiss();
            }
        });
        dialogBuilder.show();
    }

    private void removePasskey(){
        if(SharedPrefs.passkey == null)
            Snackbar.make(findViewById(R.id.clSettings), R.string.passkey_already_removed, Snackbar.LENGTH_SHORT).show();
        else {
            dialogBuilder = new AlertDialog.Builder(getSupportActionBar().getThemedContext(), R.style.MyAlertDialogStyle);
            dialogBuilder.setTitle(getResources().getString(R.string.remove_passkey));
            dialogBuilder.setMessage(getResources().getString(R.string.remove_passkey_confirm));
            dialogBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    try {
                        settings.edit().putBoolean(SharedPrefs.PREFS_KEY_BOOL, false).commit();
                        db.reEncryptPasswords(db.getWritableDatabase(), Encryption.encryptSHA1(SharedPrefs.passkey),
                                new SecretKeySpec(Encryption.DEFAULT_KEY_VALUE, Encryption.AES_ALGORITHM));
                        db.deletePasskey();
                        SharedPrefs.passkey = null;
                        setResult(MainActivity.DB_UPDATED);
                        Snackbar.make(findViewById(R.id.clSettings), R.string.remove_passkey_success, Snackbar.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    dialog.dismiss();
                    db.close();
                }
            });
            dialogBuilder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });
            dialogBuilder.show();
        }
    }

    @Override
    public void onImport(boolean isDbImported) {
        if(isDbImported) {
            if(SharedPrefs.passkey != null)
                settings.edit().putBoolean(SharedPrefs.PREFS_KEY_BOOL, true).commit();
            else
                settings.edit().putBoolean(SharedPrefs.PREFS_KEY_BOOL, false).commit();
            Snackbar.make(findViewById(R.id.clSettings), R.string.backup_restored, Snackbar.LENGTH_LONG).show();
            setResult(MainActivity.DB_UPDATED);
        }
        else
            Snackbar.make(findViewById(R.id.clSettings), R.string.cant_restore, Snackbar.LENGTH_LONG).show();
    }
}
