/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU Affero General Public License
* as published by the Free Software Foundation; either version 3
* of the License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.commons.notification.job;

import java.util.List;
import java.util.Map;

import org.exoplatform.commons.api.notification.MessageInfo;
import org.exoplatform.commons.api.notification.NotificationMessage;
import org.exoplatform.commons.api.notification.UserNotificationSetting;
import org.exoplatform.commons.api.notification.UserNotificationSetting.FREQUENCY;
import org.exoplatform.commons.api.notification.service.NotificationProviderService;
import org.exoplatform.commons.api.notification.service.NotificationService;
import org.exoplatform.commons.api.notification.service.ProviderSettingService;
import org.exoplatform.commons.api.notification.service.UserNotificationService;
import org.exoplatform.job.MultiTenancyJob;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.mail.MailService;
import org.exoplatform.services.mail.Message;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;

public class NotificationJob extends MultiTenancyJob {
  private static final Log LOG = ExoLogger.getLogger(NotificationJob.class);

  @Override
  public Class<? extends MultiTenancyTask> getTask() {
    return SendNotificationTask.class;
  }

  public class SendNotificationTask extends MultiTenancyTask {

    public SendNotificationTask(JobExecutionContext context, String repoName) {
      super(context, repoName);
    }
    
    @SuppressWarnings("unchecked")
    private <T> T getService(Class<T> clazz) {
      return (T) container.getComponentInstanceOfType(clazz);
    }
    
    
    private void processSendEmailNotification(NotificationService notificationService, NotificationProviderService notificationProviderService,
                                               MailService mailService, UserNotificationSetting userSetting) {
      long startTime = System.currentTimeMillis();
      LOG.info("Process send daily email notification for user: " + userSetting.getUserId());
      try {
        // get all notificationMessage will send to this user.
        Map<String, List<NotificationMessage>> notificationMessageMap = notificationService.getNotificationMessagesByUser(userSetting);

        if (notificationMessageMap.size() > 0) {

          // build digest messageInfo
          MessageInfo messageInfo = notificationProviderService.buildMessageInfo(notificationMessageMap, userSetting);
          if(messageInfo != null) {
            Message message_ = messageInfo.makeEmailNotification();
            
            mailService.sendMessage(message_);
            LOG.info("Process send daily email notification successfully for user: " + userSetting.getUserId() + ": " + (System.currentTimeMillis() - startTime) + " ms");
          }
        }
      } catch (Exception e) {
        LOG.error("Failed to send email for user " + userSetting.getUserId(), e);
      }
    }
    
    private UserNotificationSetting getDefaultUserNotificationSetting(UserNotificationSetting setting) {
      UserNotificationSetting notificationSetting = UserNotificationSetting.getInstance();
      ProviderSettingService settingService = getService(ProviderSettingService.class);
      List<String> activesProvider = settingService.getActiveProviderIds(false);
      for (String string : activesProvider) {
        if(setting.isInWeekly(string)) {
          notificationSetting.addProvider(string, FREQUENCY.WEEKLY_KEY);
        } else if(setting.isInDaily(string)) {
          notificationSetting.addProvider(string, FREQUENCY.DAILY_KEY);
        } else if(setting.isInMonthly(string)) {
          notificationSetting.addProvider(string, FREQUENCY.MONTHLY_KEY);
        }
      }

      return notificationSetting.setUserId(setting.getUserId()).setLastUpdateTime(setting.getLastUpdateTime());
    }

    @Override
    public void run() {
      super.run();
      try {
        LOG.info("Start run job to send daily email notification .....");
        UserNotificationService userService = getService(UserNotificationService.class);
        NotificationService notificationService = getService(NotificationService.class);
        NotificationProviderService notificationProviderService = getService(NotificationProviderService.class);
        MailService mailService = getService(MailService.class);
        
        JobDataMap dataMap = context.getMergedJobDataMap();
        int offset = 0, limit = dataMap.getIntValue(NotificationInfoJob.MAX_EMAIL_TO_SEND_ONE_TIME);
        limit = (limit <= 0) ? 20 : limit;
        int timeSleep = dataMap.getIntValue(NotificationInfoJob.SLEEP_TIME_TO_NEXT_SEND);
        
        // case one: for user had setting

        // get size of all userSetting have daily setting
        long size = userService.getSizeDailyUserNotificationSettings();

        List<UserNotificationSetting> userSettings;
        while ((size - offset) > 0) {
          
          // get userSetting have daily setting
          userSettings = userService.getDailyUserNotificationSettings(offset, limit);
          for (UserNotificationSetting userSetting : userSettings) {
            //
            processSendEmailNotification(notificationService, notificationProviderService, mailService, userSetting);
            
          }
          
          offset += limit;
          if(timeSleep > 0) {
            Thread.sleep(timeSleep * 1000);
          }
        }
        
        
        // case two: for user used default setting.
        UserNotificationSetting userNotificationSetting;
        // get all user had default setting
        List<UserNotificationSetting> usersDefaultSettings = userService.getDefaultDailyUserNotificationSettings();
        
        size = usersDefaultSettings.size();
        offset = 0;
        while ((size - offset) > 0) {
          int toIndex = offset + limit;
          if (toIndex > size)
            toIndex = (int) size;
          List<UserNotificationSetting> subList = usersDefaultSettings.subList(offset, toIndex);

          for (UserNotificationSetting setting : subList) {
            userNotificationSetting = getDefaultUserNotificationSetting(setting);
            //
            processSendEmailNotification(notificationService, notificationProviderService, mailService, userNotificationSetting);
          }

          offset = toIndex;
          if(timeSleep > 0) {
            Thread.sleep(timeSleep * 1000);
          }
        }
        
      } catch (Exception e) {
        LOG.error("Failed to running NotificationJob", e);
      }
      
    }
  }
}
