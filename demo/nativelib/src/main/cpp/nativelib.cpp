#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring JNICALL
Java_com_cloudcare_ft_mobile_sdk_demo_nativelib_NativeLib_crashTest(JNIEnv *env,
                                                                    jobject /* this */) {
    int *a = nullptr;
    *a = 42;
    (*a)++;
    int v = *a;

    return env->NewStringUTF(
            "Crash Test Successful!");
}
