package com.example.cxp.crosswalkdemo;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.os.Build;
import android.view.View;
import android.widget.Button;

import org.xwalk.core.XWalkPreferences;
import org.xwalk.core.XWalkResourceClient;
import org.xwalk.core.XWalkView;
import org.xwalk.core.XWalkSettings;
import org.xwalk.core.JavascriptInterface;

import java.io.File;

public class MainActivity extends Activity {
    XWalkView xWalkWebView;
    public String fileFullName;//照相后的照片的全整路径
    private boolean fromTakePhoto; //是否是从摄像界面返回的webview
    final Handler mHandler = new Handler();
    public String allPath="test";
    public String allName="name.jpg";
    private Button rr;
    private Button ww;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        xWalkWebView=(XWalkView)findViewById(R.id.xWalkWebView);
        //xWalkWebView.loadUrl("http://sina.com.cn");
        xWalkWebView.loadUrl("file:///android_asset/index.html");
        XWalkSettings settings = xWalkWebView.getSettings();
        settings.setDomStorageEnabled(true);
        settings.setCacheMode(XWalkSettings.LOAD_DEFAULT);
        settings.setAllowFileAccess(true);
        settings.setDatabaseEnabled(true);
        settings.setJavaScriptEnabled(true);
        rr = (Button) findViewById(R.id.rr);
        ww = (Button) findViewById(R.id.ww);
        rr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getLocalStorageUserKey();
            }
        } );
        ww.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                w2();
            }
        } );
        // turn on debugging
        XWalkPreferences.setValue(XWalkPreferences.REMOTE_DEBUGGING, true);


        xWalkWebView.setResourceClient(new XWalkResourceClient(xWalkWebView){
            @Override
            public void onLoadFinished(XWalkView view, String url) {
                super.onLoadFinished(view, url);
                Log.v("TAG","LoadFinished");
            }
            @Override
            public void onLoadStarted(XWalkView view, String url) {
                super.onLoadStarted(view, url);
                Log.v("TAG","LoadStarted");
            }

        });
        // 获取到UserAgentString
        String userAgent = settings.getUserAgentString();
        // 打印结果
        Log.v("TAG", "User Agent:" + userAgent);
        xWalkWebView.addJavascriptInterface(new JsInterface(), "NativeInterface");
        xWalkWebView.setUIClient(new MyXWalkUIClient(xWalkWebView));
        xWalkWebView.onDestroy();

    }
    class JsInterface {
        public JsInterface() {
        }
        @JavascriptInterface
        public String sayHello() {
            Log.v("TAG","Hello World!");
            return "Hello World!";
        }
        @JavascriptInterface
        public String clickOnAndroid(){
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    fromTakePhoto  = true;
                    //调用 启用摄像头的自定义方法
                    //takePhoto("testimg" + Math.random()*1000+1 + ".jpg");
                    allName=Math.random()*1000+1 + ".jpg";
                    takePhoto(allName);
                    Log.v("fileFullName" ,fileFullName);
                    allPath=fileFullName;
                }
            });
            return fileFullName;
        }
        @JavascriptInterface
        public void getUserKey(String userKey){
            Log.v("WebViewFragment","读取到userKey : " + userKey);
            //已经拿到值，进行相关操作
        }
    }
    /*
     * 调用摄像头的方法
     */
    public void takePhoto(String filename) {
        Log.v("test2","----start to take photo2 ----");
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_MEDIA_TITLE, "TakePhoto");
        //判断是否有SD卡
        String sdDir = null;
        boolean isSDcardExist = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        if(isSDcardExist) {
            sdDir = Environment.getExternalStorageDirectory().getAbsolutePath();
        }else {
            sdDir = Environment.getRootDirectory().getAbsolutePath();
        }
        //确定相片保存路径
        String targetDir = sdDir + "/" + "webview_camera";
        File file = new File(targetDir);
        if (!file.exists()) {
            file.mkdirs();
        }
        fileFullName = targetDir + "/" + filename;
        Log.v("fileFullName2" ,fileFullName);
        //初始化并调用摄像头
        intent.putExtra(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        //判断是否是AndroidN以及更高的版本
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            File outputFile = new File(fileFullName);
            if (!outputFile.getParentFile().exists()) {
                outputFile.getParentFile().mkdir();
            }
            Uri contentUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".fileProvider", outputFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, contentUri);
        }else {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(fileFullName)));
        }

        startActivityForResult(intent, 1);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.v("requestCode" ,requestCode+"");
        Log.v("resultCode" ,resultCode+"");
        //String name = data.getStringExtra("fileFullName");
        Log.v("fileFullName3" ,fileFullName+"");
        if (fromTakePhoto && requestCode ==1 && resultCode ==-1) {
            xWalkWebView.loadUrl("javascript:wave2('" + fileFullName + "')");
        }else {
            xWalkWebView.loadUrl("javascript:wave2('Please take your photo')");
        }
        fromTakePhoto = false;
        super.onActivityResult(requestCode, resultCode, data);
    }
    private void getLocalStorageUserKey() {
        xWalkWebView.loadUrl("javascript:(function(){ var localStorage = window.localStorage; window.NativeInterface.getUserKey(localStorage.getItem('userKey'))})()");
    }
    public void w2(){
        String userKey = "123";
        String js = "window.localStorage.setItem('userKey','" + userKey + "');";
        String jsUrl = "javascript:(function(){ var localStorage = window.localStorage; localStorage.setItem('userKey','" + userKey + "')})()";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            xWalkWebView.evaluateJavascript(js, null);
        } else {
            xWalkWebView.loadUrl(jsUrl);
            //xWalkWebView.reload(0);
        }
    }
}
