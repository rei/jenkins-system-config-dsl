package com.rei.jenkins.systemdsl

import com.rei.jenkins.systemdsl.aws.AwsHelpers
import com.rei.jenkins.systemdsl.aws.MockAwsHelpers
import com.rei.jenkins.systemdsl.doc.RequiresPlugin

class GlobalHelpers {

    @RequiresPlugin('aws-java-sdk')
    AwsHelpers awsHelpers() {
        if (System.getenv("MOCK_AWS")?.equals("true")) {
            return new MockAwsHelpers()
        }
        return new AwsHelpers()
    }

}
