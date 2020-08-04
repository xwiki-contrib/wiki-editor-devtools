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
package org.xwiki.editor.tool.autocomplete.internal.velocity;

import java.util.Arrays;
import java.util.Vector;

import javax.inject.Provider;

import org.junit.jupiter.api.Test;
import org.xwiki.editor.tool.autocomplete.HintData;
import org.xwiki.editor.tool.autocomplete.Hints;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.plugin.XWikiPluginInterface;
import com.xpn.xwiki.plugin.XWikiPluginManager;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.in;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link XWikiAutoCompletionMethodFinder}.
 * 
 * @version $Id$
 */
@ComponentTest
class XWikiAutoCompletionMethodFinderTest
{
    @InjectMockComponents
    private XWikiAutoCompletionMethodFinder methodFinder;

    @MockComponent
    private AutoCompletionMethodFinder defaultMethodFinder;

    @MockComponent
    private Provider<XWikiContext> xwikiContextProvider;

    private class TestClass
    {
    }

    @Test
    void findMethodsWhenMatching()
    {
        XWikiContext xcontext = mock(XWikiContext.class);
        when(this.xwikiContextProvider.get()).thenReturn(xcontext);
        XWiki xwiki = mock(XWiki.class);
        when(xcontext.getWiki()).thenReturn(xwiki);
        XWikiPluginManager xwikiPluginManager = mock(XWikiPluginManager.class);
        when(xwiki.getPluginManager()).thenReturn(xwikiPluginManager);
        when(xwikiPluginManager.getPlugins()).thenReturn(new Vector<>(Arrays.asList("tag", "query")));
        XWikiPluginInterface tagPlugin = mock(XWikiPluginInterface.class);
        when(xwikiPluginManager.getPlugin("tag")).thenReturn(tagPlugin);
        when(this.defaultMethodFinder.findMethods(TestClass.class, "t")).thenReturn(
            new Hints().withHints(new HintData("tag", "tag")));

        Hints hints = this.methodFinder.findMethods(TestClass.class, "t");

        // 2 because it also contains
        // name = [tag], description = [tag XWikiPluginInterface$MockitoMock$675869742]
        assertEquals(2, hints.getHints().size());
        assertThat(new HintData("tag", "tag"), is(in(hints.getHints())));
    }
}
