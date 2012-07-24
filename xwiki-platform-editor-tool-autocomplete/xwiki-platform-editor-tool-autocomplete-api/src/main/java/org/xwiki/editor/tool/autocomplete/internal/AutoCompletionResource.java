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

import java.lang.reflect.Method;
import java.util.ArrayList;
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
import org.xwiki.rest.XWikiRestComponent;
import org.xwiki.velocity.VelocityManager;
import org.xwiki.velocity.internal.util.InvalidVelocityException;
import org.xwiki.velocity.internal.util.VelocityParser;
import org.xwiki.velocity.internal.util.VelocityParserContext;

/**
 * REST Resource for returning autocompletion hints. The content to autocomplete is passed in the request body and the
 * position of the cursor is passed as a request paramer named {@code offset}.
 * 
 * @version $Id$
 * @since 4.1M2
 */
@Component("org.xwiki.editor.tool.autocomplete.internal.AutoCompletionResource")
@Path("/autocomplete")
public class AutoCompletionResource implements XWikiRestComponent
{
    @Inject
    private VelocityManager velocityManager;

    @Inject
    private ComponentManager componentManager;

    private VelocityParser parser = new VelocityParser();

    @POST
    public Hints getAutoCompletionHints(@QueryParam("offset") int offset, @QueryParam("syntax") String syntax,
        String content)
    {
        Hints hints = new Hints();

        // Only do something if the content is defined and not empty and if offset is >= 0
        if (!StringUtils.isEmpty(content) && offset > -1) {
            hints = hints.withHints(getHints(content, offset));
        }

        return hints;
    }

    private List<String> getHints(String content, int offset)
    {
        List<String> results = new ArrayList<String>();
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

                    // Get the property before the first dot
                    StringBuffer propertyBlock = new StringBuffer();
                    int methodPos = this.parser.getMethodOrProperty(chars, dollarPos, propertyBlock, context);
                    String propertyName = propertyBlock.toString().substring(1);

                    if (methodPos < blockPos) {
                        // Get methods!
                        if (chars[methodPos] == '.') {

                            String fragment = "";
                            boolean autoCompleteMethods = false;

                            // Handle the case where the cursor is just after the dot
                            if (methodPos + 1 == offset) {
                                autoCompleteMethods = true;
                            } else {
                                propertyBlock = new StringBuffer();
                                methodPos = this.parser.getMethodOrProperty(chars, methodPos, propertyBlock, context);
                                if (methodPos == blockPos) {
                                    autoCompleteMethods = true;
                                    fragment = propertyBlock.toString().substring(1);
                                }
                            }

                            if (autoCompleteMethods) {
                                // Find methods using Reflection
                                results.addAll(getMethods(propertyName, fragment, velocityContext));
                            }
                        }
                    } else {
                        String fragment = propertyBlock.toString().substring(1);
                        results.addAll(getVelocityContextKeys(fragment, velocityContext));
                    }
                }

            } catch (InvalidVelocityException e) {
                throw new RuntimeException(e);
            }
        }

        return results;
    }

    private List<String> getVelocityContextKeys(String fragmentToMatch, VelocityContext velocityContext)
    {
        List<String> keys = new ArrayList<String>();

        addVelocityKeys(keys, velocityContext.getKeys(), fragmentToMatch);
        if (velocityContext.getChainedContext() != null) {
            addVelocityKeys(keys, velocityContext.getChainedContext().getKeys(), fragmentToMatch);
        }

        return keys;
    }

    private void addVelocityKeys(List<String> results, Object[] keys, String fragmentToMatch)
    {
        for (Object key : keys) {
            if (key instanceof String && ((String) key).startsWith(fragmentToMatch)) {
                results.add((String) key);
            }
        }
    }

    private List<String> getMethods(String propertyName, String fragmentToMatch, VelocityContext velocityContext)
    {
        List<String> methodNames = new ArrayList<String>();
        Object propertyClass = velocityContext.get(propertyName);

        // Allow special handling for classes that have registered a custom introspection handler
        if (this.componentManager.hasComponent(AutoCompletionMethodFinder.class, propertyName)) {
            try {
                AutoCompletionMethodFinder finder =
                    this.componentManager.getInstance(AutoCompletionMethodFinder.class, propertyName);
                methodNames.addAll(finder.findMethods(propertyClass.getClass()));
            } catch (ComponentLookupException e) {
                // Component not found, continue with standard finder...
            }
        }

        if (methodNames.isEmpty()) {
            for (Method method : propertyClass.getClass().getDeclaredMethods()) {
                String methodName = method.getName().toLowerCase();
                if (methodName.startsWith(fragmentToMatch)
                    || methodName.startsWith("get" + fragmentToMatch.toLowerCase())) {
                    // Don't print void return types!
                    String returnType = method.getReturnType().getSimpleName();

                    // Add simplified velocity without the get()
                    if (methodName.startsWith("get" + fragmentToMatch.toLowerCase())) {
                        methodNames.add(StringUtils.uncapitalize(methodName.substring(3))
                            + (returnType == "void" ? "" : " " + returnType));
                    }

                    methodNames.add(method.getName() + "(...)" + (returnType == "void" ? "" : " " + returnType));
                }
            }
        }

        return methodNames;
    }
}
