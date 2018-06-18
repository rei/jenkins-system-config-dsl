package com.rei.jenkins.systemdsl

import hudson.model.BuildableItem
import hudson.model.Cause
import javaposse.jobdsl.dsl.DslScriptLoader
import javaposse.jobdsl.dsl.GeneratedItems
import javaposse.jobdsl.plugin.JenkinsJobManagement

class SeedJobsConfiguration extends DslSection {
    private List<String> dslScripts = []
    private Map<String, String> configXmls = [:]
    private boolean buildAfterCreation = true

    void jobDsl(String script) {
        dslScripts << script
    }

    void configXml(String jobName, String xml) {
        configXmls[jobName] = xml
    }

    /**
     * builds any generated jobs after creation (default: true)
     */
    void buildAfterCreation() {
        buildAfterCreation(true)
    }

    /**
     * whether or not to build any generated jobs after creation
     */
    void buildAfterCreation(boolean buildAfterCreation) {
        this.buildAfterCreation = buildAfterCreation
    }

    void save() {
        super.save()

        dslScripts.each { script ->
            JenkinsJobManagement jm = new JenkinsJobManagement(System.out, System.getenv(), jenkins.root)

            DslScriptLoader scriptLoader = new DslScriptLoader(jm)
            GeneratedItems items = scriptLoader.runScript(script)
            if (buildAfterCreation) {
                items.jobs.each {
                    jenkins.getItemByFullName(it.jobName, BuildableItem.class)?.scheduleBuild(new SystemDslCause())
                }
            }
        }

        configXmls.each { jobName, xml ->
            BuildableItem job = jenkins.createProjectFromXML(jobName, new ByteArrayInputStream(xml.bytes))

            if (buildAfterCreation) {
                job.scheduleBuild(new SystemDslCause())
            }
        }
    }

    private static class SystemDslCause extends Cause {
        @Override
        String getShortDescription() {
            return "Started by a System DSL script";
        }
    }
}
