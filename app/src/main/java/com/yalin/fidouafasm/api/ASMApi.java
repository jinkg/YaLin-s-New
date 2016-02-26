package com.yalin.fidouafasm.api;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;

/**
 * Created by YaLin on 2016/1/13.
 */
public class ASMApi {
    public static void doOperation(Activity activity, int requestCode, String asmMessage) {
        if (TextUtils.isEmpty(asmMessage)) {
            throw new IllegalArgumentException("asmMessage can not be null");
        }
        Intent intent = ASMIntent.getASMOperationIntent(asmMessage);
        activity.startActivityForResult(intent, requestCode);
    }

    public static void doDiscover(Activity activity, int requestCode, String asmMessage) {
        if (TextUtils.isEmpty(asmMessage)) {
            throw new IllegalArgumentException("asmMessage can not be null");
        }
        Intent intent = ASMIntent.getASMOperationIntent(asmMessage);
        activity.startActivityForResult(intent, requestCode);
    }
}
