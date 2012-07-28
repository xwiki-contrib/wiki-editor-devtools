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

import javax.inject.Inject;

import org.xwiki.context.Execution;
import org.xwiki.model.internal.DefaultModelContext;

import com.xpn.xwiki.XWikiContext;

/**
 * To be extended by method finders that require getting the XWiki Context.
 *
 * @version $Id$
 * @since 4.2M2
 */
public abstract class AbstractXWikiContextAutoCompletionMethodFinder extends AbstractAutoCompletionMethodFinder
{
    /**
     * @see #getXWikiContext()
     */
    @Inject
    private Execution execution;

    /**
     * @return the XWiki Context from which we get the XWiki Object which in turn allows us to get the XWiki Plugin
     *         Manager to get the list of plugins and the plugin types.
     */
    protected XWikiContext getXWikiContext()
    {
        return (XWikiContext) this.execution.getContext().getProperty(DefaultModelContext.XCONTEXT_KEY);
    }
}
