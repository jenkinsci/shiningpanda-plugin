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
