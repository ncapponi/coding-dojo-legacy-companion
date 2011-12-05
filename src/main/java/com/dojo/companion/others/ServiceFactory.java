package com.dojo.companion.others;

public class ServiceFactory {
   
   private static ConfigurationService configurationService = new ConfigurationService();
   private static CategoryManagementService categoryManagementService = new CategoryManagementService();

   public static Service getService(String serviceName) {
      if (serviceName.equals("configuration"))
         return configurationService;
      else
         return categoryManagementService;
   }

}
