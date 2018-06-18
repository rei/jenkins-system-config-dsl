package com.rei.jenkins.systemdsl.aws

class AmiQuery {
    String nameRegex
    List<String> executableUsers
    List<String> owners
    Map<String, String> filters = [:]
}
