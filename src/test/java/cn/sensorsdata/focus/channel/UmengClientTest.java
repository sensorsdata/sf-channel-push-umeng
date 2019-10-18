/*
 * Copyright 2019 Sensors Data Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.sensorsdata.focus.channel;

import com.sensorsdata.focus.channel.entry.LandingType;
import com.sensorsdata.focus.channel.entry.MessagingTask;
import com.sensorsdata.focus.channel.entry.PushTask;

import junit.framework.TestCase;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class UmengClientTest extends TestCase {

  // umeng 账号
  private static final String ANDROID_APP_KEY = "aaaaaaaaaaaaaaaaaaaaaaaa";
  private static final String ANDROID_MASTER_SECRET = "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb";
  private static final String IOS_APP_KEY = "cccccccccccccccccccccccc";
  private static final String IOS_MASTER_SECRET = "dddddddddddddddddddddddddddddddd";
  // 测试设备的推送 ID
  private static final String CLIENT_ID = "eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee";

  @Test
  @Ignore
  public void testSend() throws Exception {
    UmengChannelConfig umengChannelConfig = new UmengChannelConfig();
    umengChannelConfig.setAndroidAppKey(ANDROID_APP_KEY);
    umengChannelConfig.setAndroidMasterSecret(ANDROID_MASTER_SECRET);
    umengChannelConfig.setIosAppKey(IOS_APP_KEY);
    umengChannelConfig.setIosMasterSecret(IOS_MASTER_SECRET);

    PushTask pushTask = new PushTask();
    pushTask.setLandingType(LandingType.CUSTOMIZED);
    Map<String, String> testMap = new LinkedHashMap<>();
    testMap.put("aaa", "bb");
    pushTask.setCustomized(testMap);
    pushTask.setMsgContent("content111");
    pushTask.setMsgTitle("title123");
    pushTask.setClientId(CLIENT_ID);

    MessagingTask messagingTask = new MessagingTask();
    messagingTask.setPushTask(pushTask);

    UmengClient umengClient = new UmengClient();
    umengClient.initChannelClient(umengChannelConfig);
    umengClient.send(Collections.singletonList(messagingTask));
    umengClient.close();
  }
}