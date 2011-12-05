package com.dojo.companion.others;

import com.dojo.companion.Companion;

public class Service {

   public String getProperty(String property) throws ServiceException {
      if (property.equals(Companion.DONE_DIRECTORY))
         return "doneDir";
      else if (property.equals(Companion.PRODUCTS_ROOTDIRECTORY_KEY))
         return "rootDir";
      else if (property.equals("toto"))
         return "update";
      else
         throw new ServiceException("Property " + property + " was not found");
   }
}
