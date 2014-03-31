/*
 * RmdTemplateDiscovery.java
 *
 * Copyright (C) 2009-14 by RStudio, Inc.
 *
 * Unless you have received this program directly from RStudio pursuant
 * to the terms of a commercial license agreement with RStudio, then
 * this program is licensed to you under the terms of version 3 of the
 * GNU Affero General Public License. This program is distributed WITHOUT
 * ANY EXPRESS OR IMPLIED WARRANTY, INCLUDING THOSE OF NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. Please refer to the
 * AGPL (http://www.gnu.org/licenses/agpl-3.0.txt) for more details.
 *
 */
package org.rstudio.studio.client.rmarkdown;


import org.rstudio.core.client.widget.Operation;
import org.rstudio.core.client.widget.OperationWithInput;
import org.rstudio.studio.client.application.events.EventBus;
import org.rstudio.studio.client.common.GlobalDisplay;
import org.rstudio.studio.client.rmarkdown.events.RmdTemplateDiscoveredEvent;
import org.rstudio.studio.client.rmarkdown.events.RmdTemplateDiscoveryCompletedEvent;
import org.rstudio.studio.client.rmarkdown.model.RMarkdownServerOperations;
import org.rstudio.studio.client.rmarkdown.model.RmdDiscoveredTemplate;
import org.rstudio.studio.client.server.ServerError;
import org.rstudio.studio.client.server.ServerRequestCallback;

import com.google.inject.Inject;

public class RmdTemplateDiscovery implements 
   RmdTemplateDiscoveredEvent.Handler,
   RmdTemplateDiscoveryCompletedEvent.Handler
{
   @Inject
   public RmdTemplateDiscovery(EventBus eventBus, 
                               GlobalDisplay display,
                               RMarkdownServerOperations server)
   {
      server_ = server;
      display_ = display;

      eventBus.addHandler(RmdTemplateDiscoveredEvent.TYPE, this);
      eventBus.addHandler(RmdTemplateDiscoveryCompletedEvent.TYPE, this);
   }
   
   @Override
   public void onRmdTemplateDiscovered(RmdTemplateDiscoveredEvent event)
   {
      if (onTemplateDiscovered_ != null)
         onTemplateDiscovered_.execute(event.getTemplate());
   }
   
   @Override
   public void onRmdTemplateDiscoveryCompleted(
         RmdTemplateDiscoveryCompletedEvent event)
   {
      onCompleted_.execute();
   }
   
   public void discoverTemplates(
         OperationWithInput<RmdDiscoveredTemplate> onTemplateDiscovered,
         Operation onCompleted)
   {
      onTemplateDiscovered_ = onTemplateDiscovered;
      onCompleted_ = onCompleted;
      server_.discoverRmdTemplates(new ServerRequestCallback<Boolean>()
      {
         @Override
         public void onResponseReceived(Boolean discovered)
         {
            if (!discovered)
               showError(null);
         }

         @Override
         public void onError(ServerError error)
         {
            showError(error.getMessage());
         }
         
         private void showError(String error)
         {
            display_.showErrorMessage("R Markdown Templates Not Found",
                  "An error occurred while looking for R Markdown templates. " + 
                  (error == null ? "" : error));
         }
      });
   }
   
   private final RMarkdownServerOperations server_;
   private final GlobalDisplay display_;

   private OperationWithInput<RmdDiscoveredTemplate> onTemplateDiscovered_;
   private Operation onCompleted_;
}