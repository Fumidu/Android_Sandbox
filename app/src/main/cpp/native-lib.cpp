#include <jni.h>
#include <string>

extern "C"
jstring
Java_com_example_gerald_bacasable_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++ made in Gérald";
    return env->NewStringUTF(hello.c_str());
}

extern "C"
jstring
Java_com_example_gerald_bacasable_MainActivity_string2FromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello2 = "Bien e boujour brave compagnie !";
    return env->NewStringUTF(hello2.c_str());
}

