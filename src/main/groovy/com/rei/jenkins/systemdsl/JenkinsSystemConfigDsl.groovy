package com.rei.jenkins.systemdsl

import java.util.logging.Level
import java.util.logging.Logger

import jenkins.model.Jenkins

import com.rei.jenkins.systemdsl.doc.RequiresPlugin

class JenkinsSystemConfigDsl extends GlobalHelpers {
    static Throwable error

    static void configure(@DelegatesTo(JenkinsSystemConfigDsl) Closure config) {
        configure(Jenkins.instance, config)
    }

    static void configure(Jenkins jenkins, @DelegatesTo(JenkinsSystemConfigDsl) Closure config) {
        config.delegate = new JenkinsSystemConfigDsl(jenkins:jenkins)
        try {
            config.call()
        } catch (Throwable t) {
            error = t
            logger.log(Level.SEVERE, "error running System Config DSL script!", t)
        }
    }

    static Logger logger = Logger.getLogger("system-dsl")

    private Jenkins jenkins

    void global(@DelegatesTo(GlobalConfiguration) Closure config) {
        configureSection(GlobalConfiguration, config)
    }

    void masterNode(@DelegatesTo(MasterNodeConfiguration) Closure config) {
        configureSection(MasterNodeConfiguration, config)
    }

    @RequiresPlugin('git')
    void git(@DelegatesTo(GitConfiguration) Closure config) {
        configureSection(GitConfiguration, config)
    }

    @RequiresPlugin('mailer')
    void mailer(@DelegatesTo(MailerConfiguration) Closure config) {
        configureSection(MailerConfiguration, config)
    }

    @RequiresPlugin('email-ext')
    void extendedEmail(@DelegatesTo(ExtendedEmailConfiguration) Closure config) {
        configureSection(ExtendedEmailConfiguration, config)
    }

    void security(@DelegatesTo(SecurityConfiguration) Closure config) {
        configureSection(SecurityConfiguration, config)
    }

    void clouds(@DelegatesTo(CloudConfiguration) Closure config) {
        configureSection(CloudConfiguration, config)
    }

    void credentials(@DelegatesTo(CredentialsConfiguration) Closure config) {
        configureSection(CredentialsConfiguration, config)
    }

    void theme(@DelegatesTo(ThemeConfiguration) Closure config) {
        configureSection(ThemeConfiguration, config)
    }

    void rebuild(@DelegatesTo(RebuildConfiguration) Closure config) {
        configureSection(RebuildConfiguration, config)
    }

    void stashNotifier(@DelegatesTo(StashNotifierConfiguration) Closure config) {
        configureSection(StashNotifierConfiguration, config)
    }

    void seedJobs(@DelegatesTo(SeedJobsConfiguration) Closure config) {
        configureSection(SeedJobsConfiguration, config)
    }

    void tools(@DelegatesTo(ToolsConfiguration) Closure config) {
        configureSection(ToolsConfiguration, config)
    }

    private void configureSection(Class<? extends DslSection> section, Closure config) {
        config.delegate = section.newInstance()
        config.resolveStrategy = Closure.DELEGATE_FIRST
        config.logger = logger
        config.delegate.jenkins = jenkins
        config.call()
        config.delegate.save()
    }
}
