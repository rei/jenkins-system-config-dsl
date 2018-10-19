package com.rei.jenkins.systemdsl

import java.lang.reflect.Array

import hudson.model.JDK
import hudson.plugins.gradle.GradleInstallation
import hudson.plugins.gradle.GradleInstaller
import hudson.plugins.groovy.GroovyInstallation
import hudson.plugins.groovy.GroovyInstaller
import hudson.tasks.Maven
import hudson.tools.BatchCommandInstaller
import hudson.tools.CommandInstaller
import hudson.tools.InstallSourceProperty
import hudson.tools.JDKInstaller
import hudson.tools.ToolInstaller
import hudson.tools.ZipExtractionInstaller
import jenkins.plugins.nodejs.tools.NodeJSInstallation
import jenkins.plugins.nodejs.tools.NodeJSInstaller

import com.rei.jenkins.systemdsl.doc.RequiresPlugin


class ToolsConfiguration extends DslSection {

    private List<JdkInstallConfiguration> jdks = []
    private List<ToolInstallConfiguration> gradles = []
    private List<ToolInstallConfiguration> mavens = []
    private List<ToolInstallConfiguration> groovys = []
    private List<NodeJsInstallConfiguration> nodeJsInstalls = []

    void jdk(@DelegatesTo(JdkInstallConfiguration) Closure config) {
        jdks << configure(new JdkInstallConfiguration(), config)
    }

    @RequiresPlugin('gradle')
    void gradle(@DelegatesTo(ToolInstallConfiguration) Closure config) {
        gradles << configure(new ToolInstallConfiguration(), config)
    }

    void maven(@DelegatesTo(ToolInstallConfiguration) Closure config) {
        mavens << configure(new ToolInstallConfiguration(), config)
    }

    void groovy(@DelegatesTo(ToolInstallConfiguration) Closure config) {
        groovys << configure(new ToolInstallConfiguration(), config)
    }


    @RequiresPlugin('nodejs')
    void nodeJs(@DelegatesTo(NodeJsInstallConfiguration) Closure config) {
        nodeJsInstalls << configure(new NodeJsInstallConfiguration(), config)
    }

    void save() {
        installTools(jdks, JDK) {
            def installer = new JDKInstaller(it.version, true)
            installer.descriptor.doPostCredential(it.oracleUsername, it.oraclePassword)
            return installer
        }

        installTools(gradles, GradleInstallation) { new GradleInstaller(it.version) }
        installTools(mavens, Maven.MavenInstallation) {
            new Maven.MavenInstaller(it.version)
        }

        installTools(nodeJsInstalls, NodeJSInstallation) {
            new NodeJSInstaller(it.version, it.globalNpmPackages.join(' '), it.globalNpmPackageRefreshHours)
        }

        installTools(groovys, GroovyInstallation) { new GroovyInstaller(it.version) }

        super.save()
    }

    private void installTools(List installs, Class installationType, Closure installerFactory) {
        if (!installs.isEmpty()) {
            def installations = installs.collect {
                def installers = []
                if(it.version != null) installers.add(installerFactory(it))
                installers.addAll(it.commands)
                def installerProps = new InstallSourceProperty(installers)
                installationType.newInstance(it.name, "", [installerProps])
            }.toArray(Array.newInstance(installationType, 0))

            def descriptor = jenkins.getDescriptor(installationType.name)
            descriptor.installations = installations
            descriptor.save()
        }
    }

    class ToolInstallConfiguration extends GlobalHelpers {
        protected String name
        protected String version
        protected List<ToolInstaller> commands = []

        void name(String name) {
            this.name = name
        }

        void version(String version) {
            this.version = version
        }

        void downloadArchive(String url, String label, String subDir = '') {
            commands << new ZipExtractionInstaller(label, url, subDir)
        }

        void shellCommand(String command, String label, String homeDir = '') {
            commands << new CommandInstaller(label, command, homeDir)
        }

        void batchCommand(String command, String label, String homeDir = '') {
            commands << new BatchCommandInstaller(label, command, homeDir)
        }
    }

    class JdkInstallConfiguration extends ToolInstallConfiguration {
        protected String oracleUsername
        protected String oraclePassword

        void oracleUsername(String username) {
            this.oracleUsername = username
        }

        void oraclePassword(String oraclePassword) {
            this.oraclePassword = oraclePassword
        }
    }

    class NodeJsInstallConfiguration extends ToolInstallConfiguration {
        protected List<String> globalNpmPackages = []
        protected long globalNpmPackageRefreshHours = 72

        void globalNpmPackages(List<String> globalNpmPackages) {
            this.globalNpmPackages = globalNpmPackages
        }

        void globalNpmPackageRefreshHours(long globalNpmPackageRefreshHours) {
            this.globalNpmPackageRefreshHours = globalNpmPackageRefreshHours
        }
    }
}
