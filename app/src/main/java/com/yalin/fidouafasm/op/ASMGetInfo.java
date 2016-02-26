package com.yalin.fidouafasm.op;


import com.yalin.fidouafasm.api.StatusCode;
import com.yalin.fidouafasm.authenticator.Simulator;
import com.yalin.fidouafasm.msg.ASMResponse;
import com.yalin.fidouafasm.msg.obj.AuthenticatorInfo;
import com.yalin.fidouafasm.msg.obj.GetInfoOut;
import com.yalin.fidouafasm.utils.StatLog;

/**
 * Created by YaLin on 2016/1/18.
 */
public class ASMGetInfo extends ASMOperator {
    private static final String TAG = ASMGetInfo.class.getSimpleName();

    private final HandleResultCallback callback;

    public ASMGetInfo(HandleResultCallback callback) {
        this.callback = callback;
    }

    @Override
    public void handle() {
        StatLog.printLog(TAG, "asm getInfo");
        ASMResponse<GetInfoOut> response = new ASMResponse<>();

        GetInfoOut getInfoOut = new GetInfoOut();
        getInfoOut.Authenticators = getAvailableAuthenticator();
        response.responseData = getInfoOut;
        response.statusCode = StatusCode.UAF_ASM_STATUS_OK;
        String getInfoResult = gson.toJson(response);
        StatLog.printLog(TAG, "get info result:" + getInfoResult);
        if (callback != null) {
            callback.onHandleResult(getInfoResult);
        }
    }

    private AuthenticatorInfo[] getAvailableAuthenticator() {
        return Simulator.discover();
    }
}
