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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.xwiki.test.docker.junit5.UITest;
import org.xwiki.test.ui.TestUtils;
import org.xwiki.test.ui.po.ViewPage;
import org.xwiki.test.ui.po.editor.WikiEditPage;

/**
 * Tests autocompletion in the Velocity macro while being in the wiki editor.
 * 
 * @version $Id$
 */
@UITest
class AutoCompleteIT
{
    @Test
    void autoComplete(TestUtils setup, TestInfo info)
    {
        // Register a test user
        setup.deletePage("XWiki", "TestUser");

        // Create a test user. Note that we create it as an advanced user so that he gets the Edit Wiki edit menu that
        // we need to edit in wiki mode below.
        setup.createUserAndLogin("TestUser", "TestPassword", "usertype", "Advanced");

        // Create test page in which we're going to test autocompletion
        ViewPage vp = setup.createPage(info.getTestClass().get().getSimpleName(), info.getTestMethod().get().getName(),
            "", "AutoCompletion Test");

        // Start creating a velocity macro and trigger autocompletion
        WikiEditPage wep = vp.editWiki();
        //wep.setContent("{{velocity}}$");

        // TODO: Continue test by triggering autocompletion here and asserting results!
    }
}
