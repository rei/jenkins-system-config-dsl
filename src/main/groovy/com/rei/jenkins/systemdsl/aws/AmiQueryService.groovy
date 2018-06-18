package com.rei.jenkins.systemdsl.aws

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.logging.Logger

import com.amazonaws.services.ec2.AmazonEC2
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder
import com.amazonaws.services.ec2.model.DescribeImagesRequest
import com.amazonaws.services.ec2.model.Filter

class AmiQueryService {
    private static final Logger logger = Logger.getLogger(AmiQueryService.name);
    public static final DateTimeFormatter CREATION_DATE_FORMAT = DateTimeFormatter.ISO_DATE_TIME

    private static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1)
    AmazonEC2 ec2

    AmiQueryService() {
        this(AmazonEC2ClientBuilder.defaultClient())
    }

    AmiQueryService(AmazonEC2 ec2) {
        this.ec2 = ec2
    }

    String lookupAmi(AmiQuery query) {
        def request = new DescribeImagesRequest()
                .withFilters(query.filters.collect { new Filter(it.key, [it.value])})
                .withExecutableUsers(query.executableUsers)
                .withOwners(query.owners)

        return ec2.describeImages(request).images.findAll { query.nameRegex == null || it.name.matches(query.nameRegex) }
                            .sort { ZonedDateTime.parse(it.creationDate, CREATION_DATE_FORMAT) }
                            .last().imageId
    }

    void scheduleAmiLookup(AmiQuery query, long interval, TimeUnit unit, Closure updateCallback) {
        def ami = lookupAmi(query)
        scheduler.scheduleAtFixedRate({
            def newAmi = lookupAmi(query)

            if (ami != newAmi) {
                logger.info("found ami $newAmi that replaces $ami, updating configuration")
                updateCallback(ami, newAmi)
                ami = newAmi
            }
        }, 0, interval, unit)
    }
}
