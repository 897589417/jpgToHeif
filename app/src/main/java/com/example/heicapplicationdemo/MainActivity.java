package com.example.heicapplicationdemo;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.heifwriter.HeifWriter;

import com.bumptech.glide.Glide;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.tbruyelle.rxpermissions.Permission;
import com.tbruyelle.rxpermissions.RxPermissions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import rx.functions.Action1;

public class MainActivity extends AppCompatActivity {

    private ImageView mImg1;
    private TextView mTv1;
    private Button mBtn1;
    private ImageView mImg2;
    private TextView mTv2;
    private Button mBtn2;
    private LocalMedia imgUrl = null;

    private Context context = this;

    private ImageView mImg3;
    private TextView mTv3;
    private Button mBtn3;
    private String destination1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mImg1 = findViewById(R.id.img1);
        mTv1 = findViewById(R.id.tv1);
        mBtn1 = findViewById(R.id.btn1);
        mImg2 = findViewById(R.id.img2);
        mTv2 = findViewById(R.id.tv2);
        mBtn2 = findViewById(R.id.btn2);
        mImg3 = findViewById(R.id.img3);
        mTv3 = findViewById(R.id.tv3);
        mBtn3 = findViewById(R.id.btn3);


        mBtn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkSeparatePerm();
            }
        });
        mBtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                jpg2Heif();
            }
        });
        mBtn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                heif2Jpg();
            }
        });

    }

    private void heif2Jpg() {
        // Using HEIFWriter from Google
        // https://developer.android.com/reference/androidx/heifwriter/HeifWriter

       /* val heif = HEIF()
        // Step 1: Load file
        heif.load("HEIC.heic")
        // Step 2: Check type of HEIF format, cause it have many types: Still Image, Grid Image ... (Apple is using GridImage type for that format)
        when (heif.primaryImage) {
            is GridImageItem -> {}
            is IdentityImageItem -> {}
            is OverlayImageItem -> {}
            is HEVCImageItem -> {}
            is AVCImageItem -> {}
        }

        // Step 3: In-case this is GridImageItem
        // Get size width, height
        val originalWidth = primaryImage.size.width
        val originalHeight = primaryImage.size.height
        // Getting original rotation degree of Original Image file
        val rotationDegree =
                (heif.itemProperties.findLast { it is RotateProperty } as? RotateProperty)?.rotation?.value
                ?: 0
        // Apple is using 48 tiles and join to 1 images, parse of its and then convert its to HEVC bitstream by FFMPEG
        for (rowIndex in 0 until primaryImage.rowCount) {
            for (columnIndex in 0 until primaryImage.columnCount) {
                // Getting the tile image based column / row
                val hevcImageItem = primaryImage.getImage(columnIndex, rowIndex) as HEVCImageItem
                // Getting decoder config and then using mobile-ffmpeg to write to HEVC bitstream in local storage.
             ....
            }

            // After getting 48 tiles as bitstream files, join its and merge into 1 files by ffmpeg.
        	....
            // Loading Color profiles and attach it.
        	....
            // Convert BitStream to JPG.JPEG.PNG file by FFMPEG
        	....
            // Loading Exifdata and attach it.
					....
            // Delete temp files and finish convert.
        }*/

    }

    private void jpg2Heif() {
        Bitmap bitmap;
        bitmap = BitmapFactory.decodeFile(imgUrl.getRealPath());
        Log.e("cxx", "获取bitmap成功");
        int imageHeight = bitmap.getHeight();
        int imageWidth = bitmap.getWidth();
        Log.e("cxx", "获取宽高");
//        destination1 = getExternalFilesDir("/").getAbsolutePath() + "/photo2.heic";
        Random random = new Random();
        int fileName2 = Integer.valueOf(random.nextInt(Integer.MAX_VALUE));
        String destinations = Environment.getExternalStorageDirectory() + "/Pictures/"+fileName2+".heic";
        Log.e("cxx", "生成路径：" + destinations);
        try {
            HeifWriter heifWriter = new HeifWriter.Builder(destinations, imageWidth, imageHeight, HeifWriter.INPUT_MODE_BITMAP).setQuality(90).build();
            heifWriter.start();
            heifWriter.addBitmap(bitmap);
            heifWriter.stop(0);
            heifWriter.close();
            Log.e("cxx", "生成完毕：");
            long mbSize = getFileSize(destinations);
            mTv2.setText(destinations+"--大小:"+mbSize);
            Glide.with(context).load(destinations).into(mImg2);
            Log.e("cxx", "加载完毕");

            File f = new File(destinations);
            // 通知图库更新
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri uri = Uri.fromFile(f);
            intent.setData(uri);
            context.sendBroadcast(intent);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //分别请求多个权限
    private void checkSeparatePerm() {
        RxPermissions.getInstance(this).requestEach(Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE).subscribe(new Action1<Permission>() {
            @Override
            public void call(Permission permission) {
                if (permission.name.equals(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    //当WRITE_EXTERNAL_STORAGE权限获取成功时，t.granted=true
                    Log.i("permissions", Manifest.permission.READ_EXTERNAL_STORAGE + "：" + permission.granted);
                    if (permission.granted) {
                        checkPic();
                    }
                }
                if (permission.name.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    //当CAMERA权限获取成功时，t.granted=true
                    Log.i("permissions", Manifest.permission.WRITE_EXTERNAL_STORAGE + "：" + permission.granted);
                }
            }
        });
    }

    //————————————————
//    版权声明：本文为CSDN博主「chaseDreamer_」的原创文章，遵循CC 4.0 BY-SA版权协议，转载请附上原文出处链接及本声明。
//    原文链接：https://blog.csdn.net/chaseDreamer_/article/details/121077531
    private void checkPic() {
        //maxSelectNum()选择图片的数量
        PictureSelector.create(this)
                .openGallery(PictureMimeType.ofImage())
                .imageEngine(GlideEngine.createGlideEngine())
                .maxSelectNum(1)
                .forResult(PictureConfig.CHOOSE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case PictureConfig.CHOOSE_REQUEST: {
                    List<LocalMedia> result = PictureSelector.obtainMultipleResult(data);
                    if (result.size() <= 0) {
                        return;
                    }
                    imgUrl = result.get(0);
                    long mbSize = getFileSize(imgUrl.getRealPath());
                    mTv1.setText(imgUrl.getFileName() + "--大小：" + mbSize+"mb");
                    Glide.with(this).load(result.get(0).getPath()).into(mImg1);

          /*          Log.e(
                            "cxx",
                            "result:"+
                                    result.size.toString()+
                                    "--mUpdateUserMsgImgHeadCir:"+
                                    result[0].path
                    )*/
                }
            }
        }
    }

    private long getFileSize(String realPath) {
        File file = new File(realPath);
        long size = 0;
        long mbSize = 0;
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            size = fis.available();
            mbSize = size/1024/1024;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return  mbSize;
    }
//————————————————
//    版权声明：本文为CSDN博主「chaseDreamer_」的原创文章，遵循CC 4.0 BY-SA版权协议，转载请附上原文出处链接及本声明。
//    原文链接：https://blog.csdn.net/chaseDreamer_/article/details/121035850
}