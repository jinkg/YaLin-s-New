package com.yalin.fidouafasm.op;

import android.app.Activity;

import com.yalin.fidouafasm.api.StatusCode;
import com.yalin.fidouafasm.authenticator.Simulator;
import com.yalin.fidouafasm.msg.ASMRequest;
import com.yalin.fidouafasm.msg.ASMResponse;
import com.yalin.fidouafasm.msg.obj.AuthenticateIn;
import com.yalin.fidouafasm.msg.obj.AuthenticateOut;
import com.yalin.fidouafasm.utils.StatLog;

public class ASMAuth extends ASMOperator implements Simulator.BiometricsAuthResultCallback {
    private static final String TAG = ASMAuth.class.getSimpleName();
    private final ASMRequest request;
    private final Activity activity;
    private final HandleResultCallback callback;

    public ASMAuth(Activity activity, ASMRequest request, HandleResultCallback callback) {
        if (!(request.args instanceof AuthenticateIn)) {
            throw new IllegalStateException("asm request must has a AuthenticateIn object");
        }
        this.activity = activity;
        this.request = request;
        this.callback = callback;
    }

    @Override
    public void handle() {
        StatLog.printLog(TAG, "asm auth show biometrics auth");
        Simulator.getInstance(request.authenticatorIndex).showBiometricsAuth(activity, this);
    }

    @Override
    public void onAuthSuccess(Simulator simulator, String biometricsId) {
        StatLog.printLog(TAG, "asm auth biometrics auth success,prepare asmRequest");
        ASMResponse<AuthenticateOut> response = new ASMResponse<>();
        try {
            AuthenticateOut authenticateOut = Simulator.authenticate(biometricsId, (AuthenticateIn) request.args, request.authenticatorIndex);
            response.statusCode = StatusCode.UAF_ASM_STATUS_OK;
            response.responseData = authenticateOut;
        } catch (Exception e) {
            response.statusCode = StatusCode.UAF_ASM_STATUS_ACCESS_DENIED;
        }
        if (callback != null) {
            callback.onHandleResult(gson.toJson(response));
        }
    }

    @Override
    public void onAuthFailed(Simulator simulator) {
        StatLog.printLog(TAG, "asm auth biometrics auth failed");
    }
}
