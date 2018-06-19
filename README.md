# Jenkins System Config DSL Plugin

This is a Jenkins plugin that takes advantage of a Jenkins feature where it runs any groovy scripts located
in $JENKINS_HOME/init.groovy.d on startup. These scripts have full access to any classes defined in any installed plugins
which is how the System DSL plugin is hooked in. The plugin follows a similar pattern to the Job DSL Plugin and
exposes a Groovy based DSL to configure the Jenkins System Configuration and all of the plugins.


*Example:*

    com.rei.jenkins.systemdsl.JenkinsSystemConfigDsl.configure  {
      global {
          url("https://jenkins.rei.com/")
          environmentVariables([TZ: 'America/Los_Angeles'])
          quietPeriod(5)
      }
    
      masterNode {
          numExecutors(4)
          mode(Node.Mode.NORMAL)
      }
    
      git {
          author("Jenkins", "jenkins@rei.com")
      }
    
      ...
    }
    
See https://rei.github.io/jenkins-system-config-dsl/ for full reference documentation