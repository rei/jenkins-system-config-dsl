package com.rei.jenkins.systemdsl.aws

import static org.junit.Assert.*

import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

import org.junit.Test

import com.amazonaws.services.ec2.AmazonEC2
import com.amazonaws.services.ec2.model.DescribeImagesRequest
import com.amazonaws.services.ec2.model.DescribeImagesResult
import com.amazonaws.services.ec2.model.Image

class AmiQueryServiceTest {
    @Test
    void canFindLatestAmi() {
        def images = [
                new Image(imageId: 'old', creationDate: '2018-02-01T22:03:21.000Z'),
                new Image(imageId: 'ami123456', creationDate: '2018-03-02T22:03:21.000Z'),
                new Image(imageId: 'old2', creationDate: '2018-02-10T12:03:21.000Z')
        ]

        def service = new AmiQueryService([describeImages: { DescribeImagesRequest req -> new DescribeImagesResult(images: images) }] as AmazonEC2)
        def query = new AmiQuery(filters: [name: 'foo.*'])

        def ami = service.lookupAmi(query)

        service.scheduleAmiLookup(query, 10, TimeUnit.MILLISECONDS) { oldAmi, newAmi ->
            ami = newAmi
        }

        sleep(50) // ensure at least one interval

        assertEquals('ami123456', ami)

        images << new Image(imageId: 'ami654321', creationDate: '2018-03-03T22:03:21.000Z')

        sleep(50) // ensure at least one interval
        assertEquals('ami654321', ami)
    }
}
