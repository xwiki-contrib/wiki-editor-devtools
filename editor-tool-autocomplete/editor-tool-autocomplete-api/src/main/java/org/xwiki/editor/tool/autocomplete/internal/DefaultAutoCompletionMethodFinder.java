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

import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.xwiki.component.annotation.Component;
import org.xwiki.editor.tool.autocomplete.HintData;
import org.xwiki.editor.tool.autocomplete.Hints;

/**
 * Returns autocompletion hints by finding all methods using introspection.
 * 
 * @version $Id$
 */
@Component
@Singleton
public class DefaultAutoCompletionMethodFinder extends AbstractAutoCompletionMethodFinder
{
    /**
     * Keyword prefix for getter methods.
     */
    private static final String GETTER_KEYWORD = "get";

    /**
     * Prefix of methods injected by AspectJ that we need to exclude.
     */
    private static final String ASPECTJ_METHOD_PREFIX = "ajc$";

    @Override
    public Hints findMethods(Class<?> propertyClass, String fragmentToMatch)
    {
        Hints hints = new Hints();
        String lowerCaseFragment = fragmentToMatch.toLowerCase();

        for (Method method : propertyClass.getMethods()) {
            String methodName = method.getName().toLowerCase();
            if (!methodName.startsWith(ASPECTJ_METHOD_PREFIX)
                && (methodName.startsWith(lowerCaseFragment) || methodName.startsWith(GETTER_KEYWORD
                    + lowerCaseFragment))) {
                // Add simplified velocity without the get()
                if (methodName.startsWith(GETTER_KEYWORD + lowerCaseFragment)) {
                    // Suggest shorthand velocity getter method only if it has no parameters.
                    if (method.getParameterTypes().length == 0) {
                        String getter = StringUtils.uncapitalize(method.getName().substring(3));
                        hints.withHints(new HintData(getter, printShorthand(getter, method)));
                    }
                } else {
                    hints.withHints(new HintData(method.getName(), printMethod(method.getName(), method)));
                }
            }
        }

        return hints;
    }
}
