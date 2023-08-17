package com.rei.jenkins.systemdsl

import static org.junit.Assert.assertTrue

import hudson.model.Node

import org.junit.Rule
import org.junit.Test
import org.jvnet.hudson.test.JenkinsRule

import com.amazonaws.services.ec2.model.InstanceType

class JenkinsSystemConfigDslTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule()

    @Test
    void canConfigureStuff() {
        JenkinsSystemConfigDsl.configure(jenkins.jenkins) {

            global {
                configUrl('https://example.com/init.groovy')

                url('https://192.168.99.100:8080')

                passwords {
                    password('KEY', 'topsecret')
                }
            }

            masterNode {
                numExecutors(5)
                labels(['x', 'y'])
            }

            git {
                author('Joe Blow', 'jblow@example.com')
            }

            mailer {
                smtpPort(22)
                smtpServer('somehost.example.com')
            }

            extendedEmail {
                copySettingsFromMailer()
                defaultSubject('$PROJECT_NAME - $BUILD_STATUS!')
                defaultBody('${JELLY_SCRIPT,template="build-result-console"}')
                defaultContentType('text/html')
            }

            security {
                ldap {
                    server('ldaps://ldaps.example.com:636')
                    rootDN('OU=EX,DC=corp,DC=com')
                    userSearchBase('OU=Accounts')
                    userSearchFilter('sAMAccountName={0}')
                    groupSearchBase('OU=Groups')
                    groupSearchFilter('(&(objectclass=groups)(cn={0}))')
                    groupMembershipStrategy {
                        groupSearch('(memberOf={0})')
                    }
                    managerDN('CN=ldapauth,OU=Service,OU=Accounts,OU=EX,DC=corp,DC=com')
                    managerPasswordSecret('t0ps3cret')
                    displayNameAttributeName('displayname')
                    emailAddressAttributeName('mail')
                }

                authorization {
                    projectMatrixAuthorization {
                        addPermission('ADMINISTER', 'test-admin')
                    }
//                    anyoneCanDoAnything()
                }

                disableJobDslScriptSecurity()
                enableCsrfProtection()
            }

            credentials {
                global {
                    secretText('slack-notifier', 'asjf;lkjdafjdsajflksajdfa', 'Slack notification account token')
                    usernamePassword('stash-notifier', 'jenkins', 'password', 'Stash notifier plugin credentials')

                    ssh('git', 'git', '''-----BEGIN RSA PRIVATE KEY-----
BLAHBLAHBLAHBLAHBLAHBLAHFAKEFAKEFAKEFAKE==
-----END RSA PRIVATE KEY-----
''', 'Example jenkins user')

                    gitlabApiToken('gitlab', 'password', 'Gitlab API token')

                }
            }

            gitlab {
                apiTokenId("gitlab")
                name("Gitlab")
                hostUrl("https://gitlab.com")
            }

            clouds {
                ec2('aws') {
                    credentials {
                        instanceProfile()
                    }

                    region('us-west-2')
                    privateKey('xxxxxxxxxxxxxxxxxxxxxxx')
                    instanceCap(10)

                    ami {
                        amiId('ami-123456abcd')
                        description('builder node')
                        instanceType(InstanceType.M4Xlarge)
                        availabilityZone('us-west-2c')
                        securityGroups(['build-nodes'])
                        remoteFSRoot('/home/ec2-user')
                        remoteUser('ec2-user')
                        unix {
                            sshPort(22)
                        }
                        labels(['build', 'test'])
                        mode(Node.Mode.NORMAL)
                        idleTerminationMinutes(10)
                        stopOnTerminate()
                        initScript('blah')
                        numExecutors(6)
                        subnetId('subnet-12345676')

                        tag('app:identifier', 'jenkins-alpine')
                        tag('Name', 'Jenkins')

                        instanceCap(10)
                        iamInstanceProfile('arn:aws:iam::1234567890:instance-profile/jenkins-builder-instance-profile')
                        blockDeviceMapping('/dev/xvda=:250:true:gp2')

                    }
                }
            }

            theme {
                cssUrl('https://example.com/global.css')
            }

            rebuild {
                rememberPasswords()
            }

            stashNotifier {
                stashRootUrl('https://git.rei.com')
                credentialsId('stash-notifier')
                ignoreUnverifiedSsl()
            }

            seedJobs {
                configXml('xml-job', '''
<!-- 1. xml-job -->
<project>
    <description>a test xml job</description>
    <scm class='hudson.scm.NullSCM'></scm>
    <canRoam>true</canRoam>
    <disabled>false</disabled>
    <builders>
        <hudson.tasks.Shell>
            <command>echo hello</command>
        </hudson.tasks.Shell>
    </builders>
</project>

'''.trim())
                jobDsl('''
3.times { n ->
    job("dsl-job$n") {
        description 'a test dsl job'    
        steps {
            shell "echo hello from jenkins job dsl $n"
        }
    }
}
'''.trim())
            }

            tools {
                jdk {
                    name('JDK8')
                    version('jdk-8u152-oth-JPR')
                    shellCommand('''
JAVA_HOME=~/tools/hudson.model.JDK/JDK8/jre
echo "Shell Stuff"
'''.trim(), '')
                }

                jdk {
                    name('JDK11')
                    downloadArchive(
                            'https://download.java.net/java/GA/jdk11/13/GPL/openjdk-11.0.1_linux-x64_bin.tar.gz',
                            '',
                            'jdk-11.0.1'
                    )
                }

                jdk {
                    name('JDK9')
                    version('jdk-9.0.4-oth-JPR')
                    oracleUsername('jblow')
                    oraclePassword('t0ps3cret')
                }

                gradle {
                    name('2.14.1')
                    version('2.14.1')
                }

                def mavenVersions = ["3.5.0",
                                     "3.3.3",
                                     "3.2.2"]

                for (m in mavenVersions) {
                    maven {
                        name("Maven ${m}")
                        version(m)
                    }
                }

                nodeJs {
                    name('6.x')
                    version('6.11.3')
                }

                groovy {
                    name('2.4.7')
                    version('2.4.7')
                }
            }
        }


        def configFiles = [:]

        jenkins.jenkins.root.listFiles().each {
            if (it.name.endsWith('.xml')) {
                println "-------- $it.name --------"
                println it.text
                println "-" * 40
                configFiles[it.name] = it.text
            } else {
                println it.name
            }
        }

        String configXml = configFiles['config.xml']

        assertTrue(configXml.contains('<numExecutors>5</numExecutors>'))
        assertTrue(configXml.contains('<mode>NORMAL</mode>'))
        assertTrue(configXml.contains('<useSecurity>true</useSecurity>'))
        assertTrue(configXml.contains('hudson.security.ProjectMatrixAuthorizationStrategy') && configXml.contains('hudson.model.Hudson.Administer:test-admin'))
        assertTrue(configXml.contains('hudson.security.LDAPSecurityRealm'))
        assertTrue(configXml.contains('hudson.security.csrf.DefaultCrumbIssuer'))
        assertTrue(configXml.contains('<excludeClientIPFromCrumb>false</excludeClientIPFromCrumb>'))

        assertTrue(configXml.contains('ldaps://ldaps.example.com:636') && configXml.contains('CN=ldapauth,OU=Service,OU=Accounts,OU=EX,DC=corp,DC=com'))

        assertTrue(configXml.contains('<name>JDK8</name>') && configXml.contains('<command>JAVA_HOME=~/tools/hudson.model.JDK/JDK8/jre'))
        assertTrue(configXml.contains('<name>JDK9</name>'))
        assertTrue(configXml.contains('EC2Cloud>'))
        assertTrue(configXml.contains('<useInstanceProfileForCredentials>true'))
        assertTrue(configXml.contains('<ami>ami-123456abcd</ami>'))
        assertTrue(configXml.contains('<name>app:identifier</name>'))

        assertTrue(configFiles['credentials.xml'].contains('<id>slack-notifier</id>'))
        assertTrue(configFiles['credentials.xml'].contains('<id>stash-notifier</id>'))
        assertTrue(configFiles['credentials.xml'].contains('<id>gitlab</id>'))

        assertTrue(configFiles['envInject.xml'].contains('<name>KEY</name>'))

        assertTrue(configFiles['hudson.plugins.emailext.ExtendedEmailPublisher.xml'].contains('<smtpHost>somehost.example.com</smtpHost>'))

        assertTrue(configFiles['hudson.plugins.git.GitSCM.xml'].contains('<globalConfigName>Joe Blow</globalConfigName>'))

        assertTrue(configFiles['hudson.plugins.gradle.Gradle.xml'].contains('<name>2.14.1</name>'))
        assertTrue(configFiles['hudson.plugins.groovy.Groovy.xml'].contains('<name>2.4.7</name>'))

        assertTrue(configFiles['hudson.tasks.Mailer.xml'].contains('<smtpHost>somehost.example.com</smtpHost>'))
        assertTrue(configFiles['hudson.tasks.Maven.xml'].contains('<name>Maven 3.3.3</name>'))
        assertTrue(configFiles['hudson.tasks.Maven.xml'].contains('<name>Maven 3.5.0</name>'))
        assertTrue(configFiles['hudson.tasks.Maven.xml'].contains('<name>Maven 3.2.2</name>'))

        assertTrue(configFiles['hudson.tools.JDKInstaller.xml'].contains('<username>jblow</username>'))
        assertTrue(configFiles['javaposse.jobdsl.plugin.GlobalJobDslSecurityConfiguration.xml'].contains('<useScriptSecurity>false</useScriptSecurity>'))

        assertTrue(configFiles['jenkins.model.JenkinsLocationConfiguration.xml'].contains('<jenkinsUrl>https://192.168.99.100:8080/</jenkinsUrl>'))

        assertTrue(configFiles['jenkins.plugins.nodejs.tools.NodeJSInstallation.xml'].contains('<name>6.x</name>'))


        assertTrue(configFiles['org.codefirst.SimpleThemeDecorator.xml'].contains('<url>https://example.com/global.css</url>'))
        assertTrue(configFiles['org.jenkinsci.plugins.stashNotifier.StashNotifier.xml'].contains('<credentialsId>stash-notifier</credentialsId>'))
    }
}
