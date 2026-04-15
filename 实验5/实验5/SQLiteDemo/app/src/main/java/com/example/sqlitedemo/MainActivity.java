package com.example.sqlitedemo;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_sqlite_write).setOnClickListener(new BtnOnClick());
        findViewById(R.id.btn_sqlite_read).setOnClickListener(new BtnOnClick());
    }

    private class BtnOnClick implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.btn_sqlite_write) {
                Intent intent = new Intent(MainActivity.this, SQLiteWriteActivity.class);
                startActivity(intent);
            } else if (v.getId() == R.id.btn_sqlite_read) {
                Intent intent = new Intent(MainActivity.this,SQLiteReadActivity.class);
                startActivity(intent);
            }
        }
    }

}
