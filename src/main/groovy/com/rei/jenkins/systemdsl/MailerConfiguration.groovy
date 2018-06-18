package com.rei.jenkins.systemdsl

import hudson.tasks.Mailer
import net.sf.json.JSONObject

import com.rei.jenkins.systemdsl.doc.ExampleArgs
import com.rei.jenkins.systemdsl.doc.JenkinsHelpTexts

class MailerConfiguration extends DslSection {
    private JSONObject json = new JSONObject()
    private Mailer.DescriptorImpl mailerInstance

    /**
     * sets the SMTP server to send email with
     * @param server
     */
    @JenkinsHelpTexts('hudson/tasks/Mailer/help-smtpServer.html')
    void smtpServer(String server) {
        logger.info("setting SMTP server to $server")
        mailer.setSmtpHost(server)
    }

    /**
     * @param replyTo
     */
    @ExampleArgs('jdoe@example.com')
    void replyToAddress(String replyTo) {
        mailer.setReplyToAddress(replyTo)
    }

    @ExampleArgs('@example.com')
    @JenkinsHelpTexts('hudson/tasks/Mailer/help-defaultSuffix.html')
    void defaultSuffix(String defaultSuffix) {
        mailer.setDefaultSuffix(defaultSuffix)
    }

    /**
     * authentication information for the SMTP server
     * @param username
     * @param password
     */
    @ExampleArgs(['jdoe', 't0ps3cret'])
    void authentication(String username, String password) {
        mailer.setSmtpAuth(username, password)
    }

    @JenkinsHelpTexts('hudson/tasks/Mailer/help-smtpPort.html')
    void smtpPort(int smtpPort) {
        mailer.setSmtpPort(smtpPort as String)
    }

    @JenkinsHelpTexts('hudson/tasks/Mailer/help-useSsl.html')
    void useSsl() {
        useSsl(true)
    }

    @JenkinsHelpTexts('hudson/tasks/Mailer/help-useSsl.html')
    void useSsl(boolean useSsl) {
        mailer.setUseSsl(useSsl)
    }

    /**
     * The charset to use for the text and subject.
     * @param charset
     */
    void charset(String charset) {
        mailer.setCharset(charset)
    }

    void save() {
        mailer.save()
    }

    private Mailer.DescriptorImpl getMailer() {
        if (mailerInstance == null) {
            mailerInstance = jenkins.getDescriptorByType(Mailer.DescriptorImpl.class)
        }
        return mailerInstance
    }
}


