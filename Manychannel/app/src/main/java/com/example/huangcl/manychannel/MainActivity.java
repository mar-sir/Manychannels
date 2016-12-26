package com.example.huangcl.manychannel;

import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    TextView infoTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        infoTxt = (TextView) findViewById(R.id.infoTxt);


        ApplicationInfo appInfo = null;
        try {
            appInfo = this.getPackageManager()
                    .getApplicationInfo(getPackageName(),
                            PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String msg=appInfo.metaData.getString("UMENG_CHANNEL");
        Log.i("chun", msg);
        //println flavor
        if ("debug".equals(msg)) {
            infoTxt.setText("manychannel_debug");
        } else if ("fir.im".equals(msg)) {
            infoTxt.setText("manychannel_firim");
        } else if ("zhushou.360.cn".equals(msg)) {
            infoTxt.setText("manychannel_sanliuling");
        }else if ("wandoujia.com".equals(msg)) {
            infoTxt.setText("manychannel_wandoujia");
        }
    }
}
