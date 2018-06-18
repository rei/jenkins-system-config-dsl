package com.rei.jenkins.systemdsl

import hudson.Functions
import hudson.slaves.ComputerListener
import hudson.slaves.EnvironmentVariablesNodeProperty.Entry
import hudson.slaves.EnvironmentVariablesNodeProperty
import jenkins.install.SetupWizard
import jenkins.model.Jenkins
import jenkins.model.JenkinsLocationConfiguration

import org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
import org.jenkinsci.plugins.envinject.EnvInjectGlobalPasswordEntry
import org.jenkinsci.plugins.envinject.EnvInjectNodeProperty
import org.kohsuke.stapler.StaplerRequest

import com.rei.jenkins.systemdsl.doc.ExampleArgs
import com.rei.jenkins.systemdsl.doc.RequiresPlugin

class GlobalConfiguration extends DslSection {
    private def globalPasswords = [:]

    /**
     * url displayed in warning on Manage Jenkins page pointing at system config in source control for reference
     */
    @ExampleArgs('https://git.company.com/jenkins/config.groovy')
    void configUrl(String url) {
        ManageJenkinsWarning.configUrl = url
    }

    /**
     * system message that will be shown in a banner at the top of jenkins
     * @param msg
     */
    @ExampleArgs("'some system wide banner message'")
    void systemMessage(String msg) {
        logger.info("setting system message to $msg")
        jenkins.setSystemMessage(msg)
    }

    /**
     * the amount of time to wait between scm commits to start a build
     *
     * @param period
     */
    void quietPeriod(int period) {
        logger.info("setting quiet period to $period")
        jenkins.setQuietPeriod(period)
    }

    /**
     * sets the number of times to retry checking out source code
     */
    void scmCheckoutRetryCount(int count) {
        logger.info("setting scm checkout retry count to $count")
        jenkins.setScmCheckoutRetryCount(count)
    }

    /**
     * sets the base url for this Jenkins instance
     */
    @ExampleArgs("'http://localhost:8080/'")
    void url(String url) {
        logger.info("setting url to $url")
        jenkins.getExtensionList(JenkinsLocationConfiguration).get(0).setUrl(url)
    }

    /**
     * The email address of the jenkins admin
     */
    @ExampleArgs("'jdoe@example.com'")
    void adminEmail(String email) {
        logger.info("admin email to $email")
        jenkins.getExtensionList(JenkinsLocationConfiguration).get(0).setAdminAddress(email)
    }

    /**
     * Adds global environment variables that will be available to all jobs
     * @param vars
     */
    @ExampleArgs("[SOME_GLOBAL_VAR: 'some value']")
    void environmentVariables(Map<String, String> vars) {
        def nodeProps = new EnvironmentVariablesNodeProperty(vars.entrySet().collect { new Entry(it.key, it.value) })
        jenkins.getGlobalNodeProperties().replaceBy(Collections.singleton(nodeProps))
    }

    @RequiresPlugin('envinject')
    void passwords(@DelegatesTo(GlobalPasswordsConfig) Closure config) {
        config.resolveStrategy = Closure.DELEGATE_FIRST
        config.delegate = new GlobalPasswordsConfig()
        config.call()
    }

    @IgnoreJRERequirement
    void save() {
        if (!globalPasswords.isEmpty()) {
            StaplerRequest req = [bindParametersToList: { Class type, String prefix ->
                globalPasswords.collect { new EnvInjectGlobalPasswordEntry(it.key, it.value) }
            }] as StaplerRequest

            Functions.getSortedDescriptorsForGlobalConfigUnclassified()
                     .find { it instanceof EnvInjectNodeProperty.EnvInjectNodePropertyDescriptor }
                     .configure(req, null)

            super.save()
            ComputerListener.all().each { it.onConfigurationChange() }
        }
        super.save()
    }

    class GlobalPasswordsConfig {
        /**
         * adds a 'global password' that will be injected as an environment variable in all jobs by the envinject plugin
         * @param name
         * @param value
         */
        @ExampleArgs(["'API_KEY'", "'t0ps3cret'"])
        void password(String name, String value) {
            globalPasswords[name] = value
        }
    }
}
