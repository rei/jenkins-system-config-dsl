package com.rei.jenkins.systemdsl

import java.nio.file.Files

import hudson.util.Secret

import org.apache.commons.fileupload.disk.DiskFileItemFactory
import org.apache.commons.lang.StringUtils
import org.jenkinsci.plugins.plaincredentials.impl.FileCredentialsImpl
import org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl

import com.cloudbees.jenkins.plugins.awscredentials.AWSCredentialsImpl
import com.cloudbees.jenkins.plugins.sshcredentials.impl.BasicSSHUserPrivateKey
import com.cloudbees.plugins.credentials.Credentials
import com.cloudbees.plugins.credentials.CredentialsScope
import com.cloudbees.plugins.credentials.SecretBytes
import com.cloudbees.plugins.credentials.SystemCredentialsProvider
import com.cloudbees.plugins.credentials.domains.Domain
import com.cloudbees.plugins.credentials.impl.CertificateCredentialsImpl
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl
import com.dabsquared.gitlabjenkins.connection.GitLabApiTokenImpl

class CredentialsConfiguration extends DslSection {

    private List<Credentials> credentials = []

    void global(@DelegatesTo(ScopedCredentialsConfiguration) Closure config) {
        config.delegate = new ScopedCredentialsConfiguration(scope: CredentialsScope.GLOBAL)
        config.resolveStrategy = Closure.DELEGATE_FIRST
        config.call()
    }

    void system(@DelegatesTo(ScopedCredentialsConfiguration) Closure config) {
        config.delegate = new ScopedCredentialsConfiguration(scope: CredentialsScope.SYSTEM)
        config.resolveStrategy = Closure.DELEGATE_FIRST
        config.call()
    }

    void save() {
        def provider = jenkins.getExtensionList(SystemCredentialsProvider)[0]
        provider.domainCredentialsMap = [(Domain.global()): credentials]
        provider.save()
        super.save()
    }

    class ScopedCredentialsConfiguration extends GlobalHelpers {
        CredentialsScope scope

        void awsCredentials(String id, String accessKey, String secretKey, String description) {
            credentials += new AWSCredentialsImpl(scope, id, accessKey, secretKey, description)
        }

        void secretText(String id, String secret, String description) {
            credentials += new StringCredentialsImpl(scope, id, description, Secret.fromString(secret))
        }

        void ssh(String id, String username, String passphrase, String pathOnMaster, String description) {
            def privateKey = new BasicSSHUserPrivateKey.FileOnMasterPrivateKeySource(pathOnMaster)
            credentials += new BasicSSHUserPrivateKey(scope, id, username, privateKey, passphrase, description)
        }

        void ssh(String id, String username, String privateKeyText, String description) {
            def privateKey = new BasicSSHUserPrivateKey.DirectEntryPrivateKeySource(privateKeyText)
            credentials += new BasicSSHUserPrivateKey(scope, id, username, privateKey, null, description)
        }

        void certificate(String id, String passphrase, String pathOnMaster, String description) {
            def cert = new CertificateCredentialsImpl.FileOnMasterKeyStoreSource(pathOnMaster)
            credentials += new CertificateCredentialsImpl(scope, id, description, passphrase, cert)
        }

        void secretFile(String id, String pathOnMaster, String description) {
            def factory = new DiskFileItemFactory()
            def fileName = StringUtils.substringAfterLast(pathOnMaster, '/')
            def fileItem = factory.createItem("", "application/octet-stream", false, fileName)
            def out = fileItem.getOutputStream()
            def file = new File(pathOnMaster)
            Files.copy(file.toPath(), out)
            credentials += new FileCredentialsImpl(scope, id, description, fileItem, "", "")
        }

        void secretFile(String id, String filename, String text, String description) {
            credentials += new FileCredentialsImpl(scope, id, description, filename, SecretBytes.fromString(text))
        }

        void usernamePassword(String id, String username, String password, String description) {
            credentials += new UsernamePasswordCredentialsImpl(scope, id, description, username, password)
        }

        void gitlabApiToken(String id, String apiToken, String description) {
            credentials += new GitLabApiTokenImpl(scope, id, description, Secret.fromString(apiToken))
        }

    }
}
