package com.shamkhi.deligo.domain.security.oauth2;

import com.shamkhi.deligo.domain.security.model.AuthProvider;

import java.util.Map;


public class OAuth2UserInfoFactory {

    private OAuth2UserInfoFactory() {}

    public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        if (registrationId.equalsIgnoreCase(AuthProvider.GOOGLE.toString())) {
            return new GoogleOAuth2UserInfo(attributes);
        } else if (registrationId.equalsIgnoreCase(AuthProvider.FACEBOOK.toString())) {
            return new FacebookOAuth2UserInfo(attributes);
        } else if (registrationId.equalsIgnoreCase(AuthProvider.AUTH0.toString())) {
            return new Auth0OAuth2UserInfo(attributes);
        }

        throw new IllegalArgumentException("Désolé ! La connexion avec " + registrationId + " n'est pas supportée.");
    }
}