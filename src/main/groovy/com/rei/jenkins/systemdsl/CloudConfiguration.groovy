package com.rei.jenkins.systemdsl

import java.util.concurrent.TimeUnit

import hudson.model.Node
import hudson.plugins.ec2.AMITypeData
import hudson.plugins.ec2.AmazonEC2Cloud
import hudson.plugins.ec2.ConnectionStrategy
import hudson.plugins.ec2.EC2Tag
import hudson.plugins.ec2.EbsEncryptRootVolume
import hudson.plugins.ec2.HostKeyVerificationStrategyEnum
import hudson.plugins.ec2.SlaveTemplate
import hudson.plugins.ec2.SpotConfiguration
import hudson.plugins.ec2.Tenancy
import hudson.plugins.ec2.UnixData
import hudson.plugins.ec2.WindowsData
import jenkins.model.Jenkins

import com.amazonaws.services.ec2.model.InstanceType
import com.cloudbees.jenkins.plugins.sshcredentials.SSHUserPrivateKey
import com.cloudbees.jenkins.plugins.sshcredentials.impl.BasicSSHUserPrivateKey
import com.cloudbees.plugins.credentials.Credentials
import com.cloudbees.plugins.credentials.CredentialsMatchers
import com.cloudbees.plugins.credentials.CredentialsProvider
import com.cloudbees.plugins.credentials.CredentialsScope
import com.cloudbees.plugins.credentials.SystemCredentialsProvider
import com.cloudbees.plugins.credentials.domains.Domain

import com.rei.jenkins.systemdsl.aws.AmiQuery
import com.rei.jenkins.systemdsl.aws.AmiQueryService
import com.rei.jenkins.systemdsl.doc.ValidValues

class CloudConfiguration extends DslSection {
    private static AmiQueryService amiQueryService = new AmiQueryService()

    private def clouds = []

    void ec2(String name, @DelegatesTo(EC2Configuration) Closure config) {
        def ec2Configuration = new EC2Configuration(name: name)
        config.delegate = ec2Configuration
        config.resolveStrategy = Closure.DELEGATE_FIRST
        config.call()
        clouds += ec2Configuration.getCloud()
    }

    void save() {
        jenkins.clouds.clear()
        jenkins.clouds.addAll(clouds)
        super.save()
    }

    class EC2Configuration extends GlobalHelpers {
        private String name
        private boolean useInstanceProfileForCredentials
        private String credentialsId
        private String region
        private String privateKey
        private String privateKeyCredentialId = 'ec2'
        private String sshUserName = 'ec2-user'
        private int instanceCap
        private List<SlaveTemplate> slaveTemplates = []
        private String roleArn
        private String roleSessionName

        void region(String region) {
            this.region = region
        }

        void privateKey(String pk) {
            privateKey(pk, 'ec2')
        }

        void privateKey(String privateKey, String credentialId) {
            this.privateKey = privateKey
            this.privateKeyCredentialId = credentialId
        }

        void sshUserName(String userName) {
            this.sshUserName = userName
        }

        void instanceCap(int cap) {
            this.instanceCap = cap
        }

        void roleArn(String roleArn) {
            this.roleArn = roleArn
        }

        void roleSessionName(String roleSessionName) {
            this.roleSessionName = roleSessionName
        }

        void credentials(@DelegatesTo(AwsCredentialsConfiguration) Closure config) {
            config.delegate = new AwsCredentialsConfiguration()
            config.resolveStrategy = Closure.DELEGATE_FIRST
            config.call()
        }

        class AwsCredentialsConfiguration extends GlobalHelpers {
            void instanceProfile() {
                useInstanceProfileForCredentials = true
            }

            void credentialsId(String credentialsId) {
                EC2Configuration.this.credentialsId = credentialsId
            }
        }

        void ami(@DelegatesTo(AmiConfiguration) Closure config) {
            config.delegate = new AmiConfiguration()
            config.call()
            slaveTemplates += config.delegate.template
        }

        class AmiConfiguration extends GlobalHelpers {
            private String ami
            private String description
            private String zone
            private List<String> securityGroups
            private String remoteAdmin
            private String remoteFS
            private InstanceType type
            private boolean ebsOptimized
            private List<String> labels
            private Node.Mode mode
            private String initScript
            private String tmpDir
            private String userData = ""
            private int numExecutors
            private String jvmopts
            private String subnetId
            private int idleTerminationMinutes
            private String iamInstanceProfile
            private boolean deleteRootOnTermination = true
            private boolean useEphemeralDevices
            private String blockDeviceMapping
            private int instanceCap
            private boolean stopOnTerminate
            private ConnectionStrategy connectionStrategy
            private Tenancy tenancy
            private int launchTimeout
            private boolean connectBySSHProcess
            private HostKeyVerificationStrategyEnum hostKeyVerificationStrategy = HostKeyVerificationStrategyEnum.OFF
            private int minInstances = 0;
            private int minSpareInstances = 0;
            private boolean t2Unlimited = false
            private boolean monitoring = false

            private List<EC2Tag> tags = []

            private AMITypeData amiType

            /**
             * the ami id to launch
             * @param ami
             */
            void amiId(String ami) { this.ami = ami }

            void amiLookup(@DelegatesTo(AmiLookupConfig) Closure config) {
                def lookupConfig = new AmiLookupConfig()
                config.delegate = lookupConfig
                config.call()

                this.ami = amiQueryService.lookupAmi(lookupConfig.query)
                if (lookupConfig.interval > 0) {
                    amiQueryService.scheduleAmiLookup(lookupConfig.query, lookupConfig.interval, lookupConfig.unit) { oldAmi, newAmi ->
                        AmazonEC2Cloud cloud = jenkins.clouds.getByName(name)
                        def template = cloud.templates.find { it.ami == oldAmi }
                        template.ami = newAmi
                        jenkins.save()
                    }
                }
            }


            void description(String description) { this.description = description }
            void availabilityZone(String zone) { this.zone = zone }
            void securityGroups(List<String> securityGroups) { this.securityGroups = securityGroups }
            void remoteFSRoot(String remoteFS) { this.remoteFS = remoteFS }
            void remoteUser(String remoteAdmin) { this.remoteAdmin = remoteAdmin }

            void instanceType(InstanceType type) { this.type = type }
            void instanceType(String type) {
                this.type = InstanceType.fromValue(type)
            }

            void ebsOptimized() { this.ebsOptimized = true }
            void labels(List<String> labels) { this.labels = labels }

            void mode(Node.Mode mode) { this.mode = mode }
            void mode(@ValidValues(enumConstantsOf = Node.Mode) String mode) {
                this.mode = Node.Mode.valueOf(mode)
            }

            void initScript(String initScript) { this.initScript = initScript }
            void tmpDir(String tmpDir) { this.tmpDir = tmpDir }
            void userData(String userData) { this.userData = userData }
            void numExecutors(int numExecutors) { this.numExecutors = numExecutors }
            void jvmopts(String jvmopts) { this.jvmopts = jvmopts }
            void subnetId(String subnetId) { this.subnetId = subnetId }
            void idleTerminationMinutes(int idleTerminationMinutes) { this.idleTerminationMinutes = idleTerminationMinutes }
            void minInstances(int minInstances) { this.minInstances = minInstances }
            void minSpareInstances(int minSpareInstances) { this.minSpareInstances = minSpareInstances }
            void iamInstanceProfile(String iamInstanceProfile) { this.iamInstanceProfile = iamInstanceProfile }
            void deleteRootOnTermination() { this.deleteRootOnTermination = true }
            void useEphemeralDevices() { this.useEphemeralDevices = true }
            void blockDeviceMapping(String blockDeviceMapping) { this.blockDeviceMapping = blockDeviceMapping }
            void instanceCap(int instanceCap) { this.instanceCap = instanceCap }
            void launchTimeout(int timeout) { this.launchTimeout = timeout }
            void stopOnTerminate() { this.stopOnTerminate = true }

            void enableT2Unlimited() {
                this.t2Unlimited = true
            }
            void enableMonitoring() {
                this.monitoring = true
            }

            void connectBySSHProcess() { this.connectBySSHProcess = true }
            void connectionStrategy(ConnectionStrategy strategy) { this.connectionStrategy = strategy }

            void tenancy(Tenancy tenancy) { this.tenancy = tenancy }

            void hostKeyVerificationStrategy(String strategy) {
                this.hostKeyVerificationStrategy = HostKeyVerificationStrategyEnum.values().find { it.equalsDisplayText(strategy) || it.name().equals(strategy)}
            }

            void hostKeyVerificationStrategy(HostKeyVerificationStrategyEnum strategy) { this.hostKeyVerificationStrategy = strategy }

            void tag(String name, String value) {
                tags += new EC2Tag(name, value)
            }

            void windows(@DelegatesTo(WindowsTypeConfig) Closure typeConfig) {
                typeConfig.delegate = new WindowsTypeConfig()
                typeConfig.call()
                amiType = typeConfig.delegate.typeData
            }

            class WindowsTypeConfig extends GlobalHelpers {
                private String password
                private boolean useHTTPS
                private int bootDelay

                void adminPassword(String password) {
                    this.password = password
                }

                void useHttps() {
                    useHTTPS = true
                }

                void bootDelay(int bootDelay) {
                    this.bootDelay = bootDelay
                }

                WindowsData getTypeData() {
                    return new WindowsData(password, useHTTPS, bootDelay as String)
                }
            }

            void unix(@DelegatesTo(UnixTypeConfig) Closure typeConfig) {
                typeConfig.delegate = new UnixTypeConfig()
                typeConfig.call()
                amiType = typeConfig.delegate.typeData
            }

            class UnixTypeConfig extends GlobalHelpers {
                private String rootCommandPrefix
                private String slaveCommandPrefix
                private String slaveCommandSuffix
                private int sshPort
                private String bootDelay

                void rootCommandPrefix(String rootCommandPrefix) { this.rootCommandPrefix = rootCommandPrefix }
                void slaveCommandPrefix(String slaveCommandPrefix) { this.slaveCommandPrefix = slaveCommandPrefix }
                void slaveCommandSuffix(String slaveCommandSuffix)  {this.slaveCommandSuffix = slaveCommandSuffix}
                void sshPort(int sshPort) { this.sshPort = sshPort }
                void bootDelay(Sttring bootDelay) { this.bootDelay = bootDelay }

                UnixData getTypeData() {
                    return new UnixData(rootCommandPrefix, slaveCommandPrefix, slaveCommandSuffix, sshPort as String, bootDelay)
                }
            }

            SlaveTemplate getTemplate() {
                return new SlaveTemplate(ami, zone, null, securityGroups.join(','), remoteFS, type, ebsOptimized,
                        labels.join(' '), mode, description, initScript, tmpDir, userData, numExecutors as String, remoteAdmin,
                        amiType, jvmopts, stopOnTerminate, subnetId, tags, idleTerminationMinutes as String,  minInstances, minSpareInstances,
                        instanceCap as String, iamInstanceProfile, deleteRootOnTermination, useEphemeralDevices,
                        launchTimeout as String, false, blockDeviceMapping, connectBySSHProcess, monitoring, t2Unlimited,
                        connectionStrategy,
                        -1, null, hostKeyVerificationStrategy, tenancy, null, null, null, null)
            }

        }


        AmazonEC2Cloud getCloud() {
            return new AmazonEC2Cloud(name, useInstanceProfileForCredentials,
                                      credentialsId, region, null /* deprecated private key */,
                                      createSshKeysCredentials(privateKey),
                                      instanceCap as String, slaveTemplates,
                                      roleArn, roleSessionName)
        }

        private String createSshKeysCredentials(String privateKey) {
            def credentialStore = new SystemCredentialsProvider.UserFacingAction().store
            def credential = new BasicSSHUserPrivateKey(CredentialsScope.GLOBAL, privateKeyCredentialId, 'ec2-user',
                    new BasicSSHUserPrivateKey.DirectEntryPrivateKeySource(privateKey), '', "EC2 Private Key for ${name}")


            def existing = credentialStore.getCredentials(Domain.global()).find { it.descriptor.id == privateKeyCredentialId }
            if (existing != null) {
                credentialStore.updateCredentials(Domain.global(), existing, credential)
            } else {
                credentialStore.addCredentials(Domain.global(), credential)
            }
            return credential.id
        }
    }

    static class AmiLookupConfig extends GlobalHelpers  {
        private AmiQuery query = new AmiQuery()
        long interval = -1
        TimeUnit unit

        /**
         *  (Optional) Limit search to users with explicit launch permission on the image.
         *  Valid items are the numeric account ID or self.
         * @param users
         */
        void executableUsers(List<String> users) {
            query.executableUsers = users
        }

        /**
         * (Optional) Limit search to specific AMI owners. Valid items are the numeric account ID, amazon, or self.
         * @param users
         */
        void owners(List<String> users) {
            query.owners = users
        }

        /**
         * (Optional) One or more name/value pairs to filter off of.
         * There are several valid keys, for a full reference, check out describe-images in the AWS CLI reference.
         * @param key
         * @param value
         */
        void filter(String key, String value) {
            query.filters[key] = value
        }

        void architecture(String architecture) {
            filter('architecture', architecture)
        }

        void description(String description) {
            filter('description', description)
        }

        void ownerAlias(@ValidValues(values=['amazon', 'aws-marketplace', 'microsoft']) String alias) {
            filter('owner-alias', alias)
        }

        void imageType(@ValidValues(values=['machine', 'kernel', 'ramdisk']) String type) {
            filter('image-type', type)
        }

        void platform(@ValidValues(values=['windows']) String platform) {
            filter('platform', platform)
        }

        void name(String name) {
            filter('name', name)
        }

        void nameRegex(String regex) {
            query.nameRegex = regex
        }

        void tag(String key, String value) {
            filter('tag-key', key)
            filter('tag-value', value)
        }

        void scheduledUpdate(long interval, TimeUnit unit) {
            this.interval = interval
            this.unit = unit
        }

    }
}
