/*
 * (C) Copyright IBM Corp. 2010
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry.impl;

import java.util.Map;

import fabric.registry.CompositePart;
import fabric.registry.exception.IncompleteObjectException;

/**
 * Implementation class for a CompositePart.
 */
public class CompositePartImpl extends AbstractRegistryObject implements CompositePart {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2010";

	/** Instance variables */
	private String compositeId = null;
	private String servicePlatformId = null;
	private String serviceId = null;
	private String attributes = null;
	private String attributesURI = null;

	protected CompositePartImpl(String compositeId, String servicePlatformId, String serviceId, String attributes,
			String attributesURI) {
		this.compositeId = compositeId;
		this.servicePlatformId = servicePlatformId;
		this.serviceId = serviceId;
		this.attributes = attributes;
		this.attributesURI = attributesURI;
	}

	@Override
	public String getCompositeId() {
		return compositeId;
	}

	@Override
	public void setCompositeId(String compositeId) {
		this.compositeId = compositeId;
	}

	@Override
	public String getServicePlatformId() {
		return servicePlatformId;
	}

	@Override
	public void setServicePlatformId(String servicePlatformId) {
		this.servicePlatformId = servicePlatformId;
	}

	@Override
	public String getServiceId() {
		return serviceId;
	}

	@Override
	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}

	@Override
	public String getAttributes() {
		return attributes;
	}

	@Override
	public void setAttributes(String attributes) {
		this.attributes = attributes;
	}

	@Override
	public String getAttributesURI() {
		return attributesURI;
	}

	@Override
	public void setAttributesURI(String attributesURI) {
		this.attributesURI = attributesURI;
	}

	@Override
	public Map<String, String> getAttributesMap() {
		return AbstractFactory.buildAttributesMap(attributes);
	}

	@Override
	public void setAttributesMap(Map<String, String> attributes) {
		this.attributes = AbstractFactory.convertMapToNVPString(attributes);
	}

	@Override
	public String key() {
		return new StringBuffer(compositeId).append("/").append(servicePlatformId).append("/").append(serviceId)
				.toString();
	}

	@Override
	public void validate() throws IncompleteObjectException {
		if (this.compositeId == null || this.compositeId.length() == 0 || this.servicePlatformId == null
				|| this.servicePlatformId.length() == 0 || this.serviceId == null || this.serviceId.length() == 0) {
			throw new IncompleteObjectException("Composite id, service platform and service id are all mandatory.");
		}
	}

	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer("CompositePart::");
		buf.append(" Composite ID: ").append(compositeId);
		buf.append(" System Platform ID: ").append(servicePlatformId);
		buf.append(" System ID: ").append(serviceId);
		buf.append(", Attributes: ").append(attributes);
		buf.append(", AttributesURI: ").append(attributesURI);
		return buf.toString();
	}

	@Override
	public boolean equals(Object obj) {
		boolean equal = false;
		if (obj instanceof CompositePart) {
			CompositePart cp = (CompositePart) obj;
			if (cp.getCompositeId() == null ? compositeId == null : cp.getCompositeId().equals(compositeId)
					&& cp.getServicePlatformId() == null ? servicePlatformId == null : cp.getServicePlatformId()
					.equals(servicePlatformId)
					&& cp.getServiceId() == null ? serviceId == null : cp.getServiceId().equals(serviceId)
					&& cp.getAttributes() == null ? attributes == null : cp.getAttributes().equals(attributes)
					&& cp.getAttributesURI() == null ? attributesURI == null : cp.getAttributesURI().equals(
					attributesURI)) {

				equal = true;
			}
		}
		return equal;
	}
}
