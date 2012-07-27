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
import java.util.Collections;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.editor.tool.autocomplete.AutoCompletionMethodFinder;
import org.xwiki.test.AbstractMockingComponentTestCase;
import org.xwiki.test.annotation.MockingRequirement;

import com.xpn.xwiki.XWikiContext;

/**
 * Unit tests for {@link org.xwiki.editor.tool.autocomplete.internal.ContextAutoCompletionMethodFinder}.
 *
 * @version $Id$
 * @since 4.2M1
 */
public class ContextAutoCompletionMethodFinderTest extends AbstractMockingComponentTestCase
{
    @MockingRequirement
    private ContextAutoCompletionMethodFinder finder;

    private class TestClass
    {
    }

    @Before
    public void setImposteriser()
    {
        getMockery().setImposteriser(ClassImposteriser.INSTANCE);
    }

    @Test
    public void findMethodsWhenMatching() throws Exception
    {
        final AutoCompletionMethodFinder defaultMethodFinder =
            getComponentManager().getInstance(AutoCompletionMethodFinder.class);
        final Execution execution = getComponentManager().getInstance(Execution.class);
        final ExecutionContext executionContext = new ExecutionContext();
        final XWikiContext context = getMockery().mock(XWikiContext.class);
        executionContext.setProperty("xwikicontext", context);

        getMockery().checking(new Expectations()
        {
            {
                oneOf(defaultMethodFinder).findMethods(TestClass.class, "");
                will(returnValue(new ArrayList<HintData>()));
                oneOf(execution).getContext();
                will(returnValue(executionContext));
                oneOf(context).keySet();
                will(returnValue(Collections.singleton("doc")));
                oneOf(context).get("doc");
                will(returnValue(new TestClass()));
            }
        });

        List<HintData> hints = this.finder.findMethods(TestClass.class, "");

        Assert.assertEquals(1, hints.size());
    }
}
