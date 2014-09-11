/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2010, 2014
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry.impl;

import java.util.Map;

import fabric.registry.SystemWiring;
import fabric.registry.exception.IncompleteObjectException;

/**
 * Implementation class for a SystemWiring.
 */
public class SystemWiringImpl extends AbstractRegistryObject implements SystemWiring {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2010, 2014";

	/** Instance variables */
	private String compositeId = null;
	private String fromSystemPlatformId = null;
	private String fromSystemId = null;
	private String fromInterfaceId = null;
	private String toSystemPlatformId = null;
	private String toSystemId = null;
	private String toInterfaceId = null;
	private String attributes = null;
	private String attributesURI = null;

	protected SystemWiringImpl(String compositeId, String fromSystemPlatformId, String fromSystemId,
			String fromInterfaceId, String toSystemPlatformId, String toSystemId, String toInterfaceId,
			String attributes, String attributesURI) {

		this.compositeId = compositeId;
		this.fromSystemPlatformId = fromSystemPlatformId;
		this.fromSystemId = fromSystemId;
		this.fromInterfaceId = fromInterfaceId;
		this.toSystemPlatformId = toSystemPlatformId;
		this.toSystemId = toSystemId;
		this.toInterfaceId = toInterfaceId;
		this.attributes = attributes;
		this.attributesURI = attributesURI;
	}

	@Override
	public String getCompositeId() {

		return compositeId;
	}

	@Override
	public void setCompositeId(String id) {

		this.compositeId = id;
	}

	@Override
	public String getFromSystemPlatformId() {

		return fromSystemPlatformId;
	}

	@Override
	public void setFromSystemPlatformId(String fromSystemPlatformId) {

		this.fromSystemPlatformId = fromSystemPlatformId;
	}

	@Override
	public String getFromSystemId() {

		return fromSystemId;
	}

	@Override
	public void setFromSystemId(String fromSystemId) {

		this.fromSystemId = fromSystemId;
	}

	@Override
	public String getFromInterfaceId() {

		return fromInterfaceId;
	}

	@Override
	public void setFromInterfaceId(String fromInterfaceId) {

		this.fromInterfaceId = fromInterfaceId;
	}

	@Override
	public String getToSystemPlatformId() {

		return toSystemPlatformId;
	}

	@Override
	public void setToSystemPlatformId(String toSystemPlatformId) {

		this.toSystemPlatformId = toSystemPlatformId;
	}

	@Override
	public String getToSystemId() {

		return toSystemId;
	}

	@Override
	public void setToSystemId(String toSystemId) {

		this.toSystemId = toSystemId;
	}

	@Override
	public String getToInterfaceId() {

		return toInterfaceId;
	}

	@Override
	public void setToInterfaceId(String toInterfaceId) {

		this.toInterfaceId = toInterfaceId;
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

		return new StringBuffer(compositeId).append("/").append(fromSystemPlatformId).append("/").append(fromSystemId)
				.append("/").append(fromInterfaceId).append("/").append(toSystemPlatformId).append("/").append(
						toSystemId).append("/").append(toInterfaceId).toString();
	}

	@Override
	public void validate() throws IncompleteObjectException {

		if (this.compositeId == null || this.compositeId.length() == 0 || this.fromSystemPlatformId == null
				|| this.fromSystemPlatformId.length() == 0 || this.fromSystemId == null
				|| this.fromSystemId.length() == 0 || this.fromInterfaceId == null
				|| this.fromInterfaceId.length() == 0 || this.toSystemPlatformId == null
				|| this.fromSystemPlatformId.length() == 0 || this.toSystemId == null || this.toSystemId.length() == 0
				|| this.toInterfaceId == null || this.toInterfaceId.length() == 0) {

			throw new IncompleteObjectException("To/From System platforms, systems and interface are all mandatory.");
		}
	}

	@Override
	public boolean equals(Object obj) {

		boolean equal = false;
		if (obj instanceof SystemWiring) {
			SystemWiring sw = (SystemWiring) obj;
			if (sw.getCompositeId() == null ? compositeId == null : sw.getCompositeId().equals(compositeId)
					&& sw.getFromSystemPlatformId() == null ? fromSystemPlatformId == null : sw
					.getFromSystemPlatformId().equals(fromSystemPlatformId)
					&& sw.getFromSystemId() == null ? fromSystemId == null : sw.getFromSystemId().equals(fromSystemId)
					&& sw.getFromInterfaceId() == null ? fromInterfaceId == null : sw.getFromInterfaceId().equals(
					fromInterfaceId)
					&& sw.getToSystemPlatformId() == null ? toSystemPlatformId == null : sw.getToSystemPlatformId()
					.equals(toSystemPlatformId)
					&& sw.getToSystemId() == null ? toSystemId == null : sw.getToSystemId().equals(toSystemId)
					&& sw.getToInterfaceId() == null ? toInterfaceId == null : sw.getToInterfaceId().equals(
					toInterfaceId)
					&& sw.getAttributes() == null ? attributes == null : sw.getAttributes().equals(attributes)
					&& sw.getAttributesURI() == null ? attributesURI == null : sw.getAttributesURI().equals(
					attributesURI)) {

				equal = true;
			}
		}
		return equal;
	}

	@Override
	public String toString() {

		StringBuffer buf = new StringBuffer("CompositePart::");
		buf.append(" Composite ID: ").append(compositeId);
		buf.append(" (From) System Platform ID: ").append(fromSystemPlatformId);
		buf.append(" (From) System ID: ").append(fromSystemId);
		buf.append(" (From) Interface ID: ").append(fromInterfaceId);
		buf.append(" (To) System Platform ID: ").append(toSystemPlatformId);
		buf.append(" (To) System ID: ").append(toSystemId);
		buf.append(" (To) Interface ID: ").append(toInterfaceId);
		buf.append(", Attributes: ").append(attributes);
		buf.append(", AttributesURI: ").append(attributesURI);
		return buf.toString();
	}
}
