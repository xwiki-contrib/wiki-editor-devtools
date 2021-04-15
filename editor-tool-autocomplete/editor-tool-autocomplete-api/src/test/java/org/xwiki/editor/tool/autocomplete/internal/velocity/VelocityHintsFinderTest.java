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

import java.util.Arrays;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.inject.Named;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.editor.tool.autocomplete.HintData;
import org.xwiki.editor.tool.autocomplete.Hints;
import org.xwiki.editor.tool.autocomplete.TargetContent;
import org.xwiki.editor.tool.autocomplete.TargetContentType;
import org.xwiki.script.service.ScriptServiceManager;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectComponentManager;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;
import org.xwiki.test.mockito.MockitoComponentManager;
import org.xwiki.velocity.VelocityManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link VelocityHintsFinder}.
 *
 * @version $Id:$
 */
@ComponentTest
class VelocityHintsFinderTest
{
    @InjectMockComponents
    private TestableVelocityHintsFinder hintsFinder;

    @MockComponent
    private VelocityManager velocityManager;

    @MockComponent
    private AutoCompletionMethodFinder defaultMethodFinder;

    @InjectComponentManager
    private MockitoComponentManager componentManager;

    @MockComponent
    @Named("context")
    private ComponentManager contextComponentManager;

    private class AncillaryTestClass
    {
        public String method1()
        {
            return "";
        }

        public void method2()
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

        public String getSomething(String parameter)
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
    void findHintsWhenNoDollarSign()
    {
        when(this.velocityManager.getVelocityContext()).thenReturn(new VelocityContext());
        String content = "whatever";

        Hints hints =
            this.hintsFinder.findHints(new TargetContent(content, content.length(), TargetContentType.VELOCITY));

        assertEquals(0, hints.getHints().size());
        assertEquals(0, hints.getStartOffset());
    }

    @Test
    void findHintsWhenOnlyDollarSign()
    {
        // Note that we create nested Velocity Context in order to verify that we support that
        Context innerContext = new VelocityContext();
        innerContext.put("key1", "value1");
        VelocityContext vcontext = new VelocityContext(innerContext);
        vcontext.put("key2", "value2");
        when(this.velocityManager.getVelocityContext()).thenReturn(vcontext);

        String content = "$";

        Hints hints =
            this.hintsFinder.findHints(new TargetContent(content, content.length(), TargetContentType.VELOCITY));

        // Verify we can get keys from the Velocity Context and its inner context too. We test this since our
        // Velocity tools are put in an inner Velocity Context.
        assertEquals(5, hints.getHints().size());

        // Also verifies that hints are sorted
        SortedSet<HintData> expected = new TreeSet<>();
        expected.addAll(Arrays.asList(
            new HintData("doc", "doc"),
            new HintData("key1", "key1"),
            new HintData("key2", "key2"),
            new HintData("sdoc", "sdoc"),
            new HintData("tdoc", "tdoc")
        ));
        assertEquals(expected, hints.getHints());
        assertEquals("$".length(), hints.getStartOffset());
    }

    @Test
    void findHintsWhenOnlyDollarAndBangSigns()
    {
        when(this.velocityManager.getVelocityContext()).thenReturn(
            createTestVelocityContext("key", "value", "otherKey", "otherValue"));

        String content = "$!";
        Hints hints =
            this.hintsFinder.findHints(new TargetContent(content, content.length(), TargetContentType.VELOCITY));

        assertEquals(5, hints.getHints().size());
        assertTrue(hints.getHints().contains(new HintData("key", "key")));
        assertTrue(hints.getHints().contains(new HintData("otherKey", "otherKey")));
        assertEquals("$!".length(), hints.getStartOffset());
    }

    @Test
    void findHintsWhenOnlyDollarAndCurlyBracketSigns()
    {
        when(this.velocityManager.getVelocityContext()).thenReturn(createTestVelocityContext("key", "value"));

        String content = "${";
        Hints hints =
            this.hintsFinder.findHints(new TargetContent(content, content.length(), TargetContentType.VELOCITY));

        assertEquals(4, hints.getHints().size());
        assertTrue(hints.getHints().contains(new HintData("key", "key")));
        assertEquals("${".length(), hints.getStartOffset());
    }

    @Test
    void findHintsWhenOnlyDollarBangAndCurlyBracketSigns()
    {
        when(this.velocityManager.getVelocityContext()).thenReturn(createTestVelocityContext("key", "value"));

        String content = "$!{";
        Hints hints =
            this.hintsFinder.findHints(new TargetContent(content, content.length(), TargetContentType.VELOCITY));

        assertEquals(4, hints.getHints().size());
        assertTrue(hints.getHints().contains(new HintData("key", "key")));
        assertEquals("$!{".length(), hints.getStartOffset());
    }

    @Test
    void findHintsWhenDollarSignFollowedBySomeLetters()
    {
        when(this.velocityManager.getVelocityContext()).thenReturn(
            createTestVelocityContext("key", "value", "otherKey", "otherValue"));

        String content = "$ke";
        Hints hints =
            this.hintsFinder.findHints(new TargetContent(content, content.length(), TargetContentType.VELOCITY));

        assertEquals(1, hints.getHints().size());
        assertTrue(hints.getHints().contains(new HintData("key", "key")));
        assertEquals("$".length(), hints.getStartOffset());
    }

    @Test
    void findHintsWhenDollarSignFollowedByBangSymbol()
    {
        when(this.velocityManager.getVelocityContext()).thenReturn(
            createTestVelocityContext("key", "value", "otherKey", "otherValue"));

        String content = "$!ke";
        Hints hints =
            this.hintsFinder.findHints(new TargetContent(content, content.length(), TargetContentType.VELOCITY));

        assertEquals(1, hints.getHints().size());
        assertTrue(hints.getHints().contains(new HintData("key", "key")));
        assertEquals("$!".length(), hints.getStartOffset());
    }

    @Test
    void findHintsWhenDollarSignFollowedByCurlyBracketSymbol()
    {
        when(this.velocityManager.getVelocityContext()).thenReturn(
            createTestVelocityContext("key", "value", "otherKey", "otherValue"));

        String content = "${ke";
        Hints hints =
            this.hintsFinder.findHints(new TargetContent(content, content.length(), TargetContentType.VELOCITY));

        assertEquals(1, hints.getHints().size());
        assertTrue(hints.getHints().contains(new HintData("key", "key")));
        assertEquals("${".length(), hints.getStartOffset());
    }

    @Test
    void findHintsWhenDollarSignFollowedByBangAndCurlyBracketSymbol()
    {
        when(this.velocityManager.getVelocityContext()).thenReturn(
            createTestVelocityContext("key", "value", "otherKey", "otherValue"));

        String content = "$!{ke";
        Hints hints =
            this.hintsFinder.findHints(new TargetContent(content, content.length(), TargetContentType.VELOCITY));

        assertEquals(1, hints.getHints().size());
        assertTrue(hints.getHints().contains(new HintData("key", "key")));
        assertEquals("$!{".length(), hints.getStartOffset());
    }


    @Test
    void findHintsWhenDollarSignFollowedBySomeNonMatchingLetters()
    {
        when(this.velocityManager.getVelocityContext()).thenReturn(createTestVelocityContext("key", "value"));

        String content = "$o";
        Hints hints =
            this.hintsFinder.findHints(new TargetContent(content, content.length(), TargetContentType.VELOCITY));

        assertEquals(0, hints.getHints().size());
        assertEquals("$".length(), hints.getStartOffset());
    }

    /**
     * See http://jira.xwiki.org/browse/WIKIEDITOR-18
     */
    @Test
    void findHintsWhenDollarSignFollowedBySomeNonMatchingLettersThenDot()
    {
        when(this.velocityManager.getVelocityContext()).thenReturn(createTestVelocityContext("key", "value"));

        String content = "$o.";
        Hints hints =
            this.hintsFinder.findHints(new TargetContent(content, content.length(), TargetContentType.VELOCITY));

        assertEquals(0, hints.getHints().size());
        assertEquals(0, hints.getStartOffset());
    }

    @Test
    void findHintsWhenInvalidAutoCompletion()
    {
        when(this.velocityManager.getVelocityContext()).thenReturn(createTestVelocityContext());

        String content = "$k ";
        Hints hints =
            this.hintsFinder.findHints(new TargetContent(content, content.length(), TargetContentType.VELOCITY));

        assertEquals(0, hints.getHints().size());
        assertEquals(0, hints.getStartOffset());
    }

    @Test
    void findHintsWhenJustAfterTheDot()
    {
        when(this.velocityManager.getVelocityContext()).thenReturn(
            createTestVelocityContext("key", new TestClass()));

        Hints expectedMethods = new Hints().withHints(
            new HintData("doWork", "doWork(...) AncillaryTestClass"),
            new HintData("something", "something String"),
            new HintData("getSomething", "getSomething(...) String"),
            new HintData("method1", "method1(...)"),
            new HintData("method2", "method2(...) String")
        );
        setupMethodFinderMock(expectedMethods, "", TestClass.class);

        String content = "$key.";
        Hints hints =
            this.hintsFinder.findHints(new TargetContent(content, content.length(), TargetContentType.VELOCITY));

        assertEquals(5, hints.getHints().size());

        // Verify methods are returned sorted
        SortedSet<HintData> expected = new TreeSet<>();
        expected.add(new HintData("doWork", "doWork(...) AncillaryTestClass"));
        expected.add(new HintData("getSomething", "getSomething(...) String"));
        expected.add(new HintData("method1", "method1(...)"));
        expected.add(new HintData("method2", "method2(...) String"));
        expected.add(new HintData("something", "something String"));
        assertEquals(expected, hints.getHints());
        assertEquals("$key.".length(), hints.getStartOffset());
    }

    @Test
    void findHintsWhenThereIsContentAfterCursor()
    {
        when(this.velocityManager.getVelocityContext()).thenReturn(
            createTestVelocityContext("key", new TestClass()));

        Hints expectedMethods = new Hints().withHints(new HintData("doWork", "doWork(...) AncillaryTestClass"));
        setupMethodFinderMock(expectedMethods, "do", TestClass.class);

        String content = "$key.do";
        Hints hints =
            this.hintsFinder.findHints(new TargetContent(content, content.length(), TargetContentType.VELOCITY));

        assertEquals(1, hints.getHints().size());

        // Verify methods are returned sorted
        SortedSet<HintData> expected = new TreeSet<>();
        expected.add(new HintData("doWork", "doWork(...) AncillaryTestClass"));
        assertEquals(expected, hints.getHints());
        assertEquals("$key.".length(), hints.getStartOffset());
    }

    @Test
    void findHintsdWhenThereIsContentAfterCursorWithCapitalLetters()
    {
        when(this.velocityManager.getVelocityContext()).thenReturn(
            createTestVelocityContext("key", new TestClass()));

        Hints expectedMethods = new Hints().withHints(new HintData("doWork", "doWork(...) AncillaryTestClass"));
        setupMethodFinderMock(expectedMethods, "doW", TestClass.class);

        String content = "$key.doW";
        Hints hints =
            this.hintsFinder.findHints(new TargetContent(content, content.length(), TargetContentType.VELOCITY));

        assertEquals(1, hints.getHints().size());

        // Verify methods are returned sorted
        SortedSet<HintData> expected = new TreeSet<>();
        expected.add(new HintData("doWork", "doWork(...) AncillaryTestClass"));
        assertEquals(expected, hints.getHints());
        assertEquals("$key.".length(), hints.getStartOffset());
    }

    @Test
    void findHintsWhenGetter()
    {
        when(this.velocityManager.getVelocityContext()).thenReturn(
            createTestVelocityContext("key", new TestClass()));

        Hints expectedMethods = new Hints().withHints(new HintData("something", "something String"));
        setupMethodFinderMock(expectedMethods, "s", TestClass.class);

        String content = "$key.s";
        Hints hints =
            this.hintsFinder.findHints(new TargetContent(content, content.length(), TargetContentType.VELOCITY));

        assertEquals(1, hints.getHints().size());
        assertEquals("$key.".length(), hints.getStartOffset());
    }

    @Test
    void findHints()
    {
        when(this.velocityManager.getVelocityContext()).thenReturn(
            createTestVelocityContext("key", new TestClass()));

        Hints expectedMethods = new Hints().withHints(
            new HintData("method", "method1(...)"),
            new HintData("method2", "method2(...) String")
        );
        setupMethodFinderMock(expectedMethods, "m", TestClass.class);

        String content = "$key.m";
        Hints hints =
            this.hintsFinder.findHints(new TargetContent(content, content.length(), TargetContentType.VELOCITY));

        assertEquals(2, hints.getHints().size());
        assertEquals("$key.".length(), hints.getStartOffset());
    }

    @Test
    void findHintsForScriptServiceProperty() throws Exception
    {
        when(this.contextComponentManager.hasComponent(AutoCompletionMethodFinder.class, "services")).thenReturn(true);
        AutoCompletionMethodFinder scriptServiceFinder = mock(AutoCompletionMethodFinder.class);
        when(this.contextComponentManager.getInstance(AutoCompletionMethodFinder.class, "services")).thenReturn(
            scriptServiceFinder);
        ScriptServiceManager scriptServiceManager = mock(ScriptServiceManager.class);
        when(this.velocityManager.getVelocityContext()).thenReturn(
            createTestVelocityContext("services", scriptServiceManager));
        when(scriptServiceFinder.findMethods(scriptServiceManager.getClass(), "t")).thenReturn(
            new Hints().withHints(new HintData("test", "test")));

        String content = "$services.t";
        Hints hints =
            this.hintsFinder.findHints(new TargetContent(content, content.length(), TargetContentType.VELOCITY));

        assertEquals(1, hints.getHints().size());
        assertTrue(hints.getHints().contains(new HintData("test", "test")));
        assertEquals("$services.".length(), hints.getStartOffset());
    }

    @Test
    void findHintsForScriptServicePropertyWhenNestedCalls() throws Exception
    {
        when(this.contextComponentManager.hasComponent(AutoCompletionMethodFinder.class, "services")).thenReturn(true);
        AutoCompletionMethodFinder scriptServiceFinder = mock(AutoCompletionMethodFinder.class);
        when(this.contextComponentManager.getInstance(AutoCompletionMethodFinder.class, "services")).thenReturn(
            scriptServiceFinder);
        ScriptServiceManager scriptServiceManager = mock(ScriptServiceManager.class);
        when(this.velocityManager.getVelocityContext()).thenReturn(
            createTestVelocityContext("services", scriptServiceManager));
        when(scriptServiceFinder.findMethodReturnTypes(scriptServiceManager.getClass(), "query")).thenReturn(
            Arrays.asList(TestClass.class));
        when(this.defaultMethodFinder.findMethods(TestClass.class, "m")).thenReturn(
            new Hints().withHints(new HintData("method", "method")));

        String content = "$services.query.m";
        Hints hints =
            this.hintsFinder.findHints(new TargetContent(content, content.length(), TargetContentType.VELOCITY));

        assertEquals(1, hints.getHints().size());
        assertTrue(hints.getHints().contains(new HintData("method", "method")));
        assertEquals("$services.query.".length(), hints.getStartOffset());
    }

    @Test
    void findScriptServiceHintWhenUsingADot() throws Exception
    {
        when(this.contextComponentManager.hasComponent(AutoCompletionMethodFinder.class, "services")).thenReturn(true);
        ScriptServicesAutoCompletionMethodFinder scriptServiceFinder =
            mock(ScriptServicesAutoCompletionMethodFinder.class);
        when(this.contextComponentManager.getInstance(AutoCompletionMethodFinder.class, "services")).thenReturn(
            scriptServiceFinder);
        ScriptServiceManager scriptServiceManager = mock(ScriptServiceManager.class);
        when(this.velocityManager.getVelocityContext()).thenReturn(
            createTestVelocityContext("services", scriptServiceManager));
        when(scriptServiceFinder.findMethodReturnTypes(scriptServiceManager.getClass(), "security.authorization"))
            .thenReturn(Arrays.asList(TestClass.class));
        when(this.defaultMethodFinder.findMethods(TestClass.class, "")).thenReturn(
            new Hints().withHints(new HintData("method", "method")));

        String content = "$services.security.authorization.";
        Hints hints =
            this.hintsFinder.findHints(new TargetContent(content, content.length(), TargetContentType.VELOCITY));

        assertEquals(1, hints.getHints().size());
        assertTrue(hints.getHints().contains(new HintData("method", "method")));
    }

    @Test
    void findScriptServiceHintWithDotNoResult() throws Exception
    {
        when(this.contextComponentManager.hasComponent(AutoCompletionMethodFinder.class, "services")).thenReturn(true);
        ScriptServicesAutoCompletionMethodFinder scriptServiceFinder =
            mock(ScriptServicesAutoCompletionMethodFinder.class);
        when(this.contextComponentManager.getInstance(AutoCompletionMethodFinder.class, "services")).thenReturn(
            scriptServiceFinder);
        ScriptServiceManager scriptServiceManager = mock(ScriptServiceManager.class);
        when(this.velocityManager.getVelocityContext()).thenReturn(
            createTestVelocityContext("services", scriptServiceManager));

        String content = "$services.security.authorization.something";
        Hints hints =
            this.hintsFinder.findHints(new TargetContent(content, content.length(), TargetContentType.VELOCITY));

        assertTrue(hints.getHints().isEmpty());
    }

    @Test()
    @Disabled("Not working yet")
    void findHintsWhenSetDoneAbove()
    {
        when(this.velocityManager.getVelocityContext()).thenReturn(
            createTestVelocityContext("key", new TestClass()));

        String content = "#set ($mydoc = $key.doWork())\n$mydoc.";
        Hints hints =
            this.hintsFinder.findHints(new TargetContent(content, content.length(), TargetContentType.VELOCITY));

        assertEquals(2, hints.getHints().size());
    }

    @Test
    void findHintsForChainedMethod()
    {
        when(this.velocityManager.getVelocityContext()).thenReturn(
            createTestVelocityContext("key", new TestClass()));

        Hints expectedMethods = new Hints().withHints(
            new HintData("method1", "method1"),
            new HintData("method2", "method2")
        );
        when(this.defaultMethodFinder.findMethods(AncillaryTestClass.class, "m")).thenReturn(expectedMethods);
        when(this.defaultMethodFinder.findMethodReturnTypes(TestClass.class, "doWork")).thenReturn(
            Arrays.asList(AncillaryTestClass.class));

        String content = "$key.doWork().m";
        Hints hints =
            this.hintsFinder.findHints(new TargetContent(content, content.length(), TargetContentType.VELOCITY));

        assertEquals(2, hints.getHints().size());
        assertEquals(new HintData("method1", "method1"), hints.getHints().first());
        assertEquals(new HintData("method2", "method2"), hints.getHints().last());
        assertEquals("$key.doWork().".length(), hints.getStartOffset());
    }

    @Test
    void findHintsForChainedMethodEndingWithDot()
    {
        when(this.velocityManager.getVelocityContext()).thenReturn(
            createTestVelocityContext("key", new TestClass()));

        Hints expectedMethods = new Hints().withHints(
            new HintData("method1", "method1"),
            new HintData("method2", "method2")
        );
        when(this.defaultMethodFinder.findMethods(AncillaryTestClass.class, "")).thenReturn(expectedMethods);
        when(this.defaultMethodFinder.findMethodReturnTypes(TestClass.class, "doWork")).thenReturn(
            Arrays.asList((Class) AncillaryTestClass.class));

        String content = "$key.doWork().";
        Hints hints =
            this.hintsFinder.findHints(new TargetContent(content, content.length(), TargetContentType.VELOCITY));

        assertEquals(2, hints.getHints().size());
        assertEquals(new HintData("method1", "method1"), hints.getHints().first());
        assertEquals(new HintData("method2", "method2"), hints.getHints().last());
        assertEquals("$key.doWork().".length(), hints.getStartOffset());
    }

    @Test
    void findHintsForDeepChainedMethod()
    {
        when(this.velocityManager.getVelocityContext()).thenReturn(
            createTestVelocityContext("key", new TestClass()));

        Hints expectedMethods = new Hints().withHints(
            new HintData("split", "split")
        );
        when(this.defaultMethodFinder.findMethodReturnTypes(TestClass.class, "doWork")).thenReturn(
            Arrays.asList((Class) AncillaryTestClass.class));
        when(this.defaultMethodFinder.findMethodReturnTypes(AncillaryTestClass.class, "method1")).thenReturn(
            Arrays.asList((Class) String.class));
        when(this.defaultMethodFinder.findMethods(String.class, "spl")).thenReturn(expectedMethods);

        String content = "$key.doWork().method1().spl";
        Hints hints =
            this.hintsFinder.findHints(new TargetContent(content, content.length(), TargetContentType.VELOCITY));

        assertEquals(1, hints.getHints().size());
        assertEquals(new HintData("split", "split"), hints.getHints().first());
        assertEquals("$key.doWork().method1().".length(), hints.getStartOffset());
    }

    @Test
    void findHintsWhenSeveralDollar()
    {
        when(this.velocityManager.getVelocityContext()).thenReturn(
            createTestVelocityContext("key", new TestClass()));

        Hints expectedMethods = new Hints().withHints(
            new HintData("method1", "method1")
        );
        setupMethodFinderMock(expectedMethods, "", TestClass.class);

        String content = "$var\n$key.";
        Hints hints =
            this.hintsFinder.findHints(new TargetContent(content, content.length(), TargetContentType.VELOCITY));

        assertEquals(1, hints.getHints().size());
        assertEquals(new HintData("method1", "method1"), hints.getHints().first());
        assertEquals("$var\n$key.".length(), hints.getStartOffset());
    }

    @Test
    void findHintsWhenVelocityVariableDefined()
    {
        when(this.velocityManager.getVelocityContext()).thenReturn(new VelocityContext());

        String content = "##comment\n"
            + "#set ($var = $services.wiki.all)\n"
            + "$var\n"
            + "#if ($var == \"test\")\n"
            + "whatever\n"
            + "#end\n"
            + "#set ($var2 = \"test\")\n"
            + "$";
        Hints hints =
            this.hintsFinder.findHints(new TargetContent(content, content.length(), TargetContentType.VELOCITY));
        assertEquals(5, hints.getHints().size());
        assertTrue(hints.getHints().contains(new HintData("var", "$var")));
        assertTrue(hints.getHints().contains(new HintData("var2", "$var2")));
        assertEquals(content.length(), hints.getStartOffset());
    }

    @Test
    void findHintsWhenCurrentLineDefinesAVelocityVariableAndEmptyVariable()
    {
        when(this.velocityManager.getVelocityContext()).thenReturn(new VelocityContext());

        String content = "#set ($";
        Hints hints =
            this.hintsFinder.findHints(new TargetContent(content, content.length(), TargetContentType.VELOCITY));
        assertEquals(3, hints.getHints().size());
        // Verify that we don't include an empty autocompletion hint matching the #set...
        SortedSet<HintData> expected = new TreeSet<>();
        expected.addAll(Arrays.asList(
            new HintData("doc", "doc"),
            new HintData("sdoc", "sdoc"),
            new HintData("tdoc", "tdoc")
        ));
        assertEquals(expected, hints.getHints());
        assertEquals(content.length(), hints.getStartOffset());
    }

    @Test
    void findHintsWhenCurrentLineDefinesAVelocityVariableAndNonEmptyVariable()
    {
        when(this.velocityManager.getVelocityContext()).thenReturn(new VelocityContext());

        String content = "#set ($var";
        Hints hints =
            this.hintsFinder.findHints(new TargetContent(content, content.length() - 1, TargetContentType.VELOCITY));
        assertEquals(0, hints.getHints().size());
        assertEquals(0, hints.getStartOffset());
    }

    @Test
    void findHintsWhenVelocityVariableDefinedInSeveralVelocityRenderingMacros()
    {
        when(this.velocityManager.getVelocityContext()).thenReturn(new VelocityContext());

        String previousContent = "#set ($var3 = \"whatever\")\n";
        String mainContent = "##comment\n"
            + "#set ($var = $services.wiki.all)\n"
            + "$var\n"
            + "#if ($var == \"test\")\n"
            + "whatever\n"
            + "#end\n"
            + "#set ($var2 = \"test\")\n"
            + "$";
        Hints hints = this.hintsFinder.findHints(new TargetContent(mainContent, mainContent.length(),
            TargetContentType.VELOCITY, Arrays.asList(previousContent)));
        assertEquals(6, hints.getHints().size());
        assertTrue(hints.getHints().contains(new HintData("var", "$var")));
        assertTrue(hints.getHints().contains(new HintData("var2", "$var2")));
        assertTrue(hints.getHints().contains(new HintData("var3", "$var3")));
        assertEquals(mainContent.length(), hints.getStartOffset());
    }

    private VelocityContext createTestVelocityContext(Object... properties)
    {
        final VelocityContext context = new VelocityContext();
        for (int i = 0; i < properties.length; i += 2) {
            context.put((String) properties[i], properties[i + 1]);
        }
        return context;
    }

    private void setupMethodFinderMock(Hints expectedMethodNames, String fragmentToMatch, Class methodClass)
    {
        when(this.defaultMethodFinder.findMethods(methodClass, fragmentToMatch)).thenReturn(expectedMethodNames);
    }
}
