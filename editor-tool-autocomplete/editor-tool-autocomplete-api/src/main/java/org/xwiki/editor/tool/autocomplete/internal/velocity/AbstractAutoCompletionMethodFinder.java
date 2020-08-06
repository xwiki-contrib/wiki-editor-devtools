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

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.xpn.xwiki.util.Programming;

/**
 * Helper class when writing {@link AutoCompletionMethodFinder}s.
 *
 * @version $Id$
 */
public abstract class AbstractAutoCompletionMethodFinder implements AutoCompletionMethodFinder
{
    private static final String COMMA_AND_SPACE = ", ";

    /**
     * Pretty print a method hint.
     *
     * @param methodName the method name to print
     * @param method the method to pretty print
     * @return the pretty printed string representing the method hint
     */
    protected String printMethod(String methodName, Method method)
    {
        // Step 1: Add method name
        // Step 2: Add parameters
        // Step 3: Add return type (Don't print void return types!)
        return methodName
            + getParameters(method)
            + getReturnType(method)
            + getProgrammingRights(method);
    }

    /**
     * Pretty print a shorthand hint.
     *
     * @param methodName the shorthand name to print
     * @param method the method for which to print the return type
     * @return the pretty printed string representing the shorthand hint
     */
    protected String printShorthand(String methodName, Method method)
    {
        // Step 1: Add method name
        // Step 2: Add return type (Don't print void return types!)
        return methodName
            + getReturnType(method)
            + getProgrammingRights(method);
    }

    /**
     * Pretty print a shorthand hint.
     *
     * @param methodName the shorthand name to print
     * @param returnType the class return type
     * @return the pretty printed string representing the shorthand hint
     */
    protected String printShorthand(String methodName, Class<?> returnType)
    {
        // Step 1: Add method name
        // Step 2: Add return type (Don't print void return types!)
        return methodName
            + (returnType == null ? "" : " " + returnType.getSimpleName());
    }

    /**
     * @param method the method from which to extract the parameters
     * @return the pretty-printed method parameters
     */
    private StringBuilder getParameters(Method method)
    {
        StringBuilder builder = new StringBuilder();

        builder.append('(');
        Type[] parameterTypes = method.getGenericParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            builder.append(serializeType(parameterTypes[i]));
            if (i < parameterTypes.length - 1) {
                builder.append(COMMA_AND_SPACE);
            }
        }
        builder.append(')');

        return builder;
    }

    /**
     * @param method the method from which to extract the return type
     * @return the pretty-printed method return type
     */
    private StringBuilder getReturnType(Method method)
    {
        // If the return type has generics, make sure we print them
        return new StringBuilder()
            .append(' ')
            .append(serializeType(method.getGenericReturnType()));
    }

    /**
     * @param method the method from which to check if PR are needed
     * @return the pretty text to signify to the user that PR are needed for this method
     */
    private StringBuilder getProgrammingRights(Method method)
    {
        StringBuilder builder = new StringBuilder();

        Programming programming = method.getAnnotation(Programming.class);
        if (programming != null) {
            builder.append(' ');
            builder.append("(Programming Rights)");
        }

        return builder;
    }

    @Override
    public List<Class<?>> findMethodReturnTypes(Class<?> propertyClass, String methodName)
    {
        return findMatchingReturnTypes(propertyClass, methodName);
    }

    /**
     * @param propertyClass the class in which to look for methods
     * @param methodName the name of the method to find
     * @return all methods (ie all signatures) named with {@code methodName} in class {@code propertyClass}
     */
    private List<Method> findMatchingMethods(Class<?> propertyClass, String methodName)
    {
        List<Method> methods = new ArrayList<>();
        for (Method method : propertyClass.getMethods()) {
            if (method.getName().equals(methodName)) {
                methods.add(method);
            }
        }
        return methods;
    }

    /**
     * @param propertyClass the class from which to find the method signatures
     * @param methodName the method signature to look for
     * @return the classes returned by the method signatures matching the passed {@code methodName} for class
     *         {@code propertyClass}
     */
    private List<Class<?>> findMatchingReturnTypes(Class<?> propertyClass, String methodName)
    {
        List<Class<?>> returnTypes = new ArrayList<>();
        List<Method> methods = findMatchingMethods(propertyClass, methodName);
        for (Method method : methods) {
            returnTypes.add(method.getReturnType());
        }
        return returnTypes;
    }

    // Code copied from ReflectionsUtils because:
    // - ReflectionUtils doesn't support displaying simple names
    // - ReflectionUtils has static methods and this cannot be overridden :(

    private String getTypeName(Type type)
    {
        return type instanceof Class ? ((Class) type).getSimpleName() : type.getTypeName();
    }

    private String serializeType(Type type)
    {
        if (type == null) {
            return null;
        } else {
            StringBuilder sb = new StringBuilder();
            if (type instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) type;
                String rawTypeName = getTypeName(parameterizedType.getRawType());
                handleOwnerTypeSerialization(parameterizedType, rawTypeName, sb);
                handleActualTypeArguments(parameterizedType, sb);
            } else {
                sb.append(getTypeName(type));
            }

            return sb.toString();
        }
    }

    private void handleActualTypeArguments(ParameterizedType parameterizedType, StringBuilder sb)
    {
        if (parameterizedType.getActualTypeArguments() != null
            && parameterizedType.getActualTypeArguments().length > 0)
        {
            sb.append("<");
            boolean first = true;
            Type[] var5 = parameterizedType.getActualTypeArguments();
            int var6 = var5.length;

            for (int var7 = 0; var7 < var6; ++var7) {
                Type typeArgument = var5[var7];
                if (!first) {
                    sb.append(COMMA_AND_SPACE);
                }

                sb.append(getTypeName(typeArgument));
                first = false;
            }

            sb.append(">");
        }
    }

    private void handleOwnerTypeSerialization(ParameterizedType parameterizedType, String rawTypeName, StringBuilder sb)
    {
        if (parameterizedType.getOwnerType() != null) {
            if (parameterizedType.getOwnerType() instanceof Class) {
                sb.append(((Class) parameterizedType.getOwnerType()).getName());
            } else {
                sb.append(parameterizedType.getOwnerType().toString());
            }

            sb.append('.');
            if (parameterizedType.getOwnerType() instanceof ParameterizedType) {
                sb.append(rawTypeName.replace(((Class) ((ParameterizedType) parameterizedType.getOwnerType())
                    .getRawType()).getName() + '$', ""));
            } else {
                sb.append(rawTypeName);
            }
        } else {
            sb.append(rawTypeName);
        }
    }
}
