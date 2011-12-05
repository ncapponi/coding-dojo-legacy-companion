package com.dojo.companion.others;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UpdateHistoryReader {

   private final int catId;
   private final String country_;
   private final String doneFilePath;

   public UpdateHistoryReader(int catId, String country_, String doneFilePath) {
      this.catId = catId;
      this.country_ = country_;
      this.doneFilePath = doneFilePath;
   }

   public List getNewProductsIds() {
      if (catId==111801)
         return Arrays.asList(13, 14, 15);
      else
         return new ArrayList();
   }

   public List getUpdatedProductsIds() {
      if (catId==124901)
         return Arrays.asList(5, 6, 7);
      else if (catId==111801)
         return Arrays.asList(10, 11, 12);
      else
         return new ArrayList();
   }

   public List getDeletedProductsIds() {
      if (catId==111801)
         return Arrays.asList(16, 17, 18);
      else
         return new ArrayList();
   }

}
