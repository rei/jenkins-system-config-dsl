package com.rei.jenkins.systemdsl.doc

import java.lang.annotation.Documented
import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * used to show example arguments in the api documentation
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@interface ExampleArgs {
    /**
     * Example arguments for the given method
     */
    String[] value()
}