package com.umeng.push.android;

import com.umeng.push.AndroidNotification;

import java.util.List;

public class AndroidListCast extends AndroidNotification {
  public AndroidListCast(String appkey, String appMasterSecret) throws Exception {
    setAppMasterSecret(appMasterSecret);
    setPredefinedKeyValue("appkey", appkey);
    this.setPredefinedKeyValue("type", "listcast");
  }

  public void setDeviceToken(List<String> tokens) throws Exception {
    setPredefinedKeyValue("device_tokens", joinTokens(tokens));
  }
}