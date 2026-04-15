package com.example.handlerdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    public TextView mTextView;
    public Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextView = (TextView) findViewById(R.id.show);
        mTextView.setText("欢迎进入APP！！！");

        // 步骤2：在主线程中创建Handler实例
        mHandler = new Mhandler();

        // 采用继承Thread类实现多线程演示
        new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(4000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // 步骤3：创建所需的消息对象
                Message msg = Message.obtain();
                msg.what = 1; // 消息标识
                msg.obj = "A"; // 消息内存存放

                // 步骤4：在工作线程中通过Handler发送消息到消息队列中
                mHandler.sendMessage(msg);
            }
        }.start();
        // 步骤5：开启工作线程（同时启动了Handler）

        // 此处用2个工作线程展示
        new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(8000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // 通过sendMessage（）发送
                // a. 定义要发送的消息
                Message msg = Message.obtain();
                msg.what = 2; //消息的标识
                msg.obj = "B"; // 消息的存放
                // b. 通过Handler发送消息到其绑定的消息队列
                mHandler.sendMessage(msg);
            }
        }.start();
    }

    // 步骤1：（自定义）新创建Handler子类(继承Handler类) & 复写handleMessage（）方法
    class Mhandler extends Handler {

        // 通过复写handlerMessage() 从而确定更新UI的操作
        @Override
        public void handleMessage(Message msg) {
            // 根据不同线程发送过来的消息，执行不同的UI操作
            // 根据 Message对象的what属性 标识不同的消息
            switch (msg.what) {
                case 1:
                    mTextView.setText("执行了线程1的UI操作");
                    break;
                case 2:
                    mTextView.setText("执行了线程2的UI操作");
                    break;
            }
        }
    }
}