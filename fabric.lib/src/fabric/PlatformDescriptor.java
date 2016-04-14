/*
 * (C) Copyright IBM Corp. 2008, 2014
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric;

import java.util.NoSuchElementException;
import java.util.StringTokenizer;

/**
 * Immutable data structure describing a platform.
 */
public class PlatformDescriptor {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2008, 2012";

    /*
     * Class fields
     */

    /** The ID of the platform. */
    private String platform = null;

    /** The type of the platform. */
    private String platformType = null;

    /** The string representation of this instance. */
    private String toStringDescriptor = null;

    /*
     * Class methods
     */

    /**
     * Constructs a new instance.
     */
    protected PlatformDescriptor() {
    }

    /**
     * Constructs a new instance based upon an existing instance.
     * 
     * @param source
     *            the platform descriptor to replicate.
     */
    public PlatformDescriptor(PlatformDescriptor source) {

        platform = source.platform;
        platformType = source.platformType;

    }

    /**
     * Constructs a new instance by splitting a platform descriptor the format:
     * <p>
     * <code>platform-id:platform-type</code>
     * </p>
     * into its component parts.
     * 
     * @param name
     *            the platform descriptor to split.
     */
    public PlatformDescriptor(String name) {

        StringTokenizer platformDescriptorTokenizer = new StringTokenizer(name, "/");
        splitPlatformName(platformDescriptorTokenizer);

    }

    /**
     * Splits a platform descriptor into its component parts and records them.
     * 
     * @param nameTokenizer
     *            the tokenizer used to split the name.
     */
    protected void splitPlatformName(StringTokenizer nameTokenizer) {

        String exceptionMessage = "Invalid number of tokens in platform descriptor [%s] (valid format is \"<platform-id>[:<platform-type>]\")";

        try {

            String[] platformParts = nameTokenizer.nextToken().split(":");

            switch (platformParts.length) {
                case 2:
                    platformType = platformParts[1];
                case 1:
                    platform = platformParts[0];
                    break;
                default:
                    throw new IllegalArgumentException(exceptionMessage);
            }

        } catch (NoSuchElementException e) {
            throw new IllegalArgumentException(exceptionMessage);
        }
    }

    /**
     * Answers the ID of the platform.
     * 
     * @return the platform ID.
     */
    public String platform() {

        return platform;
    }

    /**
     * Answers the type of the platform.
     * 
     * @return the platform type, or <code>null</code> if it has not been set in the descriptor.
     */
    public String platformType() {

        return platformType;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return platform;

    }

    /**
     * Generates the string representation of this descriptor.
     * 
     * @return the platform descriptor
     */
    public String toFullDescriptor() {

        /* If we need to generate the string form of this instance... */
        if (toStringDescriptor == null) {
            toStringDescriptor = platform + ((platformType != null) ? ':' + platformType : "");
        }

        return toStringDescriptor;

    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {

        /* To hold the result */
        boolean isEqual = false;

        if (obj != null) {
            isEqual = toString().equals(obj.toString());
        }

        return isEqual;
    }

    /**
     * Checks two instances for equality based upon name only.
     * 
     * @param obj
     *            the object to test.
     */
    public boolean equalsDescriptor(PlatformDescriptor obj) {

        /* To hold the result */
        boolean isEqual = false;

        if (obj != null) {
            isEqual = toFullDescriptor().equals(obj.toFullDescriptor());
        }

        return isEqual;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return toString().hashCode();
    }
}
