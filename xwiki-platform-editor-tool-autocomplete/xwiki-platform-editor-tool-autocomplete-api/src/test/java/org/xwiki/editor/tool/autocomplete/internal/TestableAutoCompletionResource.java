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

import javax.ws.rs.Path;

import org.apache.velocity.VelocityContext;
import org.xwiki.component.annotation.Component;

/**
 * Make it possible to easily test {@link AutoCompletionResource} so that we don't have to mock lots of old core stuff.
 *
 * @version $Id$
 * @since 5.2M1
 */
@Component("org.xwiki.editor.tool.autocomplete.internal.AutoCompletionResource")
@Path("/autocomplete")
public class TestableAutoCompletionResource extends AutoCompletionResource
{
    private VelocityContext velocityContext;

    public void setVelocityContext(VelocityContext velocityContext)
    {
        this.velocityContext = velocityContext;
    }

    @Override
    protected VelocityContext getVelocityContext()
    {
        return this.velocityContext;
    }
}
