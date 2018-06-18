package com.rei.jenkins.systemdsl.aws

import static org.junit.Assert.assertEquals

import org.junit.Test

import com.amazonaws.services.ec2.AmazonEC2
import com.amazonaws.services.ec2.model.DescribeImagesRequest
import com.amazonaws.services.ec2.model.DescribeImagesResult
import com.amazonaws.services.ec2.model.Image

import com.rei.jenkins.systemdsl.CloudConfiguration

class AmiQueryServiceIT {
    @Test
    void canFindLatestAmi() {
        def service = new AmiQueryService()

        def lookupConfig = new CloudConfiguration.AmiLookupConfig()
        def config = {
            owners(['xxxxxxx'])
            name('docker.*')
            architecture('x86_64')

        }

        config.delegate = lookupConfig
        config.call()

        def ami = service.lookupAmi(config.delegate.query)
        println ami
    }
}
