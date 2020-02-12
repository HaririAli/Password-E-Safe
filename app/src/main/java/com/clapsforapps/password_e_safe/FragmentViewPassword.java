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
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.MenuItem;
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
 * Created by ALI on 1/4/2017.
 */

public class FragmentViewPassword extends Fragment implements CompoundButton.OnCheckedChangeListener,
        ActionMenuView.OnMenuItemClickListener, TextWatcher, View.OnClickListener {

    private EditText etTitle, etUsername, etEmail, etPassword, etNotes;
    private TextView tvTitle, tvUsername, tvEmail, tvPassword, tvNotes;
    private SwitchCompat swPass;
    private ActionMenuView amvFragmentPassword;
    private Toolbar tbFragmentPassword;
    private ImageView ivPasswordHeader;
    private CollapsingToolbarLayout ctlFragmentPassword;
    private Password password;
    private SharedPreferences settings;
    private AlertDialog.Builder builder;
    private Database db;
    private FragmentsListener fragmentsListener;

    private boolean isEdited = false, isTypeChanged = false, isDataSetChanged = false, temp = false;
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
        amvFragmentPassword = (ActionMenuView)getView().findViewById(R.id.amvFragmentPassword);
        tbFragmentPassword = (Toolbar)getView().findViewById(R.id.tbFragmentPassword);
        ivPasswordHeader = (ImageView)getView().findViewById(R.id.ivPasswordHeader);
        ctlFragmentPassword = (CollapsingToolbarLayout)getView().findViewById(R.id.ctlFragmentPassword);

        getActivity().getMenuInflater().inflate(R.menu.menu_view_password, amvFragmentPassword.getMenu());
        amvFragmentPassword.setOnMenuItemClickListener(this);
        amvFragmentPassword.setVisibility(View.VISIBLE);

        tbFragmentPassword.setNavigationOnClickListener(this);

        swPass.setOnCheckedChangeListener(this);

        etTitle.addTextChangedListener(this);
        etUsername.addTextChangedListener(this);
        etEmail.addTextChangedListener(this);
        etPassword.addTextChangedListener(this);
        etNotes.addTextChangedListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        if(db == null)
            db = ((MainActivity)getActivity()).getDb();
        if(password != null) {
            //Log.wtf("onStart", "Password not null");
            setLayout();
            loadData(true);
        }
        //Log.wtf("onStart", "Password whatever");
        swPass.setChecked(false);
    }

    @Override
    public void onClick(View v) {
        if(((View)v.getParent()).getId() == R.id.tbFragmentPassword){
            onBackPressed();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(buttonView.getId() == R.id.swPass){
            temp = isEdited;
            if(isChecked)
                etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            else
                etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            if(!temp)
                isEdited = false;
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_delete:
                deletePassword();
                break;

            case R.id.action_refresh:
                loadData(password.getType() != type);
                break;

            case R.id.action_save:
                builder = new AlertDialog.Builder(getActivity(), R.style.MyAlertDialogStyle);
                builder.setTitle(getResources().getString(R.string.edit_password));
                builder.setMessage(getResources().getString(R.string.edit_password_confirm));
                builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int pos) {
                        if(savePassword())
                            Snackbar.make(getView(), R.string.update_success, Snackbar.LENGTH_LONG).show();
                        else
                            Snackbar.make(getView(), R.string.update_fail, Snackbar.LENGTH_LONG).show();
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
                break;

            case R.id.action_type_google:
                setType(R.id.action_type_google, R.drawable.menu_google, Database.TYPE_GOOGLE);
                isEdited = password.getType() != Database.TYPE_GOOGLE;
                break;
            case R.id.action_type_facebook:
                setType(R.id.action_type_facebook, R.drawable.menu_facebook, Database.TYPE_FACEBOOK);
                isEdited = password.getType() != Database.TYPE_FACEBOOK;
                break;
            case R.id.action_type_twitter:
                setType(R.id.action_type_twitter, R.drawable.menu_twitter, Database.TYPE_TWITTER);
                isEdited = password.getType() != Database.TYPE_TWITTER;
                break;
            case R.id.action_type_microsoft:
                setType(R.id.action_type_microsoft, R.drawable.menu_microsoft, Database.TYPE_MICROSOFT);
                isEdited = password.getType() != Database.TYPE_MICROSOFT;
                break;
            case R.id.action_type_instagram:
                setType(R.id.action_type_instagram, R.drawable.menu_instagram, Database.TYPE_INSTAGRAM);
                isEdited = password.getType() != Database.TYPE_INSTAGRAM;
                break;
            case R.id.action_type_snapchat:
                setType(R.id.action_type_snapchat, R.drawable.menu_snapchat, Database.TYPE_SNAPCHAT);
                isEdited = password.getType() != Database.TYPE_SNAPCHAT;
                break;
            case R.id.action_type_custom:
                setType(R.id.action_type_custom, R.drawable.menu_custom, Database.TYPE_CUSTOM);
                isEdited = password.getType() != Database.TYPE_CUSTOM;
                break;
            case R.id.action_type_linkedIn:
                setType(R.id.action_type_linkedIn, R.drawable.menu_linkedin, Database.TYPE_LINKEDIN);
                isEdited = password.getType() != Database.TYPE_LINKEDIN;
                break;
        }
        return true;
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        isEdited = true;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case SharedPrefs.REQUEST_WRTIE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    try {
                        backup();
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

    public void onBackPressed(){
        if(isEdited){
            builder = new AlertDialog.Builder(getActivity(), R.style.MyAlertDialogStyle);
            builder.setTitle(getResources().getString(R.string.saveChanges));
            builder.setMessage(getResources().getString(R.string.saveChangesConfirm));
            builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int pos) {
                    if(savePassword())
                        fragmentsListener.onViewPasswordEnded(true, getResources().getString(R.string.update_success), isTypeChanged);
                    else
                        fragmentsListener.onViewPasswordEnded(false, getResources().getString(R.string.update_fail), false);
                    dialog.dismiss();
                }
            });
            builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    fragmentsListener.onViewPasswordEnded(isDataSetChanged, null, isTypeChanged);
                    dialog.dismiss();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
        else {
            fragmentsListener.onViewPasswordEnded(isDataSetChanged, null, isTypeChanged);
            isDataSetChanged = false;
        }
    }

    public void setPassword(Password password){
        this.password = password;
    }

    private void setType(int menuId, int iconId, int type){
        for(int index = 0; index < amvFragmentPassword.getMenu().findItem(R.id.action_pass_type).getSubMenu().size(); index++)
            amvFragmentPassword.getMenu().findItem(R.id.action_pass_type).getSubMenu().getItem(index).setChecked(false);
        amvFragmentPassword.getMenu().findItem(R.id.action_pass_type).setIcon(iconId);
        amvFragmentPassword.getMenu().findItem(R.id.action_pass_type).getSubMenu().findItem(menuId).setChecked(true);
        this.type = type;
    }

    private void loadData(boolean isTypeChanged){
        if(isTypeChanged){
            switch (password.getType()){
                case Database.TYPE_GOOGLE:
                    setType(R.id.action_type_google, R.drawable.menu_google, Database.TYPE_GOOGLE);
                    break;
                case Database.TYPE_FACEBOOK:
                    setType(R.id.action_type_facebook, R.drawable.menu_facebook, Database.TYPE_FACEBOOK);
                    break;
                case Database.TYPE_TWITTER:
                    setType(R.id.action_type_twitter, R.drawable.menu_twitter, Database.TYPE_TWITTER);
                    break;
                case Database.TYPE_MICROSOFT:
                    setType(R.id.action_type_microsoft, R.drawable.menu_microsoft, Database.TYPE_MICROSOFT);
                    break;
                case Database.TYPE_INSTAGRAM:
                    setType(R.id.action_type_instagram, R.drawable.menu_instagram, Database.TYPE_INSTAGRAM);
                    break;
                case Database.TYPE_SNAPCHAT:
                    setType(R.id.action_type_snapchat, R.drawable.menu_snapchat, Database.TYPE_SNAPCHAT);
                    break;
                case Database.TYPE_LINKEDIN:
                    setType(R.id.action_type_linkedIn, R.drawable.menu_linkedin, Database.TYPE_LINKEDIN);
                    break;
                case Database.TYPE_CUSTOM:
                    setType(R.id.action_type_custom, R.drawable.menu_custom, Database.TYPE_CUSTOM);
                    break;
            }
        }
        tbFragmentPassword.setTitle(password.getTag());
        etTitle.setText(password.getTag());
        etUsername.setText(password.getUsername());
        etEmail.setText(password.getEmail());
        etNotes.setText(password.getNotes());

        try {
            etPassword.setText(Encryption.decryptAES(password.getPassword(),
                    settings.getBoolean(SharedPrefs.PREFS_KEY_BOOL, false) ?
                            Encryption.encryptSHA1(SharedPrefs.passkey)
                            : new SecretKeySpec(Encryption.DEFAULT_KEY_VALUE, Encryption.AES_ALGORITHM)));
        }catch (Exception ex){
            ex.printStackTrace();
            fragmentsListener.onViewPasswordEnded(false, getResources().getString(R.string.load_fail), false);
        }
        isEdited = false;
    }

    private boolean savePassword(){
        try {
            Password pass = new Password();
            pass.setId(password.getId());
            pass.setTag(etTitle.getText().toString());
            pass.setUsername(etUsername.getText().toString());
            pass.setEmail(etEmail.getText().toString());
            pass.setNotes(etNotes.getText().toString());
            pass.setPassword(Encryption.encryptAES(etPassword.getText().toString(), settings.getBoolean(SharedPrefs.PREFS_KEY_BOOL,false) ?
                    Encryption.encryptSHA1(SharedPrefs.passkey) : new SecretKeySpec(Encryption.DEFAULT_KEY_VALUE, Encryption.AES_ALGORITHM)));
            pass.setType(type);
            db.updatePassword(pass);
            db.close();
            password.setNotes(pass.getNotes());
            password.setTag(pass.getTag());
            password.setEmail(pass.getEmail());
            password.setUsername(pass.getUsername());
            password.setPassword(pass.getPassword());
            if(password.getType() != type) {
                password.setType(pass.getType());
                setLayout();
                isTypeChanged = true;
            }
            isEdited = false;
            isDataSetChanged = true;
            if(settings.getBoolean(SharedPrefs.PREFS_AUTO_BACKUP, false))
                backup();
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            if(ex instanceof IOException){
                Toast.makeText(getActivity(), getResources().getString(R.string.backup_fail), Toast.LENGTH_LONG).show();
                return true;
            }
            return false;
        }
    }

    private void deletePassword(){
        builder = new AlertDialog.Builder(getActivity(), R.style.MyAlertDialogStyle);
        builder.setTitle(getResources().getString(R.string.delete_Password));
        builder.setMessage(getResources().getString(R.string.delete_confirmation));
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int pos) {
                db.deletePassword(password.getId());
                db.close();
                if(settings.getBoolean(SharedPrefs.PREFS_AUTO_BACKUP, false))
                    try {
                        backup();
                    }
                    catch (IOException ex){
                        ex.printStackTrace();
                        Toast.makeText(getActivity(), getResources().getString(R.string.backup_fail), Toast.LENGTH_LONG).show();
                    }
                dialog.dismiss();
                fragmentsListener.onViewPasswordEnded(true, getResources().getString(R.string.delete_success), true);
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

    private void backup() throws IOException {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            SharedPrefs.exportDB(getActivity());
        else
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_EXTERNAL_STORAGE);
    }

    private void setLayout(){
        switch (password.getType()){
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
        amvFragmentPassword.setBackgroundResource(R.color.googleBlue);

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
        amvFragmentPassword.setBackgroundResource(R.color.facebook);

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
        amvFragmentPassword.setBackgroundResource(R.color.twitter);

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
        amvFragmentPassword.setBackgroundResource(R.color.microsoft);

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
        amvFragmentPassword.setBackgroundResource(R.drawable.instagram_actionmenu);

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
        amvFragmentPassword.setBackgroundResource(R.color.snapchatPrimary);

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
        amvFragmentPassword.setBackgroundResource(R.color.linkedin);

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
        amvFragmentPassword.setBackgroundResource(R.color.colorPrimary);

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
