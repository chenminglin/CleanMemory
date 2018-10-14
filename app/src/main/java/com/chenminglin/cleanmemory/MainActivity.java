package com.chenminglin.cleanmemory;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    final String TAG = getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //To change body of implemented methods use File | Settings | File Templates.
                ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
                List<ActivityManager.RunningAppProcessInfo> infoList = am.getRunningAppProcesses();
                List<ActivityManager.RunningServiceInfo> serviceInfos = am.getRunningServices(100);

                long beforeMem = getAvailMemory(MainActivity.this);
                Log.d(TAG, "-----------before memory info : " + beforeMem);
                int count = 0;
                if (infoList != null) {
                    for (int i = 0; i < infoList.size(); ++i) {
                        ActivityManager.RunningAppProcessInfo appProcessInfo = infoList.get(i);
                        Log.d(TAG, "process name : " + appProcessInfo.processName);
                        //importance 该进程的重要程度  分为几个级别，数值越低就越重要。
                        Log.d(TAG, "importance : " + appProcessInfo.importance);

                        // 一般数值大于RunningAppProcessInfo.IMPORTANCE_SERVICE的进程都长时间没用或者空进程了
                        // 一般数值大于RunningAppProcessInfo.IMPORTANCE_VISIBLE的进程都是非可见进程，也就是在后台运行着
                        if (appProcessInfo.importance > ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE) {
                            String[] pkgList = appProcessInfo.pkgList;
                            for (int j = 0; j < pkgList.length; ++j) {//pkgList 得到该进程下运行的包名
                                Log.d(TAG, "It will be killed, package name : " + pkgList[j]);
                                am.killBackgroundProcesses(pkgList[j]);
                                count++;
                            }
                        }

                    }
                }

                long afterMem = getAvailMemory(MainActivity.this);
                Log.d(TAG, "----------- after memory info : " + afterMem);
                Toast.makeText(MainActivity.this, "clear " + count + " process, "
                        + (afterMem - beforeMem) + "M", Toast.LENGTH_LONG).show();
            }
        });


        getAppList();
    }


    private void getAppList() {
        PackageManager pm = getPackageManager(); // Return a List of all packages that are installed on the device.
        List<PackageInfo> packages = pm.getInstalledPackages(0);
        for (PackageInfo packageInfo : packages) { // 判断系统/非系统应用
            if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) // 非系统应用
            {
                System.out.println("MainActivity.getAppList, packageInfo=" + packageInfo.packageName);
                ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
                am.killBackgroundProcesses(packageInfo.packageName);
            } else { // 系统应用
            }
        }
    }

    //获取可用内存大小
    private long getAvailMemory(Context context) {
        // 获取android当前可用内存大小
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);
        //mi.availMem; 当前系统的可用内存
        //return Formatter.formatFileSize(context, mi.availMem);// 将获取的内存大小规格化
        Log.d(TAG, "可用内存---->>>" + mi.availMem / (1024 * 1024));
        return mi.availMem / (1024 * 1024);
    }
}
