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

import org.junit.*;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.editor.tool.autocomplete.HintData;
import org.xwiki.editor.tool.autocomplete.Hints;
import org.xwiki.script.service.ScriptService;
import org.xwiki.test.mockito.MockitoComponentMockingRule;

import static org.junit.Assert.assertEquals;

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
        MockitoComponentMockingRule cm = mocker.getInstance(ComponentManager.class);
        cm.registerMockComponent(ScriptService.class, "query");

        Hints hints = mocker.getComponentUnderTest().findMethods(null, "q");
        assertEquals(1, hints.getHints().size());
        assertEquals(new HintData("query", "query"), hints.getHints().first());
    }

    @Test
    public void findMethodsWhenNotMatchingService() throws Exception
    {
        MockitoComponentMockingRule cm = mocker.getInstance(ComponentManager.class);
        cm.registerMockComponent(ScriptService.class);

        Hints hints = mocker.getComponentUnderTest().findMethods(null, "q");
        assertEquals(0, hints.getHints().size());
    }

    @Test
    public void findMethodsWhenNoScriptService() throws Exception
    {
        Hints hints = mocker.getComponentUnderTest().findMethods(null, "");
        assertEquals(0, hints.getHints().size());
    }

    @Test
    public void findMethodReturnTypesWhenExists() throws Exception
    {
        MockitoComponentMockingRule cm = mocker.getInstance(ComponentManager.class);
        ScriptService scriptService = cm.registerMockComponent(ScriptService.class, "query");

        List<Class> types = mocker.getComponentUnderTest().findMethodReturnTypes(null, "query");
        assertEquals(1, types.size());
        assertEquals(scriptService.getClass(), types.get(0));
    }

    @Test
    public void findMethodReturnTypesWhenDoesntExists() throws Exception
    {
        // "unknown" is supposed to be a hint for a ScriptService that doesn't exist. We verify we don't throw any
        // exception and return an empty result.
        List<Class> types = mocker.getComponentUnderTest().findMethodReturnTypes(null, "unknown");
        assertEquals(0, types.size());
    }
}
