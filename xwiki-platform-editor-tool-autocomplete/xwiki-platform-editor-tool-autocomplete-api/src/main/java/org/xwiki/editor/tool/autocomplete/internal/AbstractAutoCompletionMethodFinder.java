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

import org.xwiki.editor.tool.autocomplete.AutoCompletionMethodFinder;

/**
 * Helper class when writing {@link AutoCompletionMethodFinder}s.
 *
 * @version $Id$
 * @since 4.2M2
 */
public abstract class AbstractAutoCompletionMethodFinder implements AutoCompletionMethodFinder
{
    /**
     * Pretty print a method hint.
     *
     * @param methodName the method name to print
     * @param method the method to pretty print
     * @return the pretty printed string representing the method hint
     */
    protected String printMethod(String methodName, Method method)
    {
        StringBuilder pretty = new StringBuilder();

        // Step 1: Add method name
        pretty.append(methodName);

        // Step 2: Add parameters
        pretty.append('(');
        Class<?>[] parameterTypes = method.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            pretty.append(parameterTypes[i].getSimpleName());
            if (i < parameterTypes.length - 1) {
                pretty.append(", ");
            }
        }
        pretty.append(')');

        // Step 3: Add return type (Don't print void return types!)
        String returnType = method.getReturnType().getSimpleName();
        if (returnType != null) {
            pretty.append(' ');
            pretty.append(returnType);
        }

        return pretty.toString();
    }
}
