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

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.editor.tool.autocomplete.Hints;
import org.xwiki.editor.tool.autocomplete.HintsFinder;
import org.xwiki.editor.tool.autocomplete.TargetContent;
import org.xwiki.editor.tool.autocomplete.TargetContentLocator;
import org.xwiki.rest.XWikiRestComponent;

/**
 * REST Resource for returning autocompletion hints. The content to autocomplete is passed in the request body, the
 * position of the cursor and the syntax in which the content is written in are passed as request parameters.
 *
 * @version $Id$
 */
@Component
@Named("org.xwiki.editor.tool.autocomplete.internal.AutoCompletionResource")
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
@Path("/autocomplete")
public class AutoCompletionResource implements XWikiRestComponent
{
    @Inject
    private Logger logger;

    /**
     * Used to extract the content and the type under the cursor position.
     */
    @Inject
    private TargetContentLocator targetContentLocator;

    @Inject
    @Named("context")
    private ComponentManager contextComponentManager;

    /**
     * Main REST entry point for getting Autocompletion hints.
     *
     * @param offset the position of the cursor in the full content
     * @param syntaxId the syntax in which the content is written in
     * @param content the full content on which we need to return completion hints for the offset position
     * @return the list of autocompletion hints at the offset position
     */
    @POST
    public Hints getAutoCompletionHints(@QueryParam("offset") int offset, @QueryParam("syntax") String syntaxId,
        String content)
    {
        Hints hints = new Hints();
        TargetContent targetContent = this.targetContentLocator.locate(content, syntaxId, offset);
        if (targetContent != null) {
            try {
                HintsFinder finder = this.contextComponentManager.getInstance(HintsFinder.class,
                    StringUtils.lowerCase(targetContent.getType().name()));
                hints = finder.findHints(targetContent);
                // Subtract the temporary user input size from the initial offset to get the absolute start offset of
                // the user's input.
                hints.withStartOffset(offset - hints.getStartOffset());
            } catch (ComponentLookupException e) {
                this.logger.error("No Autocompletion support for content type [{}]", targetContent.getType(), e);
            }
        }
        return hints;
    }
}
