/*
 * Licensed Materials - Property of IBM
 * 
 * (C) Copyright IBM Corp. 2009, 2014
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry;

import java.util.logging.Level;
import java.util.logging.Logger;

import fabric.bus.SharedEndPoint;
import fabric.core.properties.Properties;
import fabric.registry.exception.FactoryCreationException;
import fabric.registry.exception.IncompleteObjectException;
import fabric.registry.exception.PersistenceException;
import fabric.registry.ext.CustomQueryFactory;
import fabric.registry.impl.AbstractFactory;
import fabric.registry.impl.ActorFactoryImpl;
import fabric.registry.impl.ActorPluginFactoryImpl;
import fabric.registry.impl.BearerFactoryImpl;
import fabric.registry.impl.CachedMessageFactoryImpl;
import fabric.registry.impl.CompositePartFactoryImpl;
import fabric.registry.impl.CompositeServiceFactoryImpl;
import fabric.registry.impl.DefaultConfigFactoryImpl;
import fabric.registry.impl.FabricPluginFactoryImpl;
import fabric.registry.impl.FactoryBuilder;
import fabric.registry.impl.NodeConfigFactoryImpl;
import fabric.registry.impl.NodeFactoryImpl;
import fabric.registry.impl.NodeIpMappingFactoryImpl;
import fabric.registry.impl.NodeNeighbourFactoryImpl;
import fabric.registry.impl.NodePluginFactoryImpl;
import fabric.registry.impl.PlatformFactoryImpl;
import fabric.registry.impl.RouteFactoryImpl;
import fabric.registry.impl.ServiceFactoryImpl;
import fabric.registry.impl.SystemFactoryImpl;
import fabric.registry.impl.SystemPluginFactoryImpl;
import fabric.registry.impl.SystemWiringFactoryImpl;
import fabric.registry.impl.TaskFactoryImpl;
import fabric.registry.impl.TaskNodeFactoryImpl;
import fabric.registry.impl.TaskPluginFactoryImpl;
import fabric.registry.impl.TaskServiceFactoryImpl;
import fabric.registry.impl.TaskSubscriptionFactoryImpl;
import fabric.registry.impl.TypeFactoryImpl;
import fabric.registry.persistence.PersistenceManager;

/**
 * Main class representing the interface to the Fabric Registry.
 * 
 * Use this class to access the factories used to create, find, save and delete RegistryObjects. RegistryObjects can
 * also be saved and deleted directly using the save() and delete() methods.
 * 
 */
public class FabricRegistry {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009, 2014";

	// Table constants
	public static final String NODES = "FABRIC.NODES";
	public static final String NODE_NEIGHBOURS = "FABRIC.NODE_NEIGHBOURS";
	public static final String NODE_IP_MAPPING = "FABRIC.NODE_IP_MAPPING";
	public static final String BEARERS = "FABRIC.BEARERS";

	public static final String COMPOSITE_SYSTEMS = "FABRIC.COMPOSITE_SERVICES";
	public static final String COMPOSITE_PARTS = "FABRIC.COMPOSITE_PARTS";

	public static final String PLATFORMS = "FABRIC.PLATFORMS";

	public static final String SYSTEMS = "FABRIC.SERVICES";
	public static final String SYSTEM_WIRING = "FABRIC.SERVICE_WIRING";

	public static final String DATA_FEEDS = "FABRIC.DATA_FEEDS";

	public static final String ACTOR_CONFIG = "FABRIC.ACTOR_CONFIG";
	public static final String PLATFORM_CONFIG = "FABRIC.PLATFORM_CONFIG";
	public static final String NODE_CONFIG = "FABRIC.NODE_CONFIG";
	public static final String DEFAULT_CONFIG = "FABRIC.DEFAULT_CONFIG";

	public static final String TASK_SYSTEMS = "FABRIC.TASK_SERVICES";

	public static final String ROUTES = "FABRIC.ROUTES";

	public static final String MESSAGE_CACHE = "FABRIC.MESSAGE_CACHE";

	public static final String ACTORS = "FABRIC.ACTORS";

	public static final String TASKS = "FABRIC.TASKS";

	public static final String ACTOR_PLUGINS = "FABRIC.ACTOR_PLUGINS";
	public static final String FABLET_PLUGINS = "FABRIC.FABLET_PLUGINS";
	public static final String TASK_PLUGINS = "FABRIC.TASK_PLUGINS";
	public static final String NODE_PLUGINS = "FABRIC.NODE_PLUGINS";
	public static final String SYSTEM_PLUGINS = "FABRIC.SYSTEM_PLUGINS";

	public static final String TASK_SUBSCRIPTIONS = "FABRIC.TASK_SUBSCRIPTIONS";

	public static final String TASK_NODES = "FABRIC.TASK_NODES";

	public static final String ACTOR_TYPES = "FABRIC.ACTOR_TYPES";
	public static final String FEED_TYPES = "FABRIC.FEED_TYPES";
	public static final String NODE_TYPES = "FABRIC.NODE_TYPES";
	public static final String PLATFORM_TYPES = "FABRIC.PLATFORM_TYPES";
	public static final String SYSTEM_TYPES = "FABRIC.SERVICE_TYPES";

	/* The connection to the home node which can be used with this Registry connection */
	public static SharedEndPoint homeNodeEndPoint = null;

	/*
	 * Class methods
	 */

	/**
	 * Answers an instance of a <code>DefaultConfigFactory</code> used to create instances of <code>DefaultConfig</code>
	 * that can then be saved to, or deleted from, the database.
	 * 
	 * @param localQueriesOnly
	 *            <code>true</code> if queries should be performed against the local database; <code>false</code> if
	 *            they should be distributed.
	 * 
	 * @return the factory.
	 */
	public static DefaultConfigFactory getDefaultConfigFactory(boolean localQueriesOnly) {

		DefaultConfigFactory dcf = DefaultConfigFactoryImpl.getInstance(localQueriesOnly);
		return dcf;
	}

	/**
	 * Answers an instance of an <code>DefaultConfigFactory</code> used to create instances of
	 * <code>DefaultConfig</code> that can then be saved to, or deleted from, the database.
	 * <p>
	 * If the Fabric Registry is distributed, then queries made using this factory will always be distributed.
	 * </p>
	 * 
	 * @return the factory.
	 */
	public static DefaultConfigFactory getDefaultConfigFactory() {

		DefaultConfigFactory dcf = DefaultConfigFactoryImpl.getInstance(false);
		return dcf;
	}

	/**
	 * Answers an instance of a <code>NodeConfigFactory</code> used to create instances of <code>NodeConfig</code> that
	 * can then be saved to, or deleted from, the database.
	 * 
	 * @param localQueriesOnly
	 *            <code>true</code> if queries should be performed against the local database; <code>false</code> if
	 *            they should be distributed.
	 * 
	 * @return the factory.
	 */
	public static NodeConfigFactory getNodeConfigFactory(boolean localQueriesOnly) {

		NodeConfigFactory ncf = NodeConfigFactoryImpl.getInstance(localQueriesOnly);
		return ncf;
	}

	/**
	 * Answers an instance of an <code>DefaultConfigFactory</code> used to create instances of
	 * <code>DefaultConfig</code> that can then be saved to, or deleted from, the database.
	 * <p>
	 * If the Fabric Registry is distributed, then queries made using this factory will always be distributed.
	 * </p>
	 * 
	 * @return the factory.
	 */
	public static NodeConfigFactory getNodeConfigFactory() {

		NodeConfigFactory ncf = NodeConfigFactoryImpl.getInstance(false);
		return ncf;
	}

	/**
	 * Answers an instance of an <code>ActorFactory</code> used to create instances of <code>Actors</code> that can then
	 * be saved to, or deleted from, the database.
	 * 
	 * @param localQueriesOnly
	 *            <code>true</code> if queries should be performed against the local database; <code>false</code> if
	 *            they should be distributed.
	 * 
	 * @return the factory.
	 */
	public static ActorFactory getActorFactory(boolean localQueriesOnly) {

		ActorFactoryImpl afi = ActorFactoryImpl.getInstance(localQueriesOnly);
		return afi;
	}

	/**
	 * Answers an instance of an <code>ActorFactory</code> used to create instances of <code>Actors</code> that can then
	 * be saved to, or deleted from, the database.
	 * <p>
	 * If the Fabric Registry is distributed, then queries made using this factory will always be distributed.
	 * </p>
	 * 
	 * @return the factory.
	 */
	public static ActorFactory getActorFactory() {

		ActorFactoryImpl afi = ActorFactoryImpl.getInstance(false);
		return afi;
	}

	/**
	 * Answers an instance of a <code>PlatformFactory</code> used to create instances of <code>Platforms</code> that can
	 * then be saved to, or deleted from, the database.
	 * 
	 * @param localQueriesOnly
	 *            <code>true</code> if queries should be performed against the local database; <code>false</code> if
	 *            they should be distributed.
	 * 
	 * @return the factory.
	 */
	public static PlatformFactory getPlatformFactory(boolean localQueriesOnly) {

		PlatformFactoryImpl pfi = PlatformFactoryImpl.getInstance(localQueriesOnly);
		return pfi;
	}

	/**
	 * Answers an instance of a <code>PlatformFactory</code> used to create instances of <code>Platforms</code> that can
	 * then be saved to, or deleted from, the database.
	 * <p>
	 * If the Fabric Registry is distributed, then queries made using this factory will always be distributed.
	 * </p>
	 * 
	 * @return the factory.
	 */
	public static PlatformFactory getPlatformFactory() {

		PlatformFactoryImpl pfi = PlatformFactoryImpl.getInstance(false);
		return pfi;
	}

	/**
	 * Answers an instance of a <code>NodeFactory</code> used to create instances of <code>Nodes</code> that can then be
	 * saved to, or deleted from, the database.
	 * 
	 * @param localQueriesOnly
	 *            <code>true</code> if queries should be performed against the local database; <code>false</code> if
	 *            they should be distributed.
	 * 
	 * @return the factory.
	 */
	public static NodeFactory getNodeFactory(boolean localQueriesOnly) {

		NodeFactory nf = NodeFactoryImpl.getInstance(localQueriesOnly);
		return nf;
	}

	/**
	 * Answers an instance of a <code>NodeFactory</code> used to create instances of <code>Nodes</code> that can then be
	 * saved to, or deleted from, the database.
	 * <p>
	 * If the Fabric Registry is distributed, then queries made using this factory will always be distributed.
	 * </p>
	 * 
	 * @return the factory.
	 */
	public static NodeFactory getNodeFactory() {

		NodeFactory nf = NodeFactoryImpl.getInstance(false);
		return nf;
	}

	/**
	 * Answers an instance of a <code>SystemFactory</code> used to create instances of <code>Systems</code> that can
	 * then be saved to, or deleted from, the database.
	 * 
	 * @param localQueriesOnly
	 *            <code>true</code> if queries should be performed against the local database; <code>false</code> if
	 *            they should be distributed.
	 * 
	 * @return the factory.
	 */
	public static SystemFactory getSystemFactory(boolean localQueriesOnly) {

		SystemFactoryImpl sfi = SystemFactoryImpl.getInstance(localQueriesOnly);
		return sfi;
	}

	/**
	 * Answers an instance of a <code>SystemFactory</code> used to create instances of <code>Systems</code> that can
	 * then be saved to, or deleted from, the database.
	 * <p>
	 * If the Fabric Registry is distributed, then queries made using this factory will always be distributed.
	 * </p>
	 * 
	 * @return the factory.
	 */
	public static SystemFactory getSystemFactory() {

		SystemFactoryImpl sfi = SystemFactoryImpl.getInstance(false);
		return sfi;
	}

	/**
	 * Answers an instance of a <code>ServiceFactory</code> used to create instances of <code>DataFeeds</code> (i.e.
	 * associations between systems and services) that can then be saved to, or deleted from, the database.
	 * 
	 * @param localQueriesOnly
	 *            <code>true</code> if queries should be performed against the local database; <code>false</code> if
	 *            they should be distributed.
	 * 
	 * @return the factory.
	 */
	public static ServiceFactory getServiceFactory(boolean localQueriesOnly) {

		ServiceFactoryImpl sfi = ServiceFactoryImpl.getInstance(localQueriesOnly);
		return sfi;
	}

	/**
	 * Answers an instance of a <code>ServiceFactory</code> used to create instances of <code>DataFeeds</code> (i.e.
	 * associations between systems and services) that can then be saved to, or deleted from, the database.
	 * <p>
	 * If the Fabric Registry is distributed, then queries made using this factory will always be distributed.
	 * </p>
	 * 
	 * @return the factory.
	 */
	public static ServiceFactory getServiceFactory() {

		ServiceFactoryImpl sfi = ServiceFactoryImpl.getInstance(false);
		return sfi;
	}

	/**
	 * Answers an instance of a <code>TaskFactory</code> used to create instances of <code>Tasks</code> that can then be
	 * saved to, or deleted from, the database.
	 * <p>
	 * When saving or deleting a <code>Task</code> that has dependent Registry entries, these dependencies will also be
	 * created in or removed from the Registry.
	 * </p>
	 * 
	 * @param localQueriesOnly
	 *            <code>true</code> if queries should be performed against the local database; <code>false</code> if
	 *            they should be distributed.
	 * 
	 * @return the factory.
	 */
	public static TaskFactory getTaskFactory(boolean localQueriesOnly) {

		TaskFactory tf = TaskFactoryImpl.getInstance(localQueriesOnly);
		return tf;
	}

	/**
	 * Answers an instance of a <code>TaskFactory</code> used to create instances of <code>Tasks</code> that can then be
	 * saved to, or deleted from, the database.
	 * <p>
	 * When saving or deleting a <code>Task</code> that has dependent Registry entries, these dependencies will also be
	 * created in or removed from the Registry.
	 * </p>
	 * <p>
	 * If the Fabric Registry is distributed, then queries made using this factory will always be distributed.
	 * </p>
	 * 
	 * @return the factory.
	 */
	public static TaskFactory getTaskFactory() {

		TaskFactory tf = TaskFactoryImpl.getInstance(false);
		return tf;
	}

	/**
	 * Answers an instance of a <code>TypeFactory</code> used to create instances of <code>Types</code>. Created
	 * <code>Types</code> can then be saved to or deleted from the Registry.
	 * 
	 * @param localQueriesOnly
	 *            <code>true</code> if queries should be performed against the local database; <code>false</code> if
	 *            they should be distributed.
	 * 
	 * @return the factory.
	 */
	public static TypeFactory getTypeFactory(boolean localQueriesOnly) {

		TypeFactory tf = TypeFactoryImpl.getInstance(localQueriesOnly);
		return tf;
	}

	/**
	 * Answers an instance of a <code>TypeFactory</code> used to create instances of <code>Types</code>. Created
	 * <code>Types</code> can then be saved to or deleted from the Registry.
	 * <p>
	 * If the Fabric Registry is distributed, then queries made using this factory will always be distributed.
	 * </p>
	 * 
	 * @return the factory.
	 */
	public static TypeFactory getTypeFactory() {

		TypeFactory tf = TypeFactoryImpl.getInstance(false);
		return tf;
	}

	/**
	 * Answers an instance of a <code>ActorPluginFactory</code> used to create instances of <code>ActorPlugins</code>
	 * Created <code>ActorPlugins</code> can then be saved to or deleted from the Registry.
	 * 
	 * No remote queries for ActorPluginFactory permitted, all plugins must be locally configured and started
	 * 
	 * @param localOnly
	 *            indicates wether localOnly plugins wanted
	 * 
	 * @return the factory.
	 * 
	 */
	public static ActorPluginFactory getActorPluginFactory(boolean localOnly) {

		ActorPluginFactoryImpl pfl = ActorPluginFactoryImpl.getInstance(localOnly);
		return pfl;
	}

	/**
	 * Answers an instance of a <code>FabricPluginFactory</code> used to create instances of <code>FabricPlugins</code>
	 * Created <code>FabricPlugins</code> can then be saved to or deleted from the Registry.
	 * 
	 * No remote queries for FabricPluginFactory permitted, all plugins must be locally configured and started
	 * 
	 * @param localOnly
	 *            indicates wether localOnly plugins wanted
	 * 
	 * @return the factory.
	 * 
	 */
	public static FabricPluginFactory getFabricPluginFactory(boolean localOnly) {

		FabricPluginFactoryImpl pfl = FabricPluginFactoryImpl.getInstance(localOnly);
		return pfl;
	}

	/**
	 * Answers an instance of a <code>TaskPluginFactory</code> used to create instances of <code>TaskPlugins</code>
	 * Created <code>TaskPlugins</code> can then be saved to or deleted from the Registry.
	 * 
	 * No remote queries for TaskPluginFactory permitted, all plugins must be locally configured and started
	 * 
	 * @param localOnly
	 *            indicates wether localOnly plugins wanted
	 * 
	 * @return the factory.
	 * 
	 */
	public static TaskPluginFactory getTaskPluginFactory(boolean localOnly) {

		TaskPluginFactoryImpl pfl = TaskPluginFactoryImpl.getInstance(localOnly);
		return pfl;
	}

	/**
	 * Answers an instance of a <code>NodePluginFactory</code> used to create instances of <code>NodePlugins</code>
	 * Created <code>NodePlugins</code> can then be saved to or deleted from the Registry.
	 * 
	 * No remote queries for NodePluginFactory permitted, all plugins must be locally configured and started
	 * 
	 * @param localOnly
	 *            indicates wether localOnly plugins wanted
	 * 
	 * @return the factory.
	 * 
	 */
	public static NodePluginFactory getNodePluginFactory(boolean localOnly) {

		NodePluginFactoryImpl pfl = NodePluginFactoryImpl.getInstance(localOnly);
		return pfl;
	}

	/**
	 * Answers an instance of a <code>SystemPluginFactory</code> used to create instances of <code>SystemPlugins</code>
	 * Created <code>SystemPlugins</code> can then be saved to or deleted from the Registry.
	 * 
	 * No remote queries for SystemPluginFactory permitted, all plugins must be locally configured and started
	 * 
	 * @param localOnly
	 *            indicates wether localOnly plugins wanted
	 * 
	 * @return the factory.
	 * 
	 */
	public static SystemPluginFactory getSystemPluginFactory(boolean localOnly) {

		SystemPluginFactoryImpl pfl = SystemPluginFactoryImpl.getInstance(localOnly);
		return pfl;
	}

	/**
	 * Answers an instance of a <code>NodeIpMappingFactory</code> used to create instances of
	 * <code>NodeIpMappings</code>. Created <code>NodeIpMappings</code> can then be saved to or deleted from the
	 * Registry.
	 * 
	 * @param localQueriesOnly
	 *            <code>true</code> if queries should be performed against the local database; <code>false</code> if
	 *            they should be distributed.
	 * 
	 * @return the factory.
	 */
	public static NodeIpMappingFactory getNodeIpMappingFactory(boolean localQueriesOnly) {

		NodeIpMappingFactoryImpl nmfi = NodeIpMappingFactoryImpl.getInstance(localQueriesOnly);
		return nmfi;
	}

	/**
	 * Answers an instance of a <code>NodeIpMappingFactory</code> used to create instances of
	 * <code>NodeIpMappings</code>. Created <code>NodeIpMappings</code> can then be saved to or deleted from the
	 * Registry.
	 * <p>
	 * If the Fabric Registry is distributed, then queries made using this factory will always be distributed.
	 * </p>
	 * 
	 * @return the factory.
	 */
	public static NodeIpMappingFactory getNodeIpMappingFactory() {

		NodeIpMappingFactoryImpl nmfi = NodeIpMappingFactoryImpl.getInstance(false);
		return nmfi;
	}

	/**
	 * Answers an instance of a <code>BearerFactory</code> used to create instances of <code>Bearer</code>s. Created
	 * <codeBearer</code>s can then be saved to or deleted from the Registry.
	 * 
	 * @param localQueriesOnly
	 *            <code>true</code> if queries should be performed against the local database; <code>false</code> if
	 *            they should be distributed.
	 * 
	 * @return the factory.
	 */
	public static BearerFactory getBearerFactory(boolean localQueriesOnly) {

		BearerFactory bf = BearerFactoryImpl.getInstance(localQueriesOnly);
		return bf;
	}

	/**
	 * Answers an instance of a <code>BearerFactory</code> used to create instances of <code>Bearer</code>s. Created
	 * <code>Bearer</code>s can then be saved to or deleted from the Registry.
	 * <p>
	 * If the Fabric Registry is distributed, then queries made using this factory will always be distributed.
	 * </p>
	 * 
	 * @return the factory.
	 */
	public static BearerFactory getBearerFactory() {

		BearerFactory bf = BearerFactoryImpl.getInstance(false);
		return bf;
	}

	/**
	 * Answers an instance of a <code>NodeNeighbourFactory</code> used to create instances of
	 * <code>NodeNeighbours</code>. Created <code>NodeNeighbours</code> can then be saved to or deleted from the
	 * Registry.
	 * 
	 * @param localQueriesOnly
	 *            <code>true</code> if queries should be performed against the local database; <code>false</code> if
	 *            they should be distributed.
	 * 
	 * @return the factory.
	 */
	public static NodeNeighbourFactory getNodeNeighbourFactory(boolean localQueriesOnly) {

		NodeNeighbourFactoryImpl nnfi = NodeNeighbourFactoryImpl.getInstance(localQueriesOnly);
		return nnfi;
	}

	/**
	 * Answers an instance of a <code>NodeNeighbourFactory</code> used to create instances of
	 * <code>NodeNeighbours</code>. Created <code>NodeNeighbours</code> can then be saved to or deleted from the
	 * Registry.
	 * <p>
	 * If the Fabric Registry is distributed, then queries made using this factory will always be distributed.
	 * </p>
	 * 
	 * @return the factory.
	 */
	public static NodeNeighbourFactory getNodeNeighbourFactory() {

		NodeNeighbourFactoryImpl nnfi = NodeNeighbourFactoryImpl.getInstance(false);
		return nnfi;
	}

	/**
	 * Answers an instance of a <code>TaskSubscriptionFactory</code> used to create instances of
	 * <code>TaskSubscriptions</code>. Created <code>TaskSubscriptions</code> can then be saved to or deleted from the
	 * Registry.
	 * 
	 * @param localQueriesOnly
	 *            <code>true</code> if queries should be performed against the local database; <code>false</code> if
	 *            they should be distributed.
	 * 
	 * @return the factory.
	 */
	public static TaskSubscriptionFactory getTaskSubscriptionFactory(boolean localQueriesOnly) {

		TaskSubscriptionFactoryImpl tsfi = TaskSubscriptionFactoryImpl.getInstance(localQueriesOnly);
		return tsfi;
	}

	/**
	 * Answers an instance of a <code>TaskSubscriptionFactory</code> used to create instances of
	 * <code>TaskSubscriptions</code>. Created <code>TaskSubscriptions</code> can then be saved to or deleted from the
	 * Registry.
	 * <p>
	 * If the Fabric Registry is distributed, then queries made using this factory will always be distributed.
	 * </p>
	 * 
	 * @return the factory.
	 */
	public static TaskSubscriptionFactory getTaskSubscriptionFactory() {

		TaskSubscriptionFactoryImpl tsfi = TaskSubscriptionFactoryImpl.getInstance(false);
		return tsfi;
	}

	/**
	 * Answers an instance of a <code>TaskServiceFactory</code> used to create instances of <code>TaskServices</code>.
	 * These can then be saved to or deleted from the Registry.
	 * 
	 * @param localQueriesOnly
	 *            <code>true</code> if queries should be performed against the local database; <code>false</code> if
	 *            they should be distributed.
	 * 
	 * @return the factory.
	 */
	public static TaskServiceFactory getTaskServiceFactory(boolean localQueriesOnly) {

		TaskServiceFactoryImpl tsfi = TaskServiceFactoryImpl.getInstance(localQueriesOnly);
		return tsfi;
	}

	/**
	 * Answers an instance of a <code>TaskServiceFactory</code> used to create instances of <code>TaskServices</code>.
	 * Created <code>TaskServices</code> can then be saved to or deleted from the Registry.
	 * <p>
	 * If the Fabric Registry is distributed, then queries made using this factory will always be distributed.
	 * </p>
	 * 
	 * @return the factory.
	 */
	public static TaskServiceFactory getTaskServiceFactory() {

		TaskServiceFactoryImpl tsfi = TaskServiceFactoryImpl.getInstance(false);
		return tsfi;
	}

	/**
	 * Answers an instance of a <code>TaskNodeFactory</code> used to create instances of <code>TaskNodes</code>. Created
	 * <code>TaskNodes</code> can then be saved to or deleted from the Registry.
	 * 
	 * @param localQueriesOnly
	 *            <code>true</code> if queries should be performed against the local database; <code>false</code> if
	 *            they should be distributed.
	 * 
	 * @return the factory.
	 */
	public static TaskNodeFactory getTaskNodeFactory(boolean localQueriesOnly) {

		TaskNodeFactoryImpl tnfi = TaskNodeFactoryImpl.getInstance(localQueriesOnly);
		return tnfi;
	}

	/**
	 * Answers an instance of a <code>TaskNodeFactory</code> used to create instances of <code>TaskNodes</code>. Created
	 * <code>TaskNodes</code> can then be saved to or deleted from the Registry.
	 * <p>
	 * If the Fabric Registry is distributed, then queries made using this factory will always be distributed.
	 * </p>
	 * 
	 * @return the factory.
	 */
	public static TaskNodeFactory getTaskNodeFactory() {

		TaskNodeFactoryImpl tnfi = TaskNodeFactoryImpl.getInstance(false);
		return tnfi;
	}

	/**
	 * Answers an instance of a <code>RouteFactory</code> used to create instances of <code>Routes</code>. Created
	 * <code>Routes</code> can then be saved to or deleted from the Registry.
	 * 
	 * @param localQueriesOnly
	 *            <code>true</code> if queries should be performed against the local database; <code>false</code> if
	 *            they should be distributed.
	 * 
	 * @return the factory.
	 */
	public static RouteFactory getRouteFactory(boolean localQueriesOnly) {

		RouteFactoryImpl rfi = RouteFactoryImpl.getInstance(localQueriesOnly);
		return rfi;
	}

	/**
	 * Answers an instance of a <code>RouteFactory</code> used to create instances of <code>Routes</code>. Created
	 * <code>Routes</code> can then be saved to or deleted from the Registry.
	 * <p>
	 * If the Fabric Registry is distributed, then queries made using this factory will always be distributed.
	 * </p>
	 * 
	 * @return the factory.
	 */
	public static RouteFactory getRouteFactory() {

		RouteFactoryImpl rfi = RouteFactoryImpl.getInstance(false);
		return rfi;
	}

	/**
	 * Answers an instance of a <code>CompositeServiceFactory</code> used to create instances of
	 * <code>CompositeServices</code>. Created <code>CompositeServices</code> can then be saved to or deleted from the
	 * Registry.
	 * <p>
	 * If the Fabric Registry is distributed, then queries made using this factory will always be distributed.
	 * </p>
	 * 
	 * @param localQueriesOnly
	 *            <code>true</code> if queries should be performed against the local database; <code>false</code> if
	 *            they should be distributed.
	 * 
	 * @return the factory.
	 */
	public static CompositeServiceFactory getCompositeSystemFactory(boolean localQueriesOnly) {

		CompositeServiceFactoryImpl csfi = CompositeServiceFactoryImpl.getInstance(localQueriesOnly);
		return csfi;
	}

	/**
	 * Answers an instance of a <code>CompositeServiceFactory</code> used to create instances of
	 * <code>CompositeServices</code>. Created <code>CompositeServices</code> can then be saved to or deleted from the
	 * Registry.
	 * <p>
	 * If the Fabric Registry is distributed, then queries made using this factory will always be distributed.
	 * </p>
	 * 
	 * @return the factory.
	 */
	public static CompositeServiceFactory getCompositeSystemFactory() {

		CompositeServiceFactoryImpl csfi = CompositeServiceFactoryImpl.getInstance(false);
		return csfi;
	}

	/**
	 * Answers an instance of a <code>CompositePartFactory</code> used to create instances of
	 * <code>CompositeParts</code>. Created <code>CompositeParts</code> can then be saved to or deleted from the
	 * Registry.
	 * <p>
	 * If the Fabric Registry is distributed, then queries made using this factory will always be distributed.
	 * </p>
	 * 
	 * @param localQueriesOnly
	 *            <code>true</code> if queries should be performed against the local database; <code>false</code> if
	 *            they should be distributed.
	 * 
	 * @return the factory.
	 */
	public static CompositePartFactory getCompositePartFactory(boolean localQueriesOnly) {

		CompositePartFactoryImpl cpfi = CompositePartFactoryImpl.getInstance(localQueriesOnly);
		return cpfi;
	}

	/**
	 * Answers an instance of a <code>CompositePartFactory</code> used to create instances of
	 * <code>CompositeParts</code>. Created <code>CompositeParts</code> can then be saved to or deleted from the
	 * Registry.
	 * <p>
	 * If the Fabric Registry is distributed, then queries made using this factory will always be distributed.
	 * </p>
	 * 
	 * @return the factory.
	 */
	public static CompositePartFactory getCompositePartFactory() {

		CompositePartFactoryImpl cpfi = CompositePartFactoryImpl.getInstance(false);
		return cpfi;
	}

	/**
	 * Answers an instance of a <code>SystemWiringFactory</code> used to create instances of <code>SystemWiring</code>.
	 * Created <code>SystemWiring</code> can then be saved to or deleted from the Registry.
	 * <p>
	 * If the Fabric Registry is distributed, then queries made using this factory will always be distributed.
	 * </p>
	 * 
	 * @param localQueriesOnly
	 *            <code>true</code> if queries should be performed against the local database; <code>false</code> if
	 *            they should be distributed.
	 * 
	 * @return the factory.
	 */
	public static SystemWiringFactory getSystemWiringFactory(boolean localQueriesOnly) {

		SystemWiringFactoryImpl swfi = SystemWiringFactoryImpl.getInstance(localQueriesOnly);
		return swfi;
	}

	/**
	 * Answers an instance of a <code>SystemWiringFactory</code> used to create instances of <code>SystemWiring</code>.
	 * Created <code>SystemWiring</code> can then be saved to or deleted from the Registry.
	 * <p>
	 * If the Fabric Registry is distributed, then queries made using this factory will always be distributed.
	 * </p>
	 * 
	 * @return the factory.
	 */
	public static SystemWiringFactory getSystemWiringFactory() {

		SystemWiringFactoryImpl swfi = SystemWiringFactoryImpl.getInstance(false);
		return swfi;
	}

	/**
	 * Answers an instance of a <code>CachedMessageFactory</code> used to create instances of <code>CachedMessage</code>
	 * .
	 * <p>
	 * If the Fabric Registry is distributed, then queries made using this factory will always be distributed.
	 * </p>
	 * 
	 * @param localQueriesOnly
	 *            <code>true</code> if queries should be performed against the local database; <code>false</code> if
	 *            they should be distributed.
	 * 
	 * @return the factory.
	 */
	public static CachedMessageFactory getCachedMessageFactory(boolean localQueriesOnly) {

		return CachedMessageFactoryImpl.getInstance(localQueriesOnly);
	}

	/**
	 * Answers an instance of a <code>CachedMessageFactory</code> used to create instances of <code>CachedMessage</code>
	 * .
	 * <p>
	 * If the Fabric Registry is distributed, then queries made using this factory will always be distributed.
	 * </p>
	 * 
	 * @return the factory.
	 */
	public static CachedMessageFactory getCachedMessageFactory() {

		return CachedMessageFactoryImpl.getInstance(false);
	}

	/**
	 * Saves the specified object to the Registry by calling the appropriate factory save() method. This is an
	 * alternative to calling the factory directly - both achieve the same purpose.
	 * 
	 * If the object does not already exist in the database, a new row will be created in the relevant Registry local
	 * database table. Alternatively, if it already exists, the data for that object will be updated.
	 * 
	 * @param obj
	 *            the RegistryObject to be saved
	 * @return
	 */
	public static boolean save(RegistryObject obj) throws IncompleteObjectException {

		String type = obj.getClass().toString();

		if (obj instanceof Service) {
			return getServiceFactory(true).save(obj);
		} else if (obj instanceof System) {
			return getSystemFactory(true).save(obj);
		} else if (obj instanceof Node) {
			return getNodeFactory(true).save(obj);
		} else if (obj instanceof Platform) {
			return getPlatformFactory(true).save(obj);
		} else if (obj instanceof Type) {
			return getTypeFactory(true).save(obj);
		} else if (obj instanceof Actor) {
			return getActorFactory(true).save(obj);
		} else if (obj instanceof Task) {
			return getTaskFactory(true).save(obj);
		} else if (obj instanceof ActorPlugin) {
			return getActorPluginFactory(true).save(obj);
		} else if (obj instanceof FabricPlugin) {
			return getFabricPluginFactory(true).save(obj);
		} else if (obj instanceof NodePlugin) {
			return getNodePluginFactory(true).save(obj);
		} else if (obj instanceof TaskPlugin) {
			return getTaskPluginFactory(true).save(obj);
		} else if (obj instanceof TaskNode) {
			return getTaskNodeFactory(true).save(obj);
		} else if (obj instanceof Route) {
			return getRouteFactory(true).save(obj);
		} else if (obj instanceof TaskSubscription) {
			return getTaskSubscriptionFactory(true).save(obj);
		} else if (obj instanceof TaskService) {
			return getTaskServiceFactory(true).save(obj);
		} else if (obj instanceof NodeNeighbour) {
			return getNodeNeighbourFactory(true).save(obj);
		} else if (obj instanceof NodeIpMapping) {
			return getNodeIpMappingFactory(true).save(obj);
		} else if (obj instanceof CompositeService) {
			return getCompositeSystemFactory(true).save(obj);
		} else if (obj instanceof CompositePart) {
			return getCompositePartFactory(true).save(obj);
		} else if (obj instanceof SystemWiring) {
			return getSystemWiringFactory(true).save(obj);
		} else if (obj instanceof CachedMessage) {
			return getCachedMessageFactory(true).save(obj);
		}

		return false;
	}

	/**
	 * Saves an object using the specified factory.
	 * 
	 * This method can be used in cases where the fabric registry data model has been extended.
	 * 
	 * @param object
	 *            The object to be saved
	 * @param factoryClass
	 *            The class that is used to instantiate an instance of the factory
	 * @return
	 */
	public static boolean save(RegistryObject object, Class<CustomQueryFactory> factoryClass)
			throws FactoryCreationException, IncompleteObjectException {

		String type = object.getClass().toString();
		AbstractFactory factory = FactoryBuilder.createFactory(factoryClass);
		return factory.save(object, factory);
	}

	/**
	 * Deletes the specified object from the Registry by calling the appropriate factory save() method. This is an
	 * alternative to calling the factory directly - both achieve the same purpose.
	 * 
	 * @param obj
	 * @return
	 */
	public static boolean delete(RegistryObject obj) {

		if (obj instanceof Service) {
			return getServiceFactory(true).delete(obj);
		} else if (obj instanceof System) {
			return getSystemFactory(true).delete(obj);
		} else if (obj instanceof Platform) {
			return getPlatformFactory(true).delete(obj);
		} else if (obj instanceof Node) {
			return getNodeFactory(true).delete(obj);
		} else if (obj instanceof Type) {
			return getTypeFactory(true).delete(obj);
		} else if (obj instanceof Actor) {
			return getActorFactory(true).delete(obj);
		} else if (obj instanceof Task) {
			return getTaskFactory(true).delete(obj);
		} else if (obj instanceof ActorPlugin) {
			return getActorPluginFactory(true).delete(obj);
		} else if (obj instanceof FabricPlugin) {
			return getFabricPluginFactory(true).delete(obj);
		} else if (obj instanceof NodePlugin) {
			return getNodePluginFactory(true).delete(obj);
		} else if (obj instanceof TaskPlugin) {
			return getTaskPluginFactory(true).delete(obj);
		} else if (obj instanceof TaskNode) {
			return getTaskNodeFactory(true).delete(obj);
		} else if (obj instanceof Route) {
			return getRouteFactory(true).delete(obj);
		} else if (obj instanceof TaskSubscription) {
			return getTaskSubscriptionFactory(true).delete(obj);
		} else if (obj instanceof TaskService) {
			return getTaskServiceFactory(true).delete(obj);
		} else if (obj instanceof NodeNeighbour) {
			return getNodeNeighbourFactory(true).delete(obj);
		} else if (obj instanceof NodeIpMapping) {
			return getNodeIpMappingFactory(true).delete(obj);
		} else if (obj instanceof CompositeService) {
			return getCompositeSystemFactory(true).delete(obj);
		} else if (obj instanceof CompositePart) {
			return getCompositePartFactory(true).delete(obj);
		} else if (obj instanceof SystemWiring) {
			return getSystemWiringFactory(true).delete(obj);
		} else if (obj instanceof CachedMessage) {
			return getCachedMessageFactory(true).delete(obj);
		}

		return false;
	}

	/**
	 * Deletes an object using the specified factory.
	 * 
	 * This method can be used in cases where the fabric registry data model has been extended.
	 * 
	 * @param object
	 *            The object to be deleted
	 * @param factoryClassName
	 *            The fully-qualified class name that is used to instantiate an instance of the factory
	 * @return
	 */
	public static boolean delete(RegistryObject object, Class<CustomQueryFactory> factoryClass) {

		String type = object.getClass().toString();

		AbstractFactory factory;
		try {
			factory = FactoryBuilder.createFactory(factoryClass);
			return factory.delete(object, factory);
		} catch (FactoryCreationException e) {
			return false;
		}
	}

	/**
	 * Configures local Registry access using an underlying JDBC connection.
	 * 
	 * @throws PersistenceException
	 *             if JDBC connection establishment fails.
	 */
	protected static void connectJDBC(String fabricUrl, Properties config) throws PersistenceException {

		PersistenceManager.connect(fabricUrl, config);
		Logger logger = Logger.getLogger("fabric.registry");
		logger.log(Level.INFO, "Connected to Registry at \"{0}\"", fabricUrl);
	}

	// protected static void connectRemote(String nodeId, MqttConfig config, String registryCommands,
	// String registryCommandResponses) throws PersistenceException {
	//
	// PersistenceManager.connectRemote(nodeId, config, registryCommands, registryCommandResponses);
	// Logger logger = Logger.getLogger("fabric.registry");
	// logger.log(Level.INFO, "Connected to registry {0}", nodeId);
	// }

	/**
	 * Answers the Registry connection status.
	 * 
	 * @return <code>true</code> if connected to the Registry, <code>false</code> otherwise.
	 */
	public static boolean isConnected() {

		return PersistenceManager.isConnected();
	}

	/**
	 * Closes the connection to the Fabric Registry.
	 * 
	 * @throws PersistenceException
	 *             if an error occurs closing the connection.
	 */
	public static void disconnect() throws PersistenceException {

		PersistenceManager.disconnect();
		Logger logger = Logger.getLogger("fabric.registry");
		logger.log(Level.INFO, "Disconnected from registry");
	}

	/**
	 * Runs an arbitrary SQL query against the Fabric Registry. The specified factory class is then used to instantiate
	 * the objects that should be created for the matching results.
	 * 
	 * 
	 * @param sql
	 *            The SQL SELECT statement to run
	 * @param factoryClass
	 *            The class of the factory used to instantiate the Registry Objects
	 * @param localOnly
	 *            - indicates whether the query should reflect local registry only
	 * @return
	 * @throws PersistenceException
	 *             if the query fails for some reason
	 * @throws FactoryCreationException
	 *             if an error occurs when loading or instantiating the factory class specified.
	 */
	public static RegistryObject[] runQuery(String sql, Class factoryClass, boolean localOnly)
			throws PersistenceException, FactoryCreationException {

		if (sql.toUpperCase().startsWith("DELETE") || sql.toUpperCase().startsWith("INSERT")
				|| sql.toUpperCase().startsWith("UPDATE")) {
			throw new PersistenceException("Invalid SQL - only SELECT is allowed using this method");
		}
		RegistryObject[] objects = null;
		AbstractFactory factory = FactoryBuilder.createFactory(factoryClass);
		if (factory != null && sql != null) {
			objects = PersistenceManager.getPersistence().queryRegistryObjects(sql, factory, localOnly);
		}
		return objects;
	}

	// /**
	// * Runs a SQL query against the Fabric Registry where the expected result is a single integer value.
	// *
	// * @param sql
	// * - the SQL SELECT statement to run.
	// * @param localOnly - indicates whether the query should reflect local registry only
	// * @return an integer value.
	// * @throws PersistenceException
	// * if an error occurs running the SQL statement or it is not a SELECT statement.
	// */
	// public static int runIntQuery(String sql, boolean localOnly) throws PersistenceException {
	//
	// if (sql == null) {
	// return 0;
	// }
	//
	// if (sql.toUpperCase().startsWith("DELETE") || sql.toUpperCase().startsWith("INSERT")
	// || sql.toUpperCase().startsWith("UPDATE")) {
	// throw new PersistenceException("Invalid SQL - only SELECT is allowed using this method");
	// }
	// return PersistenceManager.getPersistence().queryInt(sql, localOnly);
	// }

	/**
	 * Runs a SQL query against the Fabric Registry where the expected result is a single String value.
	 * 
	 * @param sql
	 *            - the SQL SELECT statement to run.
	 * @param localOnly
	 *            - indicates whether the query should reflect local registry only
	 * @return a string value or null if the SQL string is null or no match is found.
	 * @throws PersistenceException
	 *             if an error occurs running the SQL statement or it is not a SELECT statement.
	 */
	public static String runStringQuery(String sql, boolean localOnly) throws PersistenceException {

		if (sql == null) {
			return null;
		}

		if (sql.toUpperCase().startsWith("DELETE") || sql.toUpperCase().startsWith("INSERT")
				|| sql.toUpperCase().startsWith("UPDATE")) {
			throw new PersistenceException("Invalid SQL - only SELECT is allowed using this method");
		}
		return PersistenceManager.getPersistence().queryString(sql, localOnly);
	}

	/**
	 * Runs a SQL query against the Fabric Registry and expresses the results as an array of Object arrays.
	 * 
	 * @param sql
	 *            - the SQL select to run
	 * @param localOnly
	 *            - indicates whether the query should reflect local registry only
	 * @return
	 */
	public static Object[] runQuery(String sql, boolean localOnly) throws PersistenceException {

		if (sql == null) {
			return null;
		}

		if (sql.toUpperCase().startsWith("DELETE") || sql.toUpperCase().startsWith("INSERT")
				|| sql.toUpperCase().startsWith("UPDATE")) {
			throw new PersistenceException("Invalid SQL - only SELECT is allowed using this method");
		}
		return PersistenceManager.getPersistence().query(sql, localOnly);
	}

	/**
	 * Runs the SQL update statement ('DELETE', 'INSERT' or 'UPDATE') against the Fabric Registry.
	 * 
	 * @param sql
	 *            The SQL statements to run
	 * @return a boolean indicating whether the update was successful or not.
	 * @throws PersistenceException
	 */
	public static boolean runUpdates(String[] sql) throws PersistenceException {

		if (sql == null || sql.length == 0 || sql[0].toUpperCase().startsWith("SELECT")) {
			throw new PersistenceException(
					"Invalid update SQL - valid statements must start with either DELETE, INSERT or UPDATE.");
		}
		return PersistenceManager.getPersistence().updateRegistryObjects(sql);
	}
}