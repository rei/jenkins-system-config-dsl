package com.rei.jenkins.systemdsl.doc

import java.lang.annotation.Documented
import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * Indicates that a plugin must be installed to use the features provided by the annotated DSL method.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@interface RequiresPlugin {
    /**
     * The Plugin ID or short name of the required plugin.
     *
     * @return the Plugin ID or short name of the required plugin
     */
    String value()
}