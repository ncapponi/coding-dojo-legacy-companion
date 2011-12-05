package com.dojo.companion.others;

public class Category {

   private final int catid;
   private final String country;
   private CategoryFeatures categoryFeatures;

   public Category(int catid, String country) {
      this.catid = catid;
      this.country = country;
   }

   public CategoryFeatures getFeatures() {
      return this.categoryFeatures;
   }

   public void setFeatures(CategoryFeatures categoryFeatures) {
      this.categoryFeatures = categoryFeatures;
   }

   public boolean hasCatid(int catId) {
      return this.catid == catId;
   }

   public boolean hasCountry(String country) {
      return this.country.equals(country);
   }

   public int getCatid() {
      return catid;
   }

   public String getCountry() {
      return country;
   }
   
   

}
