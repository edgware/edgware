/*
 * (C) Copyright IBM Corp. 2009, 2012
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.logging.Logger;

import fabric.bus.messages.INotifier;

/**
 * Base Fabric class common utility methods to handle property change events between classes within the Fabric.
 */
public abstract class Notifier extends Fabric implements INotifier {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009, 2012";

	/*
	 * Class constants
	 */

	public Notifier(Logger logger) {
		super(logger);
	}

	/*
	 * Class static fields
	 */

	/** Property change helper class */
	private PropertyChangeSupport propertyChangeHelper = new PropertyChangeSupport(this);

	/*
	 * Class methods
	 */

	/**
	 * @see fabric.bus.messages.INotifier#addChangeListener(java.beans.PropertyChangeListener)
	 */
	@Override
	public void addChangeListener(PropertyChangeListener listener) {

		propertyChangeHelper.addPropertyChangeListener(listener);

	}

	/**
	 * @see fabric.bus.messages.INotifier#removeChangeListener(java.beans.PropertyChangeListener)
	 */
	@Override
	public void removeChangeListener(PropertyChangeListener listener) {

		propertyChangeHelper.removePropertyChangeListener(listener);

	}

	/**
	 * @see fabric.bus.messages.INotifier#fireChangeNotification(java.lang.String, java.lang.Object, java.lang.Object)
	 */
	@Override
	public void fireChangeNotification(String name, Object oldValue, Object newValue) {

		propertyChangeHelper.firePropertyChange(name, oldValue, newValue);

	}

	/**
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	@Override
	public void propertyChange(PropertyChangeEvent event) {

		/* The default action is to do nothing */

	}
}
