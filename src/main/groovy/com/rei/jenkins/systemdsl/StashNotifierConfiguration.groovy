package com.rei.jenkins.systemdsl

import org.jenkinsci.plugins.stashNotifier.StashNotifier

class StashNotifierConfiguration extends DslSection {
    private String credentialsId
    private String stashRootUrl
    private boolean ignoreUnverifiedSsl
    private boolean includeBuildNumberInKey
    private boolean prependParentProjectKey
    private boolean disableInprogressNotification
    private boolean considerUnstableAsSuccess

    void credentialsId(String credentialsId) { this.credentialsId = credentialsId }
    void stashRootUrl(String stashRootUrl) { this.stashRootUrl = stashRootUrl }
    
    void ignoreUnverifiedSsl() { this.ignoreUnverifiedSsl = true }
    void includeBuildNumberInKey() { this.includeBuildNumberInKey = true }
    void prependParentProjectKey() { this.prependParentProjectKey = true }
    void disableInprogressNotification() { this.disableInprogressNotification = true }
    void considerUnstableAsSuccess() { this.considerUnstableAsSuccess = true }

    void save() {
        def config = jenkins.getDescriptorByType(StashNotifier.DescriptorImpl)
        config.setStashRootUrl(stashRootUrl)
        config.setCredentialsId(credentialsId)
        config.setIgnoreUnverifiedSsl(ignoreUnverifiedSsl)
        config.setIncludeBuildNumberInKey(includeBuildNumberInKey)
        config.setPrependParentProjectKey(prependParentProjectKey)
        config.setDisableInprogressNotification(disableInprogressNotification)
        config.setConsiderUnstableAsSuccess(considerUnstableAsSuccess)
        config.save()
    }
}
