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
                configUrl('https://git.rei.com/projects/ALP/repos/alpine-jenkins-docker/browse/init.groovy')

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
                author('Joe Blow', 'jblow@rei.com')
            }

            mailer {
                smtpPort(22)
                smtpServer('somehost.rei.com')
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
                    rootDN('OU=REI,DC=corp,DC=com')
                    userSearchBase('OU=Accounts')
                    userSearchFilter('sAMAccountName={0}')
                    groupSearchBase('OU=Groups')
                    groupSearchFilter('(&(objectclass=groups)(cn={0}))')
                    groupMembershipStrategy {
                        groupSearch('(memberOf={0})')
                    }
                    managerDN('CN=ldapauth,OU=Service,OU=Accounts,OU=REI,DC=corp,DC=com')
                    managerPasswordSecret('t0ps3cret')
                    displayNameAttributeName('displayname')
                    emailAddressAttributeName('mail')
                }

                authorization {
                    projectMatrixAuthorization {
                        addPermission('ADMINISTER', 'jskjons')
                    }
//                    anyoneCanDoAnything()
                }

                disableRemoteCli()
                disableJobDslScriptSecurity()
            }

            credentials {
                global {
                    secretText('slack-notifier', 'asjf;lkjdafjdsajflksajdfa', 'Slack notification account token')
                    usernamePassword('stash-notifier', 'jenkins', 'password', 'Stash notifier plugin credentials')

                    ssh('git', 'git', '''-----BEGIN RSA PRIVATE KEY-----
MIIEpAIBAAKCAQEAqEUH/VUPcEakkzFy8VcDyAiVY04qyqZTErQpBzaUPayZACvJ
Di9Cj8pBqR0pStjEB31LNG3yQPfwuP2jnrz+cA/NnpjvGMP7zq0TiKYyK3VD2i/c
DL+sjTl9FZ8jquLAvGgvlwl1N9PEV+VCIXOXUjpFHLvd9R6VAuyKXbxq/ed58Vbf
PpL3w4fqeswt/YvaSR4pY1EQnTfJ+TCl0d0taRAaDxLo2jjtO2EneyFdxpNrHw+T
zNwIS/dzbb1LBWt1N7NGOcVFK0xxpP0h0tS6bEnaLiY4PEDTuwrgUO+ya/6zbpp+
RDxkeMm5ok7WdAIuSAsdw1+RSrbh8tFAuG8lqwIDAQABAoIBAHYrRFkcCyOF+L2F
1Hq36OfpXz/F/TcjZuOTsdxm7+P/+dKs7R0RA2WHXGUvHXh6ke/QhafkLmSBuoOv
W+B2SRjZgEUIkaCKwaN62GT2kfUC/QuU4KvzT3I+FSHCCIJRi5jEcedXtQHnrdMs
JSzoyDPux+pN5KnnOC49f04kq95yNSQcB6H7Nk/QlKMm6qiF3xmBKQw7EVPOvpSK
elfTM+a0SE/oTElZIVct4isQNVmqRxX5EKJtGo59NITN4yhlnVfaPN4CHIujoVqR
XqrkmhC4IxVkS4fISGunvdXdqYX7P4Cd275ckpz5ed/HXdlM+BhMCw3FYBi03fFz
gvP2fYECgYEA0NxDu5UO5LlhoV4huBeFpEeguvisGi4oMJj50M5h+gi6ywJn8cen
RTu0v0I0D/Z7edmibfscXYNciVdXydimxNHk3qNjW3TAMEn9C8wMBidQ8+Sj7c/6
t8nNyRh5yijAEg7SByjsH2CFIbmad44xgJ2Z+XxGsbYAOqWe6zTD6GMCgYEAzj95
sAWF5a3G2yKnRNHdDUsLaXJ/a2HYL2gVpHPxUtN+kHvG+dKBZjpqc+uTR/kfjCtO
DQ8JJqXDnSNr0tbWXhbpp+5RNjfy11NU2U6KvBJSGRLeQ5eMuig854gMicWkrfIb
4DBQqjs4lc+C8i8iDvRrhCl1cTDUyVounb0E/BkCgYA3IV0Tn6Xdw/08Tg+Se0sA
cRPBJrCu/G4JXefbMQ71o+ZCffEYBf5mLPtp3LzHVeWD5WmVpEn5eRos+owmsHRc
0ZabGf+4/VlZpb4QphyqZyhcKIcI1/QzHSafpUFIlncUjSrtTuT850pc+5QFaNgy
PeeNzjsO356x3FPVfAkWRwKBgQCqWk11cSpmSgGs8FN+iWTu3ORBJXxPQpLgnTT7
D3TtN5kbV9FCeXe961QQCS0uTnubOA2QxbGGRXZr7Vza2e5X6s71kOdtRAsFhWPY
1YHL08oRwb7pz1xCSof7qSjKBwB9WDNkGiQWZzHWs35x8TJNbd78W44QfwfSg/Vq
/jwGyQKBgQCpruFb6qg7rLVWcb/27Lkt66zjyuxlvDYWQ9i9qH5PYSneVwobpOiR
1Q/gDEGFKT+qXo7kYL9S9HUxk6IrfKV2TcQ31fxxZB9oAs0zKiD+SVBE8Eh6Hd6Q
c0UPsQSZy49VbS1snbU5tbDaTAZEVjdKn6Hi+ni5WiUdgYvCSKKiUQ==
-----END RSA PRIVATE KEY-----
''', 'Bitbucket jenkins user')


                }
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
                cssUrl('https://s3-us-west-2.amazonaws.com/jenkins-alpine/global.css')
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
if ! keytool -list -alias REIIssuingCA01 -keystore ${JAVA_HOME}/lib/security/cacerts -noprompt -storepass changeit ; then
  curl https://git.rei.com/projects/DKR/repos/jdk8-newrelic/browse/config/REIIssuingCA01.crt?raw -o /tmp/REIIssuingCA01.crt 
  sudo keytool -importcert -file /tmp/REIIssuingCA01.crt -alias REIIssuingCA01 -keystore ${JAVA_HOME}/lib/security/cacerts -noprompt -storepass changeit
fi
if ! keytool -list -alias REIIssuingCA02 -keystore ${JAVA_HOME}/lib/security/cacerts -noprompt -storepass changeit ; then
  curl https://git.rei.com/projects/DKR/repos/jdk8-newrelic/browse/config/REIIssuingCA02.crt?raw -o /tmp/REIIssuingCA02.crt
  sudo keytool -importcert -file /tmp/REIIssuingCA02.crt -alias REIIssuingCA02 -keystore ${JAVA_HOME}/lib/security/cacerts -noprompt -storepass changeit
fi
if ! keytool -list -alias REI_ROOT_CA_sha2 -keystore ${JAVA_HOME}/lib/security/cacerts -noprompt -storepass changeit ; then
  curl https://git.rei.com/projects/DKR/repos/jdk8-newrelic/browse/config/rei_root_b64.cer?raw -o /tmp/rei_root_b64.cer
  sudo keytool -importcert -file /tmp/rei_root_b64.cer -alias REI_ROOT_CA_sha2 -keystore ${JAVA_HOME}/lib/security/cacerts -noprompt -storepass changeit
fi
if ! keytool -list -alias REI_issuing1_sha2 -keystore ${JAVA_HOME}/lib/security/cacerts -noprompt -storepass changeit ; then
  curl https://git.rei.com/projects/DKR/repos/jdk8-newrelic/browse/config/rei_issuing1_b64.cer?raw -o /tmp/rei_issuing1_b64.cer
  sudo keytool -importcert -file /tmp/rei_issuing1_b64.cer -alias REI_issuing1_sha2 -keystore ${JAVA_HOME}/lib/security/cacerts -noprompt -storepass changeit
fi
if ! keytool -list -alias REI_issuing2_sha2 -keystore ${JAVA_HOME}/lib/security/cacerts -noprompt -storepass changeit ; then
  curl https://git.rei.com/projects/DKR/repos/jdk8-newrelic/browse/config/rei_issuing2_b64.cer?raw -o /tmp/rei_issuing2_b64.cer
  sudo keytool -importcert -file /tmp/rei_issuing2_b64.cer -alias REI_issuing2_sha2 -keystore ${JAVA_HOME}/lib/security/cacerts -noprompt -storepass changeit
fi
rm -f /tmp/rei_*
'''.trim(), '')
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

                maven {
                    name('Maven 3.3.3')
                    version('3.3.3')
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
        assertTrue(configXml.contains('hudson.security.ProjectMatrixAuthorizationStrategy') && configXml.contains('hudson.model.Hudson.Administer:jskjons'))
        assertTrue(configXml.contains('hudson.security.LDAPSecurityRealm'))
        assertTrue(configXml.contains('ldaps://ldaps.example.com:636') && configXml.contains('CN=ldapauth,OU=Service,OU=Accounts,OU=REI,DC=corp,DC=com'))

        assertTrue(configXml.contains('<name>JDK8</name>') && configXml.contains('<command>JAVA_HOME=~/tools/hudson.model.JDK/JDK8/jre'))
        assertTrue(configXml.contains('<name>JDK9</name>'))
        assertTrue(configXml.contains('EC2Cloud>'))
        assertTrue(configXml.contains('<useInstanceProfileForCredentials>true'))
        assertTrue(configXml.contains('<ami>ami-123456abcd</ami>'))
        assertTrue(configXml.contains('<name>app:identifier</name>'))

        assertTrue(configFiles['credentials.xml'].contains('<id>slack-notifier</id>'))
        assertTrue(configFiles['credentials.xml'].contains('<id>stash-notifier</id>'))

        assertTrue(configFiles['envInject.xml'].contains('<name>KEY</name>'))

        assertTrue(configFiles['hudson.plugins.emailext.ExtendedEmailPublisher.xml'].contains('<smtpHost>somehost.rei.com</smtpHost>'))

        assertTrue(configFiles['hudson.plugins.git.GitSCM.xml'].contains('<globalConfigName>Joe Blow</globalConfigName>'))

        assertTrue(configFiles['hudson.plugins.gradle.Gradle.xml'].contains('<name>2.14.1</name>'))
        assertTrue(configFiles['hudson.plugins.groovy.Groovy.xml'].contains('<name>2.4.7</name>'))

        assertTrue(configFiles['hudson.tasks.Mailer.xml'].contains('<smtpHost>somehost.rei.com</smtpHost>'))
        assertTrue(configFiles['hudson.tasks.Maven.xml'].contains('<name>Maven 3.3.3</name>'))

        assertTrue(configFiles['hudson.tools.JDKInstaller.xml'].contains('<username>jblow</username>'))
        assertTrue(configFiles['javaposse.jobdsl.plugin.GlobalJobDslSecurityConfiguration.xml'].contains('<useScriptSecurity>false</useScriptSecurity>'))

        assertTrue(configFiles['jenkins.CLI.xml'].contains('<enabled>false</enabled>'))
        assertTrue(configFiles['jenkins.model.JenkinsLocationConfiguration.xml'].contains('<jenkinsUrl>https://192.168.99.100:8080/</jenkinsUrl>'))

        assertTrue(configFiles['jenkins.plugins.nodejs.tools.NodeJSInstallation.xml'].contains('<name>6.x</name>'))

        assertTrue(configFiles['org.codefirst.SimpleThemeDecorator.xml'].contains('<cssUrl>https://s3-us-west-2.amazonaws.com/jenkins-alpine/global.css</cssUrl>'))
        assertTrue(configFiles['org.jenkinsci.plugins.stashNotifier.StashNotifier.xml'].contains('<credentialsId>stash-notifier</credentialsId>'))
    }
}
