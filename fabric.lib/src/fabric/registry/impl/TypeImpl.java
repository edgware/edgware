/*
 * (C) Copyright IBM Corp. 2009
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry.impl;

import fabric.registry.Type;
import fabric.registry.exception.IncompleteObjectException;

/**
 * Implementation class for a <code>Type</code>.
 */
public class TypeImpl extends AbstractRegistryObject implements Type {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009";

	private int classifier = -1;
	private String id = null;
	private String description = null;
	private String attributes = null;
	private String attributesUri = null;

	protected TypeImpl() {
	}

	protected TypeImpl(String id, String description, String attributes, String attributesUri) {
		this.id = id;
		this.description = description;
		this.attributes = attributes;
		this.attributesUri = attributesUri;
	}

	@Override
	public String getAttributes() {
		return attributes;
	}

	@Override
	public String getAttributesUri() {
		return attributesUri;
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
	public void setAttributes(String attributes) {
		this.attributes = attributes;
	}

	@Override
	public void setAttributesUri(String uri) {
		this.attributesUri = uri;
	}

	@Override
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public void setId(String id) {
		this.id = id;
	}

	@Override
	public void validate() throws IncompleteObjectException {
		if (id == null || id.length() == 0) {
			throw new IncompleteObjectException("Type identifier not specified.");
		}
	}

	@Override
	public int getClassifier() {
		return classifier;
	}

	@Override
	public void setClassifier(int classifier) {

		this.classifier = classifier;
		if (getShadow() != null) {
			((TypeImpl) getShadow()).setClassifier(classifier);
		}

	}

	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer("Type::");
		buf.append(" Type Id: ").append(id);
		buf.append(", Description: ").append(description);
		buf.append(", Attributes: ").append(attributes);
		buf.append(", AttributesURI: ").append(attributesUri);
		return buf.toString();
	}

	@Override
	public boolean equals(Object object) {
		boolean equal = false;
		if (object != null && object instanceof Type) {
			Type type = (Type) object;
			if (type.getId() == null ? id == null
					: type.getId().equals(id) && type.getDescription() == null ? description == null : type
							.getDescription().equals(description)
							&& type.getAttributes() == null ? attributes == null : type.getAttributes().equals(
							attributes)
							&& type.getAttributesUri() == null ? attributesUri == null : type.getAttributesUri()
							.equals(attributesUri)) {

				equal = true;
			}
		}
		return equal;
	}

	@Override
	public String key() {
		return this.getId();
	}

}
