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

import com.sensorsdata.focus.channel.ChannelConfig;
import com.sensorsdata.focus.channel.annotation.ConfigField;
import com.sensorsdata.focus.channel.annotation.SfChannelConfig;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@SfChannelConfig
@Data
public class UmengChannelConfig extends ChannelConfig {

  @ConfigField(cname = "Android 项目 AppKey", desc = "平台 Android 类型应用 AppKey，可在平台配置页面获取")
  @NotBlank
  @Size(min = 24, max = 24)
  private String androidAppKey;

  @ConfigField(cname = "Android 项目 MasterSecret", desc = "平台 Android 类型应用 MasterSecret，可在平台配置页面获取")
  @NotBlank
  @Size(min = 32, max = 32)
  private String androidMasterSecret;

  @ConfigField(cname = "iOS 项目 AppKey", desc = "平台 iOS 类型应用 AppKey，可在平台配置页面获取")
  @NotBlank
  @Size(min = 24, max = 24)
  private String iosAppKey;

  @ConfigField(cname = "iOS 项目 MasterSecret", desc = "平台 iOS 类型应用 MasterSecret，可在平台配置页面获取")
  @NotBlank
  @Size(min = 32, max = 32)
  private String iosMasterSecret;

  @ConfigField(cname = "厂商通道", desc = "可选配置。https://developer.umeng.com/docs/66632/detail/98589")
  private String channelMiActivity;
}
