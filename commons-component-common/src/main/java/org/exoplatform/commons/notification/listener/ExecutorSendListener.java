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
package org.exoplatform.commons.notification.listener;

import java.util.concurrent.Callable;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.MessageInfo;
import org.exoplatform.commons.api.notification.model.NotificationMessage;
import org.exoplatform.commons.api.notification.plugin.AbstractNotificationPlugin;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.mail.MailService;
import org.exoplatform.services.mail.Message;

public class ExecutorSendListener implements Callable<NotificationMessage> {
  private static final Log            LOG = ExoLogger.getExoLogger(ExecutorSendListener.class);

  private static ExecutorSendListener instance;

  private NotificationMessage         message;

  @Override
  public NotificationMessage call() throws Exception {
    // process send email notification
    processSendEmailNotifcation();
    return instance.message;
  }

  public NotificationMessage getNotificationMessage() {
    return message;
  }

  private ExecutorSendListener setNotificationMessage(NotificationMessage message) {
    this.message = message.clone();
    return this;
  }

  private static ExecutorSendListener getInstance() {
    if (instance == null) {
      synchronized (ExecutorSendListener.class) {
        if (instance == null) {
          instance = new ExecutorSendListener();
        }
      }
    }
    return instance;
  }

  public static ExecutorSendListener getInstance(NotificationMessage message) {
    return getInstance().setNotificationMessage(message);
  }

  private void processSendEmailNotifcation() {
    NotificationContext nCtx = NotificationContextImpl.DEFAULT;
    NotificationMessage message = ExecutorSendListener.getInstance().getNotificationMessage();
    AbstractNotificationPlugin supportProvider = ((NotificationContextImpl) nCtx).getNotificationPluginContainer().getPlugin(message.getKey());
    if (supportProvider != null) {
      nCtx.setNotificationMessage(message);
      MessageInfo messageInfo = supportProvider.buildMessage(nCtx);
      
      if (messageInfo != null) {
        Message message_ = messageInfo.makeEmailNotification();

        MailService mailService = (MailService) PortalContainer.getInstance().getComponentInstanceOfType(MailService.class);

        try {
          mailService.sendMessage(message_);
          LOG.info("Process send email notification successfully ... " + message.getKey().getId() + " for user: " + messageInfo.getTo());
        } catch (Exception e) {
          LOG.error("Send email error!", e);
        }
      }

    }
  }
}
