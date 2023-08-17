// MyAIDLInterface.aidl
package com.test.testcicd;

import com.test.testcicd.MethodObject;
import com.test.testcicd.CallBackAIDLInterface;
// Declare any non-default types here with import statements

interface MyAIDLInterface {
    void commonMethod();
    void setStringText(String text);

    MethodObject getMethodObject();

    void register(CallBackAIDLInterface aidl);

    void unregister(CallBackAIDLInterface aidl);
}