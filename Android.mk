LOCAL_PATH:= $(call my-dir)





include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under, src)
LOCAL_SRC_FILES += src/info/kghost/android/openvpn/IVpnService.aidl
LOCAL_SRC_FILES += src/com/haisoft/hvpn/IVpnServiceFile.aidl

LOCAL_PACKAGE_NAME := OpenVpn
#LOCAL_SDK_VERSION := current

LOCAL_JNI_SHARED_LIBRARIES := libjni_openvpn

LOCAL_REQUIRED_MODULES := libjni_openvpn

#LOCAL_SHARED_LIBRARIES :=  


#LOCAL_PROGUARD_FLAG_FILES := proguard.flags

include $(BUILD_PACKAGE)

ifeq ($(strip $(LOCAL_PACKAGE_OVERRIDES)),)
# Use the following include to make our test apk.
include $(call all-makefiles-under, $(LOCAL_PATH))
endif