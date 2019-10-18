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

import com.sensorsdata.focus.channel.ChannelClient;
import com.sensorsdata.focus.channel.ChannelConfig;
import com.sensorsdata.focus.channel.annotation.SfChannelClient;
import com.sensorsdata.focus.channel.entry.MessagingTask;
import com.sensorsdata.focus.channel.entry.PushTask;
import com.sensorsdata.focus.channel.push.PushTaskUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.umeng.push.AndroidNotification;
import com.umeng.push.UmengPushClient;
import com.umeng.push.android.AndroidListCast;
import com.umeng.push.ios.IosListCast;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@SfChannelClient(version = "v0.1.1", desc = "SF 友盟推送客户端")
@Slf4j
public class UmengClient extends ChannelClient {

  // 每次批量发送请求最多包含多少推送 ID
  private static final int BATCH_SIZE = 100;

  private String androidAppKey;
  private String androidMasterSecret;
  private String iosAppKey;
  private String iosMasterSecret;
  private UmengPushClient umengPushClient;

  private String channelMiActivity;

  private static final int IOS_CLIENT_ID_LENGTH = 64;
  private static final int ANDROID_CLIENT_ID_LENGTH = 44;

  private static final String STR_SF_DATA = "sf_data";

  @Override
  public void initChannelClient(ChannelConfig channelConfig) {
    UmengChannelConfig umengChannelConfig = (UmengChannelConfig) channelConfig;

    androidAppKey = umengChannelConfig.getAndroidAppKey();
    androidMasterSecret = umengChannelConfig.getAndroidMasterSecret();
    iosAppKey = umengChannelConfig.getIosAppKey();
    iosMasterSecret = umengChannelConfig.getIosMasterSecret();
    umengPushClient = new UmengPushClient(true);

    channelMiActivity = umengChannelConfig.getChannelMiActivity();
  }

  @Override
  public void send(List<MessagingTask> messagingTasks) throws Exception {
    // 将推送内容相同的任务分到一组，后面按组批量推送
    Collection<List<MessagingTask>> taskGroups = PushTaskUtils.groupByTaskContent(messagingTasks, BATCH_SIZE);

    for (List<MessagingTask> taskList : taskGroups) {
      List<MessagingTask> iosTasks = new ArrayList<>();
      List<MessagingTask> androidTasks = new ArrayList<>();
      for (MessagingTask messagingTask : taskList) {
        String clientId = messagingTask.getPushTask().getClientId();
        switch (clientId.length()) {
          case IOS_CLIENT_ID_LENGTH:
            iosTasks.add(messagingTask);
            break;
          case ANDROID_CLIENT_ID_LENGTH:
            androidTasks.add(messagingTask);
            break;
          default:
            log.warn("unknown platform by client_id length. [task='{}']", messagingTask);
            break;
        }
      }

      try {
        processIosPush(iosTasks);
      } catch (Exception e) {
        log.warn("process ios push with exception. [tasks='{}']", iosTasks);
        log.warn("exception detail", e);
        for (MessagingTask task : iosTasks) {
          task.setSuccess(false);
          task.setFailReason(ExceptionUtils.getMessage(e));
        }
      }

      try {
        processAndroidPush(androidTasks);
      } catch (Exception e) {
        log.warn("process android push with exception. [tasks='{}']", iosTasks);
        log.warn("exception detail", e);
        for (MessagingTask task : androidTasks) {
          task.setSuccess(false);
          task.setFailReason(ExceptionUtils.getMessage(e));
        }
      }
    }
  }


  private static List<String> getDeviceTokensFromTaskList(List<MessagingTask> messagingTasks) {
    List<String> deviceTokens = new ArrayList<>();
    for (MessagingTask messagingTask : messagingTasks) {
      deviceTokens.add(messagingTask.getPushTask().getClientId());
    }
    return deviceTokens;
  }

  private void processIosPush(List<MessagingTask> messagingTasks) throws Exception {
    if (CollectionUtils.isEmpty(messagingTasks)) {
      return;
    }
    if (StringUtils.isBlank(iosAppKey) || StringUtils.isBlank(iosMasterSecret)) {
      log.error("ios config is incomplete. [appKey='{}', masterSecret='{}']", iosAppKey, iosMasterSecret);
      throw new Exception("ios config is incomplete");
    }

    PushTask pushTask = messagingTasks.get(0).getPushTask();
    IosListCast listCast = new IosListCast(iosAppKey, iosMasterSecret);
    listCast.setDeviceToken(getDeviceTokensFromTaskList(messagingTasks));
    listCast.setAlert(pushTask.getMsgTitle(), pushTask.getMsgContent());
    listCast.setCustomizedField(STR_SF_DATA, pushTask.getSfData());

    String result = umengPushClient.send(listCast);
    log.debug("process ios push. [result='{}']", result);

    fillPushResult(result, messagingTasks);
  }

  private void processAndroidPush(List<MessagingTask> messagingTasks) throws Exception {
    if (CollectionUtils.isEmpty(messagingTasks)) {
      return;
    }
    if (StringUtils.isBlank(androidAppKey) || StringUtils.isBlank(androidMasterSecret)) {
      log.error("android config is incomplete. [appKey='{}', masterSecret='{}']", androidAppKey, androidMasterSecret);
      return;
    }
    PushTask pushTask = messagingTasks.get(0).getPushTask();
    AndroidListCast listCast = new AndroidListCast(androidAppKey, androidMasterSecret);
    listCast.setDeviceToken(getDeviceTokensFromTaskList(messagingTasks));
    listCast.setTicker(pushTask.getMsgTitle());
    listCast.setTitle(pushTask.getMsgTitle());
    listCast.setText(pushTask.getMsgContent());
    String custom = "";
    if (StringUtils.isEmpty(custom)) {
      listCast.goAppAfterOpen();
    } else {
      listCast.goCustomAfterOpen(custom);
    }
    listCast.setDisplayType(AndroidNotification.DisplayType.NOTIFICATION);
    listCast.setExtraField(STR_SF_DATA, pushTask.getSfData());

    if (StringUtils.isNotBlank(channelMiActivity)) {
      listCast.setMipush(true);
      listCast.setMiActivity(channelMiActivity);
    }

    String result = umengPushClient.send(listCast);
    log.debug("process android push. [result='{}']", result);

    fillPushResult(result, messagingTasks);
  }

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private static void fillPushResult(String result, List<MessagingTask> messagingTasks) {
    JsonNode resultNode;
    try {
      resultNode = OBJECT_MAPPER.readTree(result);
    } catch (IOException e) {
      log.warn("can't parse result. [result='{}']", result);
      for (MessagingTask task : messagingTasks) {
        task.setSuccess(false);
        task.setFailReason(result);
      }
      return;
    }

    String errorMessage = null;
    if (!resultNode.has("ret") || !"SUCCESS".equals(resultNode.get("ret").asText())) {
      errorMessage = result;
    }
    for (MessagingTask task : messagingTasks) {
      task.setSuccess(errorMessage == null);
      task.setFailReason(errorMessage);
    }
  }
}
