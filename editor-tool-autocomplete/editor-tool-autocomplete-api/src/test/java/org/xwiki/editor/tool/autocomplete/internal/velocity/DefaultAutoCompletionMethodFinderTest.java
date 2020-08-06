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

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.jupiter.api.Test;
import org.xwiki.editor.tool.autocomplete.HintData;
import org.xwiki.editor.tool.autocomplete.Hints;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;

import com.xpn.xwiki.util.Programming;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link DefaultAutoCompletionMethodFinder}.
 *
 * @version $Id$
 */
@ComponentTest
class DefaultAutoCompletionMethodFinderTest
{
    @InjectMockComponents
    private DefaultAutoCompletionMethodFinder methodFinder;

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

        public String getSomething(List<String> parameter)
        {
            return "";
        }

        public String display()
        {
            return "";
        }

        public String display(String param1)
        {
            return "";
        }

        public String display(String param1, String param2)
        {
            return "";
        }

        public String display(String param1, Object param2)
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
    void findMethodsWhenMatching()
    {
        Hints hints = this.methodFinder.findMethods(TestClass.class, "m");

        assertThat(hints.getHints(), containsInAnyOrder(
            new HintData("method2", "method2() String (Programming Rights)"),
            new HintData("method1", "method1(String, AncillaryTestClass, int) void")));
    }

    @Test
    void findMethodsWhenMatchingGetter()
    {
        Hints hints = this.methodFinder.findMethods(TestClass.class, "so");

        assertThat(hints.getHints(), containsInAnyOrder(
            new HintData("getSomething", "getSomething() String"),
            new HintData("getSomething", "getSomething(List<String>) String"),
            new HintData("something", "something String")));
    }

    @Test
    void findMethodsWhenCamelCaseMatching()
    {
        Hints hints = this.methodFinder.findMethods(TestClass.class, "getSo");

        SortedSet<HintData> expected = new TreeSet<>();
        expected.add(new HintData("getSomething", "getSomething() String"));
        expected.add(new HintData("getSomething", "getSomething(List<String>) String"));
        assertEquals(expected, hints.getHints());
    }

    @Test
    void findMethods()
    {
        Hints hints = this.methodFinder.findMethods(TestClass.class, "");

        assertThat(hints.getHints(), containsInAnyOrder(
            new HintData("class", "class Class<?>"),
            new HintData("getClass", "getClass() Class<?>"),
            new HintData("equals", "equals(Object) boolean"),
            new HintData("hashCode", "hashCode() int"),
            new HintData("notify", "notify() void"),
            new HintData("notifyAll", "notifyAll() void"),
            new HintData("toString", "toString() String"),
            new HintData("wait", "wait() void"),
            new HintData("wait", "wait(long) void"),
            new HintData("wait", "wait(long, int) void"),
            new HintData("doWork", "doWork() AncillaryTestClass"),
            new HintData("method1", "method1(String, AncillaryTestClass, int) void"),
            new HintData("method2", "method2() String (Programming Rights)"),
            new HintData("getSomething", "getSomething() String"),
            new HintData("getSomething", "getSomething(List<String>) String"),
            new HintData("something", "something String"),
            new HintData("display", "display() String"),
            new HintData("display", "display(String) String"),
            new HintData("display", "display(String, String) String"),
            new HintData("display", "display(String, Object) String")));
    }

    @Test
    void findMethodsWithSameNameDifferentParameters()
    {
        Hints hints = this.methodFinder.findMethods(TestClass.class, "display");

        assertEquals(4, hints.getHints().size());
        assertThat(hints.getHints(), containsInAnyOrder(
            new HintData("display", "display() String"),
            new HintData("display", "display(String) String"),
            new HintData("display", "display(String, String) String"),
            new HintData("display", "display(String, Object) String")));
    }

    @Test
    void excludeAspectJMethods()
    {
        Hints hints = this.methodFinder.findMethods(TestClass.class, "a");

        assertEquals(0, hints.getHints().size());
    }

    @Test
    void findMethodReturnTypes()
    {
        List<Class<?>> types = this.methodFinder.findMethodReturnTypes(TestClass.class, "doWork");
        assertEquals(1, types.size());
        assertEquals(AncillaryTestClass.class, types.get(0));
    }
}
