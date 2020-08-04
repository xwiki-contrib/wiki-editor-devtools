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

import java.util.Collections;

import javax.inject.Named;
import javax.inject.Provider;

import org.junit.jupiter.api.Test;
import org.xwiki.editor.tool.autocomplete.Hints;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import com.xpn.xwiki.XWikiContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ContextAutoCompletionMethodFinder}.
 * 
 * @version $Id:$
 */
@ComponentTest
class ContextAutoCompletionMethodFinderTest
{
    @InjectMockComponents
    @Named("context")
    private ContextAutoCompletionMethodFinder methodFinder;

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
        when(this.defaultMethodFinder.findMethods(TestClass.class, "")).thenReturn(new Hints());
        when(xcontext.keySet()).thenReturn(Collections.singleton("doc"));
        when(xcontext.get("doc")).thenReturn(new TestClass());

        Hints hints = this.methodFinder.findMethods(TestClass.class, "");

        assertEquals(1, hints.getHints().size());
    }
}
