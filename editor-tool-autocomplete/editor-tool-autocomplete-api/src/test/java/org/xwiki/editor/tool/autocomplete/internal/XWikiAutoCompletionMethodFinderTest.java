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

import org.junit.*;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.editor.tool.autocomplete.AutoCompletionMethodFinder;
import org.xwiki.editor.tool.autocomplete.HintData;
import org.xwiki.editor.tool.autocomplete.Hints;
import org.xwiki.model.internal.DefaultModelContext;
import org.xwiki.test.mockito.MockitoComponentMockingRule;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.plugin.XWikiPluginInterface;
import com.xpn.xwiki.plugin.XWikiPluginManager;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

/**
 * Unit tests for {@link XWikiAutoCompletionMethodFinder}.
 * 
 * @version $Id$
 */
public class XWikiAutoCompletionMethodFinderTest
{
    @Rule
    public MockitoComponentMockingRule<XWikiAutoCompletionMethodFinder> mocker =
        new MockitoComponentMockingRule<XWikiAutoCompletionMethodFinder>(
            XWikiAutoCompletionMethodFinder.class);

    private XWikiAutoCompletionMethodFinder finder;

    private class TestClass
    {
    }

    @Before
    public void setUp() throws Exception
    {
        this.finder = mocker.getComponentUnderTest();
    }

    @Test
    public void findMethodsWhenMatching() throws Exception
    {
        Execution execution = mocker.getInstance(Execution.class);
        ExecutionContext executionContext = mock(ExecutionContext.class);
        when(execution.getContext()).thenReturn(executionContext);
        XWikiContext xwikiContext = mock(XWikiContext.class);
        when(executionContext.getProperty(DefaultModelContext.XCONTEXT_KEY)).thenReturn(xwikiContext);
        XWiki xwiki = mock(XWiki.class);
        when(xwikiContext.getWiki()).thenReturn(xwiki);
        XWikiPluginManager xwikiPluginManager = mock(XWikiPluginManager.class);
        when(xwiki.getPluginManager()).thenReturn(xwikiPluginManager);
        when(xwikiPluginManager.getPlugins()).thenReturn(new Vector<String>(Arrays.asList("tag", "query")));
        XWikiPluginInterface tagPlugin = mock(XWikiPluginInterface.class);
        when(xwikiPluginManager.getPlugin("tag")).thenReturn(tagPlugin);

        AutoCompletionMethodFinder defaultMethodFinder = mocker.getInstance(AutoCompletionMethodFinder.class);
        when(defaultMethodFinder.findMethods(TestClass.class, "t")).thenReturn(
            new Hints().withHints(new HintData("tag", "tag")));

        Hints hints = this.finder.findMethods(TestClass.class, "t");

        assertEquals(1, hints.getHints().size());
        assertEquals(new HintData("tag", "tag"), hints.getHints().first());
    }
}
