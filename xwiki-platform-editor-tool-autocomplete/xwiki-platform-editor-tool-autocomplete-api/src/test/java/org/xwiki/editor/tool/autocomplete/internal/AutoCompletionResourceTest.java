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

import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;
import org.jmock.Expectations;
import org.junit.Test;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.script.service.ScriptService;
import org.xwiki.script.service.ScriptServiceManager;
import org.xwiki.test.AbstractMockingComponentTestCase;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.annotation.MockingRequirement;
import org.xwiki.velocity.VelocityManager;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

import junit.framework.Assert;

@ComponentList({
    ScriptServicesAutoCompletionMethodFinder.class
})
public class AutoCompletionResourceTest extends AbstractMockingComponentTestCase
{
    @MockingRequirement(exceptions = {ComponentManager.class})
    private AutoCompletionResource resource;

    @Override
    public void configure() throws Exception
    {
        registerMockComponent(ScriptServiceManager.class);
        registerMockComponent(ScriptService.class, "test");
    }

    private class TestClass
    {
        public void method1()
        {
        }

        public String method2()
        {
            return "";
        }
    }

    @Test
    public void getAutoCompletionHintsWhenOnlyDollarSign() throws Exception
    {
        Context innerContext = new VelocityContext();
        innerContext.put("key1", "value1");
        final VelocityContext vcontext = new VelocityContext(innerContext);
        vcontext.put("key2", "value2");
        setUpMocks(vcontext);

        Hints hints = this.resource.getAutoCompletionHints(1, "$");

        // Verify we can get keys from the Velocity Context and its inner context too. We test this since our
        // Velocity tools are put in an inner Velocity Context.
        Assert.assertEquals(2, hints.getHints().size());
        assertThat(hints.getHints(), containsInAnyOrder("key1", "key2"));
    }

    @Test
    public void getAutoCompletionHintsWhenDollarSignFollowedBySomeLetters() throws Exception
    {
        setUpMocks(createTestVelocityContext("key", "value", "otherKey", "otherValue"));

        Hints hints = this.resource.getAutoCompletionHints(3, "$ke");

        Assert.assertEquals(1, hints.getHints().size());
        assertThat(hints.getHints(), containsInAnyOrder("key"));
    }

    @Test
    public void getAutoCompletionHintsWhenDollarSignFollowedBySomeNonMatchingLetters() throws Exception
    {
        setUpMocks(createTestVelocityContext("key", "value"));

        Hints hints = this.resource.getAutoCompletionHints(2, "$o");

        Assert.assertEquals(0, hints.getHints().size());
    }

    @Test
    public void getAutoCompletionHintsWhenInvalidAutoCompletion() throws Exception
    {
        setUpMocks(createTestVelocityContext());

        Hints hints = this.resource.getAutoCompletionHints(3, "$k ");

        Assert.assertEquals(0, hints.getHints().size());
    }

    @Test
    public void getAutoCompletionHintsForMethod() throws Exception
    {
        setUpMocks(createTestVelocityContext("key", new TestClass()));

        Hints hints = this.resource.getAutoCompletionHints(6, "$key.m");

        Assert.assertEquals(2, hints.getHints().size());
        assertThat(hints.getHints(), containsInAnyOrder("method1(...)", "method2(...) String"));
    }

    @Test
    public void getAutoCompletionHintsForScriptServiceProperty() throws Exception
    {
        ScriptServiceManager scriptServiceManager = getComponentManager().getInstance(ScriptServiceManager.class);
        setUpMocks(createTestVelocityContext("services", scriptServiceManager));

        Hints hints = this.resource.getAutoCompletionHints(11, "$services.t");

        Assert.assertEquals(1, hints.getHints().size());
        assertThat(hints.getHints(), containsInAnyOrder("test"));
    }

    private void setUpMocks(final VelocityContext velocityContext) throws Exception
    {
        final VelocityManager velocityManager = getComponentManager().getInstance(VelocityManager.class);
        getMockery().checking(new Expectations() {{
            oneOf(velocityManager).getVelocityContext();
                will(returnValue(velocityContext));
        }});
    }

    private VelocityContext createTestVelocityContext(Object... properties)
    {
        final VelocityContext context = new VelocityContext();
        for (int i = 0; i < properties.length; i += 2) {
            context.put((String) properties[i], properties[i + 1]);
        }
        return context;
    }
}
