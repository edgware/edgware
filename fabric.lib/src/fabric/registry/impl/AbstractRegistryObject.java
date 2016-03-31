/*
 * (C) Copyright IBM Corp. 2009
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry.impl;

import fabric.registry.RegistryObject;
import fabric.registry.exception.IncompleteObjectException;

/**
 * Abstract super class for all Registry objects.
 */
public abstract class AbstractRegistryObject implements RegistryObject, Cloneable {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009";

	protected RegistryObject shadow = null;

	protected AbstractRegistryObject() {
	}

	/**
	 * Creates a shadow (clone) of this object, preserving the original fields.
	 * 
	 * This functionality is used internally when performing updates - if an object has its primary fields intentionally
	 * modified, need to be able to update the original with the new values.
	 */
	public void createShadow() {
		try {
			shadow = (AbstractRegistryObject) this.clone();
		} catch (CloneNotSupportedException e) {
			System.err.println("Failed to clone object!!!");
			e.printStackTrace();
		}
	}

	/**
	 * Returns the shadow of this object - this is a protected clone of the object, representing the original state of
	 * the object prior to any modification (i.e. when it was last read from the database).
	 * 
	 * If no shadow exists (i.e. this object is newly created in memory and not yet saved), this method will return
	 * null.
	 */
	@Override
	public RegistryObject getShadow() {
		RegistryObject clone = null;
		try {
			if (shadow != null) {
				/* return a clone of the clone, since we don't want the user to be able to modify this object */
				clone = (RegistryObject) ((AbstractRegistryObject) shadow).clone();
			}
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return clone;
	}

	@Override
	public boolean hasChanged() {
		return !this.equals(shadow);
	}

	/**
	 * Default implementation of isValid() which invokes the validate() method.
	 * 
	 * Subclasses are responsible for implementing validate().
	 * 
	 * @return true if the state of this object is considered valid; false otherwise
	 */
	@Override
	public boolean isValid() {
		try {
			this.validate();
			return true;
		} catch (IncompleteObjectException e) {
			return false;
		}
	}
}
