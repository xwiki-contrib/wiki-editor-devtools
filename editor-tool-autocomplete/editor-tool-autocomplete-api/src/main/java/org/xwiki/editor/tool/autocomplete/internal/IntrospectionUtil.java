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

/**
 * Various utility introspection methods.
 *
 * @version $Id$
 */
public class IntrospectionUtil
{
    public static List<Method> findMethods(Class propertyClass, String methodName)
    {
        List<Method> methods = new ArrayList<Method>();
        for (Method method : propertyClass.getMethods()) {
            if (method.getName().equals(methodName)) {
                methods.add(method);
            }
        }
        return methods;
    }

    public static List<Class> findReturnTypes(Class propertyClass, String methodName)
    {
        List<Class> returnTypes = new ArrayList<Class>();
        List<Method> methods = findMethods(propertyClass, methodName);
        for (Method method : methods) {
            returnTypes.add(method.getReturnType());
        }
        return returnTypes;
    }
}
