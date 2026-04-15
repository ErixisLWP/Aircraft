package com.example.sqlitedemo;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sqlitedemo.bean.UserInfo;
import com.example.sqlitedemo.database.MyDBHelper;
import com.example.sqlitedemo.util.DateUtil;

public class SQLiteWriteActivity extends AppCompatActivity implements OnClickListener {

	private MyDBHelper mHelper;
	private EditText et_name;
	private EditText et_age;
	private EditText et_credits;
	private EditText et_totals;
	private boolean bemale = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sqlite_write);
		et_name = (EditText) findViewById(R.id.et_name);
		et_age = (EditText) findViewById(R.id.et_age);
		et_credits = (EditText) findViewById(R.id.et_credits);
		et_totals = (EditText) findViewById(R.id.et_totals);
		findViewById(R.id.btn_save).setOnClickListener(this);

		ArrayAdapter<String> typeAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_dropdown_item, typeArray);
		Spinner sp_bemale = (Spinner) findViewById(R.id.sp_bemale);
		sp_bemale.setPrompt("请选择性别");
		sp_bemale.setAdapter(typeAdapter);
		sp_bemale.setSelection(0);
		sp_bemale.setOnItemSelectedListener(new TypeSelectedListener());

		mHelper = MyDBHelper.getInstance(this, 2);
		mHelper.openWriteLink();
	}
	
	private String[] typeArray = {"男", "女"};
	class TypeSelectedListener implements OnItemSelectedListener {
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			bemale = (arg2==0)?true:false;
		}
		public void onNothingSelected(AdapterView<?> arg0) {
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mHelper.closeLink();
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.btn_save) {
			String name = et_name.getText().toString();
			String age = et_age.getText().toString();
			String credits = et_credits.getText().toString();
			String totals = et_totals.getText().toString();
			if (name==null || name.length()<=0) {
				showToast("请先填写姓名");
				return;
			}
			if (age==null || age.length()<=0) {
				showToast("请先填写年龄");
				return;
			}
			if (credits==null || credits.length()<=0) {
				showToast("请先填写积分");
				return;
			}
			if (totals==null || totals.length()<=0) {
				showToast("请先填写购买总额");
				return;
			}
			
			UserInfo info = new UserInfo();
			info.name = name;
			info.age = Integer.parseInt(age);
			info.sex = bemale;
			info.credits = Long.parseLong(credits);
			info.totals = Float.parseFloat(totals);
			info.update_time = DateUtil.getNowDateTime("yyyy-MM-dd HH:mm:ss");
			mHelper.insert(info);
			showToast("数据已写入SQLite数据库");
		}
	}
	private void showToast(String desc) {
		Toast.makeText(getApplicationContext(), desc, Toast.LENGTH_SHORT).show();
	}
}
