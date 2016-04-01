/*
 * (C) Copyright IBM Corp. 2013, 2016
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.script;


/**
 */
public class RunScript extends JSONScript {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2013, 2016";

    public static void main(final String args[]) {

        try {

            RunScript rs = new RunScript();
            rs.init(args[2], args[0], args[1], "1883", args[3]);
            rs.runScript();
            rs.stop();
            System.exit(0);

        } catch (Exception e) {

            e.printStackTrace();

        }

    }
}