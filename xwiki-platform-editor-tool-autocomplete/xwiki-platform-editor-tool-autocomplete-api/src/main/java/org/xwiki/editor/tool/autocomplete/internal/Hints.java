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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Represents a list of hints that are returned.
 *
 * @version $Id$
 * @since 4.1M2
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "hints")
public class Hints
{
    /**
     * @see #getHints()
     */
    @XmlElement(name = "hint")
    protected List<String> hints;

    /**
     * @return the autocompletion hints
     */
    public List<String> getHints()
    {
        if (this.hints == null) {
            this.hints = new ArrayList<String>();
        }
        return this.hints;
    }

    /**
     * Build an instance of {@link Hints}.
     *
     * @param values the autocompletion hints to populate the instance with
     * @return the {@link Hints} containing the passed hints
     */
    public Hints withHints(String... values) {
        if (values != null) {
            for (String value: values) {
                getHints().add(value);
            }
        }
        return this;
    }

    /**
     * Build an instance of {@link Hints}.
     *
     * @param values the autocompletion hints to populate the instance with
     * @return the {@link Hints} containing the passed hints
     */
    public Hints withHints(Collection<String> values) {
        if (values != null) {
            getHints().addAll(values);
        }
        return this;
    }
}
