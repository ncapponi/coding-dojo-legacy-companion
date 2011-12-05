package com.dojo.companion.others;

public class CompanionException extends Exception {

   public CompanionException(String string) {
      super(string);
   }

   public CompanionException(String message, ServiceException e) {
      super(message, e);
   }

}
