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

import java.util.List;

import org.junit.Test;
import org.xwiki.test.AbstractMockingComponentTestCase;
import org.xwiki.test.annotation.MockingRequirement;

/**
 * Unit tests for {@link DefaultAutoCompletionMethodFinder}.
 *
 * @version $Id$
 * @since 4.2M1
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

        public String method2()
        {
            return "";
        }
    }

    @Test
    public void findMethodsWhenMatching()
    {
        List<HintData> hints = this.finder.findMethods(TestClass.class, "m");

        assertThat(hints, containsInAnyOrder(
            new HintData("ethod2", "method2() String"),
            new HintData("ethod1", "method1(String, AncillaryTestClass, int) void")
        ));
    }

    @Test
    public void findMethodsWhenMatchingGetter()
    {
        List<HintData> hints = this.finder.findMethods(TestClass.class, "so");

        assertThat(hints, containsInAnyOrder(
            new HintData("mething", "something String")
        ));
    }

    @Test
    public void findMethodsWhenCamelCaseMatching()
    {
        List<HintData> hints = this.finder.findMethods(TestClass.class, "getSo");

        assertThat(hints, containsInAnyOrder(
            new HintData("mething", "getSomething() String")
        ));
    }
}
