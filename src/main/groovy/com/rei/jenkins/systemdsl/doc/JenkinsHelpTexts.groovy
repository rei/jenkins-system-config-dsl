package com.rei.jenkins.systemdsl.doc

import java.lang.annotation.Documented
import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * path to jenkins help text resource on the classpath, used for api docs
 */
@Target([ElementType.METHOD])
@Retention(RetentionPolicy.RUNTIME)
@Documented
@interface JenkinsHelpTexts {
    /**
     * path to jenkins help text resource on the classpath, index should align with parameter order
     *
     * @return path to jenkins help text resource on the classpath
     */
    String[] value()
}