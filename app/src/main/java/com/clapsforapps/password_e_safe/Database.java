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
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by ALI on 11/16/2016.
 */
public class Database extends SQLiteOpenHelper {

    private static final String TABLE_PASSWORD = "password";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TAG = "tag";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_PASSWORD = "password";
    private static final String COLUMN_NOTES = "notes";
    private static final String COLUMN_TYPE = "type";
    private static final int CURRENT_DATABASE_VERSION = 2;
    private static final int INITIAL_DATABASE_VERSION = 1;

    public static final String PASSKEY = "passkey";
    public static final String DATABASE_NAME = "passwords.db";

    public static final int TYPE_GOOGLE = 1;
    public static final int TYPE_FACEBOOK = 2;
    public static final int TYPE_TWITTER = 3;
    public static final int TYPE_MICROSOFT = 4;
    public static final int TYPE_INSTAGRAM = 5;
    public static final int TYPE_SNAPCHAT = 6;
    public static final int TYPE_LINKEDIN = 7;
    public static final int TYPE_CUSTOM = 100;

    private Context context;
    private SharedPreferences settings;

    public Database(Context context){
        super(context, DATABASE_NAME, null, CURRENT_DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        db.execSQL("CREATE TABLE " + TABLE_PASSWORD + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COLUMN_TAG + " TEXT NOT NULL," +
                COLUMN_USERNAME + " TEXT, " + COLUMN_EMAIL + " TEXT NOT NULL, " + COLUMN_PASSWORD + " TEXT NOT NULL, "
                + COLUMN_NOTES + " TEXT, " + COLUMN_TYPE + " INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE " + PASSKEY + " (" + PASSKEY + " TEXT PRIMARY KEY)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        switch (oldVersion){
            case INITIAL_DATABASE_VERSION:
                try {
                    upgradeFromInitialVersion(db);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    public void deletePassword(int id) {
        getWritableDatabase().delete(TABLE_PASSWORD, COLUMN_ID + "=" + id, null);
    }

    public long addPassword(Password password) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_TAG, password.getTag());
        cv.put(COLUMN_EMAIL, password.getEmail());
        cv.put(COLUMN_PASSWORD, password.getPassword());
        cv.put(COLUMN_USERNAME, password.getUsername());
        cv.put(COLUMN_NOTES, password.getNotes());
        cv.put(COLUMN_TYPE, password.getType());
        return getWritableDatabase().insert(TABLE_PASSWORD, null, cv);
    }

    public void updatePassword(Password password) {
        // TODO Auto-generated method stub
        ContentValues cvUpdate = new ContentValues();
        cvUpdate.put(COLUMN_TAG, password.getTag());
        cvUpdate.put(COLUMN_USERNAME, password.getUsername());
        cvUpdate.put(COLUMN_EMAIL, password.getEmail());
        cvUpdate.put(COLUMN_PASSWORD, password.getPassword());
        cvUpdate.put(COLUMN_NOTES, password.getNotes());
        cvUpdate.put(COLUMN_TYPE, password.getType());
        getWritableDatabase().update(TABLE_PASSWORD, cvUpdate, COLUMN_ID + "=" + password.getId(), null);
    }

    public Password getPassword(int id) {
        // TODO Auto-generated method stub
        String[] columns = new String[]{COLUMN_ID, COLUMN_TAG, COLUMN_USERNAME, COLUMN_EMAIL, COLUMN_PASSWORD, COLUMN_NOTES, COLUMN_TYPE};
        Cursor cursor = getWritableDatabase().query(TABLE_PASSWORD, columns, COLUMN_ID + "=" + id, null, null, null, null);
        if(cursor != null){
            cursor.moveToFirst();
            Password password = new Password(id, cursor.getString(1), cursor.getString(3), cursor.getString(4),
                    cursor.getString(2), cursor.getString(5), cursor.getInt(6));
            cursor.close();
            return password;
        }
        return null;
    }

    public Cursor getAllPasswords(int sortBy){
        String[] columns = new String[]{COLUMN_ID, COLUMN_TAG, COLUMN_EMAIL, COLUMN_PASSWORD, COLUMN_USERNAME, COLUMN_NOTES, COLUMN_TYPE};
        switch (sortBy){
            case SharedPrefs.SORT_OLDEST:
                return getWritableDatabase().query(TABLE_PASSWORD, columns, null, null, null, null, COLUMN_ID + " ASC");
                //return getWritableDatabase().rawQuery("SELECT " + COLUMN_ID + ", " + COLUMN_TAG + ", "  + COLUMN_TYPE + " FROM " + TABLE_PASSWORD
                        //+ " ORDER BY " + COLUMN_ID + " ASC", null);
            case SharedPrefs.SORT_TYPE:
                return getWritableDatabase().query(TABLE_PASSWORD, columns, null, null, null, null, COLUMN_TYPE + " ASC");
                //return getWritableDatabase().rawQuery("SELECT " + COLUMN_ID + ", " + COLUMN_TAG + ", "  + COLUMN_TYPE + " FROM " + TABLE_PASSWORD
                        //+ " ORDER BY " + COLUMN_TYPE + " ASC", null);
            case SharedPrefs.SORT_TITLE_AZ:
                return getWritableDatabase().query(TABLE_PASSWORD, columns, null, null, null, null, COLUMN_TAG + " COLLATE NOCASE ASC");
                //return getWritableDatabase().rawQuery("SELECT " + COLUMN_ID + ", " + COLUMN_TAG + ", "  + COLUMN_TYPE + " FROM " + TABLE_PASSWORD
                        //+ " ORDER BY " + COLUMN_TAG + " COLLATE NOCASE ASC", null);
            case SharedPrefs.SORT_TITLE_ZA:
                return getWritableDatabase().query(TABLE_PASSWORD, columns, null, null, null, null, COLUMN_TAG + " COLLATE NOCASE DESC");
                //return getWritableDatabase().rawQuery("SELECT " + COLUMN_ID + ", " + COLUMN_TAG + ", "  + COLUMN_TYPE + " FROM " + TABLE_PASSWORD
                        //+ " ORDER BY " + COLUMN_TAG + " COLLATE NOCASE DESC", null);
            default:
                return getWritableDatabase().query(TABLE_PASSWORD, columns, null, null, null, null, COLUMN_ID + " DESC");
                //return getWritableDatabase().rawQuery("SELECT " + COLUMN_ID + ", " + COLUMN_TAG + ", "  + COLUMN_TYPE + " FROM " + TABLE_PASSWORD
                        //+ " ORDER BY " + COLUMN_ID + " DESC", null);
        }
    }

    public String getPasskey(){
        Cursor cursor = getWritableDatabase().rawQuery("SELECT " + PASSKEY + " FROM " + PASSKEY, null);
        cursor.moveToFirst();
        String passkey = cursor.getString(0);
        cursor.close();
        return passkey;
    }

    public boolean updatePasskey(String passkey) {
        ContentValues cv = new ContentValues();
        SQLiteDatabase db = getWritableDatabase();
        cv.put(PASSKEY, passkey);
        long success = db.update(PASSKEY, cv, null, null);
        if(success == 0)
            success = db.insert(PASSKEY, null, cv);
        return success > 0;
    }

    public boolean deletePasskey() {
        return getWritableDatabase().delete(PASSKEY, null, null) > 0;
    }

    public void reEncryptPasswords(SQLiteDatabase db, SecretKey oldKey, SecretKey newKey) throws Exception {
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_ID + ", " + COLUMN_PASSWORD + " FROM " + TABLE_PASSWORD, null);
        while (!cursor.isLast()){
            cursor.moveToNext();
            db.execSQL("UPDATE " + TABLE_PASSWORD + " SET " + COLUMN_PASSWORD + " = '"
                    + Encryption.encryptAES(Encryption.decryptAES(cursor.getString(1), oldKey), newKey) + "' WHERE " + COLUMN_ID + " = " + cursor.getInt(0));
        }
        cursor.close();
    }

    private void upgradeFromInitialVersion(SQLiteDatabase db) throws Exception {
        db.execSQL("UPDATE passwords SET type = '" + TYPE_GOOGLE + "' WHERE type = 'gm'");
        db.execSQL("UPDATE passwords SET type = '" + TYPE_FACEBOOK + "' WHERE type = 'fb'");
        db.execSQL("UPDATE passwords SET type = '" + TYPE_TWITTER + "' WHERE type = 'tw'");
        db.execSQL("UPDATE passwords SET type = '" + TYPE_MICROSOFT + "' WHERE type = 'ol'");
        db.execSQL("UPDATE passwords SET type = '" + TYPE_INSTAGRAM + "' WHERE type = 'ig'");
        db.execSQL("UPDATE passwords SET type = '" + TYPE_SNAPCHAT + "' WHERE type = 'sc'");
        db.execSQL("UPDATE passwords SET type = '" + TYPE_CUSTOM + "' WHERE type = 'cm'");

        db.execSQL("CREATE TABLE " + TABLE_PASSWORD + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COLUMN_TAG + " TEXT NOT NULL," +
                COLUMN_USERNAME + " TEXT, " + COLUMN_EMAIL + " TEXT NOT NULL, " + COLUMN_PASSWORD + " TEXT NOT NULL, "
                + COLUMN_NOTES + " TEXT, " + COLUMN_TYPE + " INTEGER NOT NULL)");

        db.execSQL("INSERT INTO " + TABLE_PASSWORD + " (" + COLUMN_ID + ", " + COLUMN_TAG + ", " + COLUMN_USERNAME + ", "
                + COLUMN_EMAIL + ", " + COLUMN_PASSWORD + ", " + COLUMN_NOTES + ", " + COLUMN_TYPE  +  ") SELECT * FROM passwords");

        db.execSQL("DROP table passwords");

        db.execSQL("CREATE TABLE " + PASSKEY + " (" + PASSKEY + " TEXT PRIMARY KEY)");
        settings = context.getSharedPreferences(SharedPrefs.PREFS_NAME, Activity.MODE_PRIVATE);
        SharedPrefs.passkey = settings.getString(SharedPrefs.PREFS_KEY, null);
        if(settings.getBoolean(SharedPrefs.PREFS_KEY_BOOL, false) && SharedPrefs.passkey != null){
            db.execSQL("INSERT INTO " + PASSKEY + " (" + PASSKEY + ") " + "VALUES ('"
                    + Encryption.encryptMD5(SharedPrefs.passkey) + "')");
            settings.edit().putString(SharedPrefs.PREFS_KEY, null).commit();
            reEncryptPasswords(db, new SecretKeySpec(Encryption.DEFAULT_KEY_VALUE, Encryption.AES_ALGORITHM),
                    Encryption.encryptSHA1(SharedPrefs.passkey));
            /*File oldBackupDbFile = new File(Environment.getExternalStorageDirectory() + "/Android/data/PasswordBackup", "copy_passwords.db");
            if(oldBackupDbFile.exists())
                oldBackupDbFile.delete();
            if(settings.getBoolean(SharedPrefs.PREFS_AUTO_BACKUP, false))
                SharedPrefs.exportDB(context);*/
        }
    }
}
