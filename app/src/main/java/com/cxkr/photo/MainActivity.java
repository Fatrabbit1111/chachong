package com.cxkr.photo;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cxkr.photo.album.AlbumsActivity;

import com.cxkr.photo.recycle.JunkPictureUtil;
import com.cxkr.photo.recycle.RecyclerPictureActivity;
import com.cxkr.photo.simpic.JunkSimUtil;
import com.cxkr.photo.simpic.SimPicActivity;
import com.cxkr.photo.simpic.SimPicUtil;
import com.cxkr.photo.simpic.SimilarPicBO;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private TextView tvSimSize;
    private TextView tvBlurrySize;
    private TextView tvAllSize;
    private TextView tvRecyclerSize;
    private RelativeLayout rlSim;
    private RelativeLayout rlAblum;
    private RelativeLayout rlRecycler;
    private RelativeLayout rlBlurry;
    private GridView gvSim;


    ContentResolver picCR;
    public static final Uri PIC_URI = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    ArrayList<BitmapBO> similarBOS = new ArrayList<>();
    ArrayList<BitmapBO> blurryBOList = new ArrayList<>();
    private GridViewAdapter gridViewAdapter;
    private GridViewAdapter blurryGridAdapter;
    JunkSimUtil junkSimUtil;

    private long simPicSize = 0;
    private long albumSize = 0;
    private long blurrySize = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(com.example.photo.R.layout.activity_main);
        tvSimSize = (TextView) findViewById(com.example.photo.R.id.tv_header_size);
        tvBlurrySize = (TextView) findViewById(com.example.photo.R.id.tv_blurry_size);
        tvAllSize = (TextView) findViewById(com.example.photo.R.id.tv_all_size);
        tvRecyclerSize = (TextView) findViewById(com.example.photo.R.id.tv_recycler_size);
        rlSim = (RelativeLayout) findViewById(com.example.photo.R.id.rl_simpic);
        rlAblum = (RelativeLayout) findViewById(com.example.photo.R.id.rl_album);
        rlRecycler = (RelativeLayout) findViewById(com.example.photo.R.id.rl_recycler);
        rlBlurry = (RelativeLayout) findViewById(com.example.photo.R.id.rl_blurry);

        gvSim = (GridView) findViewById(com.example.photo.R.id.gv_sim_pic);


        mToolbar = (Toolbar) findViewById(com.example.photo.R.id.id_toolbar);
        mToolbar.setTitle("照片清理");

        gridViewAdapter = new GridViewAdapter(MainActivity.this);
        gvSim.setAdapter(gridViewAdapter);

        blurryGridAdapter = new GridViewAdapter(MainActivity.this);

        queryPic();

        rlSim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent simPicIntent = new Intent(MainActivity.this, SimPicActivity.class);
                startActivityForResult(simPicIntent, 1000);
            }
        });


        rlAblum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent albumIntent = new Intent(MainActivity.this, AlbumsActivity.class);
                startActivity(albumIntent);
            }
        });

        rlRecycler.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent albumIntent = new Intent(MainActivity.this, RecyclerPictureActivity.class);
                startActivity(albumIntent);
            }
        });
    }

    private void queryPic() {
        picCR = getContentResolver();
        Cursor query = picCR.query(PIC_URI, null, "mime_type=? or mime_type=?", new String[]{"image/jpeg", "image/png"}, "datetaken");

        if (query == null) {
            //picSimilarActivity.l.sendEmptyMessage(3);
            return;
        }
        while (query.moveToNext()) {
            String picData = query.getString(query.getColumnIndex("_data"));
            long picSize = (long) query.getInt(query.getColumnIndex("_size"));
            long picID = query.getLong(query.getColumnIndex("_id"));
            long strTime = SimPicUtil.getStrTime(picData);
            if (!(0 == strTime || picData == null)) {
                BitmapBO picBO = new BitmapBO();
                picBO.setTime(strTime);
                picBO.setSize(picSize);
                picBO.setFilePath(picData);
                picBO.setId(picID);
                picBO.setChecked(true);
                similarBOS.add(picBO);
                blurryBOList.add(picBO);
                albumSize += picSize;
            }
        }

        query.close();
        if (similarBOS.size() <= 0) {
            //picSimilarActivity.l.sendEmptyMessage(3);
            return;
        }
        Collections.sort(similarBOS, new Comparator() {
            @Override
            public int compare(Object lhs, Object rhs) {
                return ((BitmapBO) lhs).getTime() - ((BitmapBO) rhs).getTime() <= 0 ? -1 : 1;
            }
        });
        albumSize = albumSize / 1024 / 1024;
        DecimalFormat decimalFormat = new DecimalFormat("0.00");//构造方法的字符格式这里如果小数不足2位,会以0补足.
        String p = decimalFormat.format(albumSize);//format 返回的是字符串
        tvAllSize.setText(p + "M");

        long recyclerSize = JunkPictureUtil.getInstance(MainActivity.this).getRecyclerPicSize();
        recyclerSize = recyclerSize / 1024 / 1024;
        String recyclerSizeM = decimalFormat.format(recyclerSize);//format 返回的是字符串
        tvRecyclerSize.setText(recyclerSizeM + "M");

        findSimilarPicture();

    }


    private void findSimilarPicture() {
        junkSimUtil = JunkSimUtil.getInstance(MainActivity.this);
        junkSimUtil.addListener(new JunkSimUtil.Callback() {
            @Override
            public void onSearchingPath(String path) {

            }

            @Override
            public void onUpdate(List<SimilarPicBO> samePicBOs) {
                SimilarPicBO similarPicBO = samePicBOs.get(samePicBOs.size() - 1);
                if (gridViewAdapter.getCount() < 4) {
                    gridViewAdapter.addItems(similarPicBO.getSamePics());
                }
                simPicSize += similarPicBO.getSimilarPicSize();
                float simPicSizeM = simPicSize / 1024 / 1024;

                DecimalFormat decimalFormat = new DecimalFormat("0.00");//构造方法的字符格式这里如果小数不足2位,会以0补足.
                String p = decimalFormat.format(simPicSizeM);//format 返回的是字符串
                tvSimSize.setText(p + "M");
            }

            @Override
            public void onFinish() {

            }
        });

        junkSimUtil.doFindSimPic(similarBOS);
    }




    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.out.println("MainActivity===onDestroy=");
        if (junkSimUtil != null) {
            junkSimUtil.destory();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1000) { //相似图片
            List<SimilarPicBO> similarPicBOs = JunkSimUtil.getInstance(MainActivity.this).getSimilarPicBOs();
            if (similarPicBOs == null || similarPicBOs.size() == 0) {
                tvSimSize.setText(0 + "M");
                gridViewAdapter.resetItems();
                return;
            }
            simPicSize = 0;
            gridViewAdapter.resetItems();
            for (SimilarPicBO similar : similarPicBOs) {
                if (gridViewAdapter.getCount() < 4) {
                    gridViewAdapter.addItems(similar.getSamePics());
                }
                simPicSize += similar.getSimilarPicSize();
                float simPicSizeM = simPicSize / 1024 / 1024;
                DecimalFormat decimalFormat = new DecimalFormat("0.00");//构造方法的字符格式这里如果小数不足2位,会以0补足.
                String p = decimalFormat.format(simPicSizeM);//format 返回的是字符串
                tvSimSize.setText(p + "M");
            }
        }
    }
}
