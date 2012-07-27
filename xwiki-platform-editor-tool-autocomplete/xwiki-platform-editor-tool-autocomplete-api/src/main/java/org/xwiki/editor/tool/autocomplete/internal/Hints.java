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
 * @since 4.2M2
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "hints")
public class Hints
{
    /**
     * @see #getHints()
     */
    @XmlElement(name = "hint")
    private List<HintData> hints;

    /**
     * @see #getStartOffset()
     */
    @XmlElement(name = "startOffset")
    private int startOffset;

    /**
     * @return the autocompletion hints
     */
    public List<HintData> getHints()
    {
        if (this.hints == null) {
            this.hints = new ArrayList<HintData>();
        }
        return this.hints;
    }

    /**
     * Build an instance of {@link Hints}.
     * 
     * @param values the autocompletion hints to populate the instance with
     * @return the {@link Hints} containing the passed hints
     */
    public Hints withHints(HintData... values)
    {
        if (values != null) {
            for (HintData value : values) {
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
    public Hints withHints(Collection<HintData> values)
    {
        if (values != null) {
            getHints().addAll(values);
        }
        return this;
    }

    /**
     * @return the replaceFrom
     */
    public int getStartOffset()
    {
        return startOffset;
    }

    /**
     * Build an instance of {@link Hints}.
     * 
     * @param startOffset the start of the user input that needs to be replaced on the client side with the selected
     *            autocompletion hint.
     * @return the {@link Hints} containing the passed hints
     */
    public Hints withStartOffset(int startOffset)
    {
        this.startOffset = startOffset;

        return this;
    }

    /**
     * Build an instance of {@link Hints}.
     * 
     * @param otherHints another instance to populate this instance with
     * @return the {@link Hints} containing also the hints from the passed instance
     */
    public Hints withHints(Hints otherHints)
    {
        this.withHints(otherHints.getHints());
        return this;
    }

    /**
     * @return true if the list of hints is empty.
     */
    public boolean isEmpty()
    {
        return getHints().isEmpty();
    }
}
