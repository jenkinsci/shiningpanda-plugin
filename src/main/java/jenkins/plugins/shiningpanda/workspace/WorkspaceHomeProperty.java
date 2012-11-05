package jenkins.plugins.shiningpanda.workspace;

import hudson.Extension;
import hudson.FilePath;
import hudson.Functions;
import hudson.Util;
import hudson.model.Node;
import hudson.slaves.NodeProperty;
import hudson.slaves.NodePropertyDescriptor;
import jenkins.model.Jenkins;
import jenkins.plugins.shiningpanda.Messages;

import org.kohsuke.stapler.DataBoundConstructor;

public class WorkspaceHomeProperty extends NodeProperty<Node>
{
    /**
     * The home folder.
     */
    public String home;

    /**
     * Constructor using fields.
     * 
     * @param home
     *            The home folder
     */
    @DataBoundConstructor
    public WorkspaceHomeProperty(String home)
    {
        // Call super
        super();
        // Store home
        this.home = home;
    }

    /**
     * Get the home folder.
     * 
     * @return The home folder
     */
    public String getHome()
    {
        return home;
    }

    /**
     * Get the home directory for the given node.
     * 
     * @param node
     *            The node
     * @return The home directory
     */
    public static FilePath get(Node node)
    {
        // Get the potential properties
        WorkspaceHomeProperty[] properties = new WorkspaceHomeProperty[] {
                node.getNodeProperties().get(WorkspaceHomeProperty.class),
                Jenkins.getInstance().getGlobalNodeProperties().get(WorkspaceHomeProperty.class) };
        // Go threw the properties
        for (WorkspaceHomeProperty property : properties)
            // Check if exists
            if (property != null)
                // Check if valid
                if (Util.fixEmpty(property.getHome()) != null)
                    // Return the home folder
                    return new FilePath(node.getChannel(), property.getHome());
        // Else relative to root
        return node.getRootPath().child(Workspace.BASENAME);
    }

    @Extension
    public static class WorkspaceHomePropertyDescriptor extends NodePropertyDescriptor
    {

        /*
         * (non-Javadoc)
         * 
         * @see hudson.model.Descriptor#getHelpFile()
         */
        @Override
        public String getHelpFile()
        {
            return Functions.getResourcePath() + "/plugin/shiningpanda/help/workspace/WorkspaceHomeProperty/help.html";
        }

        @Override
        public String getDisplayName()
        {
            return Messages.WorkspaceHomeProperty_DisplayName();
        }

    }
}
