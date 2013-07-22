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
 * Allows discovering the syntax of the content under the current cursor position.
 *
 * @version $Id$
 */
@Role
public interface TargetContentLocator
{
    /**
     * @param content the whole content from which we need to extract the content and type at the cursor position
     *                (eg the whole content could be written in wiki syntax and the content at the cursor position
     *                could be Velocity content)
     * @param syntaxId the syntax in which the whole content is written in (eg "xwiki/2.0")
     * @param currentPosition the position of the cursor
     * @return the content and type at the cursor position
     */
    TargetContent locate(String content, String syntaxId, int currentPosition);
}
