/*
 * (C) Copyright IBM Corp. 2014
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry.impl;

import fabric.registry.DefaultConfig;
import fabric.registry.exception.IncompleteObjectException;

/**
 * Implementation class for Fabric <code>DefaultConfig</code>.
 */
public class DefaultConfigImpl extends AbstractRegistryObject implements DefaultConfig {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2014";

    private String name = null;

    private String value = null;

    protected DefaultConfigImpl() {

    }

    protected DefaultConfigImpl(String name, String value) {

        this.name = name;
        this.value = value;
    }

    /*
     * (non-Javadoc)
     * @see fabric.registry.DefaultConfig#getName()
     */
    @Override
    public String getName() {

        return name;
    }

    /*
     * (non-Javadoc)
     * @see fabric.registry.DefaultConfig#setName(java.lang.String)
     */
    @Override
    public void setName(String name) {

        this.name = name;

    }

    /*
     * (non-Javadoc)
     * @see fabric.registry.DefaultConfig#getValue()
     */
    @Override
    public String getValue() {

        return value;
    }

    /*
     * (non-Javadoc)
     * @see fabric.registry.DefaultConfig#setValue(java.lang.String)
     */
    @Override
    public void setValue(String value) {

        this.value = value;
    }

    /*
     * (non-Javadoc)
     * @see fabric.registry.RegistryObject#validate()
     */
    @Override
    public void validate() throws IncompleteObjectException {

        if (name == null || name.length() == 0 || value == null || value.length() == 0) {
            throw new IncompleteObjectException("Name or value not specified.");
        }
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        StringBuilder buffy = new StringBuilder("DefaultConfig::");
        buffy.append(" Name: ").append(name);
        buffy.append(", Value: ").append(value);
        return buffy.toString();
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {

        boolean isEqual = false;
        if (obj instanceof DefaultConfigImpl) {

            DefaultConfigImpl dc = (DefaultConfigImpl) obj;

            if (dc.getName() == null ? name == null
                    : dc.getName().equals(name) && dc.getValue() == null ? value == null : dc.getValue().equals(value)) {

                isEqual = true;
            }
        }

        return isEqual;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        return toString().hashCode();
    }

    /*
     * (non-Javadoc)
     * @see fabric.registry.RegistryObject#key()
     */
    @Override
    public String key() {

        return name;
    }

}
