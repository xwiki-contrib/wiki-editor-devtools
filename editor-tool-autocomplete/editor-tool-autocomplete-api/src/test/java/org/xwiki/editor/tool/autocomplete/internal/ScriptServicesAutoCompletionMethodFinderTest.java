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

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.*;
import org.xwiki.component.descriptor.ComponentDescriptor;
import org.xwiki.component.descriptor.DefaultComponentDescriptor;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.editor.tool.autocomplete.HintData;
import org.xwiki.editor.tool.autocomplete.Hints;
import org.xwiki.script.service.ScriptService;
import org.xwiki.test.mockito.MockitoComponentMockingRule;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ScriptServicesAutoCompletionMethodFinder}.
 *
 * @version $Id:$
 */
public class ScriptServicesAutoCompletionMethodFinderTest
{
    @Rule
    public MockitoComponentMockingRule<ScriptServicesAutoCompletionMethodFinder> mocker =
        new MockitoComponentMockingRule<ScriptServicesAutoCompletionMethodFinder>(
            ScriptServicesAutoCompletionMethodFinder.class);

    @Test
    public void findMethodsWhenMatchingService() throws Exception
    {
        ComponentManager componentManager = mocker.getInstance(ComponentManager.class);
        DefaultComponentDescriptor<?> descriptor = new DefaultComponentDescriptor<ScriptService>();
        descriptor.setRoleHint("query");
        List<ComponentDescriptor<Object>> descriptors =
            Arrays.asList((ComponentDescriptor<Object>) descriptor);
        when(componentManager.getComponentDescriptorList((Type) ScriptService.class)).thenReturn(descriptors);

        Hints hints = mocker.getComponentUnderTest().findMethods(null, "q");
        assertEquals(1, hints.getHints().size());
        assertEquals(new HintData("query", "query"), hints.getHints().first());
    }

    @Test
    public void findMethodsWhenNotMatchingService() throws Exception
    {
        ComponentManager componentManager = mocker.getInstance(ComponentManager.class);
        DefaultComponentDescriptor<?> descriptor = new DefaultComponentDescriptor<ScriptService>();
        List<ComponentDescriptor<Object>> descriptors =
            Arrays.asList((ComponentDescriptor<Object>) descriptor);
        when(componentManager.getComponentDescriptorList((Type) ScriptService.class)).thenReturn(descriptors);

        Hints hints = mocker.getComponentUnderTest().findMethods(null, "q");
        assertEquals(0, hints.getHints().size());
    }

    @Test
    public void findMethodsWhenNoScriptService() throws Exception
    {
        ComponentManager componentManager = mocker.getInstance(ComponentManager.class);
        when(componentManager.getComponentDescriptorList((Type) ScriptService.class)).thenReturn(
            Collections.EMPTY_LIST);

        Hints hints = mocker.getComponentUnderTest().findMethods(null, "");
        assertEquals(0, hints.getHints().size());
    }
}
