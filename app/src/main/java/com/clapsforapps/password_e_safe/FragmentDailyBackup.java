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

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;

/**
 * Created by ALI on 9/26/2015.
 */
public class FragmentDailyBackup extends Fragment {

    private SwitchCompat swEnableAutoBackup;
    private SharedPreferences settings;
    private Button btNext;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        settings = getActivity().getSharedPreferences(SharedPrefs.PREFS_NAME, getActivity().MODE_PRIVATE);
        btNext = (Button) getActivity().findViewById(R.id.btNext);
        swEnableAutoBackup = (SwitchCompat) getActivity().findViewById(R.id.swEnableAutoBackup);
        swEnableAutoBackup.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(buttonView.equals(swEnableAutoBackup)){
                    if(isChecked){
                        settings.edit().putBoolean(SharedPrefs.PREFS_AUTO_BACKUP, true).commit();
                        Snackbar.make(getView(), getActivity().getResources().getString(R.string.auto_backup_activated), Snackbar.LENGTH_SHORT).show();
                    }
                    else {
                        settings.edit().putBoolean(SharedPrefs.PREFS_AUTO_BACKUP, false).commit();
                        Snackbar.make(getView(), getActivity().getResources().getString(R.string.auto_backup_deactivated), Snackbar.LENGTH_SHORT).show();
                    }
                }
            }
        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_daily_backup, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        btNext.setVisibility(View.VISIBLE);
        btNext.setEnabled(true);
    }
}
