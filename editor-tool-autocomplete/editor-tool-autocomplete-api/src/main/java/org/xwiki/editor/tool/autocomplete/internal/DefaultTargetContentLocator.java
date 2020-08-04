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

import java.io.StringReader;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.editor.tool.autocomplete.TargetContent;
import org.xwiki.editor.tool.autocomplete.TargetContentLocator;
import org.xwiki.editor.tool.autocomplete.TargetContentType;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.MacroBlock;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.block.match.MacroBlockMatcher;
import org.xwiki.rendering.parser.ParseException;
import org.xwiki.rendering.parser.Parser;

/**
 * Finds the content and content type at the cursor position.
 * 
 * @version $Id$
 */
@Component
@Singleton
public class DefaultTargetContentLocator implements TargetContentLocator
{
    /**
     * Special marker to be able to help discover the content and type at the cursor position.
     */
    private static final String MARKER = "xwikiautocompletionmarker";

    /**
     * Name of the Velocity macro in the XDOM.
     */
    private static final MacroBlockMatcher VELOCITY_MACRO_MATCHER = new MacroBlockMatcher("velocity");

    /**
     * Used to dynamically find the Parser for the content syntax.
     */
    @Inject
    private ComponentManager componentManager;

    /**
     * Logging framework.
     */
    @Inject
    private Logger logger;

    @Override
    public TargetContent locate(String content, String syntaxId, int currentPosition)
    {
        TargetContent targetContent = null;

        // We do the following trick ATM:
        // - insert some marker at the position of the cursor in the passed content
        // - parse the content
        // - locate all Velocity macros and check in which one the marker is present
        // - return the matching velocity macro content without the marker
        Parser parser = getParser(syntaxId);
        if (parser != null) {
            StringBuilder modifiedContent = new StringBuilder(content);
            modifiedContent.insert(currentPosition, MARKER);

            try {
                XDOM xdom = parser.parse(new StringReader(modifiedContent.toString()));

                // Find the Velocity macro that contains the marker.
                List<Block> velocityMacroBlocks = xdom.getBlocks(VELOCITY_MACRO_MATCHER, Block.Axes.DESCENDANT);
                for (Block velocityMacroBlock : velocityMacroBlocks) {
                    MacroBlock macroBlock = (MacroBlock) velocityMacroBlock;
                    int pos = macroBlock.getContent().indexOf(MARKER);
                    if (pos != -1) {
                        // We've found it, exit!
                        // Remove the marker...
                        String cleanContent = macroBlock.getContent().substring(0, pos);
                        targetContent = new TargetContent(cleanContent, pos, TargetContentType.VELOCITY);
                        break;
                    }
                }
            } catch (ParseException e) {
                // Failed to parse content for some reason, don't do autocompletion
                this.logger.debug("Failed to locate the content [{}] with syntax [{}] at the cursor position [{}]",
                    content, syntaxId, currentPosition, e);
            }
        }

        return targetContent;
    }

    /**
     * @param syntaxId the syntax id of the whole content (eg "xwiki/2.0")
     * @return the Parser for parsing the whole content
     */
    private Parser getParser(String syntaxId)
    {
        Parser parser;
        try {
            parser = this.componentManager.getInstance(Parser.class, syntaxId);
        } catch (ComponentLookupException e) {
            // No Parser exists for the passed syntax, returning null to signify it to the caller.
            parser = null;
        }
        return parser;
    }
}
