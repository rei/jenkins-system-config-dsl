package com.rei.jenkins.systemdsl.doc

import java.lang.annotation.Documented
import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * used to show the values allowed for this parameter
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@interface ValidValues {
    /**
     * use the enum constant names as the allowed values
     */
    Class<? extends Enum> enumConstantsOf() default Enum

    /**
     * for enum values, should they be passed as strings or directly referenced
     */
    boolean enumAsString() default true

    /**
     * value must be one of these specific values
     */
    String[] values() default []
}