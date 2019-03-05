package com.cxkr.photo.simpic;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;

import com.cxkr.photo.BitmapBO;
import com.cxkr.photo.ImageUtil;
import com.example.photo.R;

import java.util.List;


public class SimPicGridViewAdapter extends BaseAdapter {
    private Context mContext;
    private LayoutInflater inflater;
    private List<BitmapBO> bitmapBOList;

    public SimPicGridViewAdapter(Context mContext, List<BitmapBO> bitmapBOList) {
        this.mContext = mContext;
        inflater = LayoutInflater.from(mContext);
        this.bitmapBOList = bitmapBOList;
    }

    public void setBitmapBOList(List<BitmapBO> bitmapBOList) {
        this.bitmapBOList = bitmapBOList;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return bitmapBOList == null ? 0 : bitmapBOList.size();
    }

    @Override
    public Object getItem(int position) {
        return bitmapBOList == null ? null : bitmapBOList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

