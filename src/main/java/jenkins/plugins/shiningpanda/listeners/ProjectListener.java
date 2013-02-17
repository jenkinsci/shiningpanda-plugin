/*
 * ShiningPanda plug-in for Jenkins
 * Copyright (C) 2011-2013 ShiningPanda S.A.S.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of its license which incorporates the terms and 
 * conditions of version 3 of the GNU Affero General Public License, 
 * supplemented by the additional permissions under the GNU Affero GPL
 * version 3 section 7: if you modify this program, or any covered work, 
 * by linking or combining it with other code, such other code is not 
 * for that reason alone subject to any of the requirements of the GNU
 * Affero GPL version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * license for more details.
 *
 * You should have received a copy of the license along with this program.
 * If not, see <https://raw.github.com/jenkinsci/shiningpanda-plugin/master/LICENSE.txt>.
 */
package jenkins.plugins.shiningpanda.listeners;

import hudson.Extension;
import hudson.model.Item;
import hudson.model.listeners.ItemListener;
import jenkins.plugins.shiningpanda.workspace.Workspace;

@Extension
public class ProjectListener extends ItemListener
{

    /*
     * (non-Javadoc)
     * 
     * @see hudson.model.listeners.ItemListener#onDeleted(hudson.model.Item)
     */
    @Override
    public void onDeleted(Item item)
    {
        // Delegate
        Workspace.delete(item);
    }

    /*
     * (non-Javadoc)
     * 
     * @see hudson.model.listeners.ItemListener#onRenamed(hudson.model.Item,
     * java.lang.String, java.lang.String)
     */
    @Override
    public void onRenamed(Item item, String oldName, String newName)
    {
        // Delegate
        Workspace.delete(item, oldName);
    }

}
