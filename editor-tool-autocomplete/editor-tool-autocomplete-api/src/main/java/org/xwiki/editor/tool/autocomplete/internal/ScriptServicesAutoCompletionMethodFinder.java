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

import java.lang.reflect.Type;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.descriptor.ComponentDescriptor;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.script.service.ScriptService;

/**
 * Returns autocompletion hints for the {@code services} Velocity binding by returning the full list of all
 * {@link ScriptService} bindings that exist.
 * 
 * @version $Id$
 */
@Component
@Named("services")
@Singleton
public class ScriptServicesAutoCompletionMethodFinder extends AbstractAutoCompletionMethodFinder
{
    /**
     * Used to get Component descriptors for existing script services to get their Role Hints which are the
     * autocompletion hints we wish to return.
     */
    @Inject
    private ComponentManager componentManager;

    @Override
    public Hints findMethods(Class variableClass, String fragmentToMatch)
    {
        Hints results = new Hints();

        List<ComponentDescriptor<ScriptService>> descriptors =
            this.componentManager.getComponentDescriptorList((Type) ScriptService.class);

        for (ComponentDescriptor<ScriptService> descriptor : descriptors) {
            if (descriptor.getRoleHint().startsWith(fragmentToMatch)) {
                results.withHints(new HintData(descriptor.getRoleHint(), descriptor.getRoleHint()));
            }
        }

        return results;
    }
}
