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
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.xwiki.editor.tool.autocomplete.HintData;
import org.xwiki.editor.tool.autocomplete.Hints;
import org.xwiki.test.mockito.MockitoComponentMockingRule;

import com.xpn.xwiki.util.Programming;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * Unit tests for {@link DefaultAutoCompletionMethodFinder}.
 *
 * @version $Id$
 */
public class DefaultAutoCompletionMethodFinderTest
{
    @Rule
    public MockitoComponentMockingRule<DefaultAutoCompletionMethodFinder> mocker =
        new MockitoComponentMockingRule<>(DefaultAutoCompletionMethodFinder.class);

    private DefaultAutoCompletionMethodFinder finder;

    @Before
    public void setUp() throws Exception
    {
        this.finder = mocker.getComponentUnderTest();
    }

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

        public String getSomething(String parameter)
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

        SortedSet<HintData> expected = new TreeSet<>();
        expected.add(new HintData("getSomething", "getSomething() String"));
        expected.add(new HintData("getSomething", "getSomething(String) String"));
        assertEquals(expected, hints.getHints());
    }

    @Test
    public void excludeAspectJMethods()
    {
        Hints hints = this.finder.findMethods(TestClass.class, "a");

        assertEquals(0, hints.getHints().size());
    }

    @Test
    public void findMethodReturnTypes()
    {
        List<Class<?>> types = this.finder.findMethodReturnTypes(TestClass.class, "doWork");
        assertEquals(1, types.size());
        assertEquals(AncillaryTestClass.class, types.get(0));
    }
}
