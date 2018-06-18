package com.rei.jenkins.systemdsl.aws

import com.amazonaws.services.ec2.AmazonEC2ClientBuilder
import com.amazonaws.services.ec2.model.DescribeInstancesRequest
import com.amazonaws.services.ec2.model.Instance
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.util.EC2MetadataUtils

import com.rei.jenkins.systemdsl.doc.Helper
import com.rei.jenkins.systemdsl.doc.RequiresPlugin

class AwsHelpers {

    @Helper
    @RequiresPlugin('aws-java-sdk')
    String getS3Object(String bucket, String key) {
        return AmazonS3ClientBuilder.defaultClient().getObjectAsString(bucket, key)
    }

    @Helper
    @RequiresPlugin('aws-java-sdk')
    EC2MetadataUtils.InstanceInfo getInstanceMetadataInfo() {
        return EC2MetadataUtils.getInstanceInfo()
    }

    @Helper
    @RequiresPlugin('aws-java-sdk')
    Instance getEc2InstanceInfo() {
        return AmazonEC2ClientBuilder.defaultClient().describeInstances(
                new DescribeInstancesRequest().withInstanceIds(EC2MetadataUtils.getInstanceId())).reservations[0].instances[0]
    }
}
