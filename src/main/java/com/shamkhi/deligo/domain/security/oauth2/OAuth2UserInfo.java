package com.shamkhi.deligo.domain.security.oauth2;

import java.util.Map;

public interface OAuth2UserInfo {

    String getId();

    String getName();

    String getEmail();

    String getFirstName();

    String getLastName();

    Map<String, Object> getAttributes();
}