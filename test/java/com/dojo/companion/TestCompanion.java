package com.dojo.companion;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.dojo.companion.Companion;
import com.dojo.companion.others.Category;
import com.dojo.companion.others.CategoryFeature;
import com.dojo.companion.others.CategoryFeatures;
import com.dojo.companion.others.CategoryManagementService;
import com.dojo.companion.others.Query;
import com.dojo.companion.others.ConfigurationService;
import com.dojo.companion.others.FatalException;
import com.dojo.companion.others.ServiceFactory;

public class TestCompanion {

   ConfigurationService configurationService_ = null;
   CategoryManagementService categoryManagementService_ = null;
   public static final String CONFIGURATION_SERVICE_NAME = "configuration";

   @Before
   public void setUp() throws Exception {

      // Start Configuration service
      configurationService_ = (ConfigurationService) ServiceFactory
            .getService(TestCompanion.CONFIGURATION_SERVICE_NAME);
  
      // Start CategoryManagement service
      categoryManagementService_ = (CategoryManagementService) ServiceFactory.getService("categoryManagement");
    
      // Insert a category with the right features for the TU
      CategoryFeature categoryFeature = new CategoryFeature(
            CategoryFeatures.getEnabledForProcessFeatureName("process"), 
            CategoryFeatures.ENABLED_FOR_PROCESS_TRUE);
      CategoryFeatures categoryFeatures = new CategoryFeatures();
      categoryFeatures.add(categoryFeature);
      Category category = new Category(124901, "uk");
      category.setFeatures(categoryFeatures);
      categoryManagementService_.insertCategory(category);
      category = new Category(111801, "uk");
      category.setFeatures(categoryFeatures);
      categoryManagementService_.insertCategory(category);

      String doneFilePath = "rootDir";
      deleteDir(new File(doneFilePath));
      new File(doneFilePath).mkdir();
      new File("rootDir/history").mkdir();

   }

   @After
   public void tearDown() throws Exception {
      String doneFilePath = configurationService_.getProperty(Companion.DONE_DIRECTORY);
      deleteDir(new File(doneFilePath));
      new File(doneFilePath).mkdir();
      
   }

   /**
    * Deletes all files and subdirectories under dir.
    * Returns true if all deletions were successful.
    * If a deletion fails, the method stops attempting to delete and returns false.
    */
   private static boolean deleteDir(File dir) {
       if (dir.isDirectory()) {
           String[] children = dir.list();
           for (int i=0; i<children.length; i++) {
               boolean success = deleteDir(new File(dir, children[i]));
               if (!success) {
                   return false;
               }
           }
       }
   
       // The directory is now empty so delete it
       return dir.delete();
   }
   
   @Test
   public void testGetFile_countryNull() throws Exception {
      Companion companion = new Companion("process", null, "process");
      try{
         companion.getFile();
         fail("should throw an exception");
      }catch(FatalException fe){
      }
      
   }
   
   /**
    * Test method getFile
    * @throws Exception
    */
   @Test
   public void testGetFile() throws Exception {
	   Companion companion = new Companion("process", "uk", "process");
      File file = companion.getFile();
      assertEquals(file.getPath(), "rootDir/history");
   }
  
   /**
    * Test the buildQueries method.<br>
    * Test if the returned CompanionQuery object contains the right list of pids 
    * @throws Exception
    */
   @Test
   public void testBuildQueries() throws Exception {
	   Companion companion = new Companion("process", "uk", "process");
      ArrayList list = new ArrayList();
      list.add("rootDir/history/update_124901_10000.txt");
      list.add("rootDir/history/update_111801_10001.txt");
      ArrayList listQueries = (ArrayList) companion.buildQueries(list);
      Query companionQuery = (Query) listQueries.get(0);
      ArrayList pids = (ArrayList) companionQuery.getProducts();
      assertTrue(pids.contains(new Integer(5)));
      assertTrue(pids.contains(new Integer(6)));
      assertTrue(pids.contains(new Integer(7)));
      
      companionQuery = (Query) listQueries.get(1);
      pids = (ArrayList) companionQuery.getProducts();
      assertTrue(pids.contains(new Integer(10)));
      assertTrue(pids.contains(new Integer(11)));
      assertTrue(pids.contains(new Integer(12)));
   }

   /**
    * Test the buildQueries method.<br>
    * Test if the returned CompanionQuery object contains the right list of pids for only a deleted Pid set
    * @throws Exception
    */
   @Test
   public void testBuildQueriesOnlyDeleteFile() throws Exception {
           Companion companion = new Companion("process", "uk", "process");
      ArrayList list = new ArrayList();
      list.add("rootDir/history/delete_111801_10001.txt");
      ArrayList listQueries = (ArrayList) companion.buildQueries(list);
      Query companionQuery = (Query) listQueries.get(0);
      ArrayList pids = (ArrayList) companionQuery.getProducts();
      assertTrue(pids.contains(new Integer(16)));
      assertTrue(pids.contains(new Integer(17)));
      assertTrue(pids.contains(new Integer(18)));
   }

   /**
    * Test the buildQueries method.<br>
    * Test if the returned CompanionQuery object contains the right list of pids for only a new Pid set
    * @throws Exception
    */
   @Test
   public void testBuildQueriesOnlyNewFile() throws Exception {
           Companion companion = new Companion("process", "uk", "process");
      ArrayList list = new ArrayList();
      list.add("rootDir/history/new_111801_10001.txt");
      ArrayList listQueries = (ArrayList) companion.buildQueries(list);
      Query companionQuery = (Query) listQueries.get(0);
      ArrayList pids = (ArrayList) companionQuery.getProducts();
      assertTrue(pids.contains(new Integer(0)));
   }

   /**
    * Test the buildQueries method.<br>
    * Test if the returned CompanionQuery object contains the right list of pids for update, new and deleted Pid set
    * @throws Exception
    */
   @Test
   public void testBuildQueriesOnlyAllFile() throws Exception {
           Companion companion = new Companion("process", "uk", "process");
      ArrayList list = new ArrayList();
      list.add("rootDir/history/new_111801_10001.txt");
      list.add("rootDir/history/delete_111801_10001.txt");
      list.add("rootDir/history/update_111801_10001.txt");
      ArrayList listQueries = (ArrayList) companion.buildQueries(list);
      Query companionQuery = (Query) listQueries.get(0);
      ArrayList pids = (ArrayList) companionQuery.getProducts();
      assertTrue(pids.contains(new Integer(0)));
      
      companionQuery = (Query) listQueries.get(1);
      pids = (ArrayList) companionQuery.getProducts();
      assertTrue(pids.contains(new Integer(16)));
      assertTrue(pids.contains(new Integer(17)));
      assertTrue(pids.contains(new Integer(18)));
      
      companionQuery = (Query) listQueries.get(2);
      pids = (ArrayList) companionQuery.getProducts();
      assertTrue(pids.contains(new Integer(10)));
      assertTrue(pids.contains(new Integer(11)));
      assertTrue(pids.contains(new Integer(12)));
   }

}
