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
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import javax.crypto.spec.SecretKeySpec;

import static com.clapsforapps.password_e_safe.SharedPrefs.REQUEST_READ_EXTERNAL_STORAGE;

/**
 * Created by ALI on 1/5/2017.
 */

public class FragmentInsert extends Fragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private EditText etTitle, etUsername, etEmail, etPassword, etNotes;
    private TextView tvTitle, tvUsername, tvEmail, tvPassword, tvNotes;
    private SwitchCompat swPass;
    private FloatingActionButton fabSave;
    private Toolbar tbFragmentPassword;
    private ImageView ivPasswordHeader;
    private CollapsingToolbarLayout ctlFragmentPassword;
    private SharedPreferences settings;
    private AlertDialog.Builder builder;
    private Database db;
    private FragmentsListener fragmentsListener;

    private int type = Database.TYPE_CUSTOM;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_password, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        fragmentsListener = (FragmentsListener)getActivity();

        settings = getActivity().getSharedPreferences(SharedPrefs.PREFS_NAME, Context.MODE_PRIVATE);

        etTitle = (EditText)getView().findViewById(R.id.etTitle);
        etUsername = (EditText)getView().findViewById(R.id.etUsername);
        etEmail = (EditText)getView().findViewById(R.id.etEmail);
        etPassword = (EditText)getView().findViewById(R.id.etPassword);
        etNotes = (EditText)getView().findViewById(R.id.etNotes);

        tvTitle = (TextView)getView().findViewById(R.id.tvTitle);
        tvUsername = (TextView)getView().findViewById(R.id.tvUsername);
        tvEmail = (TextView)getView().findViewById(R.id.tvEmail);
        tvPassword = (TextView)getView().findViewById(R.id.tvPassword);
        tvNotes = (TextView)getView().findViewById(R.id.tvNotes);

        swPass = (SwitchCompat)getView().findViewById(R.id.swPass);
        fabSave = (FloatingActionButton) getView().findViewById(R.id.fabSave);
        tbFragmentPassword = (Toolbar)getView().findViewById(R.id.tbFragmentPassword);
        ivPasswordHeader = (ImageView)getView().findViewById(R.id.ivPasswordHeader);
        ctlFragmentPassword = (CollapsingToolbarLayout)getView().findViewById(R.id.ctlFragmentPassword);

        fabSave.setOnClickListener(this);
        fabSave.setVisibility(View.VISIBLE);

        tbFragmentPassword.setNavigationOnClickListener(this);
        tbFragmentPassword.setTitle(R.string.new_pass);

        swPass.setOnCheckedChangeListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        if(db == null)
            db = ((MainActivity)getActivity()).getDb();
        setLayout();
        swPass.setChecked(false);
    }

    @Override
    public void onClick(View v) {
        if(((View)v.getParent()).getId() == R.id.tbFragmentPassword){
            fragmentsListener.onInsertEnded();
        }
        if(v.getId() == R.id.fabSave)
            savePassword();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(buttonView.getId() == R.id.swPass){
            if(isChecked)
                etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            else
                etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case SharedPrefs.REQUEST_WRTIE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    try {
                        SharedPrefs.exportDB(getActivity());
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(getActivity(), getResources().getString(R.string.backup_fail), Toast.LENGTH_LONG).show();
                    }
                } else
                    Toast.makeText(getActivity(), getResources().getString(R.string.backup_fail), Toast.LENGTH_LONG).show();
                return;
            }
        }
    }

    public void setType(int type){
        this.type = type;
    }

    public void clearTexts(){
        etTitle.setText("");
        etUsername.setText("");
        etEmail.setText("");
        etPassword.setText("");
        etNotes.setText("");
    }

    private void savePassword(){
        builder = new AlertDialog.Builder(getActivity(), R.style.MyAlertDialogStyle);
        builder.setTitle(getResources().getString(R.string.new_pass));
        builder.setMessage(getResources().getString(R.string.new_pass_insert_confirm));
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                try {
                    if(etPassword.getText().length() > 0) {
                        db.addPassword(new Password(0, etTitle.getText().toString(), etEmail.getText().toString(),
                                Encryption.encryptAES(etPassword.getText().toString(),
                                        settings.getBoolean(SharedPrefs.PREFS_KEY_BOOL, false) ? Encryption.encryptSHA1(SharedPrefs.passkey)
                                                : new SecretKeySpec(Encryption.DEFAULT_KEY_VALUE, Encryption.AES_ALGORITHM)),
                                etUsername.getText().toString(), etNotes.getText().toString(), type));
                        if (settings.getBoolean(SharedPrefs.PREFS_AUTO_BACKUP, false)){
                            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                                SharedPrefs.exportDB(getActivity());
                            else
                                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_EXTERNAL_STORAGE);
                        }
                        fragmentsListener.onPasswordAdded(getResources().getString(R.string.passowrd_saved_successfully));
                    }
                    else {
                        Snackbar.make(getView(), R.string.no_password, Snackbar.LENGTH_LONG).show();
                    }
                    dialog.dismiss();
                    db.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    if(e instanceof IOException) {
                        fragmentsListener.onPasswordAdded(getResources().getString(R.string.passowrd_saved_successfully));
                        Toast.makeText(getActivity(), getResources().getString(R.string.backup_fail), Toast.LENGTH_LONG).show();
                    }
                    else
                        Snackbar.make(getView(), R.string.passowrd_saved_fail, Snackbar.LENGTH_LONG).show();
                }
            }
        });

        builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void setLayout(){
        switch (type){
            case Database.TYPE_GOOGLE:
                setGoogleLayout();
                break;
            case Database.TYPE_FACEBOOK:
                setFacebookLayout();
                break;
            case Database.TYPE_TWITTER:
                setTwitterLayout();
                break;
            case Database.TYPE_MICROSOFT:
                setMicrosoftLayout();
                break;
            case Database.TYPE_INSTAGRAM:
                setInstagramLayout();
                break;
            case Database.TYPE_SNAPCHAT:
                setSnapchatLayout();
                break;
            case Database.TYPE_CUSTOM:
                setCustomLayout();
                break;
            case Database.TYPE_LINKEDIN:
                setLinkedInLayout();
                break;
        }
    }

    private void setGoogleLayout(){
        tvUsername.setVisibility(View.GONE);
        etUsername.setVisibility(View.GONE);

        ivPasswordHeader.setImageResource(R.drawable.logo_google);
        ivPasswordHeader.setBackgroundResource(R.color.white);
        ctlFragmentPassword.setContentScrimResource(R.color.googleBlue);
        ctlFragmentPassword.setStatusBarScrimResource(R.color.googleBlueDark);
        DrawableCompat.setTintList(DrawableCompat.wrap(fabSave.getDrawable()),
                ResourcesCompat.getColorStateList(getResources(), R.color.white, null));
        fabSave.setBackgroundTintList(ResourcesCompat.getColorStateList(getResources(), R.color.googleRed, null));

        tvTitle.setTextColor(ResourcesCompat.getColor(getResources(), R.color.googleBlue, null));
        tvEmail.setTextColor(ResourcesCompat.getColor(getResources(), R.color.googleRed, null));
        tvPassword.setTextColor(ResourcesCompat.getColor(getResources(), R.color.googleYellow, null));
        tvNotes.setTextColor(ResourcesCompat.getColor(getResources(), R.color.googleGreen, null));

        swPass.setTextColor(ResourcesCompat.getColor(getResources(), R.color.googleYellow, null));

        etTitle.setTextColor(ResourcesCompat.getColor(getResources(), R.color.googleBlue, null));
        etEmail.setTextColor(ResourcesCompat.getColor(getResources(), R.color.googleRed, null));
        etPassword.setTextColor(ResourcesCompat.getColor(getResources(), R.color.googleYellow, null));
        etNotes.setTextColor(ResourcesCompat.getColor(getResources(), R.color.googleGreen, null));
    }

    private void setFacebookLayout(){
        tvUsername.setVisibility(View.VISIBLE);
        etUsername.setVisibility(View.VISIBLE);

        ivPasswordHeader.setImageResource(R.drawable.logo_facebook);
        ivPasswordHeader.setBackgroundResource(R.color.facebook);
        ctlFragmentPassword.setContentScrimResource(R.color.facebook);
        ctlFragmentPassword.setStatusBarScrimResource(R.color.facebookDark);
        DrawableCompat.setTintList(DrawableCompat.wrap(fabSave.getDrawable()),
                ResourcesCompat.getColorStateList(getResources(), R.color.white, null));
        fabSave.setBackgroundTintList(ResourcesCompat.getColorStateList(getResources(), R.color.facebook, null));

        tvTitle.setTextColor(ResourcesCompat.getColor(getResources(), R.color.facebook, null));
        tvUsername.setTextColor(ResourcesCompat.getColor(getResources(), R.color.facebook, null));
        tvEmail.setTextColor(ResourcesCompat.getColor(getResources(), R.color.facebook, null));
        tvPassword.setTextColor(ResourcesCompat.getColor(getResources(), R.color.facebook, null));
        tvNotes.setTextColor(ResourcesCompat.getColor(getResources(), R.color.facebook, null));

        swPass.setTextColor(ResourcesCompat.getColor(getResources(), R.color.facebook, null));

        etTitle.setTextColor(ResourcesCompat.getColor(getResources(), R.color.facebook, null));
        etUsername.setTextColor(ResourcesCompat.getColor(getResources(), R.color.facebook, null));
        etEmail.setTextColor(ResourcesCompat.getColor(getResources(), R.color.facebook, null));
        etPassword.setTextColor(ResourcesCompat.getColor(getResources(), R.color.facebook, null));
        etNotes.setTextColor(ResourcesCompat.getColor(getResources(), R.color.facebook, null));
    }

    private void setTwitterLayout(){
        tvUsername.setVisibility(View.VISIBLE);
        etUsername.setVisibility(View.VISIBLE);

        ivPasswordHeader.setImageResource(R.drawable.logo_twitter);
        ivPasswordHeader.setBackgroundResource(R.color.twitter);
        ctlFragmentPassword.setContentScrimResource(R.color.twitter);
        ctlFragmentPassword.setStatusBarScrimResource(R.color.twitterDark);
        DrawableCompat.setTintList(DrawableCompat.wrap(fabSave.getDrawable()),
                ResourcesCompat.getColorStateList(getResources(), R.color.white, null));
        fabSave.setBackgroundTintList(ResourcesCompat.getColorStateList(getResources(), R.color.twitter, null));

        tvTitle.setTextColor(ResourcesCompat.getColor(getResources(), R.color.twitterDark, null));
        tvUsername.setTextColor(ResourcesCompat.getColor(getResources(), R.color.twitterDark, null));
        tvEmail.setTextColor(ResourcesCompat.getColor(getResources(), R.color.twitterDark, null));
        tvPassword.setTextColor(ResourcesCompat.getColor(getResources(), R.color.twitterDark, null));
        tvNotes.setTextColor(ResourcesCompat.getColor(getResources(), R.color.twitterDark, null));

        swPass.setTextColor(ResourcesCompat.getColor(getResources(), R.color.twitterDark, null));

        etTitle.setTextColor(ResourcesCompat.getColor(getResources(), R.color.twitterDark, null));
        etUsername.setTextColor(ResourcesCompat.getColor(getResources(), R.color.twitterDark, null));
        etEmail.setTextColor(ResourcesCompat.getColor(getResources(), R.color.twitterDark, null));
        etPassword.setTextColor(ResourcesCompat.getColor(getResources(), R.color.twitterDark, null));
        etNotes.setTextColor(ResourcesCompat.getColor(getResources(), R.color.twitterDark, null));
    }

    private void setMicrosoftLayout(){
        tvUsername.setVisibility(View.GONE);
        etUsername.setVisibility(View.GONE);

        ivPasswordHeader.setImageResource(R.drawable.logo_microsoft);
        ivPasswordHeader.setBackgroundResource(R.color.microsoft);
        ctlFragmentPassword.setContentScrimResource(R.color.microsoft);
        ctlFragmentPassword.setStatusBarScrimResource(R.color.microsoftDark);
        DrawableCompat.setTintList(DrawableCompat.wrap(fabSave.getDrawable()),
                ResourcesCompat.getColorStateList(getResources(), R.color.white, null));
        fabSave.setBackgroundTintList(ResourcesCompat.getColorStateList(getResources(), R.color.microsoft, null));

        tvTitle.setTextColor(ResourcesCompat.getColor(getResources(), R.color.microsoftDark, null));
        tvEmail.setTextColor(ResourcesCompat.getColor(getResources(), R.color.microsoftDark, null));
        tvPassword.setTextColor(ResourcesCompat.getColor(getResources(), R.color.microsoftDark, null));
        tvNotes.setTextColor(ResourcesCompat.getColor(getResources(), R.color.microsoftDark, null));

        swPass.setTextColor(ResourcesCompat.getColor(getResources(), R.color.microsoftDark, null));

        etTitle.setTextColor(ResourcesCompat.getColor(getResources(), R.color.microsoftDark, null));
        etEmail.setTextColor(ResourcesCompat.getColor(getResources(), R.color.microsoftDark, null));
        etPassword.setTextColor(ResourcesCompat.getColor(getResources(), R.color.microsoftDark, null));
        etNotes.setTextColor(ResourcesCompat.getColor(getResources(), R.color.microsoftDark, null));
    }

    private void setInstagramLayout(){
        tvUsername.setVisibility(View.VISIBLE);
        etUsername.setVisibility(View.VISIBLE);

        ivPasswordHeader.setImageResource(R.drawable.logo_instagram);
        ivPasswordHeader.setBackgroundResource(R.drawable.instagram_background);
        ctlFragmentPassword.setContentScrimResource(R.drawable.instagram_toolbar);
        ctlFragmentPassword.setStatusBarScrimResource(R.drawable.instagram_statusbar);
        DrawableCompat.setTintList(DrawableCompat.wrap(fabSave.getDrawable()),
                ResourcesCompat.getColorStateList(getResources(), R.color.white, null));
        fabSave.setBackgroundTintList(ResourcesCompat.getColorStateList(getResources(), R.color.instagram4, null));

        tvTitle.setTextColor(ResourcesCompat.getColor(getResources(), R.color.instagram1, null));
        tvUsername.setTextColor(ResourcesCompat.getColor(getResources(), R.color.instagram3, null));
        tvEmail.setTextColor(ResourcesCompat.getColor(getResources(), R.color.instagram5, null));
        tvPassword.setTextColor(ResourcesCompat.getColor(getResources(), R.color.instagram7, null));
        tvNotes.setTextColor(ResourcesCompat.getColor(getResources(), R.color.instagram9, null));

        swPass.setTextColor(ResourcesCompat.getColor(getResources(), R.color.instagram7, null));

        etTitle.setTextColor(ResourcesCompat.getColor(getResources(), R.color.instagram2, null));
        etUsername.setTextColor(ResourcesCompat.getColor(getResources(), R.color.instagram4, null));
        etEmail.setTextColor(ResourcesCompat.getColor(getResources(), R.color.instagram6, null));
        etPassword.setTextColor(ResourcesCompat.getColor(getResources(), R.color.instagram8, null));
        etNotes.setTextColor(ResourcesCompat.getColor(getResources(), R.color.instagram10, null));
    }

    private void setSnapchatLayout(){
        tvUsername.setVisibility(View.VISIBLE);
        etUsername.setVisibility(View.VISIBLE);

        ivPasswordHeader.setImageResource(R.drawable.logo_snapchat);
        ivPasswordHeader.setBackgroundResource(R.color.snapchat);
        ctlFragmentPassword.setContentScrimResource(R.color.snapchatPrimary);
        ctlFragmentPassword.setStatusBarScrimResource(R.color.snapchatPrimaryDark);
        fabSave.setBackgroundResource(R.color.snapchatPrimary);
        DrawableCompat.setTintList(DrawableCompat.wrap(fabSave.getDrawable()),
                ResourcesCompat.getColorStateList(getResources(), R.color.white, null));
        fabSave.setBackgroundTintList(ResourcesCompat.getColorStateList(getResources(), R.color.snapchatPrimary, null));

        tvTitle.setTextColor(ResourcesCompat.getColor(getResources(), R.color.snapchatPrimaryDark, null));
        tvUsername.setTextColor(ResourcesCompat.getColor(getResources(), R.color.snapchatPrimaryDark, null));
        tvEmail.setTextColor(ResourcesCompat.getColor(getResources(), R.color.snapchatPrimaryDark, null));
        tvPassword.setTextColor(ResourcesCompat.getColor(getResources(), R.color.snapchatPrimaryDark, null));
        tvNotes.setTextColor(ResourcesCompat.getColor(getResources(), R.color.snapchatPrimaryDark, null));

        swPass.setTextColor(ResourcesCompat.getColor(getResources(), R.color.snapchatPrimaryDark, null));

        etTitle.setTextColor(ResourcesCompat.getColor(getResources(), R.color.snapchatPrimaryDark, null));
        etUsername.setTextColor(ResourcesCompat.getColor(getResources(), R.color.snapchatPrimaryDark, null));
        etEmail.setTextColor(ResourcesCompat.getColor(getResources(), R.color.snapchatPrimaryDark, null));
        etPassword.setTextColor(ResourcesCompat.getColor(getResources(), R.color.snapchatPrimaryDark, null));
        etNotes.setTextColor(ResourcesCompat.getColor(getResources(), R.color.snapchatPrimaryDark, null));
    }

    private void setLinkedInLayout(){
        tvUsername.setVisibility(View.VISIBLE);
        etUsername.setVisibility(View.VISIBLE);

        ivPasswordHeader.setImageResource(R.drawable.logo_linkedin);
        ivPasswordHeader.setBackgroundResource(R.color.linkedin);
        ctlFragmentPassword.setContentScrimResource(R.color.linkedin);
        ctlFragmentPassword.setStatusBarScrimResource(R.color.linkedinDark);
        DrawableCompat.setTintList(DrawableCompat.wrap(fabSave.getDrawable()),
                ResourcesCompat.getColorStateList(getResources(), R.color.white, null));
        fabSave.setBackgroundTintList(ResourcesCompat.getColorStateList(getResources(), R.color.linkedin, null));

        tvTitle.setTextColor(ResourcesCompat.getColor(getResources(), R.color.linkedin, null));
        tvUsername.setTextColor(ResourcesCompat.getColor(getResources(), R.color.linkedin, null));
        tvEmail.setTextColor(ResourcesCompat.getColor(getResources(), R.color.linkedin, null));
        tvPassword.setTextColor(ResourcesCompat.getColor(getResources(), R.color.linkedin, null));
        tvNotes.setTextColor(ResourcesCompat.getColor(getResources(), R.color.linkedin, null));

        swPass.setTextColor(ResourcesCompat.getColor(getResources(), R.color.linkedin, null));

        etTitle.setTextColor(ResourcesCompat.getColor(getResources(), R.color.linkedin, null));
        etUsername.setTextColor(ResourcesCompat.getColor(getResources(), R.color.linkedin, null));
        etEmail.setTextColor(ResourcesCompat.getColor(getResources(), R.color.linkedin, null));
        etPassword.setTextColor(ResourcesCompat.getColor(getResources(), R.color.linkedin, null));
        etNotes.setTextColor(ResourcesCompat.getColor(getResources(), R.color.linkedin, null));
    }

    private void setCustomLayout(){
        tvUsername.setVisibility(View.VISIBLE);
        etUsername.setVisibility(View.VISIBLE);

        ivPasswordHeader.setImageResource(R.drawable.ic_safe);
        ivPasswordHeader.setBackgroundResource(R.color.colorPrimaryDark);
        ctlFragmentPassword.setContentScrimResource(R.color.colorPrimary);
        ctlFragmentPassword.setStatusBarScrimResource(R.color.colorPrimaryDark);
        DrawableCompat.setTintList(DrawableCompat.wrap(fabSave.getDrawable()),
                ResourcesCompat.getColorStateList(getResources(), R.color.colorPrimary, null));
        fabSave.setBackgroundTintList(ResourcesCompat.getColorStateList(getResources(), R.color.colorAccent, null));

        tvTitle.setTextColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimaryDark, null));
        tvUsername.setTextColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimaryDark, null));
        tvEmail.setTextColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimaryDark, null));
        tvPassword.setTextColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimaryDark, null));
        tvNotes.setTextColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimaryDark, null));

        swPass.setTextColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimaryDark, null));

        etTitle.setTextColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimaryDark, null));
        etUsername.setTextColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimaryDark, null));
        etEmail.setTextColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimaryDark, null));
        etPassword.setTextColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimaryDark, null));
        etNotes.setTextColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimaryDark, null));
    }
}
