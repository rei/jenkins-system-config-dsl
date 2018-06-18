package com.rei.jenkins.systemdsl.aws

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

import com.amazonaws.services.ec2.model.GroupIdentifier
import com.amazonaws.services.ec2.model.IamInstanceProfile
import com.amazonaws.services.ec2.model.Instance
import com.amazonaws.services.ec2.model.InstanceState
import com.amazonaws.services.ec2.model.InstanceStateName
import com.amazonaws.services.ec2.model.Tag
import com.amazonaws.util.EC2MetadataUtils

class MockAwsHelpers extends AwsHelpers {
    @Override
    String getS3Object(String bucket, String key) {
        if (System.getenv("MOCK_BUCKET_" + bucket)) {
            return new File(new File(System.getenv("MOCK_BUCKET_" + bucket)), key).text
        }

        return ''
    }

    @Override
    EC2MetadataUtils.InstanceInfo getInstanceMetadataInfo() {
        return new EC2MetadataUtils.InstanceInfo(DateTimeFormatter.ISO_DATE_TIME.format(ZonedDateTime.now()),
                't2.large',
                'ami-123abc45',
                'i-0q4bw681dbd83e45d',
                null,
                'x86_64',
                '123456789012',
                null,
                null,
                'us-west-2',
                '2017-09-30',
                'us-west-2a',
                '127.0.0.1',
                null
        )
    }

    @Override
    Instance getEc2InstanceInfo() {
        def info = getInstanceMetadataInfo()
        return new Instance()
                .withArchitecture(info.architecture)
                .withImageId(info.imageId)
                .withEbsOptimized(false)
                .withIamInstanceProfile(new IamInstanceProfile().withId('mock-profile'))
                .withTags(new Tag('aws:asg', 'group'), new Tag('Name', 'mock'))
                .withInstanceId(info.instanceId)
                .withLaunchTime(new Date())
                .withPrivateIpAddress(info.privateIp)
                .withSubnetId('subnet-123abc')
                .withVpcId('vpc-123abc')
                .withState(new InstanceState().withName(InstanceStateName.Running))
                .withInstanceType(info.instanceType)
                .withSecurityGroups(new GroupIdentifier().withGroupId('sg-abc123').withGroupName('mock-sg'))
    }
}
