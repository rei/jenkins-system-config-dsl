package com.rei.jenkins.systemdsl

import hudson.model.Node

import com.rei.jenkins.systemdsl.doc.ValidValues
import com.rei.jenkins.systemdsl.doc.ExampleArgs

class MasterNodeConfiguration extends DslSection {
    /**
     * the number of executors on the Jenkins master node
     * @param num
     */
    void numExecutors(int num) {
        logger.info("setting master executors to $num")
        jenkins.setNumExecutors(num)
    }

    /**
     * labels to apply to the Jenkins master node
     * @param labels
     */
    @ExampleArgs("['linux', 'build']")
    void labels(List<String> labels) {
        logger.info("setting master node labels to $labels")
        jenkins.setLabelString(labels.join(' '))
    }

    /**
     * how to utilize this node
     * @param mode
     */
    void mode(Node.Mode mode) {
        jenkins.setMode(mode)
    }

    /**
     * how to utilize this node
     * @param mode
     */
    void mode(@ValidValues(enumConstantsOf=Node.Mode) String mode) {
        jenkins.setMode(Node.Mode.valueOf(mode))
    }
}
