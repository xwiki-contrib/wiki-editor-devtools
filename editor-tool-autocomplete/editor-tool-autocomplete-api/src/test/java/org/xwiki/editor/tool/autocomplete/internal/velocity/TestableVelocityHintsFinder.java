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

import com.xpn.xwiki.doc.XWikiDocument;

import static org.mockito.Mockito.*;

/**
 * Fakes the creation of a XWiki Document. We do this since otherwise the {@link VelocityHintsFinder} implementation
 * will try to create a real XWiki Document and this will in turn try to inject several components that are not
 * registered in the unit tests at the moment (and we don't really care about them).
 * <p>
 * This component overrides the {@link VelocityHintsFinder} one (by having a more important priority).
 *
 * @version $Id:$
 */
public class TestableVelocityHintsFinder extends VelocityHintsFinder
{
    @Override
    protected XWikiDocument createFakeXWikiDocument()
    {
        return mock(XWikiDocument.class);
    }
}
