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
import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.editor.tool.autocomplete.AutoCompletionMethodFinder;

import com.xpn.xwiki.plugin.XWikiPluginManager;

/**
 * Returns autocompletion hints for the the {@link com.xpn.xwiki.XWiki} class, with special support for plugins.
 * 
 * @version $Id$
 * @since 4.2M2
 */
@Component
@Named("xwiki")
@Singleton
public class XWikiAutoCompletionMethodFinder extends AbstractXWikiContextAutoCompletionMethodFinder
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

        // Add plugins matching the passed fragment
        String lowerCaseFragment = fragmentToMatch.toLowerCase();
        XWikiPluginManager pluginManager = getPluginManager();
        for (String pluginName : pluginManager.getPlugins()) {
            if (pluginName.toLowerCase().startsWith(lowerCaseFragment)) {
                String shorthand = printShorthand(pluginName, pluginManager.getPlugin(pluginName).getClass());
                hints.withHints(new HintData(pluginName, shorthand));
            }
        }

        return hints;
    }

    /**
     * Makes it easy for extending classes to override how the Plugin Manager is retrieved (useful for example when
     * writing unit tests).
     * 
     * @return the plugin manager instance
     */
    protected XWikiPluginManager getPluginManager()
    {
        return getXWikiContext().getWiki().getPluginManager();
    }
}
