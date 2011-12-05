package com.dojo.companion.others;

public class FatalException extends Exception {

   public FatalException(String string) {
      super(string);
   }

   public FatalException(String string, ServiceException se) {
      super(string, se);
   }

}
