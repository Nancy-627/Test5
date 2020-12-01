package com.example.test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private EditText et_name;
    private EditText et_password;
    private Button btn_submit;
    private TextView tv_result;

    Handler handler = new Handler() {
        @SuppressLint("WrongConstant")
        public void handleMessage(android.os.Message msg) {
            tv_result.setText(msg.obj + "");
            Toast.makeText(MainActivity.this, (String) msg.obj, 0).show();
        };
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        et_name = (EditText) findViewById(R.id.et_name);
        et_password = (EditText) findViewById(R.id.et_pass);

        tv_result = (TextView) findViewById(R.id.tv_result);

        btn_submit = (Button) findViewById(R.id.btn_submit);
        // 用get的方式请求，把请求内容拼接到url后面。
        // Log.i("MainActivity", path);
        btn_submit.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                final String username = et_name.getText().toString().trim();
                final String password = et_password.getText().toString().trim();
                Thread t = new Thread() {
                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        try {
                            // final String s =
                            // "http://192.168.1.103:8080/testServlet/ServletDemo?username=aaaa&password=1234";
                            final String path = "http://39.107.143.247:18080/user/login";
                            URL url = new URL(path);
                            HttpURLConnection conn = (HttpURLConnection) url
                                    .openConnection();
                            conn.setRequestMethod("POST");
                            String text = "username="
                                    + URLEncoder.encode(username) + "&password="
                                    + URLEncoder.encode(password);
                            conn.setReadTimeout(5000);
                            conn.setReadTimeout(5000);
                            conn.setRequestProperty("Content-Type",
                                    "application/x-www-form-urlencoded");
                            conn.setRequestProperty("Content-Length",
                                    text.length() + "");
                            conn.setDoOutput(true);
                            conn.setDoInput(true);
                            OutputStream os=conn.getOutputStream();
                            //把提交的文件写到流中；
                            os.write(text.getBytes());

                            if (conn.getResponseCode() == 200) {
                                InputStream is = conn.getInputStream();
                                String s = Utils.getTextFromStream(is);
                                Log.i("MainActivity", text);
                                // 发送消息，把服务器返回的本文弹出吐司显示
                                Message msg = handler.obtainMessage();
                                msg.obj = s;
                                handler.sendMessage(msg);
                            }
                        } catch (MalformedURLException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        super.run();
                    }
                };
                t.start();
            }

        });

    }

}