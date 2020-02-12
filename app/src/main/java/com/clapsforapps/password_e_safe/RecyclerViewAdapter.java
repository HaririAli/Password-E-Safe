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
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Hussein on 5/26/2016.
 */
public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> {

    public static final int ROWS = 1;
    public static final int GRID = 2;

    private RecyclerViewOnItemClickListener itemClickListener;
    private ArrayList<Object> items;
    private int type;
    private Context context;

    public RecyclerViewAdapter(ArrayList<Object> items, RecyclerViewOnItemClickListener itemClickListener, int type, Context context){
        this.items = items;
        this.type = type;
        this.itemClickListener = itemClickListener;
        this.context = context;
    }

    @Override
    public int getItemViewType(int position){
        return position == 0? 0:1;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;
        switch (type){
            case ROWS:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rowlayout, parent, false);
                break;
            case GRID:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_item, parent, false);
                break;
        }
        return new MyViewHolder(view, type);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        switch (type){
            case ROWS:
                holder.tvEmail.setText(((Password)items.get(position)).getTag());
                switch (((Password)items.get(position)).getType()){
                    case Database.TYPE_TWITTER:
                        holder.ivNetwork.setImageResource(R.drawable.icon_twitter);
                        break;
                    case Database.TYPE_INSTAGRAM:
                        holder.ivNetwork.setImageResource(R.drawable.icon_instagram);
                        break;
                    case Database.TYPE_GOOGLE:
                        holder.ivNetwork.setImageResource(R.drawable.icon_google);
                        break;
                    case Database.TYPE_MICROSOFT:
                        holder.ivNetwork.setImageResource(R.drawable.icon_microsoft);
                        break;
                    case Database.TYPE_FACEBOOK:
                        holder.ivNetwork.setImageResource(R.drawable.icon_facebook);
                        break;
                    case Database.TYPE_SNAPCHAT:
                        holder.ivNetwork.setImageResource(R.drawable.icon_snapchat);
                        break;
                    case Database.TYPE_CUSTOM:
                        holder.ivNetwork.setImageResource(R.drawable.icon_custom);
                        break;
                    case Database.TYPE_LINKEDIN:
                        holder.ivNetwork.setImageResource(R.drawable.icon_linkedin);
                        break;
                }
                break;
            case GRID:
                switch (position) {
                    case 0:
                        //icon_google
                        holder.tvNewPassword.setText("Google");
                        holder.ivNewPassword.setImageResource(R.drawable.icon_google);
                        break;
                    case 1:
                        //facebook
                        holder.tvNewPassword.setText("Facebook");
                        holder.ivNewPassword.setImageResource(R.drawable.icon_facebook);
                        break;
                    case 2:
                        //icon_twitter
                        holder.tvNewPassword.setText("Twitter");
                        holder.ivNewPassword.setImageResource(R.drawable.icon_twitter);
                        break;
                    case 3:
                        //instagram
                        holder.tvNewPassword.setText("Instagram");
                        holder.ivNewPassword.setImageResource(R.drawable.icon_instagram);
                        break;
                    case 4:
                        //icon_microsoft
                        holder.tvNewPassword.setText("Hotmail");
                        holder.ivNewPassword.setImageResource(R.drawable.icon_microsoft);
                        break;
                    case 5:
                        //snaochat
                        holder.tvNewPassword.setText("Snapchat");
                        holder.ivNewPassword.setImageResource(R.drawable.icon_snapchat);
                        break;
                    case 6:
                        //LinkedIn
                        holder.tvNewPassword.setText("LinkedIn");
                        holder.ivNewPassword.setImageResource(R.drawable.icon_linkedin);
                        break;
                    case 7:
                        //custom
                        holder.tvNewPassword.setText(context.getResources().getString(R.string.other_apps));
                        holder.ivNewPassword.setImageResource(R.drawable.icon_custom);
                }
                break;
        }
    }

    public void setHeaderMsg(String headerMsg, long dateInMillis){
        //this.headerMsg = headerMsg + " " + SharedPrefs.arabicDateFormat.format(dateInMillis);
    }

    @Override
    public int getItemCount() {
        return type == GRID ? 8 : items.size();
    }

    public interface RecyclerViewOnItemClickListener {

        void onItemClicked(View v, int position);

    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        protected View vListItem;
        protected TextView tvEmail, tvNewPassword;
        protected ImageView ivNetwork, ivNewPassword;
        protected int type;

        public MyViewHolder(View vListItem, int type){
            super(vListItem);
            this.type = type;
            this.vListItem = vListItem;
            this.vListItem.setOnClickListener(this);

            switch (type){
                case ROWS:
                    tvEmail = (TextView) vListItem.findViewById(R.id.tvEmail);
                    ivNetwork = (ImageView) vListItem.findViewById(R.id.ivNetwork);
                    break;
                case GRID:
                    tvNewPassword = (TextView) vListItem.findViewById(R.id.tvNewPassword);
                    ivNewPassword = (ImageView) vListItem.findViewById(R.id.ivNewPassword);
                    break;
            }
        }

        @Override
        public void onClick(View v) {
            itemClickListener.onItemClicked(v, getLayoutPosition());
        }

    }
}
