package com.example.ahiddenbike;

import android.os.Bundle;

import com.example.ahiddenbike.R;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class AboutActivity extends Activity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle("关于");
		setContentView(R.layout.about);
		find_and_modify_button();
	}

	private void find_and_modify_button() {

		ImageView instruction= (ImageView)findViewById(R.id.instruction_button);
		instruction.setOnClickListener(new View.OnClickListener() 
		{
			public void onClick(View arg0) {
				Intent intent = new Intent();
				intent.setClass(AboutActivity.this, InstructionActivity.class);
				startActivity(intent);
			}
		});

		
	}

}
