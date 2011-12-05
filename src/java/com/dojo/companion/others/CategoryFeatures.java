package com.dojo.companion.others;

import java.util.HashMap;
import java.util.Map;

public class CategoryFeatures {

   /**
    * The suffix of feature name which indicates the process is enabled.
    */
   public final static String ENABLED_FOR_PROCESS_FEATURE_PREFIX = "enabledFor";
   /**
    * The feature value which indicates that the process is not enabled
    */
   public final static String ENABLED_FOR_PROCESS_FALSE = "0";

   /**
    * The feature value which indicates that the process is enabled
    */
   public final static String ENABLED_FOR_PROCESS_TRUE = "1";
   
   private static Map<String, CategoryFeature> features;
   
   

   public CategoryFeatures() {
      features = new HashMap<String, CategoryFeature>();
   }

   /**
    * Get the feature name used to indicate if the specified process is enabled or not.<br>
    * For instance, getEnabledForProcessFeatureName("myProcess") => enabledForMyProcess
    * @param processName the process name
    * @return the feature name used to indicate if the specified process is enabled or not
    * @throws CategoryException if processName is null or empty
    */
   public static String getEnabledForProcessFeatureName(String processName) throws CategoryException {
      if (processName == null || processName.length() == 0) {
         throw new CategoryException("processName can't be null or empty");
      }

      // Upper case the first character of the process name
      return ENABLED_FOR_PROCESS_FEATURE_PREFIX + processName.substring(0, 1).toUpperCase() + processName.substring(1, processName.length());
   }

   public CategoryFeature get(String featureName) {
      return features.get(featureName);
   }

   public void add(CategoryFeature feature) throws CategoryException {
      if (feature == null) {
         throw new CategoryException("Feature can't be null");
      }
      if (feature.getName() == null) {
         throw new CategoryException("Feature name can't be null");
      }
      if (feature.getValue() == null) {
         throw new CategoryException("Feature value can't be null");
      }
      features.put(feature.getName(), feature);
   }

}
