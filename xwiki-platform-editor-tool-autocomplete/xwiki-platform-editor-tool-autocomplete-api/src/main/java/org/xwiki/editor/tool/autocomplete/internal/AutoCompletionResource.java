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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.VelocityContext;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.editor.tool.autocomplete.AutoCompletionMethodFinder;
import org.xwiki.editor.tool.autocomplete.TargetContent;
import org.xwiki.editor.tool.autocomplete.TargetContentLocator;
import org.xwiki.editor.tool.autocomplete.TargetContentType;
import org.xwiki.rest.XWikiRestComponent;
import org.xwiki.velocity.VelocityManager;
import org.xwiki.velocity.internal.util.InvalidVelocityException;
import org.xwiki.velocity.internal.util.VelocityParser;
import org.xwiki.velocity.internal.util.VelocityParserContext;

/**
 * REST Resource for returning autocompletion hints. The content to autocomplete is passed in the request body,
 * the position of the cursor and the syntax in which the content is written in are passed as request parameters.
 * 
 * @version $Id$
 * @since 4.2M2
 */
@Component("org.xwiki.editor.tool.autocomplete.internal.AutoCompletionResource")
@Path("/autocomplete")
public class AutoCompletionResource implements XWikiRestComponent
{
    /**
     * Used to get the Velocity Context from which we retrieve the list of bound variables that are used for
     * autocompletion.
     */
    @Inject
    private VelocityManager velocityManager;

    /**
     * Used to dynamically find Autocompletion Method finder to handle specific cases.
     * @see AutoCompletionMethodFinder
     */
    @Inject
    private ComponentManager componentManager;

    /**
     * Used to autodiscover method hints.
     */
    @Inject
    private AutoCompletionMethodFinder defaultAutoCompletionMethodFinder;

    /**
     * Used to extract the content and the type under the cursor position.
     */
    @Inject
    private TargetContentLocator targetContentLocator;

    /**
     * A Velocity Parser that we use to help parse Velocity content for figuring out autocompletion.
     */
    private VelocityParser parser = new VelocityParser();

    /**
     * Main REST entry point for getting Autocompletion hints.
     *
     * @param offset the position of the cursor in the full content
     * @param syntaxId the syntax in which the content is written in
     * @param content the full wiki content
     * @return the list of autocompletion hints
     */
    @POST
    public Hints getAutoCompletionHints(@QueryParam("offset") int offset, @QueryParam("syntax") String syntaxId,
        String content)
    {
        Hints hints = new Hints();

        // Only support autocompletion on Velocity ATM
        TargetContent targetContent = this.targetContentLocator.locate(content, syntaxId, offset);
        if (targetContent.getType() == TargetContentType.VELOCITY) {
            hints = hints.withHints(getHints(targetContent.getContent(), targetContent.getPosition()));
        }

        return hints;
    }

    /**
     * @param content the Velocity content to autocomplete
     * @param offset the position of the cursor relative to the Velocity content
     * @return the list of autocompletion hints
     */
    private List<HintData> getHints(String content, int offset)
    {
        List<HintData> results = new ArrayList<HintData>();
        char[] chars = content.toCharArray();
        VelocityContext velocityContext = this.velocityManager.getVelocityContext();

        // Use case 1: The offset is just after the dollar sign
        if (offset > 0 && chars[offset - 1] == '$') {
            // Find all objects bound to the Velocity Context. We need to also look in the chained context since this
            // is where we store Velocity Tools
            results.addAll(getVelocityContextKeys("", velocityContext));
        } else {
            // Find the dollar sign before the current position
            int dollarPos = StringUtils.lastIndexOf(content, '$', offset);

            StringBuffer velocityBlock = new StringBuffer();
            VelocityParserContext context = new VelocityParserContext();
            try {

                // Get the block after the dollar
                int blockPos = this.parser.getVar(chars, dollarPos, velocityBlock, context);
                // if newPos matches the current position then it means we have a valid block for autocompletion

                // Note: we need to handle the special case where the cursor is just after the dot since getVar will
                // not return it!
                if (blockPos + 1 == offset && chars[blockPos] == '.') {
                    blockPos++;
                    velocityBlock.append('.');
                }

                if (blockPos == offset) {
                    results.addAll(
                        getMethodsOrVariableHints(chars, dollarPos, blockPos, offset, velocityContext));
                }

            } catch (InvalidVelocityException e) {
                throw new RuntimeException(e);
            }
        }

        // Sort hints
        Collections.sort(results);

        return results;
    }

    /**
     * @param chars the content to parse
     * @param dollarPos the position of the dollar symbol
     * @param blockPos the position in the content after the whole fragment starting with the dollar symbol
     * @param offset the position in the whole content of the cursor
     * @param velocityContext the velocity context used to get the variables bound in the Velocity Context
     * @return the list of hints
     * @throws InvalidVelocityException when the code to parse is not what's expected
     */
    private List<HintData> getMethodsOrVariableHints(char[] chars, int dollarPos, int blockPos, int offset,
        VelocityContext velocityContext) throws InvalidVelocityException
    {
        List<HintData> results = new ArrayList<HintData>();
        VelocityParserContext context = new VelocityParserContext();

        // Get the property before the first dot
        StringBuffer propertyBlock = new StringBuffer();
        int methodPos = this.parser.getMethodOrProperty(chars, dollarPos, propertyBlock, context);
        String propertyName = propertyBlock.toString().substring(1);

        if (methodPos < blockPos) {
            // Get methods!
            if (chars[methodPos] == '.') {
                results.addAll(getMethods(chars, propertyName, blockPos, methodPos, offset, velocityContext));
            }
        } else {
            String fragment = propertyBlock.toString().substring(1);
            results.addAll(getVelocityContextKeys(fragment, velocityContext));
        }

        return results;
    }

    /**
     * @param chars the content to parse
     * @param propertyName the name of the property on which we want to find methods
     * @param blockPos the position in the content after the whole fragment starting with the dollar symbol
     * @param methodPos the position in the content after the whole fragment starting with the dollar symbol
     * @param offset the position in the whole content of the cursor
     * @param velocityContext the velocity context used to get the variables bound in the Velocity Context
     * @return the list of hints
     * @throws InvalidVelocityException when the code to parse is not what's expected
     */
    private List<HintData> getMethods(char[] chars, String propertyName, int blockPos, int methodPos, int offset,
        VelocityContext velocityContext) throws InvalidVelocityException
    {
        List<HintData> results = new ArrayList<HintData>();
        VelocityParserContext context = new VelocityParserContext();

        String fragment = "";
        boolean autoCompleteMethods = false;

        // Handle the case where the cursor is just after the dot
        if (methodPos + 1 == offset) {
            autoCompleteMethods = true;
        } else {
            StringBuffer propertyBlock = new StringBuffer();
            int newMethodPos = this.parser.getMethodOrProperty(chars, methodPos, propertyBlock, context);
            if (newMethodPos == blockPos) {
                autoCompleteMethods = true;
                fragment = propertyBlock.toString().substring(1);
            }
        }

        if (autoCompleteMethods) {
            // Find methods using Reflection
            results.addAll(getMethods(propertyName, fragment, velocityContext));
        }

        return results;
    }

    /**
     * Find out all Velocity variable names bound in the Velocity Context.
     *
     * @param fragmentToMatch the prefix to filter with in order to return only variable whose names start with the
     *        passed string
     * @param velocityContext the Velocity Context from which to get the bound variables
     * @return the Velocity variables
     */
    private List<HintData> getVelocityContextKeys(String fragmentToMatch, VelocityContext velocityContext)
    {
        List<HintData> hintData = new ArrayList<HintData>();

        addVelocityKeys(hintData, velocityContext.getKeys(), fragmentToMatch);
        if (velocityContext.getChainedContext() != null) {
            addVelocityKeys(hintData, velocityContext.getChainedContext().getKeys(), fragmentToMatch);
        }

        return hintData;
    }

    /**
     * Add variables to the passed results list.
     *
     * @param results the list of variable names
     * @param keys the keys containing the variables to add
     * @param fragmentToMatch the filter in order to only add variable whose names start with the passed string
     */
    private void addVelocityKeys(List<HintData> results, Object[] keys, String fragmentToMatch)
    {
        for (Object key : keys) {
            if (key instanceof String && ((String) key).startsWith(fragmentToMatch)) {
                results.add(new HintData((String) key, (String) key));
            }
        }
    }

    /**
     * @param variableName the variable name corresponding to the class for which to find the methods
     * @param fragmentToMatch the filter to only return methods matching the passed string
     * @param velocityContext the Velocity Context from which to get the Class corresponding to the variable name
     * @return the method names found in the Class pointed to by the passed variableName name
     */
    private List<HintData> getMethods(String variableName, String fragmentToMatch, VelocityContext velocityContext)
    {
        List<HintData> hintData = new ArrayList<HintData>();
        Object propertyClass = velocityContext.get(variableName);

        // Allow special handling for classes that have registered a custom introspection handler
        if (this.componentManager.hasComponent(AutoCompletionMethodFinder.class, variableName)) {
            try {
                AutoCompletionMethodFinder finder =
                    this.componentManager.getInstance(AutoCompletionMethodFinder.class, variableName);
                hintData.addAll(finder.findMethods(propertyClass.getClass(), fragmentToMatch));
            } catch (ComponentLookupException e) {
                // Component not found, continue with default finder...
            }
        }

        if (hintData.isEmpty()) {
            hintData.addAll(
                this.defaultAutoCompletionMethodFinder.findMethods(propertyClass.getClass(), fragmentToMatch));
        }

        return hintData;
    }
}
