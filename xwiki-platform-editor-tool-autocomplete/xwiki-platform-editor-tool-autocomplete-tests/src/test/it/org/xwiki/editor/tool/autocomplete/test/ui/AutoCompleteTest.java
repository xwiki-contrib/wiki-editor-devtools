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
package org.xwiki.editor.tool.autocomplete.test.ui;

import org.junit.Test;
import org.xwiki.test.ui.AbstractAdminAuthenticatedTest;
import org.xwiki.test.ui.po.ViewPage;
import org.xwiki.test.ui.po.editor.WikiEditPage;

/**
 * Tests autocompletion in the Velocity macro while being in the wiki editor.
 * 
 * @version $Id$
 * @since 4.2M2
 */
public class AutoCompleteTest extends AbstractAdminAuthenticatedTest
{
    @Test
    public void autoComplete() throws Exception
    {
        // Create test page in which we're goign to test autocompletion
        ViewPage vp = getUtil().createPage(getClass().getSimpleName(), getTestMethodName(), "", "AutoCompletion Test");
        WikiEditPage wep = vp.editWiki();
        wep.setContent("{{velocity}}$");
        // Trigger autocompletion!

    }
}
