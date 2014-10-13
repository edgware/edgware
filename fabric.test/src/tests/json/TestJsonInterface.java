/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2014
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package tests.json;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import tests.common.FabricTestJsonClient;
import fabric.client.FabricClient;
import fabric.core.properties.ConfigProperties;
import fabric.registry.FabricRegistry;
import fabric.registry.Type;

/**
 * Tests for the JSON Interface, these tests accumulate so will run in Ascending Name Order.
 * This allows us to populate the registry to enable later tests.
 * The final Z*** tests should empty out the registry entries populated 
 * by the tests returning it the state before the tests were run.
 *  
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestJsonInterface {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2014";
	
	private final static String CLASS_NAME = TestJsonInterface.class.getSimpleName();
	private final static Logger logger = Logger.getLogger(CLASS_NAME);


	private static final String payloadsFileName = "./config/testJsonInterface.payloads";
	private static final String testPropertiesFileName = "./config/test.properties";

	private static Properties testProperties = new Properties();
	private static Properties messages = new Properties();
	
	private static FabricTestJsonClient jsonClient = null;

	// Client to allow Testing of Registry Contents
	private static FabricClient client;

	private int maxDelayForRespone = 500;

	@BeforeClass
	public static void setup() throws IOException {
		
		//Configuration required for Test clients.
	    testProperties.load(new FileInputStream(testPropertiesFileName));
	    //messages and expected responses for the tests
	    messages.load(new FileInputStream(payloadsFileName));
	    
	    //New FabricClient to check Registry Entries 
	    //i.e. Checking the outcome of the JSON command	   
	    try {
			client = new FabricClient(testProperties.getProperty(ConfigProperties.NODE_NAME), CLASS_NAME, "PLAT_" + CLASS_NAME);
			client.connect();
		} catch (Exception e1) {
			Assert.fail();
			e1.printStackTrace();
		}
	}

	/**
	 * Get the next response from the Json Client
	 * @return the response as a String
	 * @throws InterruptedException
	 */
	private String getResponse() throws InterruptedException {
		return jsonClient.getResponse(maxDelayForRespone);
	}

	@Test
	public void T001_connectClient() {

		try {
			jsonClient = new FabricTestJsonClient(testProperties, CLASS_NAME);
			Assert.assertNotNull(jsonClient);
			Assert.assertTrue(jsonClient.isConnected());
		} catch (IOException e) {

			e.printStackTrace();
			Assert.fail();
		}
	}

	@Test
	public void T002_RegisterPlatformTypes() {

		try {
			jsonClient.publish(getMessage("RegisterPlatformTypes1"));
			Assert.assertEquals("Unexpected response", getExpectedResponse("RegisterPlatformTypes1"), getResponse());
			jsonClient.publish(getMessage("RegisterPlatformTypes2"));
			Assert.assertEquals("Unexpected response", getExpectedResponse("RegisterPlatformTypes2"), getResponse());
			jsonClient.publish(getMessage("RegisterPlatformTypes3"));
			Assert.assertEquals("Unexpected response", getExpectedResponse("RegisterPlatformTypes3"), getResponse());

			// Now check Registry directly
			Type type = FabricRegistry.getTypeFactory(true).getPlatformType("Person");
			Assert.assertEquals("Should be equal", Type.TYPE_PLATFORM, type.getClassifier());
			Assert.assertEquals("Should be equal", "A Person", type.getDescription());
			Assert.assertEquals("Should be equal", "Person", type.getId());
			Assert.assertEquals("Should be null", "null", type.getAttributes());
			Assert.assertEquals("Should be null", "null", type.getAttributesUri());

			type = FabricRegistry.getTypeFactory(true).getPlatformType("Building");
			Assert.assertEquals("Should be equal", Type.TYPE_PLATFORM, type.getClassifier());
			Assert.assertEquals("Should be equal", "A Building", type.getDescription());
			Assert.assertEquals("Should be equal", "Building", type.getId());
			Assert.assertEquals("Should be null", "null", type.getAttributes());
			Assert.assertEquals("Should be null", "null", type.getAttributesUri());

			type = FabricRegistry.getTypeFactory(true).getPlatformType("Vehicle");
			Assert.assertEquals("Should be equal", Type.TYPE_PLATFORM, type.getClassifier());
			Assert.assertEquals("Should be equal", "A Vehicle", type.getDescription());
			Assert.assertEquals("Should be equal", "Vehicle", type.getId());
			Assert.assertEquals("Should be null", "null", type.getAttributes());
			Assert.assertEquals("Should be null", "null", type.getAttributesUri());

		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}


	@Test
	public void Z099_DeRegisterPlatformTypes() {

		try {
			jsonClient.publish(getMessage("DeRegisterPlatformTypes3"));
			Assert.assertEquals("Unexpected response", getExpectedResponse("DeRegisterPlatformTypes3"), getResponse());
			jsonClient.publish(getMessage("DeRegisterPlatformTypes2"));
			Assert.assertEquals("Unexpected response", getExpectedResponse("DeRegisterPlatformTypes2"), getResponse());
			jsonClient.publish(getMessage("DeRegisterPlatformTypes1"));
			Assert.assertEquals("Unexpected response", getExpectedResponse("DeRegisterPlatformTypes1"), getResponse());

			// Now check Registry directly
			Type type = FabricRegistry.getTypeFactory(true).getPlatformType("Person");
			Assert.assertNull("Should not exist", type);
			type = FabricRegistry.getTypeFactory(true).getPlatformType("Building");
			Assert.assertNull("Should not exist", type);
			type = FabricRegistry.getTypeFactory(true).getPlatformType("Vehicle");
			Assert.assertNull("Should not exist", type);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}
	
	/**
	 * Get message from messages file
	 * @param key key for the message
	 * @return
	 */
	private String getMessage(String key) {
		return messages.getProperty(key);
	}
	
	/**
	 * Get expected response for a message , responses have a key of &ltkey&gt_response within the messages file
	 * @param key key for the message we want to find the expected response for
	 * @return
	 */
	private String getExpectedResponse(String key) {
		return messages.getProperty(key + "_response");
	}

}
