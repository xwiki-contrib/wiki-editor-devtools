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

import java.io.BufferedReader;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.editor.tool.autocomplete.HintData;
import org.xwiki.editor.tool.autocomplete.Hints;
import org.xwiki.editor.tool.autocomplete.HintsFinder;
import org.xwiki.editor.tool.autocomplete.TargetContent;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.velocity.VelocityManager;
import org.xwiki.velocity.internal.util.InvalidVelocityException;
import org.xwiki.velocity.internal.util.VelocityParser;
import org.xwiki.velocity.internal.util.VelocityParserContext;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;

@Component
@Named("velocity")
@Singleton
public class VelocityHintsFinder implements HintsFinder
{
    private static final String SCRIPT_SERVICE_IDENTIFIER = "services.";
    private static final String DOT = ".";

    @Inject
    private Logger logger;

    /**
     * Used to get the Velocity Context from which we retrieve the list of bound variables that are used for
     * autocompletion.
     */
    @Inject
    private VelocityManager velocityManager;

    /**
     * Used to dynamically find Autocompletion Method finder to handle specific cases.
     *
     * @see AutoCompletionMethodFinder
     */
    @Inject
    @Named("context")
    private ComponentManager contextComponentManager;

    /**
     * Used to autodiscover method hints.
     */
    @Inject
    private AutoCompletionMethodFinder defaultAutoCompletionMethodFinder;

    /**
     * Used to create fake documents and add "doc", "tdoc", and "sdoc" in the velocity context.
     */
    @Inject
    private Provider<XWikiContext> xcontextProvider;

    /**
     * A Velocity Parser that we use to help parse Velocity content for figuring out autocompletion.
     */
    private final VelocityParser parser = new VelocityParser();

    @Override
    public Hints findHints(TargetContent targetContent)
    {
        Hints results = new Hints();

        // General algorithm:
        // - We start parsing at the first dollar before the cursor
        // - We get the full reference (a reference in VTL can be a variable, a property or a method call, see
        // http://velocity.apache.org/engine/devel/user-guide.html#References)
        // - We split the reference on "." and handle first the case of a variable. If there's no "." then it means
        // we're autocompleting a variable and we find all matching Velocity context variables and return them.
        // - If there's at least one "." then we parse the whole chain of method calls till the last dot to find the
        // return type of the last method call. This allows us to know the full list of methods for autocompletion.

        // Find the dollar sign before the current position
        char[] chars = targetContent.getContent().toCharArray();
        VelocityContext velocityContext = getVelocityContext();
        int dollarPos = StringUtils.lastIndexOf(targetContent.getContent(), '$', targetContent.getPosition());

        if (dollarPos == -1) {
            return results;
        }

        // Special case for when there's no variable after the dollar position since the Velocity Parser doesn't
        // support parsing this case.
        if (isCursorDirectlyAfterDollar(chars, dollarPos, targetContent.getPosition())) {
            results = getVariableHints(targetContent, "", velocityContext);
            // Set the offset to be just after the dollar so that the completion replaces everything after it.
            results = results.withStartOffset(dollarPos + getDollarLength(targetContent.getContent(),
                dollarPos) + 1);
        } else {
            // The cursor is not directly after the dollar sign.
            try {
                // Get all the references after the dollar sign. For example if the input is "$a.b().ccc" then
                // we get "a.b().ccc".
                VelocityParserContext context = new VelocityParserContext();
                StringBuffer reference = new StringBuffer();
                StringBuffer identifier = new StringBuffer();
                int endPos = this.parser.getVar(chars, dollarPos, identifier, reference, context);

                // If endPos matches the current cursor position then it means we have a valid token for
                // autocompletion. Otherwise we don't autocomplete (for example there could be spaces between the
                // reference and the cursor position).
                // Note: We need to handle the special case when the cursor is just after the '.' char.
                if (endPos + 1 == targetContent.getPosition() && chars[endPos] == '.') {
                    endPos++;
                    reference.append('.');
                }
                if (endPos == targetContent.getPosition()) {
                    // Find out if we're autocompleting a variable. In this case there's no "." in the reference
                    int methodPos = reference.indexOf(DOT);
                    if (methodPos > -1) {
                        // Autocomplete a method!
                        results = getHintsForMethodCall(chars, dollarPos + methodPos, identifier.toString());
                    } else {
                        // Autocomplete a variable! Find all matching variables.
                        results = getVariableHints(targetContent, identifier.toString(), velocityContext);
                        // Set the offset to be just after the dollar sign so that the completion replaces everything
                        // after it.
                        results = results.withStartOffset(dollarPos + getDollarLength(targetContent.getContent(),
                            dollarPos) + 1);
                    }
                }
            } catch (InvalidVelocityException e) {
                this.logger.debug("Failed to get autocomplete hints for content [{}] at offset [{}]",
                    targetContent.getContent(), targetContent.getPosition(), e);
            }
        }

        return results;
    }

    /**
     * @param chars the content to parse
     * @param dollarPos the position of the dollar symbol
     * @param offset the position in the whole content of the cursor
     * @return false if there's a velocity variable after the dollar sign
     */
    private boolean isCursorDirectlyAfterDollar(char[] chars, int dollarPos, int offset)
    {
        boolean result = true;

        for (int i = dollarPos; i < offset; i++) {
            if (chars[i] != '$' && chars[i] != '!' && chars[i] != '{') {
                result = false;
                break;
            }
        }

        return result;
    }

    private int getDollarLength(String content, int dollarPos)
    {
        int pos = 0;
        if (content.length() > dollarPos + 1) {
            if (content.charAt(dollarPos + 1) == '!') {
                pos++;
                if (content.length() > dollarPos + 2 && content.charAt(dollarPos + 2) == '{') {
                    pos++;
                }
            } else if (content.charAt(dollarPos + 1) == '{') {
                pos++;
            }
        }
        return pos;
    }

    private Object[] getKeys(Context velocityContext)
    {
        // We call getKeys() using reflevity because
        // before Velocity 2.0 it was returning Object[]
        // and after it's returning String[].
        try {
            Method getKeys = VelocityContext.class.getMethod("getKeys");
            return (Object[]) getKeys.invoke(velocityContext);
        } catch (Exception e) {
            throw new RuntimeException("Error while trying to call AbstractContext#getKeys", e);
        }
    }

    /**
     * Find out all Velocity variable names bound in the Velocity Context.
     *
     * @param fragmentToMatch the prefix to filter with in order to return only variable whose names start with the
     *            passed string
     * @param velocityContext the Velocity Context from which to get the bound variables
     * @return the Velocity variables
     */
    private Hints getVelocityContextKeys(String fragmentToMatch, VelocityContext velocityContext)
    {
        Hints hints = new Hints();

        addVelocityKeys(hints, getKeys(velocityContext), fragmentToMatch);
        if (velocityContext.getChainedContext() != null) {
            addVelocityKeys(hints, getKeys(velocityContext.getChainedContext()), fragmentToMatch);
        }

        return hints;
    }

    /**
     * Find:
     * <ul>
     *   <li>all objects bound to the Velocity Context. We need to also look in the chained context since
     *       this is where we store Velocity Tools</li>
     *   <li>all the defined velocity variables in the current content</li>
     * </ul>
     */
    private Hints getVariableHints(TargetContent content, String fragmentToMatch, VelocityContext velocityContext)
    {
        Hints hints = getVelocityContextKeys(fragmentToMatch, velocityContext);
        if (content.getContextData() != null) {
            for (String previousVelocityMacroContent : (List<String>) content.getContextData()) {
                hints.withHints(getVelocityVariableHints(previousVelocityMacroContent, fragmentToMatch));
            }
        }
        hints.withHints(getVelocityVariableHints(content.getContent(), fragmentToMatch));
        return hints;
    }

    /**
     * @return the Velocity Context used to find existing bound variables
     */
    protected VelocityContext getVelocityContext()
    {
        VelocityContext velocityContext = this.velocityManager.getVelocityContext();
        XWikiContext context = this.xcontextProvider.get();

        // We add the "doc", "tdoc" and "sdoc" mappings since we don't get them from the Velocity Manager as they are
        // normally added based on the document passed in the request. However since we return the same method names
        // whatever the doc, we can manually add them.
        Document fakeDocument = createFakeXWikiDocument().newDocument(context);
        velocityContext.put("doc", fakeDocument);
        velocityContext.put("sdoc", fakeDocument);
        velocityContext.put("tdoc", fakeDocument);

        return velocityContext;
    }

    /**
     * Add variables to the passed results list.
     *
     * @param results the list of variable names
     * @param keys the keys containing the variables to add
     * @param fragmentToMatch the filter in order to only add variable whose names start with the passed string
     */
    private void addVelocityKeys(Hints results, Object[] keys, String fragmentToMatch)
    {
        for (Object key : keys) {
            if (key instanceof String && ((String) key).startsWith(fragmentToMatch)) {
                results.withHints(new HintData((String) key, (String) key));
            }
        }
    }

    /**
     * @param hint the hint of the finder to return. If no such component exist return the default finder
     * @return the {@link AutoCompletionMethodFinder} to use depending on the passed hint
     */
    private AutoCompletionMethodFinder getMethodFinder(String hint)
    {
        AutoCompletionMethodFinder finder = null;

        // Allow special handling for classes that have registered a custom introspection handler
        if (this.contextComponentManager.hasComponent(AutoCompletionMethodFinder.class, hint)) {
            try {
                finder = this.contextComponentManager.getInstance(AutoCompletionMethodFinder.class, hint);
            } catch (ComponentLookupException e) {
                // Component not found, continue with default finder...
            }
        }

        if (finder == null) {
            finder = this.defaultAutoCompletionMethodFinder;
        }

        return finder;
    }

    /**
     * Find hints for the passed content assuming that it's representing method calls.
     *
     * @param chars the content to parse
     * @param currentPos the current position at which method calls are starting
     * @param variableName the name of the variable on which the first method is called
     * @return the list of autocompletion hints
     * @throws InvalidVelocityException if a parsing error occurs
     */
    private Hints getHintsForMethodCall(char[] chars, int currentPos, String variableName)
        throws InvalidVelocityException
    {
        Hints results = new Hints();
        VelocityParserContext context = new VelocityParserContext();

        // Find the next method after the currentPos.
        int pos = currentPos;

        // Handle the case when the variable of the first method call does not exist, also avoiding a NPE here.
        // See http://jira.xwiki.org/browse/WIKIEDITOR-18
        Object contextVariable = getVelocityContext().get(variableName);
        if (contextVariable == null) {
            return results;
        }

        AutoCompletionMethodFinder methodFinder = getMethodFinder(variableName);
        List<Class<?>> methodClasses = Collections.singletonList(contextVariable.getClass());

        // true if we are trying to identify the hint of a script service.
        boolean isScriptService = (methodFinder instanceof ScriptServicesAutoCompletionMethodFinder);
        int nextDot = -1;
        do {
            // Handle the special case when the cursor is after the dot ('.')
            String methodName;
            if (pos == chars.length - 1) {
                methodName = "";
                pos++;
            // Special handling for identifying the hint of a script service since the hint could contain a dot
            // the idea here is to check first what's after "services." until first dot and to iterate on each
            // following dots until a script service component is actually found.
            // The until part is managed by below.
            } else if (isScriptService) {
                String identifier = new String(chars);
                int servicesDot = identifier.indexOf(SCRIPT_SERVICE_IDENTIFIER) + SCRIPT_SERVICE_IDENTIFIER.length();
                nextDot = (nextDot == -1) ? identifier.indexOf(DOT, servicesDot) : identifier.indexOf(DOT, nextDot + 1);

                if (nextDot != -1) {
                    pos = nextDot;
                    methodName = identifier.substring(servicesDot, nextDot);
                } else {
                    isScriptService = false;
                    continue;
                }
            } else {
                StringBuffer method = new StringBuffer();
                pos = this.parser.getMethodOrProperty(chars, pos, method, context);
                methodName = StringUtils.substringBefore(method.toString(), "(").substring(1);
            }

            if (pos == chars.length) {
                // Find all methods matching methodName in methodClasses
                for (Class<?> methodClass : methodClasses) {
                    results.withHints(methodFinder.findMethods(methodClass, methodName));
                }

                // Set the offset to be just after the last dot so that the completion replaces everything
                // after it.
                results = results.withStartOffset(pos - methodName.length());

                break;
            } else {
                // Find the returned type for method "methodName".
                List<Class<?>> returnTypes = new ArrayList<>();
                for (Class<?> methodClass : methodClasses) {
                    returnTypes.addAll(methodFinder.findMethodReturnTypes(methodClass, methodName));
                }

                // This is the "until" part of the script service hint finding mechanism.
                // If we are searching for a script service, we want to keep searching until either:
                //   - a return type is found (i.e. a script service component has been found)
                //   - we already reached the latest dots
                if (!isScriptService || !returnTypes.isEmpty() || nextDot == new String(chars).lastIndexOf(DOT)) {
                    methodClasses = returnTypes;

                    // Reset the method finder since we use a specialized finder only for the first autocompletion
                    // method
                    methodFinder = this.defaultAutoCompletionMethodFinder;

                    isScriptService = false;
                }
            }
        } while (true);

        return results;
    }


    /**
     * @return a fake XWiki Document instance
     */
    protected XWikiDocument createFakeXWikiDocument()
    {
        // Note: Creating an XWikiDocument instance requires that the static component manager be set up
        // unfortunately...
        Utils.setComponentManager(this.contextComponentManager);
        return new XWikiDocument(new DocumentReference("notusedwiki", "notusedspace", "notusedpage"));
    }

    protected Hints getVelocityVariableHints(String content, String fragmentToMatch)
    {
        Hints hints = new Hints();
        for (String velocityVariable : getVelocityVariables(content)) {
            if (fragmentToMatch == null || velocityVariable.startsWith(fragmentToMatch)) {
                hints.withHints(new HintData(velocityVariable, "$" + velocityVariable));
            }
        }
        return hints;
    }

    private List<String> getVelocityVariables(String content)
    {
        List<String> variables = new ArrayList<>();
        new BufferedReader(new StringReader(content))
            .lines().forEach(line -> {
                String trimmedLine = StringUtils.trim(line);
                if (trimmedLine.startsWith("#")) {
                    try {
                        VelocityParserContext context = new VelocityParserContext();
                        StringBuffer buffer = new StringBuffer();
                        parser.getDirective(trimmedLine.toCharArray(), 0, buffer, context);
                        String text = buffer.toString();
                        if (text.startsWith("#set")) {
                            int pos = text.indexOf('$', 4);
                            if (pos > -1) {
                                String variable = StringUtils.substringBefore(text.substring(pos + 1), "=").trim();
                                variables.add(variable);
                            }
                        }
                    } catch (Exception e) {
                        // Ignore
                    }
                }
            });
        return variables;
    }
}

