/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.editor.tool.autocomplete.internal;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Represents hint data (name of hint and description of hint).
 *
 * @version $Id$
 * @since 4.2M2
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class HintData implements Comparable<HintData>
{
    /**
     * @see #getName()
     */
    @XmlElement(name = "name")
    private String name;

    /**
     * @see #getDescription()
     */
    @XmlElement(name = "description")
    private String description;

    /**
     * @param name see {@link #getName()}
     * @param description see {@link #getDescription()}
     */
    public HintData(String name, String description)
    {
        this.name = name;
        this.description = description;
    }

    /**
     * @return the hint description
     */
    public String getDescription()
    {
        return this.description;
    }

    /**
     * @return the hint name (ie what will get inserted if the user picks it)
     */
    public String getName()
    {
        return this.name;
    }

    @Override
    public int compareTo(HintData hintData)
    {
        return getName().compareTo(hintData.getName());
    }

    @Override
    public boolean equals(Object object)
    {
        if (object == null) {
            return false;
        }
        if (object == this) {
            return true;
        }
        if (object.getClass() != getClass()) {
            return false;
        }
        HintData rhs = (HintData) object;
        return new EqualsBuilder()
            .append(getName(), rhs.getName())
            .append(getDescription(), getDescription())
            .isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(7, 3)
            .append(this.name)
            .append(this.description)
            .toHashCode();
    }

    @Override
    public String toString()
    {
        return String.format("name = [%s], description = [%s]", getName(), getDescription());
    }
}
