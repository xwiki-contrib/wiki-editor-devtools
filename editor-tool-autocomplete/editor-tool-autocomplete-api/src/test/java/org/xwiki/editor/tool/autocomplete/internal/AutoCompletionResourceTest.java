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

import javax.inject.Named;

import org.junit.jupiter.api.Test;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.editor.tool.autocomplete.HintData;
import org.xwiki.editor.tool.autocomplete.Hints;
import org.xwiki.editor.tool.autocomplete.HintsFinder;
import org.xwiki.editor.tool.autocomplete.TargetContent;
import org.xwiki.editor.tool.autocomplete.TargetContentLocator;
import org.xwiki.editor.tool.autocomplete.TargetContentType;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AutoCompletionResource}.
 * 
 * @version $Id:$
 */
@ComponentTest
public class AutoCompletionResourceTest
{
    @InjectMockComponents
    private AutoCompletionResource autoCompletionResource;

    @MockComponent
    private TargetContentLocator targetContentLocator;

    @MockComponent
    @Named("context")
    private ComponentManager contextComponentManager;

    @Test
    void getAutoCompletionHints() throws Exception
    {
        String content = "Something\n{{velocity}}$v{{/velocity}}";
        TargetContent targetContent = new TargetContent("$v", 2, TargetContentType.VELOCITY);
        when(this.targetContentLocator.locate(content, "xwiki/2.1", "Something\n{{velocity}}$v".length())).thenReturn(
            targetContent);
        HintsFinder finder = mock(HintsFinder.class);
        when(this.contextComponentManager.getInstance(HintsFinder.class, "velocity")).thenReturn(finder);
        Hints expectedHints = new Hints();
        expectedHints.withHints(new HintData("var", "description"));
        expectedHints.withStartOffset(1);
        when(finder.findHints(targetContent)).thenReturn(expectedHints);

        Hints hints = this.autoCompletionResource.getAutoCompletionHints("Something\n{{velocity}}$v".length(),
            "xwiki/2.1", content);

        assertEquals("Something\n{{velocity}}$".length(), hints.getStartOffset());
    }
}
