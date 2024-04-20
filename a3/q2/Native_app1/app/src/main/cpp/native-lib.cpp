#include <jni.h>
#include <string>
#include <vector>

extern "C" JNIEXPORT jstring

JNICALL
Java_com_cse535_native_1app1_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello again from C++";
    return env->NewStringUTF(hello.c_str());
}
extern "C" JNIEXPORT jbyteArray
JNICALL
Java_com_cse535_native_1app1_MainActivity_resizeImage(
        JNIEnv *env,
        jobject /* this */,
        jobjectArray arr,
        jint newSize){
    jsize height = env->GetArrayLength(arr);
    jbyteArray row = (jbyteArray)env->GetObjectArrayElement(arr, 0);
    jsize width = env->GetArrayLength(row);
    jbyteArray result = env->NewByteArray(newSize*newSize);
    jbyte* resultPtr = env->GetByteArrayElements(result, NULL);
    jbyte* rowPtr = env->GetByteArrayElements(row, NULL);
    for(int i = 0; i < newSize; i++){
        for(int j = 0; j < newSize; j++){
            int x = i * height / newSize;
            int y = j * width / newSize;
            row = (jbyteArray)env->GetObjectArrayElement(arr, x);
            rowPtr = env->GetByteArrayElements(row, NULL);
            resultPtr[i*newSize + j] = rowPtr[x*width + y];
        }
    }
    env->ReleaseByteArrayElements(result, resultPtr, 0);
    env->ReleaseByteArrayElements(row, rowPtr, 0);
    return result;
}

