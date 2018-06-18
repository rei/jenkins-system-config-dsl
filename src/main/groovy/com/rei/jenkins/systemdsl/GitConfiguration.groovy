package com.rei.jenkins.systemdsl

import hudson.plugins.git.GitSCM

import com.rei.jenkins.systemdsl.doc.ExampleArgs
import com.rei.jenkins.systemdsl.doc.JenkinsHelpTexts

class GitConfiguration extends DslSection {

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

    void save() {
        jenkins.getDescriptorByType(GitSCM.DescriptorImpl.class).save()
    }
}
