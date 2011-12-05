package com.dojo.companion.others;

import java.util.ArrayList;
import java.util.List;

public class CategoryManagementService extends Service {

   private final List<Category> categories;
   
   public CategoryManagementService() {
      this.categories = new ArrayList<Category>();
   }
   public Category getCategory(int catId, String country) throws ServiceException {
      for (Category category : categories) {
         if (category.hasCatid(catId) && category.hasCountry(country))
            return category;
      }
      return null;
   }

   public void insertCategory(Category category) {
      this.categories.add(category);
      
   }

}
