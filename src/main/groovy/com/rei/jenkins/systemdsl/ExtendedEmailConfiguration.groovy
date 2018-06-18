package com.rei.jenkins.systemdsl

import hudson.plugins.emailext.ExtendedEmailPublisherDescriptor
import hudson.plugins.emailext.GroovyScriptPath

import com.rei.jenkins.systemdsl.doc.ExampleArgs
import com.rei.jenkins.systemdsl.doc.JenkinsHelpTexts

class ExtendedEmailConfiguration extends DslSection {
    private ExtendedEmailPublisherDescriptor descriptorInstance

    /**
     * sets the SMTP server to send email with
     * @param server
     */
    @JenkinsHelpTexts('hudson/tasks/Mailer/help-smtpServer.html')
    void smtpServer(String server) {
        logger.info("setting extended email SMTP server to $server")
        descriptor.setSmtpHost(server)
    }

    /**
     * @param replyTo
     */
    @ExampleArgs('jdoe@example.com')
    void replyToAddress(String replyTo) {
        descriptor.setReplyToAddress(replyTo)
    }

    @ExampleArgs('@example.com')
    @JenkinsHelpTexts('hudson/tasks/Mailer/help-defaultSuffix.html')
    void defaultSuffix(String defaultSuffix) {
        descriptor.setDefaultSuffix(defaultSuffix)
    }

    /**
     * authentication information for the SMTP server
     * @param username
     * @param password
     */
    @ExampleArgs(['jdoe', 't0ps3cret'])
    void authentication(String username, String password) {
        descriptor.setSmtpAuth(username, password)
    }

    @JenkinsHelpTexts('hudson/tasks/Mailer/help-smtpPort.html')
    void smtpPort(int smtpPort) {
        descriptor.setSmtpPort(smtpPort as String)
    }

    @JenkinsHelpTexts('hudson/tasks/Mailer/help-useSsl.html')
    void useSsl() {
        useSsl(true)
    }

    @JenkinsHelpTexts('hudson/tasks/Mailer/help-useSsl.html')
    void useSsl(boolean useSsl) {
        descriptor.setUseSsl(useSsl)
    }

    /**
     * The charset to use for the text and subject.
     * @param charset
     */
    void charset(String charset) {
        descriptor.setCharset(charset)
    }

    /**
     * copies the base settings (server connection information, reply to) from the base Jenkins Mailer plugin
     */
    void copySettingsFromMailer() {
        descriptor.upgradeFromMailer()
    }

    /**
     * The default content type of the emails sent after a build.
     * @param defaultContentType
     */
    void defaultContentType(String defaultContentType) { descriptor.defaultContentType = defaultContentType }

    /**
     * Customize the default subject line of the email notifications. The plugin can replace certain tokens in the subject field
     * of the email so that you can include special information from the build.
     * @param defaultSubject
     */
    void defaultSubject(String defaultSubject) { descriptor.defaultSubject = defaultSubject }

    /**
     * Customize the default content of the email notifications. The plugin can replace certain tokens in the content field of
     * the email so that you can include special information from the build.
     * @param defaultBody
     */
    void defaultBody(String defaultBody) { descriptor.defaultBody = defaultBody }

    /**
     * This script will be run prior to sending the email to allow modifying the email before sending. The MimeMessage variable is
     * "msg," the build is available as "build", a logger is available as "logger". The trigger that caused the email is available
     * as "trigger" and all triggered builds are available as a map "triggered."
     *
     * You may also cancel sending the email by setting the boolean variable "cancel" to true.
     *
     * You can set the default pre-send script here and then use ${DEFAULT_PRESEND_SCRIPT} in the project settings to use the
     * script written here.
     *
     * @param defaultPresendScript
     */
    void defaultPresendScript(String defaultPresendScript) { descriptor.defaultPresendScript = defaultPresendScript }

    /**
     * This script will be run after sending the email to allow acting upon the send result. The MimeMessage variable is "msg,"
     * the SMTPTransport "transport," the session properties "props," the build is also available as "build" and a logger is
     * available as "logger." The trigger that caused the email is available as "trigger" and all triggered builds are available
     * as a map "triggered."
     *
     * You can set the default post-send script here and then use ${DEFAULT_POSTSEND_SCRIPT} in the project settings to use the
     * script written here.
     *
     * @param defaultPostsendScript
     */
    void defaultPostsendScript(String defaultPostsendScript) { descriptor.defaultPostsendScript = defaultPostsendScript }

    /**
     * These pathes allow to extend the Groovy classpath when the presend and postsend script are run.
     * The syntax of the path is either a full url or a simple file path (may be relative).
     * These pathes are common to all projects when configured at the global scope.
     *
     * @param paths
     */
    void scriptClasspath(List<String> paths) {
        descriptor.setDefaultClasspath(paths.collect { new GroovyScriptPath(it) })
    }

    /**
     * The maximum size (in MB) for all attachments. If left blank, there is no limit to the size of all attachments.
     * @param maxAttachmentSize
     */
    void maxAttachmentSize(long maxAttachmentSize) { descriptor.maxAttachmentSize = maxAttachmentSize }

    /**
     * Customize the default recipient list of the email notifications. The plugin will use this list if none is overridden
     * in the project configuration. You may use the $DEFAULT_RECIPIENTS token in projects to include this default list, as
     * well as add new addresses at the project level. To CC or BCC someone instead of putting them in the To list,
     * add cc: or bcc: before the email address (e.g., cc:someone@example.com, bcc:bob@example.com).
     *
     * @param recipientList
     */
    @ExampleArgs('jdoe@example.com')
    void defaultRecipients(String recipientList) { descriptor.defaultRecipients = recipientList }

    /**
     * Customize the default recipient list of the email notifications. The plugin will use this list if none is overridden
     * in the project configuration. You may use the $DEFAULT_RECIPIENTS token in projects to include this default list, as
     * well as add new addresses at the project level. To CC or BCC someone instead of putting them in the To list,
     * add cc: or bcc: before the email address (e.g., cc:someone@example.com, bcc:bob@example.com).
     *
     * @param recipientList
     */
    @ExampleArgs('["jdoe@example.com", "jdoe2@example.com"]')
    void defaultRecipients(List<String> recipientList) {
        defaultRecipients(recipientList.join(','))
    }

    /**
     * A comma separated list of e-mail addresses to use in the Reply-To header of the email.
     * This value will be available as $DEFAULT_REPLYTO in the project configuration.
     *
     * @param defaultReplyTo
     */
    @ExampleArgs('jdoe@example.com')
    void defaultReplyTo(String defaultReplyTo) { descriptor.defaultReplyTo = defaultReplyTo }

    /**
     * Allows filtering any email address from the list of recipients that is generated by the plugin.
     * You can specify either the username only or the full email address.
     * @param excludedCommitters
     */
    void excludedCommitters(String excludedCommitters) { descriptor.excludedCommitters = excludedCommitters }

    /**
     *  Set a List-ID header on all emails. This can help with filtering in email clients. It will also stop most
     *  auto-responders from sending replies back to the sender (out-of-office, on-vacation, etc.)
     *
     *  You can use whatever name and id you want, but the format should be similar to one of the following
     *  (including the < and > signs around the actual id):
     *      <ci-notifications.company.org>
     *      Build Notifications <ci-notifications.company.org>
     *      "Build Notifications" <ci-notifications.company.org>
     *
     * For a full description of the List-ID specification see RFC-2919.
     * @param listId
     */
    @ExampleArgs("'Build Notifications <ci-notifications.company.org>'")
    void listId(String listId) { descriptor.listId = listId }

    /**
     * Set a 'Precedence: bulk' header on all emails. This will stop most auto-responders from sending replies back to the
     * sender (out-of-office, on-vacation, etc.)
     */
    void precedenceBulk() { descriptor.precedenceBulk = true }

    /**
     * Set a 'Precedence: bulk' header on all emails. This will stop most auto-responders from sending replies back to the sender
     * (out-of-office, on-vacation, etc.)
     * @param precedenceBulk
     */
    void precedenceBulk(boolean precedenceBulk) { descriptor.precedenceBulk = precedenceBulk }

    /**
     * Email Template Testing link will only show up for users with Administrator privileges.
     */
    void adminRequiredForTemplateTesting() { descriptor.adminRequiredForTemplateTesting = true }

    /**
     * When true Email Template Testing link will only show up for users with Administrator privileges.
     */
    void adminRequiredForTemplateTesting(boolean adminRequiredForTemplateTesting) { descriptor.adminRequiredForTemplateTesting = adminRequiredForTemplateTesting }

    /**
     * Enabling watching allows individual users to add their own triggers to jobs. The list of triggers they can add is
     * restricted by the trigger implementer. For example, most script based triggers are not allowed for users to use as a watch.
     * Users must have READ access to the job in order to watch it.
     *
     * Triggers are not stored in the job itself, but as a property on the user.
     */
    void enableWatching() { descriptor.watchingEnabled = true }

    /**
     * Enabling watching allows individual users to add their own triggers to jobs. The list of triggers they can add is
     * restricted by the trigger implementer. For example, most script based triggers are not allowed for users to use as a watch.
     * Users must have READ access to the job in order to watch it.
     *
     * Triggers are not stored in the job itself, but as a property on the user.
     */
    void enableWatching(boolean enableWatching) { descriptor.watchingEnabled = enableWatching }

    /**
     * Allows emails to be sent to unregistered email addresses.
     * Security risk, see https://issues.jenkins-ci.org/browse/JENKINS-9016 for more information
     */
    void allowUnregistered() { descriptor.allowUnregisteredEnabled = true }

    /**
     * When true allows emails to be sent to unregistered email addresses.
     * Security risk, see https://issues.jenkins-ci.org/browse/JENKINS-9016 for more information
     */
    void allowUnregistered(boolean enableAllowUnregistered) { descriptor.allowUnregisteredEnabled = enableAllowUnregistered }

//    // TODO: triggers
//    private transient List<EmailTriggerDescriptor> defaultTriggers = new ArrayList<>();
//    private List<String> defaultTriggerIds = new ArrayList<>();

    void save() {
        descriptor.save()
    }

    private ExtendedEmailPublisherDescriptor getDescriptor() {
        if (this.descriptorInstance == null) {
            this.descriptorInstance = jenkins.getExtensionList(ExtendedEmailPublisherDescriptor)[0]
        }
        return this.descriptorInstance
    }
}
