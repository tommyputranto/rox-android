/*
 * Copyright 2014-2015 Daniel Pedraza-Arcega
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.grayfox.android.app.widget;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.grayfox.android.app.R;

import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

import java.util.List;

public class DrawerItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<DrawerItem> drawerItems;

    private OnItemClickListener listener;
    private int selectedPosition;

    public DrawerItemAdapter(List<DrawerItem> drawerItems) {
        this.drawerItems = drawerItems;
        selectedPosition = -1;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (DrawerItem.Type.values()[viewType]) {
            case HEADER:
                View headerRootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.drawer_header, parent, false);
                return new HeaderViewHolder(headerRootView);
            case DIVIDER:
                View dividerRootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.drawer_divider, parent, false);
                return new DividerViewHolder(dividerRootView);
            case OPTION:
                View optionRootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.drawer_option, parent, false);
                return new OptionViewHolder(optionRootView);
            default: return null;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        Context context = holder.itemView.getContext();
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) listener.onClick(position);
            }
        });
        DrawerItem drawerItem = drawerItems.get(position);
        switch (drawerItem.getType()) {
            case HEADER:
                DrawerHeader drawerHeader = (DrawerHeader) drawerItem;
                if (drawerHeader.getUser() != null) {
                    HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;
                    headerViewHolder.viewProfileTextView.setVisibility(View.VISIBLE);
                    headerViewHolder.userNameTextView.setText(drawerHeader.getUser().getName());
                    Picasso.with(context)
                            .load(drawerHeader.getUser().getPhotoUrl())
                            .placeholder(R.drawable.ic_contact_picture)
                            .into(headerViewHolder.profileImageView);
                }
                break;
            case OPTION:
                DrawerOption drawerOption = (DrawerOption) drawerItem;
                OptionViewHolder optionViewHolder = (OptionViewHolder) holder;
                optionViewHolder.nameTextView.setText(drawerOption.getNameRes());
                if (selectedPosition == position) {
                    optionViewHolder.itemView.setBackgroundColor(context.getResources().getColor(R.color.selected_background));
                    optionViewHolder.nameTextView.setTextColor(context.getResources().getColor(R.color.primary_selected_text));
                    Drawable selectedIcon = context.getResources().getDrawable(drawerOption.getSelectedIconRes());
                    selectedIcon.mutate().setColorFilter(context.getResources().getColor(R.color.selected_icon), PorterDuff.Mode.SRC_IN);
                    optionViewHolder.iconImageView.setImageDrawable(selectedIcon);
                } else {
                    optionViewHolder.itemView.setBackgroundColor(context.getResources().getColor(R.color.unselected_background));
                    optionViewHolder.nameTextView.setTextColor(context.getResources().getColor(R.color.primary_text));
                    optionViewHolder.iconImageView.setImageResource(drawerOption.getUnselectedIconRes());
                }
        }
    }

    @Override
    public int getItemCount() {
        return drawerItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        return drawerItems.get(position).getType().ordinal();
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public void setSelectedPosition(int position) {
        selectedPosition = position;
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void onClick(int position);
    }

    private static class HeaderViewHolder extends RecyclerView.ViewHolder {

        private CircleImageView profileImageView;
        private TextView userNameTextView;
        private TextView viewProfileTextView;

        public HeaderViewHolder(View rootView) {
            super(rootView);
            profileImageView = (CircleImageView) rootView.findViewById(R.id.profile);
            userNameTextView = (TextView) rootView.findViewById(R.id.user_name);
            viewProfileTextView = (TextView) rootView.findViewById(R.id.view_profile_text);
        }
    }

    private static class DividerViewHolder extends RecyclerView.ViewHolder {

        public DividerViewHolder(View rootView) {
            super(rootView);
        }
    }

    private static class OptionViewHolder extends RecyclerView.ViewHolder {

        private ImageView iconImageView;
        private TextView nameTextView;

        public OptionViewHolder(View rootView) {
            super(rootView);
            iconImageView = (ImageView) rootView.findViewById(R.id.icon);
            nameTextView = (TextView) rootView.findViewById(R.id.name);
        }
    }
}