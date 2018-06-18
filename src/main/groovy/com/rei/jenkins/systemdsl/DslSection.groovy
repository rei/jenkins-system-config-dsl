package com.rei.jenkins.systemdsl

import java.util.logging.Logger

import jenkins.model.Jenkins

import com.amazonaws.services.s3.AmazonS3ClientBuilder

import com.rei.jenkins.systemdsl.doc.Helper
import com.rei.jenkins.systemdsl.doc.RequiresPlugin

class DslSection extends GlobalHelpers {
    Jenkins jenkins
    Logger logger = Logger.getLogger("system-dsl")


    void save() {
        jenkins.save()
    }

    protected <T> T configure(T configSection, Closure config) {
        config.delegate = configSection
        config.resolveStrategy = Closure.DELEGATE_FIRST
        config.call()
        return configSection
    }
}
