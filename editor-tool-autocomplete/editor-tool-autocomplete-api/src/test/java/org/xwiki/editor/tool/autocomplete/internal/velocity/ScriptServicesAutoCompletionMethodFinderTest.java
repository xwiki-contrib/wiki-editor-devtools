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

import org.junit.jupiter.api.Test;
import org.xwiki.editor.tool.autocomplete.HintData;
import org.xwiki.editor.tool.autocomplete.Hints;
import org.xwiki.script.service.ScriptService;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectComponentManager;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.mockito.MockitoComponentManager;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link ScriptServicesAutoCompletionMethodFinder}.
 *
 * @version $Id:$
 */
@ComponentTest
class ScriptServicesAutoCompletionMethodFinderTest
{
    @InjectMockComponents
    private ScriptServicesAutoCompletionMethodFinder methodFinder;

    @InjectComponentManager
    private MockitoComponentManager componentManager;

    @Test
    void findMethodsWhenMatchingService() throws Exception
    {
        this.componentManager.registerMockComponent(ScriptService.class, "query");

        Hints hints = this.methodFinder.findMethods(null, "q");
        assertEquals(1, hints.getHints().size());
        assertEquals(new HintData("query", "query"), hints.getHints().first());
    }

    @Test
    void findMethodsWhenNotMatchingService() throws Exception
    {
        this.componentManager.registerMockComponent(ScriptService.class);

        Hints hints = this.methodFinder.findMethods(null, "q");
        assertEquals(0, hints.getHints().size());
    }

    @Test
    void findMethodsWhenNoScriptService()
    {
        Hints hints = this.methodFinder.findMethods(null, "");
        assertEquals(0, hints.getHints().size());
    }

    @Test
    void findMethodReturnTypesWhenExists() throws Exception
    {
        ScriptService scriptService = this.componentManager.registerMockComponent(ScriptService.class, "query");

        List<Class<?>> types = this.methodFinder.findMethodReturnTypes(null, "query");
        assertEquals(1, types.size());
        assertEquals(scriptService.getClass(), types.get(0));
    }

    @Test
    void findMethodReturnTypesWhenDoesntExists()
    {
        // "unknown" is supposed to be a hint for a ScriptService that doesn't exist. We verify we don't throw any
        // exception and return an empty result.
        List<Class<?>> types = this.methodFinder.findMethodReturnTypes(null, "unknown");
        assertEquals(0, types.size());
    }
}
