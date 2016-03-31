/*
 * (C) Copyright IBM Corp. 2010
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry.impl;

import java.util.Map;

import fabric.registry.CompositeService;
import fabric.registry.exception.IncompleteObjectException;

/**
 * Implementation class for a CompositeService.
 */
public class CompositeServiceImpl extends AbstractRegistryObject implements CompositeService {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2010";

	/** Instance variables */
	private String id = null;
	private String type = null;
	private String affiliation = null;
	private String credentials = null;
	private String description = null;
	private String attributes = null;
	private String attributesURI = null;

	protected CompositeServiceImpl(String id, String type, String affiliation, String credentials, String description,
			String attributes, String attributesURI) {
		this.id = id;
		this.type = type;
		this.affiliation = affiliation;
		this.credentials = credentials;
		this.description = description;
		this.attributes = attributes;
		this.attributesURI = attributesURI;
	}

	@Override
	public String getAffiliation() {
		return affiliation;
	}

	@Override
	public String getAttributes() {
		return attributes;
	}

	@Override
	public Map<String, String> getAttributesMap() {
		return AbstractFactory.buildAttributesMap(attributes);
	}

	@Override
	public String getAttributesURI() {
		return attributesURI;
	}

	@Override
	public String getCredentials() {
		return credentials;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public void setId(String id) {
		this.id = id;
	}

	@Override
	public void setType(String type) {
		this.type = type;
	}

	@Override
	public void setAffiliation(String affiliation) {
		this.affiliation = affiliation;
	}

	@Override
	public void setCredentials(String credentials) {
		this.credentials = credentials;
	}

	@Override
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public void setAttributes(String attributes) {
		this.attributes = attributes;
	}

	@Override
	public void setAttributesMap(Map<String, String> attributes) {
		this.attributes = AbstractFactory.convertMapToNVPString(attributes);
	}

	@Override
	public void setAttributesURI(String attributesURI) {
		this.attributesURI = attributesURI;
	}

	@Override
	public String key() {
		return id;
	}

	@Override
	public void validate() throws IncompleteObjectException {
		if (this.id == null || this.id.length() == 0) {
			throw new IncompleteObjectException("Composite service id is mandatory.");
		}
	}

	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer("CompositeService::");
		buf.append(" ID: ").append(id);
		buf.append(" Type: ").append(type);
		buf.append(" Affiliation: ").append(affiliation);
		buf.append(", Credentials: ").append(credentials);
		buf.append(", Description: ").append(description);
		buf.append(", Attributes: ").append(attributes);
		buf.append(", AttributesURI: ").append(attributesURI);
		return buf.toString();
	}

	@Override
	public boolean equals(Object obj) {
		boolean equal = false;
		if (obj instanceof CompositeService) {
			CompositeService cs = (CompositeService) obj;
			if (cs.getId() == null ? id == null : cs.getId().equals(id) && cs.getType() == null ? type == null : cs
					.getType().equals(type)
					&& cs.getAffiliation() == null ? affiliation == null : cs.getAffiliation().equals(affiliation)
					&& cs.getCredentials() == null ? credentials == null : cs.getCredentials().equals(credentials)
					&& cs.getDescription() == null ? description == null : cs.getDescription().equals(description)
					&& cs.getAttributes() == null ? attributes == null : cs.getAttributes().equals(attributes)
					&& cs.getAttributesURI() == null ? attributesURI == null : cs.getAttributesURI().equals(
					attributesURI)) {

				equal = true;
			}
		}
		return equal;
	}

}
