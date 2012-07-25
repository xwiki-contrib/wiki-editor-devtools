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
package org.xwiki.editor.tool.autocomplete;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class TargetContent
{
    private String content;

    private TargetContentType type;

    private int position;

    public TargetContent(String content, int position, TargetContentType type)
    {
        this.content = content;
        this.position = position;
        this.type = type;
    }

    public String getContent()
    {
        return this.content;
    }

    public TargetContentType getType()
    {
        return this.type;
    }

    public int getPosition()
    {
        return this.position;
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
        TargetContent rhs = (TargetContent) object;
        return new EqualsBuilder()
            .append(this.content, rhs.content)
            .append(this.type, rhs.type)
            .append(this.position, rhs.position)
            .isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(5, 5)
            .append(this.content)
            .append(this.type)
            .append(this.position)
            .toHashCode();
    }
}
