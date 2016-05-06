/*
 * (C) Copyright IBM Corp. 2009, 2016
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.bus.feeds.impl;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;

import fabric.Notifier;
import fabric.TaskServiceDescriptor;
import fabric.bus.messages.IEmbeddedXML;
import fabric.bus.messages.IReplicate;
import fabric.core.xml.XML;

/**
 * Class representing the list of Fabric services embedded in a subscription message.
 *
 */
public class ServiceList extends Notifier implements IEmbeddedXML {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009, 2016";

    /*
     * Class constants
     */

    /** The service list. */
    private ArrayList<TaskServiceDescriptor> serviceList = new ArrayList<TaskServiceDescriptor>();

    /** Cache of the XML form of the message. */
    private XML xmlCache = null;

    /**
     * Constructs a new instance.
     */
    public ServiceList() {

        super(Logger.getLogger("fabric.bus.feeds"));
        addChangeListener(this);

    }

    /**
     * Constructs a new instance, initialized from the specified instance.
     *
     * @param source
     *            the instance to copy.
     */
    public ServiceList(ServiceList source) {

        this();
        serviceList = (ArrayList<TaskServiceDescriptor>) source.serviceList.clone();
        xmlCache = null;

    }

    /**
     * @see fabric.bus.messages.IEmbeddedXML#init(java.lang.String, fabric.core.xml.XML)
     */
    @Override
    public void init(String element, XML messageXML) throws Exception {

        serviceList.clear();

        /* Get the XML paths for the services */
        String elementPath = XML.expandPath(element);
        elementPath = XML.regexpEscape(elementPath);
        String[] propertyPaths = messageXML.getPaths(elementPath + "/srvs\\[.*\\]/srv\\[.*\\]");

        /* For each service... */
        for (int p = 0; p < propertyPaths.length; p++) {

            /* Get and record the next service */

            String task = messageXML.get(propertyPaths[p] + "@tsk");
            String platform = messageXML.get(propertyPaths[p] + "@plt");
            String system = messageXML.get(propertyPaths[p] + "@sys");
            String service = messageXML.get(propertyPaths[p] + "@srv");

            TaskServiceDescriptor nextService = new TaskServiceDescriptor(task, platform, system, service);

            serviceList.add(nextService);

        }

        xmlCache = null;

    }

    /**
     * @see fabric.bus.messages.IEmbeddedXML#embed(java.lang.String, fabric.core.xml.XML)
     */
    @Override
    public void embed(String element, XML messageXML) throws Exception {

        /* For each service... */
        for (int s = 0; s < serviceList.size(); s++) {

            /* Serialize the service to the XML */

            TaskServiceDescriptor nextService = serviceList.get(s);

            messageXML.set(element + "/srvs/srv[%d]@tsk", nextService.task(), s);
            messageXML.set(element + "/srvs/srv[%d]@plt", nextService.platform(), s);
            messageXML.set(element + "/srvs/srv[%d]@sys", nextService.system(), s);
            messageXML.set(element + "/srvs/srv[%d]@srv", nextService.service(), s);

        }

    }

    /**
     * Answers the list of services.
     *
     * @return the service list.
     */
    public TaskServiceDescriptor[] getServices() {

        TaskServiceDescriptor[] services = new TaskServiceDescriptor[serviceList.size()];
        services = serviceList.toArray(services);
        return services;

    }

    /**
     * Sets the list of services.
     *
     * @param services
     *            the service list.
     */
    public void setServices(TaskServiceDescriptor[] services) {

        ArrayList<TaskServiceDescriptor> oldServiceList = (ArrayList<TaskServiceDescriptor>) serviceList.clone();

        serviceList.clear();
        serviceList.addAll(Arrays.asList(services));

        fireChangeNotification("serviceList", oldServiceList, serviceList);

    }

    public void addService(TaskServiceDescriptor service) {

        ArrayList<TaskServiceDescriptor> oldServiceList = (ArrayList<TaskServiceDescriptor>) serviceList.clone();
        serviceList.add(service);
        fireChangeNotification("serviceList", oldServiceList, serviceList);
    }

    /**
     * Answers the number of elements in the service list.
     *
     * @return the number of services in the list.
     */
    public int size() {

        return serviceList.size();

    }

    /**
     * @see fabric.bus.messages.IFabricMessage#toString()
     */
    @Override
    public String toString() {

        String toString = null;

        try {

            if (xmlCache == null) {

                xmlCache = new XML();
                embed("", xmlCache);

            }

            toString = xmlCache.toString();

        } catch (Exception e) {

            e.printStackTrace();
            toString = super.toString();

        }

        return toString;

    }

    /**
     * @see fabric.bus.messages.IReplicate#replicate()
     */
    @Override
    public IReplicate replicate() {

        return new ServiceList(this);

    }

    /**
     * @see fabric.Notifier#propertyChange(java.beans.PropertyChangeEvent)
     */
    @Override
    public void propertyChange(PropertyChangeEvent event) {

        super.propertyChange(event);

        /* Something has changed, so invalidate the cached XML form of this instance */
        xmlCache = null;

    }
}
