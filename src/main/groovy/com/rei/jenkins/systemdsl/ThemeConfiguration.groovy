package com.rei.jenkins.systemdsl


import org.apache.commons.lang.StringUtils
import org.codefirst.SimpleThemeDecorator
import org.jenkinsci.plugins.simpletheme.CssUrlThemeElement
import org.jenkinsci.plugins.simpletheme.FaviconUrlThemeElement
import org.jenkinsci.plugins.simpletheme.JsUrlThemeElement

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
        if (StringUtils.isNotBlank(css)) {
            decorator.elements.add(new CssUrlThemeElement(css));
        }
        if (StringUtils.isNotBlank(js)) {
            decorator.elements.add(new JsUrlThemeElement(js));
        }
        if (StringUtils.isNotBlank(favicon)) {
            decorator.elements.add(new FaviconUrlThemeElement(favicon));
        }
        decorator.save()
    }
}
