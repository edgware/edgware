/*
 * (C) Copyright IBM Corp. 2016
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.tools.rest.servlet;

import javax.servlet.annotation.WebServlet;

import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

/**
 * Jetty class that helps create the websocket for /nodeviewer calls
 */
@SuppressWarnings("serial")
@WebServlet(name = "Node Viewer Websocket Servlet", urlPatterns = {"/nodeviewer"})
public class NodeViewerServlet extends WebSocketServlet {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2016";

    @Override
    public void configure(WebSocketServletFactory factory) {
        factory.getPolicy().setIdleTimeout(Constants.timeout);
        factory.register(NodeViewerSocket.class);
    }
}