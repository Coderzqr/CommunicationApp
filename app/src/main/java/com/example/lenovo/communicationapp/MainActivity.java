package com.example.lenovo.communicationapp;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class MainActivity extends AppCompatActivity {

    Socket client=null;
    String usernamestr;
    String sendmessage;
    EditText username;
    EditText txtmessage;
    Button btnsend;
    BufferedWriter bw = null;
    Button btnconnect;
    TextView chatdialog;
    TextView txthint;
    ScrollView slv;
    private Handler myhandler=new Handler(){
        public void handleMessage(Message msg) {
                         switch (msg.what) {
                             case 0:
                                 chatdialog.setText(chatdialog.getText().toString()+"\n对方："+(String)msg.obj);
                                 break;
                             case 1:
                                 chatdialog.setText(chatdialog.getText().toString()+"\n服务器："+(String)msg.obj);
                                 break;
                             case 2:
                                 btnconnect.setVisibility(View.GONE);
                                 break;
                             case 3:
                                 btnconnect.setVisibility(View.VISIBLE);
                                 break;
                             case 4:
                                 txthint.setText("在线用户列表：");
                                 break;
                             default:
                                     break;
                            }
                    };
     };
    private boolean isDestroyed = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        username=findViewById(R.id.username);
        btnconnect=findViewById(R.id.btconnect);
        chatdialog=findViewById(R.id.ChatDialog);
        btnsend=findViewById(R.id.btn_chat_message_send);
        txtmessage=findViewById(R.id.et_chat_message);
        slv=findViewById(R.id.slv);
        txthint=findViewById(R.id.txthint);
    }
    private void destroy()  {
        if (isDestroyed) {
            return;
        }
        // 回收资源
        try {
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        isDestroyed = true;
    }
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if(keyCode == KeyEvent.KEYCODE_BACK){
            moveTaskToBack(true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    protected void onPause() {
        super.onPause();
        if (isFinishing()) {
            destroy();
        }
    }
    public void onDestroy() {
        super.onDestroy();
        destroy();//需要在onDestroy方法中进一步检测是否回收资源等。

    }
    public void btnconnectClick(View v){
        usernamestr=username.getText().toString();

            new Thread(){
                public void run() {
                    try {
                        client = new Socket("47.110.133.172", 1525);
                        bw = new BufferedWriter(new OutputStreamWriter(client.getOutputStream(), "UTF-8"));
                        client.setKeepAlive(true);
                        handmessage(2,"");
                        handmessage(4,"");
                        handmessage(1,"成功连接服务器！");
                        bw.write(usernamestr);
                        bw.newLine();
                        bw.flush();
                        handmessage(1,"上线成功！您的用户名为："+usernamestr);
                    } catch (UnknownHostException e) {
                        // TODO Auto-generated catch block
                        handmessage(1,"未知服务器");
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        handmessage(1,e.getMessage());
                    }
                    // TODO Auto-generated method stub
                    try {
                        while(true){
                            BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream(),"UTF-8"));
                            String message=br.readLine();
                            if(message==null) throw new IOException();
                            handmessage(0,message);
                        }
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        try {
                            client.close();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                        handmessage(1,"服务器异常，断开连接");
                        handmessage(3,"");
                        return;
                    }
                }
            }.start();
           // new Receive(client).start();


    }
    private void handmessage(int whatnum,String message){
        Message msg=new Message();
        msg.what=whatnum;
        msg.obj=message;
        myhandler.sendMessage(msg);
    }
    public void btnsendClick(View v){
        slv.fullScroll(ScrollView.FOCUS_DOWN);
        String targetuser=username.getText().toString();
        String tempmessage=txtmessage.getText().toString();
        sendmessage=targetuser+"|"+tempmessage;
        txtmessage.setText("");
        chatdialog.setText(chatdialog.getText().toString()+"\n我："+tempmessage);
        new Thread(){
            public void run(){
                try {
                    bw.write(sendmessage);
                    bw.newLine();
                    bw.flush();
                }
                catch(IOException e1) {
                    // TODO Auto-generated catch block
                    handmessage(1,"服务器异常，请尝试重新连接");
                    return;
                }
                catch(Exception e2){
                    handmessage(1,e2.getMessage());
                }
            }
        }.start();
    }
}
