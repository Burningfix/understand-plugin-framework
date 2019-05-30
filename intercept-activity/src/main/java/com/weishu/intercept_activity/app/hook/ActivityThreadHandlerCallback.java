package com.weishu.intercept_activity.app.hook;

import java.lang.reflect.Field;
import java.util.List;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.weishu.intercept_activity.app.RefInvoke;

/**
 * @author weishu
 * @date 16/1/7
 */
/* package */ class ActivityThreadHandlerCallback implements Handler.Callback {

    Handler mBase;

    public ActivityThreadHandlerCallback(Handler base) {
        mBase = base;
    }

    @Override
    public boolean handleMessage(Message msg) {

        // support android 8
        switch (msg.what) {
            // ActivityThread里面 "LAUNCH_ACTIVITY" 这个字段的值是100
            // 本来使用反射的方式获取最好, 这里为了简便直接使用硬编码
            case 100:   //for API 28以下
                handleLaunchActivity(msg);
                break;
            case 159:   //for API 28
                handleActivity(msg);
                break;
        }

        mBase.handleMessage(msg);
        return true;
    }

    private void handleLaunchActivity(Message msg) {
        // 这里简单起见,直接取出TargetActivity;

        Object obj = msg.obj;
        // 根据源码:
        // 这个对象是 ActivityClientRecord 类型
        // 我们修改它的intent字段为我们原来保存的即可.
        // switch (msg.what) {
        //      case LAUNCH_ACTIVITY: {
        //          Trace.traceBegin(Trace.TRACE_TAG_ACTIVITY_MANAGER, "activityStart");
        //          final ActivityClientRecord r = (ActivityClientRecord) msg.obj;

        //          r.packageInfo = getPackageInfoNoCheck(
        //                  r.activityInfo.applicationInfo, r.compatInfo);
        //         handleLaunchActivity(r, null);


        try {
            // 把替身恢复成真身
            Field intent = obj.getClass().getDeclaredField("intent");
            intent.setAccessible(true);
            Intent raw = (Intent) intent.get(obj);

            Intent target = raw.getParcelableExtra(AMSHookHelper.EXTRA_TARGET_INTENT);
            raw.setComponent(target.getComponent());

//            Field activityInfoField = obj.getClass().getDeclaredField("activityInfo");
//            activityInfoField.setAccessible(true);
//
//            // 根据 getPackageInfo 根据这个 包名获取 LoadedApk的信息; 因此这里我们需要手动填上, 从而能够命中缓存
//            ActivityInfo activityInfo = (ActivityInfo) activityInfoField.get(obj);
//
//            realLaunch(target, activityInfo);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
    private void handleActivity(Message msg) {
        try {
            // 这里简单起见,直接取出TargetActivity;
            Object obj = msg.obj;

            //android.app.servertransaction.ClientTransaction
            Log.i("sanbo.mock2", "handleActivity...obj: " + obj.toString());
            List<Object> mActivityCallbacks = (List<Object>) RefInvoke.getFieldObject(obj, "mActivityCallbacks");

            if (mActivityCallbacks.size() > 0) {
                // 新版本 更新多个Item
                // android.app.servertransaction.LaunchActivityItem
                // android.app.servertransaction.DestroyActivityItem
                // android.app.servertransaction.PauseActivityItem
                // android.app.servertransaction.ResumeActivityItem
                // android.app.servertransaction.StopActivityItem
                String className = "android.app.servertransaction.LaunchActivityItem";
                if (mActivityCallbacks.get(0).getClass().getCanonicalName().equals(className)) {
                    Object object = mActivityCallbacks.get(0);
                    Intent intent = (Intent) RefInvoke.getFieldObject(object, "mIntent");
                    Intent target = intent.getParcelableExtra(AMSHookHelper.EXTRA_TARGET_INTENT);
                    intent.setComponent(target.getComponent());


//                    /**
//                     * 其实不要也能生效。
//                     */
//                    //修改packageName，这样缓存才能命中
//                    ActivityInfo activityInfo = (ActivityInfo) RefInvoke.getFieldObject(object, "mInfo");
//                    Log.i("sanbo.activityInfo", "activityInfo:" + activityInfo);
//                    realLaunch(target, activityInfo);
                }
            }
        } catch (Throwable e) {
        }
    }
}
