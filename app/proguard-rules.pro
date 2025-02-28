# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
-keepattributes SourceFile,LineNumberTable,Signature
-keep public class * extends java.lang.Throwable  # Keep custom throwables.

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Log
-keepclassmembers class ch.qos.logback.classic.pattern.* { <init>(); }
-keep public class org.slf4j.** { *; }
-keep public class ch.qos.logback.** { *; }

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
-dontwarn javax.mail.Address
-dontwarn javax.mail.Authenticator
-dontwarn javax.mail.BodyPart
-dontwarn javax.mail.Message$RecipientType
-dontwarn javax.mail.Message
-dontwarn javax.mail.Multipart
-dontwarn javax.mail.PasswordAuthentication
-dontwarn javax.mail.Session
-dontwarn javax.mail.Transport
-dontwarn javax.mail.internet.AddressException
-dontwarn javax.mail.internet.InternetAddress
-dontwarn javax.mail.internet.MimeBodyPart
-dontwarn javax.mail.internet.MimeMessage
-dontwarn javax.mail.internet.MimeMultipart

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