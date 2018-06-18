package com.rei.jenkins.systemdsl

import net.sf.json.JSONObject

import com.sonyericsson.rebuild.RebuildDescriptor

class RebuildConfiguration extends DslSection {
    boolean rememberPasswords

    void rememberPasswords() {
        rememberPasswords(true)
    }

    void rememberPasswords(boolean remember) {
        rememberPasswords = remember
    }

    void save() {
        def config = JSONObject.fromObject([rememberPasswordEnabled: rememberPasswords as String])
        jenkins.getDescriptor(RebuildDescriptor.class).configure(null, config)
    }
}
