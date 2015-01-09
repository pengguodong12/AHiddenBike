package com.example.ahiddenbike;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.os.Bundle;
import android.os.Environment;

import com.example.ahiddenbike.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class SettingActivity extends Activity {

	private RadioGroup mRadioGroup;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting);
		//setTitle("设置");
		mRadioGroup = (RadioGroup) findViewById(R.id.menu);
		mRadioGroup.check(R.id.music1);
		try {
			if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){  //如果SD卡已准备好									
				File sdCardDir = Environment.getExternalStorageDirectory();  //得到SD卡根目录
				File BuildDir = new File(sdCardDir, getResources().getString(R.string.dir));   //打开HiddenBike目录，如不存在则生成
				if (BuildDir.exists()==false)
					BuildDir.mkdirs();
				File songFile =new File(BuildDir, getResources().getString(R.string.song));  //新建文件句柄
				if (songFile.exists()) {
					FileInputStream stream = new FileInputStream(songFile);
					if (stream.read()==9)
						mRadioGroup.check(R.id.music2);
					stream.close();
				}
			}
		}catch(IOException e) {
		}
		find_and_modify_button();
	}

	private void find_and_modify_button() {

		ImageView cancel = (ImageView) findViewById(R.id.cancel_button);
		cancel.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				finish();
			}
		});

		ImageView sure = (ImageView) findViewById(R.id.sure_button);
		sure.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				try {
				if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){  //如果SD卡已准备好									
					File sdCardDir = Environment.getExternalStorageDirectory();  //得到SD卡根目录
					File BuildDir = new File(sdCardDir, getResources().getString(R.string.dir));   //打开HiddenBike目录，如不存在则生成
					if (BuildDir.exists()==false)
						BuildDir.mkdirs();
					File songFile =new File(BuildDir, getResources().getString(R.string.song));  //新建文件句柄
					FileOutputStream stream = new FileOutputStream(songFile);
					if (mRadioGroup.getCheckedRadioButtonId()==R.id.music1)
						stream.write(3);
					else stream.write(9);
					stream.close();
				}
				}catch(IOException e) {
				}
				finish();
			}
		});
	}

}
