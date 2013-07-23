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
package org.xwiki.editor.tool.autocomplete;

import org.xwiki.component.annotation.Role;

/**
 * Allows contributing special hints. For example for Script Services, instead of returning a hint of {@code get} which
 * is the method provided by {@link org.xwiki.script.service.ScriptServiceManager} (the class type of {@code services})
 * we return the component hints of all registered {@link org.xwiki.script.service.ScriptService} components found in
 * the wiki.
 * 
 * @version $Id$
 */
@Role
public interface AutoCompletionMethodFinder
{
    /**
     * @param variableClass the class for which to find methods to return as autocompletion hints
     * @param fragmentToMatch the filter to only return methods matching the passed string
     * @return the autocompletion hints
     */
    Hints findMethods(Class variableClass, String fragmentToMatch);
}
