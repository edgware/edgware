/*
 * (C) Copyright IBM Corp. 2009
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.bus.messages;

import java.beans.PropertyChangeListener;

/**
 * Interface for classes that notify listeners of changes to their properties.
 */
public interface INotifier extends PropertyChangeListener {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009";

	/*
	 * Interface methods
	 */

	/**
	 * Adds a listener for changes to this instance.
	 * 
	 * @param listener
	 *            the listening object to add.
	 */
	public void addChangeListener(PropertyChangeListener listener);

	/**
	 * Removes a listener for changes to this instance.
	 * 
	 * @param listener
	 *            the listening object to remove.
	 */
	public void removeChangeListener(PropertyChangeListener listener);

	/**
	 * Notifiers listeners of changes to this instance.
	 * 
	 * @param name
	 *            the name of the property that has changed.
	 * 
	 * @param oldValue
	 *            the old value of the property.
	 * 
	 * @param newValue
	 *            the new value of the property.
	 */
	public void fireChangeNotification(String name, Object oldValue, Object newValue);

}