package com.shamkhi.deligo.domain.security.oauth2;

import java.util.Map;

public class Auth0OAuth2UserInfo implements OAuth2UserInfo {

    private final Map<String, Object> attributes;

    public Auth0OAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getId() {
        return (String) attributes.get("sub");
    }

    @Override
    public String getName() {
        return (String) attributes.get("name");
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }

    @Override
    public String getFirstName() {
        return (String) attributes.get("given_name");
    }

    @Override
    public String getLastName() {
        return (String) attributes.get("family_name");
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }
}