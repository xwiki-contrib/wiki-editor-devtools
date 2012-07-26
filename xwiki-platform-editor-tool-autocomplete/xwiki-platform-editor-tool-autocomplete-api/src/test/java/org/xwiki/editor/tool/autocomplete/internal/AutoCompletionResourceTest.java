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

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;
import junit.framework.Assert;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;
import org.jmock.Expectations;
import org.junit.Ignore;
import org.junit.Test;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.editor.tool.autocomplete.AutoCompletionMethodFinder;
import org.xwiki.editor.tool.autocomplete.TargetContent;
import org.xwiki.editor.tool.autocomplete.TargetContentLocator;
import org.xwiki.editor.tool.autocomplete.TargetContentType;
import org.xwiki.script.service.ScriptService;
import org.xwiki.script.service.ScriptServiceManager;
import org.xwiki.test.AbstractMockingComponentTestCase;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.annotation.MockingRequirement;
import org.xwiki.velocity.VelocityManager;

@ComponentList({ScriptServicesAutoCompletionMethodFinder.class})
public class AutoCompletionResourceTest extends AbstractMockingComponentTestCase
{
    @MockingRequirement(exceptions = {ComponentManager.class})
    private AutoCompletionResource resource;

    @Override
    public void configure() throws Exception
    {
        registerMockComponent(ScriptServiceManager.class);
        registerMockComponent(ScriptService.class, "test", "mock1");
        registerMockComponent(ScriptService.class, "othertest", "mock2");
    }

    private class AncillaryTestClass
    {
        public void method()
        {
        }
    }

    private class TestClass
    {
        public AncillaryTestClass doWork()
        {
            return new AncillaryTestClass();
        }

        public String getSomething()
        {
            return "";
        }

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
        setUpMocks("$", vcontext);

        String velocity = "{{velocity}}$";
        Hints hints = this.resource.getAutoCompletionHints(velocity.length(), "xwiki/2.0", velocity);

        // Verify we can get keys from the Velocity Context and its inner context too. We test this since our
        // Velocity tools are put in an inner Velocity Context.
        Assert.assertEquals(2, hints.getHints().size());

        // Also verifies that hints are sorted
        Assert.assertEquals(Arrays.asList(new HintData("key1", "key1"), new HintData("key2", "key2")),
            hints.getHints());
    }

    @Test
    public void getAutoCompletionHintsWhenDollarSignFollowedBySomeLetters() throws Exception
    {
        setUpMocks("$ke", createTestVelocityContext("key", "value", "otherKey", "otherValue"));

        String velocity = "{{velocity}}$ke";
        Hints hints = this.resource.getAutoCompletionHints(velocity.length(), "xwiki/2.0", velocity);

        Assert.assertEquals(1, hints.getHints().size());
        assertThat(hints.getHints(), containsInAnyOrder(new HintData("key", "key")));
    }

    @Test
    public void getAutoCompletionHintsWhenDollarSignFollowedBySomeNonMatchingLetters() throws Exception
    {
        setUpMocks("$o", createTestVelocityContext("key", "value"));

        String velocity = "{{velocity}}$o";
        Hints hints = this.resource.getAutoCompletionHints(velocity.length(), "xwiki/2.0", velocity);

        Assert.assertEquals(0, hints.getHints().size());
    }

    @Test
    public void getAutoCompletionHintsWhenInvalidAutoCompletion() throws Exception
    {
        setUpMocks("$k ", createTestVelocityContext());

        String velocity = "{{velocity}}$k ";
        Hints hints = this.resource.getAutoCompletionHints(velocity.length(), "xwiki/2.0", velocity);

        Assert.assertEquals(0, hints.getHints().size());
    }

    @Test
    public void getAutoCompletionHintsForMethodWhenJustAfterTheDot() throws Exception
    {
        setUpMocks("$key.", createTestVelocityContext("key", new TestClass()));

        final List<HintData> expectedMethods = Arrays.asList(
            new HintData("doWork", "doWork(...) AncillaryTestClass"),
            new HintData("something", "something String"),
            new HintData("getSomething", "getSomething(...) String"),
            new HintData("method1", "method1(...)"),
            new HintData("method2", "method2(...) String"));
        setUpMethodFinderMock(expectedMethods, "", TestClass.class);

        String velocity = "{{velocity}}$key.";
        Hints hints = this.resource.getAutoCompletionHints(velocity.length(), "xwiki/2.0", velocity);

        Assert.assertEquals(5, hints.getHints().size());

        // Verify methods are returned sorted
        Assert.assertEquals(Arrays.asList(
            new HintData("doWork", "doWork(...) AncillaryTestClass"),
            new HintData("getSomething", "getSomething(...) String"),
            new HintData("method1", "method1(...)"),
            new HintData("method2", "method2(...) String"),
            new HintData("something", "something String")), hints.getHints());
    }

    @Test
    public void getAutoCompletionHintsForMethodsWhenGetter() throws Exception
    {
        setUpMocks("$key.s", createTestVelocityContext("key", new TestClass()));

        final List<HintData> expectedMethods = Arrays.asList(
            new HintData("something", "something String"),
            new HintData("getSomething", "getSomething(...) String"));
        setUpMethodFinderMock(expectedMethods, "s", TestClass.class);

        String velocity = "{{velocity}}$key.s";
        Hints hints = this.resource.getAutoCompletionHints(velocity.length(), "xwiki/2.0", velocity);

        Assert.assertEquals(2, hints.getHints().size());
    }

    @Test
    public void getAutoCompletionHintsForMethod() throws Exception
    {
        setUpMocks("$key.m", createTestVelocityContext("key", new TestClass()));

        final List<HintData> expectedMethods = Arrays.asList(
            new HintData("method", "method1(...)"),
            new HintData("method2", "method2(...) String"));
        setUpMethodFinderMock(expectedMethods, "m", TestClass.class);

        String velocity = "{{velocity}}$key.m";
        Hints hints = this.resource.getAutoCompletionHints(velocity.length(), "xwiki/2.0", velocity);

        Assert.assertEquals(2, hints.getHints().size());
    }

    @Test
    public void getAutoCompletionHintsForScriptServiceProperty() throws Exception
    {
        ScriptServiceManager scriptServiceManager = getComponentManager().getInstance(ScriptServiceManager.class);
        setUpMocks("$services.t", createTestVelocityContext("services", scriptServiceManager));

        String velocity = "{{velocity}}$services.t";
        Hints hints = this.resource.getAutoCompletionHints(velocity.length(), "xwiki/2.0", velocity);

        Assert.assertEquals(1, hints.getHints().size());
        assertThat(hints.getHints(), containsInAnyOrder(new HintData("test", "test")));
    }

    @Test
    @Ignore("Not working yet")
    public void getAutoCompletionHintsWhenSetDoneAbove() throws Exception
    {
        String velocity = "#set ($mydoc = $key.doWork())\n$mydoc.";
        setUpMocks(velocity, createTestVelocityContext("key", new TestClass()));

        String content = "{{velocity}}" + velocity;
        Hints hints = this.resource.getAutoCompletionHints(content.length(), "xwiki/2.0", content);

        Assert.assertEquals(2, hints.getHints().size());
    }

    private void setUpMethodFinderMock(final List<HintData> expectedMethodNames, final String fragmentToMatch,
        final Class methodClass) throws Exception
    {
        final AutoCompletionMethodFinder finder = getComponentManager().getInstance(AutoCompletionMethodFinder.class);
        getMockery().checking(new Expectations()
        {
            {
                oneOf(finder).findMethods(methodClass, fragmentToMatch);
                will(returnValue(expectedMethodNames));
            }
        });

    }

    private void setUpMocks(final String expectedContent, final VelocityContext velocityContext) throws Exception
    {
        final VelocityManager velocityManager = getComponentManager().getInstance(VelocityManager.class);
        final TargetContentLocator locator = getComponentManager().getInstance(TargetContentLocator.class);
        getMockery().checking(new Expectations()
        {
            {
                oneOf(velocityManager).getVelocityContext();
                will(returnValue(velocityContext));
                oneOf(locator).locate(with(any(String.class)), with(equal("xwiki/2.0")), with(any(Integer.class)));
                will(returnValue(
                    new TargetContent(expectedContent, expectedContent.length(), TargetContentType.VELOCITY)));
            }
        });
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
