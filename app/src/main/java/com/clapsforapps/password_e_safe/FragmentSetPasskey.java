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

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import static com.clapsforapps.password_e_safe.R.string.match;

/**
 * Created by ALI on 9/26/2015.
 */
public class FragmentSetPasskey extends Fragment implements View.OnClickListener, TextWatcher {

    private EditText etNewKey, etConfirmNewKey;
    private TextView tvKeyMatch;
    private Button btSubmitNewKey, btSkipPassKey, btNext;
    private String passKey;
    private SharedPreferences settings;
    private LinearLayout loKey;
    private SwitchCompat swShowPass;
    private InitialConfig initialConfig;
    private boolean passwordsMatch = false, isSet = false;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        initialConfig = (InitialConfig)getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_set_passkey, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        settings = getActivity().getSharedPreferences(SharedPrefs.PREFS_NAME, getActivity().MODE_PRIVATE);

        loKey = (LinearLayout)getActivity().findViewById(R.id.loKey);
        etNewKey = (EditText)getActivity().findViewById(R.id.etNewKey);
        btSubmitNewKey = (Button)getActivity().findViewById(R.id.btSubmitNewKey);
        btSkipPassKey = (Button)getActivity().findViewById(R.id.btSkipPassKey);
        btNext = (Button)getActivity().findViewById(R.id.btNext);
        swShowPass = (SwitchCompat) getActivity().findViewById(R.id.swShowPass);
        tvKeyMatch = (TextView)getActivity().findViewById(R.id.tvKeyMatch);
        etConfirmNewKey = (EditText)getActivity().findViewById(R.id.etConfirmNewKey);
        etConfirmNewKey.addTextChangedListener(this);
        etNewKey.addTextChangedListener(this);

        btSubmitNewKey.setOnClickListener(this);
        btSkipPassKey.setOnClickListener(this);
        swShowPass.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    // show password
                    etConfirmNewKey.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    etNewKey.setTransformationMethod(PasswordTransformationMethod.getInstance());
                } else {
                    // hide password
                    etConfirmNewKey.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    etNewKey.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        if(isSet)
            updateViewPasskeySet();
        else
            btNext.setEnabled(false);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btSubmitNewKey:
                passKey = etNewKey.getText().toString();
                if(!passwordsMatch) {
                    Snackbar.make(getView(), R.string.passkey_mismatch, Snackbar.LENGTH_LONG).show();
                }
                else if (passKey.length() < 4) {
                    Snackbar.make(getView(), R.string.passkeySmall, Snackbar.LENGTH_LONG).show();
                }
                else {
                    try {
                        SharedPrefs.passkey = this.passKey;
                        Database db = new Database(getActivity());
                        db.updatePasskey(Encryption.encryptMD5(SharedPrefs.passkey));
                        db.close();
                        isSet = true;
                        updateViewPasskeySet();
                        settings.edit().putBoolean(SharedPrefs.PREFS_KEY_BOOL, true).commit();
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case R.id.btSkipPassKey:
                initialConfig.skipPasskey();
                break;
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        passwordsMatch = etConfirmNewKey.getText().toString().equals(etNewKey.getText().toString()) && etNewKey.length() > 0;
        if(passwordsMatch){
            tvKeyMatch.setText(getActivity().getResources().getString(match));
            tvKeyMatch.setTextColor(Color.GREEN);
        }
        else {
            tvKeyMatch.setText(getActivity().getResources().getString(R.string.mismatch));
            tvKeyMatch.setTextColor(Color.RED);
        }
    }

    private void updateViewPasskeySet(){
        btSubmitNewKey.setVisibility(View.GONE);
        btSkipPassKey.setVisibility(View.GONE);
        etNewKey.setVisibility(View.GONE);
        etConfirmNewKey.setVisibility(View.GONE);
        tvKeyMatch.setVisibility(View.GONE);
        swShowPass.setVisibility(View.GONE);
        loKey.setVisibility(View.VISIBLE);
        btNext.setEnabled(true);
    }
}
