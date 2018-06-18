package com.rei.jenkins.systemdsl

import net.sf.json.JSONObject

import org.codefirst.SimpleThemeDecorator

class ThemeConfiguration extends DslSection {
    private String css
    private String js
    private String favicon

    void cssUrl(String url) {
        this.css = url
    }

    void jsUrl(String url) {
        this.js = url
    }

    void faviconUrl(String url) {
        this.favicon = url
    }

    void save() {
        def decorator = jenkins.getExtensionList(SimpleThemeDecorator)[0]
        decorator.cssUrl = css
        decorator.jsUrl = js
        decorator.faviconUrl = favicon
        decorator.save()
    }
}
