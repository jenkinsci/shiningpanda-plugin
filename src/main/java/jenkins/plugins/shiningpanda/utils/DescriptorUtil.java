/*
 * ShiningPanda plug-in for Jenkins
 * Copyright (C) 2011-2012 ShiningPanda S.A.S.
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
package jenkins.plugins.shiningpanda.utils;

import hudson.XmlFile;
import hudson.model.Descriptor;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import jenkins.model.Jenkins;

import com.thoughtworks.xstream.XStream;

public class DescriptorUtil
{

    /**
     * A logger.
     */
    private static final Logger LOGGER = Logger.getLogger(DescriptorUtil.class.getName());

    /**
     * Get the configuration file for the provided ID
     * 
     * @param xs
     *            The XML stream
     * @param id
     *            The ID
     * @return The configuration file
     */
    public static XmlFile getConfigFile(XStream xs, String id)
    {
        return new XmlFile(xs, new File(Jenkins.getInstance().getRootDir(), id + ".xml"));
    }

    /**
     * Get the configuration file for the provided descriptor
     * 
     * @param xs
     *            The XML stream
     * @param descriptor
     *            The descriptor
     * @return The configuration file
     */
    public static XmlFile getConfigFile(XStream xs, Descriptor<?> descriptor)
    {
        return getConfigFile(xs, descriptor.getId());
    }

    /**
     * Load the configuration by looking first for the nominal file, and then by
     * looking for the provided IDs.
     * 
     * @param xs
     *            The XML stream
     * @param descriptor
     *            The descriptor
     * @param ids
     *            The addition IDs
     */
    public synchronized static void load(XStream xs, Descriptor<?> descriptor, String... ids)
    {
        // Get the nominal configuration file
        XmlFile file = getConfigFile(xs, descriptor);
        // CHeck if this file exists
        if (file.exists())
        {
            // If exists, load it
            load(descriptor, file);
            // No need to continue
            return;
        }
        // If not exists, look for the IDs
        for (String id : ids)
        {
            // Get the configuration file for the ID
            XmlFile aliasFile = getConfigFile(xs, id);
            // Check if exists
            if (aliasFile.exists())
            {
                // If exists load the file
                load(descriptor, aliasFile);
                // No need to continue
                return;
            }
        }
    }

    /**
     * Load a configuration file.
     * 
     * @param descriptor
     *            The descriptor to load to
     * @param file
     *            The configuration file
     */
    private static void load(Descriptor<?> descriptor, XmlFile file)
    {
        try
        {
            file.unmarshal(descriptor);
        }
        catch (IOException e)
        {
            LOGGER.log(Level.WARNING, "Failed to load " + file, e);
        }
    }

}
