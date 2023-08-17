package com.rei.jenkins.systemdsl

import com.dabsquared.gitlabjenkins.connection.GitLabConnection
import com.dabsquared.gitlabjenkins.connection.GitLabConnectionConfig

class GitlabConfiguration extends DslSection {

    private String apiTokenId;
    private String name;
    private String hostUrl;

    void apiTokenId(String apiTokenId) {
        this.apiTokenId = apiTokenId;
    }

    void name(String name) {
        this.name = name
    }

    void hostUrl(String hostUrl) {
        this.hostUrl = hostUrl
    }

    void save() {
        GitLabConnection connection = new GitLabConnection(
                name,
                hostUrl,
                apiTokenId,
                false,
                10,
                10);

        GitLabConnectionConfig config = new GitLabConnectionConfig();

        config.addConnection(connection);

        config.save();
    }
}
