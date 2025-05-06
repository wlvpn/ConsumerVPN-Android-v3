# Obfuscation/Optimization rules (R8/ProGuard)

The following rules must be added to your 'proguardFile' to be compatible with obfuscation/code optimization.

```text
# Prevent the obfuscation of class names, otherwise methods like
# class.simpleName will return the obfuscated name like A or B
-keepnames class com.wlvpn.vpnsdk.domain.value.**
-keepnames class com.wlvpn.vpnsdk.sdk.fetures.vpn.VpnConnection$*
-keepnames class com.wlvpn.vpnsdk.sdk.fetures.account.VpnAccount$*

# Protobuf
-keep class * extends com.google.protobuf.GeneratedMessageLite { *; }

# Dont warn rules
-dontwarn javax.lang.model.SourceVersion
-dontwarn javax.lang.model.element.Element
-dontwarn javax.lang.model.element.ElementKind
-dontwarn javax.lang.model.element.Modifier
-dontwarn javax.lang.model.type.TypeMirror
-dontwarn javax.lang.model.type.TypeVisitor
-dontwarn javax.lang.model.util.SimpleTypeVisitor8
-dontwarn org.bouncycastle.jsse.BCSSLParameters
-dontwarn org.bouncycastle.jsse.BCSSLSocket
-dontwarn org.bouncycastle.jsse.provider.BouncyCastleJsseProvider
-dontwarn org.conscrypt.Conscrypt$Version
-dontwarn org.conscrypt.Conscrypt
-dontwarn org.conscrypt.ConscryptHostnameVerifier
-dontwarn org.openjsse.javax.net.ssl.SSLParameters
-dontwarn org.openjsse.javax.net.ssl.SSLSocket
-dontwarn org.openjsse.net.ssl.OpenJSSE

# IKEv2
-keep class org.strongswan.android.logic.Scheduler {
    public <methods>;
}
-keep class org.strongswan.android.logic.CharonVpnService {
    private static java.lang.String getAndroidVersion();
    private static java.lang.String getDeviceString();
    private byte[][] getTrustedCertificates();
    public void updateStatus(int);
    public void updateImcState(int);
    public void addRemediationInstruction(java.lang.String);
    private byte[][] getTrustedCertificates();
    private byte[][] getUserCertificate();
    private java.security.PrivateKey getUserKey();
    native <methods>;
}
-keep class org.strongswan.android.logic.NetworkManager {
    public <methods>;
}
-keep class org.strongswan.android.logic.CharonVpnService$BuilderAdapter {
    public synchronized <methods>;
}

# WireGuard
-keep class com.wlvpn.wireguard.android.backend.GoBackend {
    public static void onNotifyHandshakeFailureCallback(int);
}

-keep interface com.gentlebreeze.vpn.module.common.api.IVpnDataTransferredRegister** { *; }
-keep class com.gentlebreeze.vpn.module.common.api.IVpnDataTransferred** { *; }

## Native methods
-keepclasseswithmembernames,includedescriptorclasses class * {
    native <methods>;
}
```
