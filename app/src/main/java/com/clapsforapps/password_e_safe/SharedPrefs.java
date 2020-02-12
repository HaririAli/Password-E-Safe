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

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.EditText;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.security.NoSuchAlgorithmException;

public class SharedPrefs {

    public static final String PREFS_NAME = "PASSKEEPER_PREFS";
    public static final String PREFS_KEY = "PASS_PREFS_STRING";
    public static final String PREFS_KEY_BOOL = "PASS";
    public static final String PREFS_FIRST_BOOL = "SPLASH";
    //public static final String PREFS_RESTORABLE_BOOL = "RESTORE";
    public static final String PREFS_AUTO_BACKUP = "AUTO_BACKUP";
    //public static final String PREFS_IS_LOCKED = "IS_LOCKED";
    public static final String PREFS_SORT_BY = "SORT_BYE";

    public static final int REQUEST_READ_EXTERNAL_STORAGE = 200;
    public static final int REQUEST_WRTIE_EXTERNAL_STORAGE = 201;

    public static final int SORT_NEWEST = 101;
    public static final int SORT_OLDEST = 102;
    public static final int SORT_TYPE = 103;
    public static final int SORT_TITLE_AZ = 104;
    public static final int SORT_TITLE_ZA = 105;

    public static String passkey = null;
    public static boolean isInApp = false;


    public static boolean exportDB(Context con) throws IOException {
        File backupFolder = new File(Environment.getExternalStorageDirectory() + "/Android/data/PasswordBackup");
        if(!backupFolder.exists())
            backupFolder.mkdir();
        File externalStorageDir = Environment.getExternalStorageDirectory();
        File dbFile =  con.getDatabasePath(Database.DATABASE_NAME);
        File backupDbFile = new File(Environment.getExternalStorageDirectory() + "/Android/data/PasswordBackup", Database.DATABASE_NAME);

        if (!backupDbFile.exists())
            backupDbFile.createNewFile();

        backupDbFile.setReadable(true);

        if (externalStorageDir.canWrite()) {
            FileChannel src = new FileInputStream(dbFile).getChannel();
            FileChannel dst = new FileOutputStream(backupDbFile).getChannel();
            dst.transferFrom(src, 0, src.size());
            src.close();
            dst.close();
            return true;
        }
        return false;
    }

    public static void importDB(Activity activity, ImportDbListener importDbListener){
        File dbFile = activity.getApplicationContext().getApplicationContext().getDatabasePath(Database.DATABASE_NAME);
        File oldBackupDbFile = new File(Environment.getExternalStorageDirectory() + "/Android/data/PasswordBackup", "copy_passwords.db");
        File backupDbFile = new File(Environment.getExternalStorageDirectory() + "/Android/data/PasswordBackup", Database.DATABASE_NAME);
        String passkeyHash = null;

        if (oldBackupDbFile.exists()) {
            oldBackupDbFile.renameTo(backupDbFile);
        }

        if (backupDbFile.exists()) {
            SQLiteDatabase db = SQLiteDatabase.openDatabase(backupDbFile.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY);
            if (db.getVersion() > 1) {
                Cursor cursor = db.rawQuery("SELECT " + Database.PASSKEY + " FROM " + Database.PASSKEY, null);
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    passkeyHash = cursor.getString(0);
                    cursor.close();
                }
            }
            db.close();

            if (passkeyHash == null) {
                try {
                    copyDB(backupDbFile, dbFile);
                    passkey = null;
                    importDbListener.onImport(true);
                } catch (IOException e) {
                    e.printStackTrace();
                    importDbListener.onImport(false);
                }
            }
            else{
                AlertDialog.Builder confirmPasskey = new AlertDialog.Builder(activity);
                View confirmPasskeyView = activity.getLayoutInflater().inflate(R.layout.confirm_passkey, null);
                EditText etConfirmPasskey = (EditText) confirmPasskeyView.findViewById(R.id.etConfirmPasskey);
                confirmPasskey.setPositiveButton(android.R.string.ok,
                        new DialogClickListener(backupDbFile, dbFile, importDbListener, etConfirmPasskey, passkeyHash));
                confirmPasskey.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                confirmPasskey.setTitle(R.string.restore_backup);
                confirmPasskey.setView(confirmPasskeyView);
                confirmPasskey.show();
            }
        }
    }

    private static void copyDB(File backupDbFile, File dbFile) throws IOException {
        FileChannel src = new FileInputStream(backupDbFile).getChannel();
        FileChannel dst = new FileOutputStream(dbFile).getChannel();
        dst.transferFrom(src, 0, src.size());
        src.close();
        dst.close();
    }

    private static class DialogClickListener implements DialogInterface.OnClickListener {

        private File backupDbFile, dbFile;
        private ImportDbListener importDbListener;
        private String passkeyHash;
        private EditText etConfirmPasskey;

        DialogClickListener(File backupDbFile, File dbFile, ImportDbListener importDbListener, EditText etConfirmPasskey, String passkeyHash){
            this.backupDbFile =backupDbFile;
            this.dbFile = dbFile;
            this.importDbListener = importDbListener;
            this.etConfirmPasskey = etConfirmPasskey;
            this.passkeyHash = passkeyHash;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            try {
                String enteredKey = etConfirmPasskey.getText().toString();
                if(Encryption.encryptMD5(enteredKey).equals(passkeyHash)) {
                    copyDB(backupDbFile, dbFile);
                    passkey = enteredKey;
                    importDbListener.onImport(true);
                }
                else {
                    importDbListener.onImport(false);
                }
            } catch (IOException | NoSuchAlgorithmException e) {
                e.printStackTrace();
                importDbListener.onImport(false);
            }
            finally {
                dialog.dismiss();
            }
        }
    }
}
