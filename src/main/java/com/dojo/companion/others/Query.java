package com.dojo.companion.others;

import java.util.ArrayList;
import java.util.List;

public class Query {

   private List productsIds;
   private List catids;
   
   public void setProducts(ArrayList pids) {
      this.productsIds = pids;
   }

   public void setCatIds(ArrayList catIds) {
      this.catids = catids;
   }

   public List getProducts() {
     return this.productsIds;
   }

   public List getCatids() {
      return catids;
   }
   

}
