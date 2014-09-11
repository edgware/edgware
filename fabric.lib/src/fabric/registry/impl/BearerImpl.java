/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2014
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry.impl;

import fabric.registry.Bearer;
import fabric.registry.exception.IncompleteObjectException;

/**
 * Implementation class for a Fabric <code>Node</code>.
 */
public class BearerImpl extends AbstractRegistryObject implements Bearer {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2014";

	private String id = null;

	private String availability = null;

	private String description = null;

	private String attributes = null;

	private String attributesUri = null;

	protected BearerImpl() {

	}

	protected BearerImpl(String id, String availability) {

		this.id = id;
		this.availability = availability;
	}

	protected BearerImpl(String id, String availability, String description, String attributes, String attributesURI) {

		this.id = id;
		this.availability = availability;
		this.description = description;
		this.attributes = attributes;
		this.attributesUri = attributesURI;
	}

	@Override
	public String getId() {

		return id;
	}

	@Override
	public void setId(String id) {

		this.id = id;
	}

	@Override
	public String getAvailability() {

		return availability;
	}

	@Override
	public void setAvailability(String availability) {

		this.availability = availability;
	}

	@Override
	public String getDescription() {

		return description;
	}

	@Override
	public void setDescription(String description) {

		this.description = description;
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

		return attributesUri;
	}

	@Override
	public void setAttributesURI(String attributesUri) {

		this.attributesUri = attributesUri;
	}

	@Override
	public void validate() throws IncompleteObjectException {

		if (id == null || id.length() == 0 || availability == null || availability.length() == 0) {
			throw new IncompleteObjectException("Id or availability not specified.");
		}
	}

	@Override
	public String toString() {

		StringBuffer buffy = new StringBuffer("Bearer::");
		buffy.append(" Bearer ID: ").append(id);
		buffy.append(", Availability: ").append(availability);
		buffy.append(", Description: ").append(description);
		buffy.append(", Attributes: ").append(attributes);
		buffy.append(", AttributesURI: ").append(attributesUri);
		return buffy.toString();
	}

	@Override
	public boolean equals(Object obj) {

		boolean equal = false;
		if (obj instanceof BearerImpl) {
			BearerImpl n = (BearerImpl) obj;
			if (n.getId().equals(id) && n.getAvailability() == null ? availability == null : n.getAvailability()
					.equals(availability)
					&& n.getDescription() == null ? description == null : n.getDescription().equals(description)
					&& n.getAttributes() == null ? attributes == null : n.getAttributes().equals(attributes)
					&& n.getAttributesURI() == null ? attributesUri == null : n.getAttributesURI()
					.equals(attributesUri)) {

				equal = true;
			}
		}
		return equal;
	}

	@Override
	public String key() {

		return id;
	}
}
