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

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;
import junit.framework.Assert;

import org.junit.Test;
import org.xwiki.test.AbstractMockingComponentTestCase;
import org.xwiki.test.annotation.MockingRequirement;

import com.xpn.xwiki.util.Programming;

/**
 * Unit tests for {@link DefaultAutoCompletionMethodFinder}.
 * 
 * @version $Id$
 * @since 4.2M2
 */
public class DefaultAutoCompletionMethodFinderTest extends AbstractMockingComponentTestCase
{
    @MockingRequirement
    private DefaultAutoCompletionMethodFinder finder;

    private class AncillaryTestClass
    {
        public void method()
        {
        }
    }

    private class TestClass
    {
        public AncillaryTestClass doWork()
        {
            return new AncillaryTestClass();
        }

        public String getSomething()
        {
            return "";
        }

        public void method1(String param1, AncillaryTestClass param2, int param3)
        {
        }

        @Programming
        public String method2()
        {
            return "";
        }

        // Simulates an aspectj method
        public void ajc$something()
        {
        }
    }

    @Test
    public void findMethodsWhenMatching()
    {
        Hints hints = this.finder.findMethods(TestClass.class, "m");

        assertThat(
            hints.getHints(),
            containsInAnyOrder(new HintData("method2", "method2() String (Programming Rights)"), new HintData(
                "method1", "method1(String, AncillaryTestClass, int) void")));
    }

    @Test
    public void findMethodsWhenMatchingGetter()
    {
        Hints hints = this.finder.findMethods(TestClass.class, "so");

        assertThat(hints.getHints(), containsInAnyOrder(new HintData("something", "something String")));
    }

    @Test
    public void findMethodsWhenCamelCaseMatching()
    {
        Hints hints = this.finder.findMethods(TestClass.class, "getSo");

        assertThat(hints.getHints(), containsInAnyOrder(new HintData("getSomething", "getSomething() String")));
    }

    @Test
    public void excludeAspectJMethods()
    {
        Hints hints = this.finder.findMethods(TestClass.class, "a");

        Assert.assertEquals(0, hints.getHints().size());
    }

}
