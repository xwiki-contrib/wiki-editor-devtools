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

import java.util.Arrays;
import java.util.Vector;

import org.jmock.Expectations;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.editor.tool.autocomplete.AutoCompletionMethodFinder;
import org.xwiki.test.jmock.AbstractMockingComponentTestCase;
import org.xwiki.test.jmock.annotation.MockingRequirement;

import com.xpn.xwiki.plugin.XWikiPluginInterface;
import com.xpn.xwiki.plugin.XWikiPluginManager;

/**
 * Unit tests for {@link XWikiAutoCompletionMethodFinder}.
 * 
 * @version $Id$
 * @since 5.2M1
 */
@MockingRequirement(TestableXWikiAutoCompletionMethodFinder.class)
public class XWikiAutoCompletionMethodFinderTest extends AbstractMockingComponentTestCase<AutoCompletionMethodFinder>
{
    private TestableXWikiAutoCompletionMethodFinder finder;

    private class TestClass
    {
    }

    @Before
    public void setImposteriser() throws Exception
    {
        getMockery().setImposteriser(ClassImposteriser.INSTANCE);
        
        this.finder = (TestableXWikiAutoCompletionMethodFinder) getMockedComponent();
    }

    @Test
    public void findMethodsWhenMatching() throws Exception
    {
        final AutoCompletionMethodFinder defaultMethodFinder =
            getComponentManager().getInstance(AutoCompletionMethodFinder.class);
        final XWikiPluginManager pluginManager = getMockery().mock(XWikiPluginManager.class);
        this.finder.setXWikiPluginManager(pluginManager);

        final XWikiPluginInterface plugin = getMockery().mock(XWikiPluginInterface.class);
        getMockery().checking(new Expectations()
        {
            {
                oneOf(defaultMethodFinder).findMethods(TestClass.class, "t");
                will(returnValue(new Hints()));
                oneOf(pluginManager).getPlugins();
                will(returnValue(new Vector<String>(Arrays.asList("tag", "query"))));
                oneOf(pluginManager).getPlugin("tag");
                will(returnValue(plugin));
            }
        });

        Hints hints = this.finder.findMethods(TestClass.class, "t");

        Assert.assertEquals(1, hints.getHints().size());
        Assert.assertEquals("tag", hints.getHints().get(0).getName());
        Assert.assertEquals("tag " + plugin.getClass().getSimpleName(), hints.getHints().get(0).getDescription());
    }
}
