/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2009
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry.impl;

import fabric.registry.Actor;
import fabric.registry.exception.IncompleteObjectException;

/**
 * Implementation class for a Fabric <code>Actor</code>.
 */
public class ActorImpl extends AbstractRegistryObject implements Actor {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009";

	private String id = null;
	private String typeId = null;
	private String roles = null;
	private String credentials = null;
	private String affiliation = null;
	private String description = null;
	private String attributes = null;
	private String attributesUri = null;

	protected ActorImpl() {
	}

	protected ActorImpl(String id, String typeId, String roles, String credentials, String affiliation,
			String description, String attributes, String attributesUri) {
		this.id = id;
		this.typeId = typeId;
		this.roles = roles;
		this.credentials = credentials;
		this.affiliation = affiliation;
		this.description = description;
		this.attributes = attributes;
		this.attributesUri = attributesUri;
	}

	@Override
	public String getAffiliation() {
		return affiliation;
	}

	@Override
	public void setAffiliation(String affiliation) {
		this.affiliation = affiliation;
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
	public String getAttributesUri() {
		return attributesUri;
	}

	@Override
	public void setAttributesUri(String attributesUri) {
		this.attributesUri = attributesUri;
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
	public String getId() {
		return id;
	}

	@Override
	public void setId(String id) {
		this.id = id;
	}

	@Override
	public String getRoles() {
		return roles;
	}

	@Override
	public void setRoles(String roles) {
		this.roles = roles;
	}

	@Override
	public String getCredentials() {
		return credentials;
	}

	@Override
	public void setCredentials(String securityClearance) {
		this.credentials = securityClearance;
	}

	@Override
	public void validate() throws IncompleteObjectException {
		if (id == null || id.length() == 0 || typeId == null || typeId.length() == 0) {
			throw new IncompleteObjectException("Missing/invalid client id and/or type id.");
		}
	}

	@Override
	public String toString() {
		StringBuffer buffy = new StringBuffer("Client::");
		buffy.append(" Client ID: ").append(id);
		buffy.append("Type ID: ").append(typeId);
		buffy.append(", Roles: ").append(roles);
		buffy.append(", Credentials: ").append(credentials);
		buffy.append(", Affiliation: ").append(affiliation);
		buffy.append(", Description: ").append(description);
		buffy.append(", Attributes: ").append(attributes);
		buffy.append(", AttributesURI: ").append(attributesUri);
		return buffy.toString();
	}

	@Override
	public boolean equals(Object obj) {
		boolean equal = false;
		if (obj != null && obj instanceof Actor) {
			Actor client = (Actor) obj;
			if (client.getId().equals(id) && client.getTypeId().equals(typeId) && client.getRoles() == null ? roles == null
					: client.getRoles().equalsIgnoreCase(roles) && client.getCredentials() == null ? credentials == null
							: client.getCredentials().equalsIgnoreCase(credentials) && client.getAffiliation() == null ? affiliation == null
									: client.getAffiliation().equalsIgnoreCase(affiliation)
											&& client.getDescription() == null ? description == null : client
											.getDescription().equalsIgnoreCase(description)
											&& client.getAttributes() == null ? attributes == null : client
											.getAttributes().equalsIgnoreCase(attributes)
											&& client.getAttributesUri() == null ? attributesUri == null : client
											.getAttributesUri().equals(attributesUri)) {

				equal = true;
			}
		}
		return equal;
	}

	@Override
	public String getTypeId() {
		return typeId;
	}

	@Override
	public void setTypeId(String typeId) {
		this.typeId = typeId;
	}

	@Override
	public Actor getShadow() {
		Actor shadowCopy = (ActorImpl) shadow;
		return shadowCopy;
	}

	@Override
	public String key() {
		return new StringBuffer(this.getId()).append("/").append(this.getTypeId()).toString();
	}

}
