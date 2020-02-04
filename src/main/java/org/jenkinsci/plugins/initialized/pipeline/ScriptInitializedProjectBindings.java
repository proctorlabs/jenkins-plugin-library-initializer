package org.jenkinsci.plugins.initialized.pipeline;

import hudson.model.Action;
import hudson.Extension;
import hudson.model.Queue;
import hudson.model.TaskListener;
import jenkins.model.Jenkins;
import hudson.model.Descriptor;
import hudson.model.DescriptorVisibilityFilter;
import org.jenkinsci.plugins.workflow.flow.FlowDefinitionDescriptor;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.flow.FlowDefinition;
import org.jenkinsci.plugins.workflow.flow.FlowExecution;
import org.jenkinsci.plugins.workflow.flow.FlowExecutionOwner;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.multibranch.*;

import java.util.List;

class ScriptInitializedProjectBindings extends FlowDefinition {

    private String runScript;
    private boolean useSandbox;

    public ScriptInitializedProjectBindings(String runScript, boolean useSandbox) {
        this.runScript = runScript;
        this.useSandbox = useSandbox;
    }

    @Override
    public FlowExecution create(FlowExecutionOwner handle, TaskListener listener, List<? extends Action> actions)
            throws Exception {
        Jenkins.get();
        Queue.Executable exec = handle.getExecutable();
        if (!(exec instanceof WorkflowRun)) {
            throw new IllegalStateException("inappropriate context");
        }

        return new CpsFlowDefinition(runScript, useSandbox).create(handle, listener, actions);
    }

    @Extension
    public static class DescriptorImpl extends FlowDefinitionDescriptor {

        @Override
        public String getDisplayName() {
            return "Pipeline from Shared Library Initializer";
        }
    }

    @Extension
    public static class HideMeElsewhere extends DescriptorVisibilityFilter {

        @Override
        public boolean filter(Object context, Descriptor descriptor) {
            if (descriptor instanceof DescriptorImpl) {
                return context instanceof WorkflowJob
                        && ((WorkflowJob) context).getParent() instanceof WorkflowMultiBranchProject;
            }
            return true;
        }

    }
}
