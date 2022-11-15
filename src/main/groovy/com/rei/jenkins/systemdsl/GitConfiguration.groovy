package com.rei.jenkins.systemdsl

import static com.rei.jenkins.systemdsl.GitConfiguration.GitHostKeyVerificationStrategy.ACCEPT_FIRST
import static com.rei.jenkins.systemdsl.GitConfiguration.GitHostKeyVerificationStrategy.KNOWN_HOSTS_FILE
import static com.rei.jenkins.systemdsl.GitConfiguration.GitHostKeyVerificationStrategy.NONE

import hudson.plugins.git.GitSCM
import hudson.plugins.git.GitStatus

import org.jenkinsci.plugins.gitclient.GitHostKeyVerificationConfiguration
import org.jenkinsci.plugins.gitclient.verifier.AcceptFirstConnectionStrategy
import org.jenkinsci.plugins.gitclient.verifier.KnownHostsFileVerificationStrategy
import org.jenkinsci.plugins.gitclient.verifier.NoHostKeyVerificationStrategy

import com.rei.jenkins.systemdsl.doc.ExampleArgs
import com.rei.jenkins.systemdsl.doc.JenkinsHelpTexts
import com.rei.jenkins.systemdsl.doc.ValidValues

class GitConfiguration extends DslSection {

    private GitHostKeyVerificationStrategy hostKeyVerificationStrategy
    private String notifyCommitAccessControlMode
    /**
     * Git author info
     */
    @JenkinsHelpTexts(['hudson/plugins/git/GitSCM/help-globalConfigName.html', 'hudson/plugins/git/GitSCM/help-globalConfigEmail.html'])
    @ExampleArgs(['\'John Doe\'', '\'jdoe@example.com\''])
    void author(String name, String email) {
        logger.info("setting git author info to $name - $email")
        def gitScm = jenkins.getDescriptorByType(GitSCM.DescriptorImpl.class)
        gitScm.setGlobalConfigName(name)
        gitScm.setGlobalConfigEmail(email)
    }

    @JenkinsHelpTexts(['org/jenkinsci/plugins/gitclient/GitHostKeyVerificationConfiguration/help-sshHostKeyVerificationStrategy.html'])
    void hostKeyVerificationStrategy(GitHostKeyVerificationStrategy strategy) {
        this.hostKeyVerificationStrategy = strategy
    }

    @JenkinsHelpTexts(['org/jenkinsci/plugins/gitclient/GitHostKeyVerificationConfiguration/help-sshHostKeyVerificationStrategy.html'])
    void hostKeyVerificationStrategy(@ValidValues(enumConstantsOf=GitHostKeyVerificationStrategy) String strategy) {
        this.hostKeyVerificationStrategy = GitHostKeyVerificationStrategy.valueOf(strategy)
    }

    void notifyCommitAccessControlMode(NotifyCommitAccessControlMode mode) {
        if (mode != NotifyCommitAccessControlMode.ENABLED) {
            this.notifyCommitAccessControlMode = mode.name().toLowerCase().replace('_', '-')
        }
    }

    void notifyCommitAccessControlMode(String mode) {
        this.notifyCommitAccessControlMode = mode
    }

    void save() {
        if (hostKeyVerificationStrategy != null) {
            def config = GitHostKeyVerificationConfiguration.get()
            switch (hostKeyVerificationStrategy) {
                case NONE: config.setSshHostKeyVerificationStrategy(new NoHostKeyVerificationStrategy()); break;
                case ACCEPT_FIRST: config.setSshHostKeyVerificationStrategy(new AcceptFirstConnectionStrategy()); break;
                case KNOWN_HOSTS_FILE: config.setSshHostKeyVerificationStrategy(new KnownHostsFileVerificationStrategy()); break;
            }
        }

        if (notifyCommitAccessControlMode != null) {
            GitStatus.NOTIFY_COMMIT_ACCESS_CONTROL = notifyCommitAccessControlMode
            System.properties[GitStatus.class.name + ".NOTIFY_COMMIT_ACCESS_CONTROL"] = notifyCommitAccessControlMode
        }

        jenkins.getDescriptorByType(GitSCM.DescriptorImpl.class).save()
    }

    static enum GitHostKeyVerificationStrategy {
        NONE, ACCEPT_FIRST, KNOWN_HOSTS_FILE
    }

    static enum NotifyCommitAccessControlMode {
        ENABLED, DISABLED, DISABLED_FOR_POLLING;
    }
}
