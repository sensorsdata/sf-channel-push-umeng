package com.umeng.push.ios;

import com.umeng.push.IosNotification;

import java.util.List;

public class IosListCast extends IosNotification {
  public IosListCast(String appkey, String appMasterSecret) throws Exception {
    setAppMasterSecret(appMasterSecret);
    setPredefinedKeyValue("appkey", appkey);
    this.setPredefinedKeyValue("type", "listcast");
  }

  public void setDeviceToken(List<String> tokens) throws Exception {
    setPredefinedKeyValue("device_tokens", joinTokens(tokens));
  }
}
