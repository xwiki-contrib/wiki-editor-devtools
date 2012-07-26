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

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.xwiki.component.annotation.Component;
import org.xwiki.context.Execution;
import org.xwiki.editor.tool.autocomplete.AutoCompletionMethodFinder;
import org.xwiki.model.internal.DefaultModelContext;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.plugin.XWikiPluginManager;

/**
 * Returns autocompletion hints for the the {@link com.xpn.xwiki.XWiki} class, with special support for plugins.
 *
 * @version $Id$
 * @since 4.2M1
 */
@Component
@Named("xwiki")
@Singleton
public class XWikiAutoCompletionMethodFinder extends AbstractAutoCompletionMethodFinder
{
    /**
     * Used to find all methods.
     */
    @Inject
    private AutoCompletionMethodFinder defaultAutoCompletionMethodFinder;

    /**
     * @see #getXWikiContext()
     */
    @Inject
    private Execution execution;

    @Override
    public List<HintData> findMethods(Class variableClass, String fragmentToMatch)
    {
        List<HintData> hintData = this.defaultAutoCompletionMethodFinder.findMethods(variableClass, fragmentToMatch);

        // Add plugins matching the passed fragment
        String lowerCaseFragment = fragmentToMatch.toLowerCase();
        XWikiPluginManager pluginManager = getPluginManager();
        for (String pluginName : pluginManager.getPlugins()) {
            if (pluginName.toLowerCase().startsWith(lowerCaseFragment)) {
                String hintName = StringUtils.removeStart(pluginName, fragmentToMatch);
                String shorthand = printShorthand(pluginName, pluginManager.getPlugin(pluginName).getClass());
                hintData.add(new HintData(hintName, shorthand));
            }
        }

        return hintData;
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

    /**
     * @return the XWiki Context from which we get the XWiki Object which in turn allows us to get the XWiki Plugin
     *         Manager to get the list of plugins and the plugin types.
     */
    private XWikiContext getXWikiContext()
    {
        return (XWikiContext) this.execution.getContext().getProperty(DefaultModelContext.XCONTEXT_KEY);
    }
}
