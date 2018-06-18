package com.rei.jenkins.systemdsl

import hudson.security.AuthorizationStrategy
import hudson.security.FullControlOnceLoggedInAuthorizationStrategy
import hudson.security.GlobalMatrixAuthorizationStrategy
import hudson.security.Permission
import hudson.security.ProjectMatrixAuthorizationStrategy
import hudson.model.RootAction
import hudson.security.LDAPSecurityRealm
import hudson.util.Secret
import javaposse.jobdsl.plugin.GlobalJobDslSecurityConfiguration
import jenkins.AgentProtocol
import jenkins.model.IdStrategy
import jenkins.model.Jenkins
import jenkins.security.plugins.ldap.FromGroupSearchLDAPGroupMembershipStrategy
import jenkins.security.plugins.ldap.FromUserRecordLDAPGroupMembershipStrategy
import jenkins.security.plugins.ldap.LDAPConfiguration
import jenkins.security.plugins.ldap.LDAPGroupMembershipStrategy

import com.rei.jenkins.systemdsl.doc.RequiresPlugin

class SecurityConfiguration extends DslSection {

    @RequiresPlugin('ldap')
    void ldap(@DelegatesTo(LdapConfiguration) Closure config) {
        def ldapConfig = configure(new LdapConfiguration(), config)
        logger.info("setting security realm to new LDAPSecurityRealm")
        jenkins.setSecurityRealm(ldapConfig.realm)
    }

    /**
     * remove the “Remember me on this computer” checkbox from the login screen.
     */
    void disableRememberMe() {
        logger.info("disabling remember me")
        jenkins.setDisableRememberMe(true)
    }

    /**
     * completely disables the Jenkins CLI
     */
    void disableRemoteCli() {
        // disable remoting cli
        jenkins.getDescriptor("jenkins.CLI").get().setEnabled(false)
        logger.info("disabling remoting cli")

        // disabled CLI access over TCP listener (separate port)
        def p = AgentProtocol.all()
        p.each { x ->
            if (x.name && x.name.contains("CLI")) {
                p.remove(x)
            }
        }

        // disable CLI access over /cli URL
        def removal = { lst ->
            lst.each { x ->
                if (x.getClass().name.contains("CLIAction")) {
                    lst.remove(x)
                }
            }
        }

        logger.info("Removing the Jenkins CLI subsystem")
        removal(jenkins.getExtensionList(RootAction.class))
        removal(jenkins.actions)
    }

    /**
     * disables the script security mechanism in the Job DSL Plugin
     *
     * Not recommended if this Jenkins instance allows untrusted users to create Job DSL jobs
     */
    @RequiresPlugin('job-dsl')
    void disableJobDslScriptSecurity() {
        def descriptor = jenkins.getDescriptorByType(GlobalJobDslSecurityConfiguration)
        descriptor.useScriptSecurity = false
        descriptor.save()
    }

    void authorization(@DelegatesTo(AuthorizationConfiguration) Closure config) {
        configure(new AuthorizationConfiguration(), config)
    }

    class AuthorizationConfiguration extends GlobalHelpers {
        /**
         * completely disables the Jenkins security subsystem
         */
        void anyoneCanDoAnything() {
            jenkins.setAuthorizationStrategy(AuthorizationStrategy.UNSECURED)
            logger.info('setting authorization strategy to unsecured')
        }

        /**
         * allows any users who are authenticated to perform any action in Jenkins
         */
        void loggedInUsersCanDoAnything() {
            jenkins.setAuthorizationStrategy(new FullControlOnceLoggedInAuthorizationStrategy())
            logger.info('setting authorization strategy to "logged in users can do anything"')
        }

        /**
         * an extension to "Matrix-based security" that allows additional ACL matrix to be defined for each project separately
         * This allows you to say things like "Joe can access project A, B, and C but he can't see D."
         * See the help of "Matrix-based security" for the concept of matrix-based security in general.
         * ACLs are additive, so the access rights granted below will be effective for all the projects.

         * @param config
         */
        @RequiresPlugin('matrix-auth')
        void projectMatrixAuthorization(@DelegatesTo(MatrixAuthorizationConfiguration) Closure config) {
            ProjectMatrixAuthorizationStrategy authorizationStrategy = new ProjectMatrixAuthorizationStrategy()
            configure(new MatrixAuthorizationConfiguration(authorizationStrategy:authorizationStrategy), config)
            jenkins.setAuthorizationStrategy(authorizationStrategy)
            logger.info('setting authorization strategy to project-matrix')
        }

        /**
         * In this scheme, you can configure who can do what.
         * a user or a group (often called 'role', depending on the security realm.) includes a special user 'anonymous',
         * which represents unauthenticated users, as well as
         * 'authenticated', which represents all authenticated users (IOW, everyone except anonymous users.)
         * Permissions are additive. That is, if an user X is in group A, B, and C, then the permissions that this user
         * actually has are the union of all permissions given to X, A, B, C, and anonymous.

         * @param config
         */
        @RequiresPlugin('matrix-auth')
        void matrixAuthorization(@DelegatesTo(MatrixAuthorizationConfiguration) Closure config) {
            GlobalMatrixAuthorizationStrategy authorizationStrategy = new GlobalMatrixAuthorizationStrategy()
            configure(new MatrixAuthorizationConfiguration(authorizationStrategy:authorizationStrategy), config)
            jenkins.setAuthorizationStrategy(authorizationStrategy)
            logger.info('setting authorization strategy to matrix')
        }


        class MatrixAuthorizationConfiguration {
            GlobalMatrixAuthorizationStrategy authorizationStrategy

            /**
             * adds a permission for the user
             * special permission aliases (case insensitive):
             *   'ADMINISTER', 'FULL_CONTROL','READ','WRITE','CREATE','UPDATE','DELETE','CONFIGURE
             *
             * special user ids 'anonymous' and 'authenticated' are supported
             *
             * @param permission - fully qualified permission or alias
             * @param user - userid or 'anonymous' or 'authenticated'
             */
            void addPermission(String permission, String user) {
                authorizationStrategy.add(getPermission(permission), user)
            }

            private Permission getPermission(String p) {
                switch (p.toUpperCase()) {
                    case 'ADMINISTER': return Jenkins.ADMINISTER
                    case 'FULL_CONTROL': return Jenkins.ADMINISTER
                    case 'READ': return Permission.READ
                    case 'WRITE': return Permission.WRITE
                    case 'CREATE': return Permission.CREATE
                    case 'UPDATE': return Permission.UPDATE
                    case 'DELETE': return Permission.DELETE
                    case 'CONFIGURE': return Permission.CONFIGURE
                    default: return Permission.fromId(p)
                }
            }
        }
    }

    class LdapConfiguration extends GlobalHelpers {
        private String server

        /**
         * The root DN to connect to. Normally something like "dc=sun,dc=com"
         */
        private String rootDN
        /**
         * Allow the rootDN to be inferred? Default is false.
         * If true, allow rootDN to be blank.
         */
        private boolean allowBlankRootDN = false
        private String userSearchBase
        private String userSearchFilter
        private String groupSearchBase
        private String groupSearchFilter
        private LDAPGroupMembershipStrategy groupMembershipStrategy
        private String managerDN
        private Secret managerPasswordSecret
        private String displayNameAttributeName
        private String emailAddressAttributeName
        private Map<String,String> extraEnvVars

        private int cacheSize
        private int cacheTtl

        private IdStrategy idStrategy = IdStrategy.CASE_INSENSITIVE
        private IdStrategy groupIdStrategy = IdStrategy.CASE_INSENSITIVE

        void server(String server) { this.server = server }
        void rootDN(String rootDN) { this.rootDN = rootDN }
        void allowBlankRootDN(boolean allowBlankRootDN) { this.allowBlankRootDN = allowBlankRootDN }
        void userSearchBase(String userSearchBase) { this.userSearchBase = userSearchBase }
        void userSearchFilter(String userSearchFilter) { this.userSearchFilter = userSearchFilter }
        void groupSearchBase(String groupSearchBase) { this.groupSearchBase = groupSearchBase }
        void groupSearchFilter(String groupSearchFilter) { this.groupSearchFilter = groupSearchFilter }
        void managerDN(String managerDN) { this.managerDN = managerDN }

        void managerPasswordSecret(String managerPasswordSecret) {
            this.managerPasswordSecret = Secret.fromString(managerPasswordSecret)
        }

        void displayNameAttributeName(String displayNameAttributeName) { this.displayNameAttributeName = displayNameAttributeName }
        void emailAddressAttributeName(String emailAddressAttributeName) { this.emailAddressAttributeName = emailAddressAttributeName }

        void groupMembershipStrategy(@DelegatesTo(GroupMembershipStrategyConfig) Closure strategyConfig) {
            strategyConfig.delegate = new GroupMembershipStrategyConfig()
            strategyConfig.call()
        }

        class GroupMembershipStrategyConfig {
            void groupSearch(String filter) {
                groupMembershipStrategy = new FromGroupSearchLDAPGroupMembershipStrategy(filter)
            }

            void userSearch(String attribute) {
                groupMembershipStrategy = new FromUserRecordLDAPGroupMembershipStrategy(attribute)
            }
        }

        LDAPSecurityRealm getRealm() {
            def conf = new LDAPConfiguration(server, rootDN, allowBlankRootDN, managerDN, managerPasswordSecret)
            conf.userSearch = userSearchFilter
            conf.userSearchBase = userSearchBase
            conf.groupSearchBase = groupSearchBase
            conf.groupSearchFilter = groupSearchFilter
            conf.groupMembershipStrategy = groupMembershipStrategy
            conf.displayNameAttributeName = displayNameAttributeName
            conf.mailAddressAttributeName = emailAddressAttributeName
            conf.extraEnvVars = extraEnvVars

            return new LDAPSecurityRealm([conf],
                                         false,
                                         new LDAPSecurityRealm.CacheConfiguration(cacheSize, cacheTtl),
                                         idStrategy,
                                         groupIdStrategy)
        }

    }
}
