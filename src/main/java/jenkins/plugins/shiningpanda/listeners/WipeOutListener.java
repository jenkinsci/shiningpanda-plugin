package jenkins.plugins.shiningpanda.listeners;

import hudson.Extension;
import hudson.model.WorkspaceListener;
import hudson.model.AbstractProject;
import jenkins.plugins.shiningpanda.workspace.Workspace;

@Extension
public class WipeOutListener extends WorkspaceListener
{

    /*
     * (non-Javadoc)
     * 
     * @see
     * hudson.model.WorkspaceListener#afterDelete(hudson.model.AbstractProject)
     */
    @Override
    public void afterDelete(@SuppressWarnings("rawtypes") AbstractProject project)
    {
        // Delegate
        Workspace.delete(project);
    }
}
