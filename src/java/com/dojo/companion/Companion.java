package com.dojo.companion;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.dojo.companion.others.Category;
import com.dojo.companion.others.CategoryException;
import com.dojo.companion.others.CategoryFeature;
import com.dojo.companion.others.CategoryFeatures;
import com.dojo.companion.others.CategoryManagementService;
import com.dojo.companion.others.CompanionException;
import com.dojo.companion.others.Query;
import com.dojo.companion.others.ConfigurationService;
import com.dojo.companion.others.FatalException;
import com.dojo.companion.others.ServiceException;
import com.dojo.companion.others.ServiceFactory;
import com.dojo.companion.others.UpdateHistoryReader;

public class Companion {

   /** The class logger */
   private final static Logger logger_ = Logger.getLogger(Companion.class.getName());

   /** Service used to get Configuration.xml properties */
   private static final String CONFIGURATION_SERVICE_NAME = "configuration";

   private static final String CATEGORY_MANAGEMENT_SERVICE_NAME = "categoryManagement";

   private static final String NEW_FILE_NAME = "new";

   private static final String DELETED_FILE_NAME = "delete";

   private static final String UPDATED_FILE_NAME = "update";

   public final static String DONE_DIRECTORY = "features.history.doneDirectory";

   public static final String PRODUCTS_ROOTDIRECTORY_KEY = "products";

   private static final String UPDATE_HISTORY_FILE_DIRECTORY = "history";

   /** Service used to get Configuration properties */
   private ConfigurationService configurationService_ = null;

   /** Service used to get infos about a categories */
   private CategoryManagementService categoryManagementService_ = null;

   /** The current country prefix */
   private String country_ = null;
   
   /** The processor name */
   private String processorName = null;

   String updatePrefix;
   String deletePrefix;
   String newPrefix;

   /**
    * Constructor. Gets all services used by Companion : processConfiguration and Configuration
    * 
    * @param name the name of the companion
    * @param country the country prefix of the companion
    * @param targetedProcess the name of the targeted process of the companion
    * @throws FatalException 
    */
   public Companion(String name, String country, String processName) throws ServiceException, FatalException {
      country_ = country;
      processorName = processName;

      configurationService_ = (ConfigurationService) ServiceFactory.getService(CONFIGURATION_SERVICE_NAME);
      categoryManagementService_ = (CategoryManagementService) ServiceFactory
            .getService(CATEGORY_MANAGEMENT_SERVICE_NAME);

      if (country_ != null) {
         String filePath = getFile().getPath();
         updatePrefix = filePath + "/" + UPDATED_FILE_NAME;
         deletePrefix = filePath + "/" + DELETED_FILE_NAME;
         newPrefix = filePath + "/" + NEW_FILE_NAME;
      }
   }

   /**
    * Builds one CompanionQuery with the list of updated category id.<br>
    * 
    * @throws CompanionException if an error occurs
    */
   public List buildQueries(List fileNames) throws CompanionException, FatalException {
      if (country_ == null) {
         throw new CompanionException("No country specified to buildQueries method.");
      }

      if (fileNames.size() == 0) {
         return null;
      }

      List returnList = new ArrayList();

      // We iterate on the list of file names
      Iterator ite = fileNames.iterator();
      while (ite.hasNext()) {

         // We store all updated productsIds in this list
         ArrayList pids = new ArrayList();
         ArrayList catIds = new ArrayList();

         String fileName = (String) ite.next();
         if (logger_.isEnabledFor(Level.DEBUG)) {
            logger_.debug("File <" + fileName + "> has been changed ...");
         }

         boolean isProcess = isProcess(fileName);
         if (isProcess) {
            int catId;
            try {
               catId = retrieveCategory(fileName);
            } catch (NumberFormatException nfe) {
               if (logger_.isEnabledFor(Level.WARN)) {
                  logger_.warn("Impossible to extract catId from filename <" + fileName + ">");
               }
               break;
            }
            Category category = getCategory(catId);        
            if (category != null) {
               CategoryFeature categoryFeature = checkIsEnabled(catId, category);     
               if (categoryFeature != null) {                 
                  String featureValue = categoryFeature.getValue();
                  if (featureValue != null && featureValue.equals(CategoryFeatures.ENABLED_FOR_PROCESS_TRUE)) {
                     checkHistoryReader(pids, fileName, catId);

                     catIds.add(catId);
                     // We build the companion query used by the framework
                     // We add the list of the productIds
                     if (pids.size() > 0) {
                        returnList.add(buildQuery(pids, catIds));
                     }
                  } else {
                     if (logger_.isEnabledFor(Level.WARN)) {
                        logger_.warn("The feature <" + processorName + "> is not enabled for category <" + catId
                              + "> country <" + country_
                              + "> in the Category Management Server, this category is not treated.");
                     }
                  }
               } else {
                  if (logger_.isEnabledFor(Level.WARN)) {
                     logger_.warn("The feature <" + processorName + "> is not defined for category <" + catId
                           + "> country <" + country_
                           + "> in the Category Management Server, this category is not treated.");
                  }
               }

            } else {
               if (logger_.isEnabledFor(Level.INFO))
                  logger_
                        .info("Category <"
                              + catId
                              + "> country <"
                              + country_
                              + "> doesn't exist in the Category Management Server, this category is not treated (could be normal : PDB categories).");
            }

         }
      }

      if (returnList.size() > 0)
         return returnList;
      else
         return null;

   }

   private Query buildQuery(ArrayList productsIds, ArrayList catIds) {
      Query query;
      query = new Query();
      query.setProducts(productsIds);
      query.setCatIds(catIds);
      return query;
   }

   private void checkHistoryReader(List productsIds, String fileName, int catId) throws FatalException {
      String doneFilePath = getDoneDirectoryPath();

      // We build a UpdateHistoryReader object to read the list of the productsIds in the scanned file
      UpdateHistoryReader historyReader = new UpdateHistoryReader(catId, country_, doneFilePath);
      // we add a productsId=0 to force the reprocessing of everything for pid=0
      if (fileName.startsWith(newPrefix) && (historyReader.getNewProductsIds().size() > 0)) {
         productsIds.add(0);
      }
      // all offers for pid=0 and for updated pid
      else if (fileName.startsWith(updatePrefix)) {
         // We iterate on the list of pids recovered in the file
         List updatePids = historyReader.getUpdatedProductsIds();
         if (updatePids.size() > 0) {
            Iterator itePids = updatePids.iterator();
            while (itePids.hasNext()) {
               Integer intPid = (Integer) itePids.next();
               productsIds.add(intPid);
            }
            productsIds.add(0);
         }
      }
      // all offers for deleted pid
      else if (fileName.startsWith(deletePrefix)) {
         List deletePids = historyReader.getDeletedProductsIds();
         Iterator itePids = deletePids.iterator();
         if (deletePids.size() > 0) {
            while (itePids.hasNext()) {
               Integer intPid = (Integer) itePids.next();
               productsIds.add(intPid);
            }
         }
      }
   }

   private String getDoneDirectoryPath() throws FatalException {
      String doneFilePath = null;
      try {
         // We get the path of the "done" directory in the pdu.properties file.
         doneFilePath = configurationService_.getProperty(DONE_DIRECTORY);
      } catch (ServiceException se) {
         throw new FatalException("Impossible to get done directory into Conf");
      }
      return doneFilePath;
   }

   private CategoryFeature checkIsEnabled(int catId, Category category) {
      CategoryFeatures categoryFeatures = category.getFeatures();

      String featureName = null;
      CategoryFeature categoryFeature = null;
      try {
         featureName = CategoryFeatures.getEnabledForProcessFeatureName(processorName);
         categoryFeature = categoryFeatures.get(featureName);
      } catch (CategoryException ce) {
         if (logger_.isEnabledFor(Level.ERROR)) {
            logger_.error("Error occured during recovering the category features for catid <" + catId + "> country <"
                  + country_ + ">.", ce);
         }
      }
      return categoryFeature;
   }

   private Category getCategory(int catId) throws CompanionException {
      Category category = null;
      try {
         category = categoryManagementService_.getCategory(catId, country_);
      } catch (ServiceException e) {
         if (logger_.isEnabledFor(Level.ERROR)) {
            logger_.error("Error occured during recover category from Category Management Server for catid <" + catId
                  + "> country <" + country_ + ">.", e);
         }
         throw new CompanionException(e.getMessage(), e);
      }

      return category;
   }

   private int retrieveCategory(String fileName) throws NumberFormatException {
      String strCatId = null;
      int catId = 0;
      if (fileName.startsWith(updatePrefix))
         strCatId = fileName.substring(updatePrefix.length() + 1, fileName.indexOf("_", updatePrefix.length() + 1));
      else if (fileName.startsWith(deletePrefix))
         strCatId = fileName.substring(deletePrefix.length() + 1, fileName.indexOf("_", deletePrefix.length() + 1));
      else if (fileName.startsWith(newPrefix))
         strCatId = fileName.substring(newPrefix.length() + 1, fileName.indexOf("_", deletePrefix.length() + 1));
      catId = Integer.parseInt(strCatId);
      return catId;
   }

   private boolean isProcess(String fileName) {
      boolean needToProcessFile = fileName.startsWith(updatePrefix) || fileName.startsWith(deletePrefix)
            || fileName.startsWith(newPrefix);
      return needToProcessFile;
   }

   /**
    * Return the directory to scan.<br>
    * This is the directory where the update history files of the XML FBS generation are stored.<br>
    * 
    * @return file to scan
    * @throws CompanionException if an error occurs
    */
   public File getFile() throws FatalException {
      if (country_ == null) {
         throw new FatalException("No country specified to getFile method.");
      }

      String rootDirectory = null;

      try {
         // We get the root directory of the Products files in the configuration (default application)
         rootDirectory = configurationService_.getProperty(PRODUCTS_ROOTDIRECTORY_KEY);

      } catch (ServiceException se) {
         // If we have a Service exception (problem during the recovering of the properties), we return a companion
         // exception
         throw new FatalException("Impossible to get products root directory from configuration file", se);
      }

      // We build the directory where the update history file are stored
      String fileDirectory = rootDirectory + "/" + UPDATE_HISTORY_FILE_DIRECTORY;
      File file = new File(fileDirectory);
      if (!file.exists()) {
         throw new FatalException("Update history file directory doesn't exist: " + file.getAbsolutePath());
      }
      return file;
   }

   /**
    * Get max depth to scan.<br>
    * 
    * @return max depth to scan
    * @throws CompanionException if an error occurs
    */
   public int getMaxDepth() throws FatalException {
      return 1;
   }
}
