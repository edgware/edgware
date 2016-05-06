/*
 * (C) Copyright IBM Corp. 2010
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.bus.messages;

/**
 * Interface for a Fabric notification message, used by the Fabric to trigger asynchronous handling of events.
 */
public interface INotificationMessage extends IServiceMessage {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2010";

    /*
     * Interface methods
     */

    /**
     * Answers the notification event (i.e. the causing event) code associated with this message.
     *
     * @return the event code.
     */
    public String getNotificationEvent();

    /**
     * Sets the notification event (i.e. the causing event) code associated with this message.
     *
     * @param event
     *            the event code.
     */
    public void setNotificationEvent(String event);

    /**
     * Answers the notification action (i.e. the causing action) associated with this message.
     *
     * @return the action.
     */
    public String getNotificationAction();

    /**
     * Sets the notification action (i.e. the causing action) code associated with this message.
     *
     * @param action
     *            the action.
     */
    public void setNotificationAction(String event);

    /**
     * Gets the message-specific argument string from the notification message.
     * <p>
     * This value will be added to any client notification messages sent in response to this message, and is a mechanism
     * that can be used to customize the value of the client notification.
     *
     * @return the argument string.
     */
    public String getNotificationArgs();

    /**
     * Sets the message-specific argument string in the notification message.
     * <p>
     * This value will be added to any client notification messages sent in response to this message, and is a mechanism
     * that can be used to customize the value of the client notification.
     *
     * @param args
     *            the argument string.
     */
    public void setNotificationArgs(String args);

}
