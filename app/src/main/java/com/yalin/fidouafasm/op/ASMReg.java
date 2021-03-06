package com.yalin.fidouafasm.op;

import android.app.Activity;

import com.yalin.fidouafasm.api.StatusCode;
import com.yalin.fidouafasm.authenticator.Simulator;
import com.yalin.fidouafasm.msg.ASMRequest;
import com.yalin.fidouafasm.msg.ASMResponse;
import com.yalin.fidouafasm.msg.obj.RegisterIn;
import com.yalin.fidouafasm.msg.obj.RegisterOut;
import com.yalin.fidouafasm.utils.StatLog;


public class ASMReg extends ASMOperator implements Simulator.BiometricsAuthResultCallback {
    public static final String TAG = ASMReg.class.getSimpleName();
    private final ASMRequest request;
    private final Activity activity;
    private final HandleResultCallback callback;

    public ASMReg(Activity activity, ASMRequest request, HandleResultCallback callback) {
        if (!(request.args instanceof RegisterIn)) {
            throw new IllegalStateException("asm request must has a RegisterIn object");
        }
        this.activity = activity;
        this.request = request;
        this.callback = callback;
    }

    @Override
    public void handle() {
        StatLog.printLog(TAG, "asm reg show biometrics auth");
        Simulator.getInstance(request.authenticatorIndex).showBiometricsAuth(activity, this);
    }

    @Override
    public void onAuthSuccess(Simulator simulator, String biometricsId) {
        StatLog.printLog(TAG, "asm reg biometrics auth success,prepare asmRequest");
        ASMResponse<RegisterOut> response = new ASMResponse<>();
        try {
            RegisterOut registerOut = Simulator.register(biometricsId, (RegisterIn) request.args, request.authenticatorIndex);
            if (registerOut != null) {
                response.responseData = registerOut;
                response.statusCode = StatusCode.UAF_ASM_STATUS_OK;
            } else {
                response.statusCode = StatusCode.UAF_ASM_STATUS_ERROR;
            }
        } catch (Exception e) {
            response.statusCode = StatusCode.UAF_ASM_STATUS_ERROR;
        }
        StatLog.printLog(TAG, "asm response is:" + gson.toJson(response));
        if (callback != null) {
            callback.onHandleResult(gson.toJson(response));
        }
    }

    @Override
    public void onAuthFailed(Simulator simulator) {
        StatLog.printLog(TAG, "asm reg biometrics auth failed");
    }
}
