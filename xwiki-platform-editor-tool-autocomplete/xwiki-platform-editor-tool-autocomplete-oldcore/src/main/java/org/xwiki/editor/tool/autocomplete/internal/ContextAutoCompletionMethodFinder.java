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
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.editor.tool.autocomplete.AutoCompletionMethodFinder;

import com.xpn.xwiki.XWikiContext;

/**
 * Returns autocompletion hints for the the {@link com.xpn.xwiki.api.Context} class, with special support for context
 * keys.
 * 
 * @version $Id$
 * @since 4.2M2
 */
@Component(hints = { "context", "xcontext" })
@Singleton
public class ContextAutoCompletionMethodFinder extends AbstractXWikiContextAutoCompletionMethodFinder
{
    /**
     * Used to find all methods.
     */
    @Inject
    private AutoCompletionMethodFinder defaultAutoCompletionMethodFinder;

    @Override
    public Hints findMethods(Class variableClass, String fragmentToMatch)
    {
        Hints hints = this.defaultAutoCompletionMethodFinder.findMethods(variableClass, fragmentToMatch);

        // Add context keys matching the passed fragment
        String lowerCaseFragment = fragmentToMatch.toLowerCase();
        XWikiContext context = getXWikiContext();
        for (Object key : context.keySet()) {
            if (key instanceof String && ((String) key).toLowerCase().startsWith(lowerCaseFragment)) {
                String shorthand = printShorthand((String) key, context.get(key).getClass());
                hints.withHints(new HintData((String) key, shorthand));
            }
        }

        return hints;
    }
}
