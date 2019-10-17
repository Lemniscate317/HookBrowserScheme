package com.l.hookbrowserscheme.hook;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.util.Log;

import com.l.hookbrowserscheme.utils.UrlUtils;

import java.lang.reflect.Field;
import java.util.List;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookLogic implements IXposedHookLoadPackage, IXposedHookZygoteInit {
    private final static String modulePackageName = HookLogic.class.getPackage().getName();
    private XSharedPreferences sharedPreferences;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        if ("com.android.browser".equals(loadPackageParam.packageName)) {
            log("enter");

            ClassLoader classLoader = loadPackageParam.classLoader;
            Class<?> aClass = classLoader.loadClass("com.android.browser.UrlHandler");
            XposedBridge.hookAllMethods(aClass, "shouldOverrideUrlLoading", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);

//                    log("2");
                    Object[] args = param.args;
                    String url = (String) args[2];

                    Object thisObject = param.thisObject;
                    Field mActivity = thisObject.getClass().getDeclaredField("mActivity");
                    mActivity.setAccessible(true);
                    Activity activity = (Activity) mActivity.get(thisObject);

                    if (activity != null) {
//                        log("not null");
                        Intent intent = Intent.parseUri(url, 1);
                        if (UrlUtils.ACCEPTED_URI_SCHEMA.matcher(url).matches() && !isSpecializedHandlerAvailable(intent, activity)) {
                            return;
                        }
                        log(url);
                    }

                }
            });

//            XposedBridge.hookAllMethods(aClass, "startActivityForUrl", new XC_MethodHook() {
//                @Override
//                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                    super.afterHookedMethod(param);
//                    boolean result = (boolean) param.getResult();
//                    log(result + "");
//                }
//            });
        }
    }

    private boolean isSpecializedHandlerAvailable(Intent intent, Activity activity) {
        List<ResolveInfo> handlers = activity.getPackageManager().queryIntentActivities(intent, 64);
        if (handlers == null || handlers.size() == 0) {
            return false;
        }
        for (ResolveInfo resolveInfo : handlers) {
            IntentFilter filter = resolveInfo.filter;
            if (filter != null && (filter.countDataAuthorities() != 0 || filter.countDataPaths() != 0)) {
                return true;
            }
        }
        return false;
    }


    private void log(String msg) {
        Log.e("HookBrowserScheme", msg);
    }

    @Override
    public void initZygote(IXposedHookZygoteInit.StartupParam startupParam) {
        this.sharedPreferences = new XSharedPreferences(modulePackageName, "default");
        XposedBridge.log(modulePackageName + " initZygote");
    }
}
