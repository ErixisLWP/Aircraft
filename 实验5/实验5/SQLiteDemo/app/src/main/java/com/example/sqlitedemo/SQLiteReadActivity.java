package com.example.sqlitedemo;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.sqlitedemo.bean.UserInfo;
import com.example.sqlitedemo.database.MyDBHelper;
import java.util.ArrayList;
import java.util.HashMap;

public class SQLiteReadActivity extends AppCompatActivity {
	private MyDBHelper myHelper;
	private ListView listv;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sqlite_read);
		//初始化ListView
		listv = (ListView) this.findViewById(R.id.ListView02);
		myHelper = MyDBHelper.getInstance(this, 2);
		myHelper.openReadLink();
		if ((readSQLite() == null)|(readSQLite().size()<=0)) {
			showToast("数据库为空。");
			finish();
		} else {
			MyAdapter BAdapter = new MyAdapter(this);
			//为ListView绑定Adapter
			listv.setAdapter(BAdapter);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		myHelper.closeLink();
	}

	private void showToast(String desc) {
		Toast.makeText(this, desc, Toast.LENGTH_SHORT).show();
	}

	/**  读取数据库数据的方法 **/
	protected ArrayList<UserInfo> readSQLite() {
		if (myHelper == null) {
			showToast("数据库连接为空。");
			return null;
		}
		ArrayList<UserInfo> userArray = myHelper.query("1=1");
		return  userArray;
	}

	/**  获得数据的方法 **/
	private ArrayList<HashMap<String, String>> getData() {
		ArrayList<HashMap<String, String>> listItem = new ArrayList<HashMap<String, String>>();
		String desc;
		ArrayList<UserInfo> uArray=readSQLite();
		setTitle(String.format("数据库查询到%d条记录，详情如下：", uArray.size()));
		/** 为数组添加数据*/
		for (int i = 0; i < uArray.size(); i++) {
			HashMap<String, String> tvitem = new HashMap<String, String>();
			desc = "";
			UserInfo info = uArray.get(i);
			desc = String.format("第%d个用户：", i + 1);
			desc = String.format("%s\n　姓名：%s", desc, info.name);
			desc = String.format("%s\n　年龄：%d", desc, info.age);
			if (info.sex)
				desc = String.format("%s\n　性别：男", desc);
			else
				desc = String.format("%s\n　性别：女", desc);
			desc = String.format("%s\n　积分：%d", desc, info.credits);
			desc = String.format("%s\n　购买总额：%8.2f", desc, info.totals);
			desc = String.format("%s\n　更新时间：%s", desc, info.update_time);
			tvitem.put("ItemId", String.valueOf(info.userid));
			tvitem.put("ItemText", desc);
			listItem.add(tvitem);
		}
		return listItem;
	}

	/**	 定义适配器 **/
	private class MyAdapter extends BaseAdapter {
		//声明一个LayoutInfalter对象用来导入布局
		private LayoutInflater mInflater;
		//声明HashMap类型的数组
		private  ArrayList<HashMap<String, String>> datas;

		public MyAdapter(Context context) {
			this.mInflater = LayoutInflater.from(context);
			this.datas = getData();
		}

		@Override
		public int getCount() {
			return datas.size();//返回数组的长度
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		/**	 动态生成每个下拉项对应的View，每个下拉项一个TextView，和一个按钮组成 **/
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			//ViewHolder对象：缓存行内控件，避免重复findViewById
			ViewHolder holder;
			//1.视图复用核心逻辑：判断当前行View是否为首次创建
			if (convertView == null) {
				//首次创建：加载行布局，readlistitem是单行的布局文件
				convertView = mInflater.inflate(R.layout.sqlite_readlistitem, null);
				holder = new ViewHolder();
				//得到各个控件的对象
				holder.text = (TextView) convertView.findViewById(R.id.iteminfo);
				holder.btn = (Button) convertView.findViewById(R.id.itembtn);
				//绑定ViewHolder对象
				convertView.setTag(holder);
			} else {
				//非首次创建：直接复用已缓存的ViewHolder，无需重新查找控件
				holder = (ViewHolder) convertView.getTag();
			}
			//2.数据绑定：将当前行的数据填充到控件中
			HashMap<String, String> map = datas.get(position);
			holder.text.setText(map.get("ItemText"));
			holder.btn.setFocusable(false);
			//3.处理按钮点击事件
			holder.btn.setOnClickListener(v ->  {
				String[] whereArgs = {datas.get(position).get("ItemId")};
				myHelper.openWriteLink();
				int deleteCount = myHelper.delete("u_id=?", whereArgs);
				if(deleteCount > 0) {
					datas = getData();        //重新获取数据
					notifyDataSetChanged();    //刷新ListView
				}
			});
			//返回组装好的行View，展示到ListView中
			return convertView;
		}
	}
	/**  定义ViewHolder类，存放控件 **/
	private final class ViewHolder {
		private TextView text;
		private Button btn;
	}
}
