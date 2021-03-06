package com.yalin.fidouafasm.authenticator;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.util.Base64;

import com.yalin.fidouafasm.crypto.Asn1;
import com.yalin.fidouafasm.crypto.KeyCodec;
import com.yalin.fidouafasm.crypto.NamedCurve;
import com.yalin.fidouafasm.crypto.SHA;
import com.yalin.fidouafasm.msg.obj.AuthenticateIn;
import com.yalin.fidouafasm.msg.obj.AuthenticateOut;
import com.yalin.fidouafasm.msg.obj.AuthenticatorInfo;
import com.yalin.fidouafasm.msg.obj.RegisterIn;
import com.yalin.fidouafasm.msg.obj.RegisterOut;

import org.spongycastle.jce.interfaces.ECPublicKey;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;

/**
 * Created by YaLin on 2016/1/18.
 */
public abstract class Simulator {
    public interface BiometricsAuthResultCallback {
        void onAuthSuccess(Simulator simulator, String biometricsId);

        void onAuthFailed(Simulator simulator);
    }

    protected static final int UAF_ALG_SIGN_SECP256R1_ECDSA_SHA256_RAW = 0x01;
    protected static final int UAF_ALG_SIGN_SECP256R1_ECDSA_SHA256_DER = 0x02;
    protected static final int UAF_ALG_SIGN_RSASSA_PSS_SHA256_RAW = 0x03;
    protected static final int UAF_ALG_SIGN_RSASSA_PSS_SHA256_DER = 0x04;
    protected static final int UAF_ALG_SIGN_SECP256K1_ECDSA_SHA256_RAW = 0x05;
    protected static final int UAF_ALG_SIGN_SECP256K1_ECDSA_SHA256_DER = 0x06;

    protected static final int TAG_ATTESTATION_CERT = 0x2E05;
    protected static final int TAG_ATTESTATION_BASIC_FULL = 0x3E07;
    protected static final int TAG_ATTESTATION_BASIC_SURROGATE = 0x3E08;

    protected static final int USER_VERIFY_PRESENCE = 0x01;
    protected static final int USER_VERIFY_FINGERPRINT = 0x02;
    protected static final int USER_VERIFY_PASSCODE = 0x04;
    protected static final int USER_VERIFY_VOICEPRINT = 0x08;
    protected static final int USER_VERIFY_FACEPRINT = 0x10;
    protected static final int USER_VERIFY_LOCATION = 0x20;
    protected static final int USER_VERIFY_EYEPRINT = 0x40;
    protected static final int USER_VERIFY_PATTERN = 0x80;
    protected static final int USER_VERIFY_HANDPRINT = 0x100;
    protected static final int USER_VERIFY_NONE = 0x200;
    protected static final int USER_VERIFY_ALL = 0x400;

    protected static final int KEY_PROTECTION_SOFTWARE = 0x01;
    protected static final int KEY_PROTECTION_HARDWARE = 0x02;
    protected static final int KEY_PROTECTION_TEE = 0x04;
    protected static final int KEY_PROTECTION_SECURE_ELEMENT = 0x08;
    protected static final int KEY_PROTECTION_REMOTE_HANDLE = 0x10;

    protected static final int MATCHER_PROTECTION_SOFTWARE = 0x01;
    protected static final int MATCHER_PROTECTION_TEE = 0x02;
    protected static final int MATCHER_PROTECTION_ON_CHIP = 0x04;

    protected static final int ATTACHMENT_HINT_INTERNAL = 0x01;
    protected static final int ATTACHMENT_HINT_EXTERNAL = 0x02;
    protected static final int ATTACHMENT_HINT_WIRED = 0x04;
    protected static final int ATTACHMENT_HINT_WIRELESS = 0x08;
    protected static final int ATTACHMENT_HINT_NFC = 0x10;
    protected static final int ATTACHMENT_HINT_BLUETOOTH = 0x20;
    protected static final int ATTACHMENT_HINT_NETWORK = 0x40;
    protected static final int ATTACHMENT_HINT_READY = 0x80;
    protected static final int ATTACHMENT_HINT_WIFI_DIRECT = 0x100;

    protected static final int TRANSACTION_CONFIRMATION_DISPLAY_ANY = 0x01;
    protected static final int TRANSACTION_CONFIRMATION_DISPLAY_PRIVILEGED_SOFTWARE = 0x02;
    protected static final int TRANSACTION_CONFIRMATION_DISPLAY_TEE = 0x04;
    protected static final int TRANSACTION_CONFIRMATION_DISPLAY_HARDWARE = 0x08;
    protected static final int TRANSACTION_CONFIRMATION_DISPLAY_REMOTE = 0x10;

    public Simulator() {

    }

    public static Simulator getInstance(int index) {
        if (index == 1) {
            return SimulatorA.getInstance();
        } else return SimulatorB.getInstance();
    }

    public static RegisterOut register(@NonNull String biometricsId, @NonNull RegisterIn registerIn, int authenticatorIndex) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
        Simulator simulator = getInstance(authenticatorIndex);
        return simulator.register(biometricsId, registerIn);
    }

    public static AuthenticateOut authenticate(@NonNull String biometricsId, @NonNull AuthenticateIn authenticateIn, int authenticatorIndex) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
        Simulator simulator = getInstance(authenticatorIndex);
        return simulator.authenticate(biometricsId, authenticateIn);
    }

    public abstract void showBiometricsAuth(@NonNull Activity activity, BiometricsAuthResultCallback callback);

    public abstract RegisterOut register(@NonNull String biometricsId, @NonNull RegisterIn registerIn) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException;

    public abstract AuthenticateOut authenticate(@NonNull String biometricsId, @NonNull AuthenticateIn authenticateIn) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException;

    public abstract String getAAID();

    public abstract String getScheme();

    public abstract String getKeyId();

    protected abstract String getPrivateKey();

    public abstract String getPublicKey();

    public abstract String getCert();

    protected abstract AuthenticatorInfo getInfo();

    public byte[] sign(byte[] signBase) throws Exception {
        PrivateKey privateKey =
                KeyCodec.getPrivKey(Base64.decode(getPrivateKey(), Base64.URL_SAFE));

        BigInteger[] signatureGen = NamedCurve.signAndFromatToRS(privateKey,
                SHA.sha(signBase, "SHA-256"));

        boolean verify = NamedCurve.verify(
                KeyCodec.getKeyAsRawBytes((ECPublicKey) KeyCodec.getPubKey(Base64.decode(getPublicKey(), Base64.URL_SAFE))),
                SHA.sha(signBase, "SHA-256"),
                Asn1.decodeToBigIntegerArray(Asn1.getEncoded(signatureGen)));
        if (!verify) {
            throw new RuntimeException("Signature match fail");
        }

        return Asn1.toRawSignatureBytes(signatureGen);
    }

    public static AuthenticatorInfo[] discover() {
        AuthenticatorInfo[] authenticatorInfos = new AuthenticatorInfo[2];
        authenticatorInfos[0] = SimulatorA.getInstance().getInfo()
                .authenticatorIndex(1)
                .isUserEnrolled(false);
        authenticatorInfos[1] = SimulatorB.getInstance().getInfo()
                .authenticatorIndex(2)
                .isUserEnrolled(false);
        return authenticatorInfos;
    }
}
