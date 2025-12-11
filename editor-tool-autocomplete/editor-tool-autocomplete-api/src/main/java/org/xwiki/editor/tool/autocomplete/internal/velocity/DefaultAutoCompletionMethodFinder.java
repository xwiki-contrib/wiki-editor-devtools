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
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.util.DefaultParameterizedType;
import org.xwiki.editor.tool.autocomplete.HintData;
import org.xwiki.editor.tool.autocomplete.Hints;
import org.xwiki.properties.converter.Converter;

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

    @Inject
    @Named("context")
    private ComponentManager componentManager;

    private Map<Type, Boolean> stringConverters = new HashMap<>();

    @Override
    public Hints findMethods(Class<?> propertyClass, String fragmentToMatch)
    {
        Hints hints = new Hints();
        String lowerCaseFragment = fragmentToMatch.toLowerCase();

        List<HintData> convertMethods = new ArrayList<>();
        for (Method method : propertyClass.getMethods()) {
            String methodName = method.getName().toLowerCase();
            if (!methodName.startsWith(ASPECTJ_METHOD_PREFIX)
                && (methodName.startsWith(lowerCaseFragment) || methodName.startsWith(GETTER_KEYWORD
                    + lowerCaseFragment))) {
                // Also add the simplified velocity shortcut when there's no parameters
                if (methodName.startsWith(GETTER_KEYWORD + lowerCaseFragment)) {
                    if (method.getParameterTypes().length == 0) {
                        // Suggest shorthand velocity getter method only if it has no parameters.
                        String getter = StringUtils.uncapitalize(method.getName().substring(3));
                        hints.withHints(new HintData(getter, printShorthand(getter, method)));
                    }
                    hints.withHints(new HintData(method.getName(), printMethod(method.getName(), method)));
                } else {
                    hints.withHints(new HintData(method.getName(), printMethod(method.getName(), method)));
                    // Special for Velocity: For each parameter type, find all Converter parameters that do conversion
                    // from another type to this parameter type.
                    // Note that the Converters don't expose metadata to say which target types they support.
                    // However, most support converting from String to another type. Check if there's a
                    // "convertToString()" method and if so, add the signature accepting a String parameter.
                    convertMethods.addAll(getConverterHintData(method));
                }
            }
        }

        // Only add the converter methods if they're not duplicates of existing method signatures
        for (HintData hintData : convertMethods) {
            if (!hints.getHints().contains(hintData)) {
                hints.withHints(new HintData(hintData.getName(), hintData.getDescription() + " (Converter)"));
            }
        }

        return hints;
    }

    private List<HintData> getConverterHintData(Method method)
    {
        List<HintData> result = new ArrayList<>();
        Type[] parameterTypes = method.getGenericParameterTypes();
        Type[] newParameterTypes = new Type[parameterTypes.length];
        boolean hasChanges = false;
        for (int i = 0; i < parameterTypes.length; i++) {
            // TODO: Also generate combinations!
            Type parameterType = parameterTypes[i];
            if (!String.class.isAssignableFrom(parameterType.getClass()) && existsStringConverterFor(parameterType)) {
                newParameterTypes[i] = String.class;
                hasChanges = true;
            } else {
                newParameterTypes[i] = parameterType;
            }
        }
        if (hasChanges) {
            result.add(new HintData(method.getName(), printMethod(method.getName(), method, newParameterTypes)));
        }
        return result;
    }

    private boolean existsStringConverterFor(Type type)
    {
        // TODO: Handle the UC when an extension containing a Converter is uninstalled. In this case the
        // stringConverters map must be updated.
        Boolean result = this.stringConverters.get(type);
        if (result == null) {
            Converter<?> converter = getConverterFor(type);
            result = converter != null;
            this.stringConverters.put(type, result);
        }
        return result;
    }

    private Converter<?> getConverterFor(Type type)
    {
        Converter<?> result;
        try {
            result = this.componentManager.getInstance(
                new DefaultParameterizedType(null, Converter.class, type));
        } catch (Exception e) {
            // Doesn't exist or cannot be loaded, we ignore it.
            result = null;
        }
        return  result;
    }
}
