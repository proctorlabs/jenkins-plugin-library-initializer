package org.jenkinsci.plugins.initialized.pipeline;

import hudson.Extension;
import hudson.model.TaskListener;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.SCMSourceCriteria;
import jenkins.scm.api.SCMProbeStat;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.workflow.flow.FlowDefinition;
import org.jenkinsci.plugins.workflow.multibranch.AbstractWorkflowBranchProjectFactory;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.Nonnull;
import java.io.IOException;

public class ScriptInitializedProjectFactory extends AbstractWorkflowBranchProjectFactory {
    private String runScript = "defaultPipeline()";
    private boolean useSandbox = true;
    private String recognizer = "config.yml";

    @DataBoundConstructor
    public ScriptInitializedProjectFactory() {
    }

    @DataBoundSetter
    public void setRunScript(String runScript) {
        this.runScript = runScript;
    }

    public String getRecognizer() {
        return this.recognizer;
    }

    @DataBoundSetter
    public void setRecognizer(String recognizer) {
        this.recognizer = recognizer;
    }

    public String getRunScript() {
        return this.runScript;
    }

    @DataBoundSetter
    public void setUseSandbox(boolean useSandbox) {
        this.useSandbox = useSandbox;
    }

    public boolean getUseSandbox() {
        return this.useSandbox;
    }

    @Override
    protected FlowDefinition createDefinition() {
        return new ScriptInitializedProjectBindings(runScript, useSandbox);
    }

    @Override
    protected SCMSourceCriteria getSCMSourceCriteria(SCMSource source) {
        return new SCMSourceCriteria() {
            private static final long serialVersionUID = -7624621144836785677L;

            @Override
            public boolean isHead(Probe probe, TaskListener listener) throws IOException {
                String localFile = recognizer;

                if (StringUtils.isEmpty(localFile)) {
                    return true;
                }

                SCMProbeStat stat = probe.stat(localFile);
                switch (stat.getType()) {
                case NONEXISTENT:
                    if (stat.getAlternativePath() != null) {
                        listener.getLogger().format("      ‘%s’ not found (but found ‘%s’, search is case sensitive)%n",
                                localFile, stat.getAlternativePath());
                    } else {
                        listener.getLogger().format("      ‘%s’ not found%n", localFile);
                    }
                    return false;
                case DIRECTORY:
                    listener.getLogger().format("      ‘%s’ found but is a directory not a file%n", localFile);
                    return false;
                default:
                    listener.getLogger().format("      ‘%s’ found%n", localFile);
                    return true;
                }
            }

            @Override
            public int hashCode() {
                return getClass().hashCode();
            }

            @Override
            public boolean equals(Object obj) {
                return getClass().isInstance(obj);
            }
        };
    }

    @Extension
    public static class DescriptorImpl extends AbstractWorkflowBranchProjectFactoryDescriptor {
        @Nonnull
        @Override
        public String getDisplayName() {
            return "by Library";
        }
    }
}
