package com.sap.security.poc.service;

import com.sap.security.poc.config.OAuth2Configuration;
import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthJSONAccessTokenResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OAuth2Service {

    @Autowired
    private OAuth2Configuration auth2Configuration;


    public String getAccessToken() throws OAuthProblemException, OAuthSystemException {
        OAuthClient client = new OAuthClient(new URLConnectionClient());
        OAuthClientRequest request =
                OAuthClientRequest.tokenLocation(auth2Configuration.getTokenUrl())
                        .setGrantType(GrantType.CLIENT_CREDENTIALS)
                        .setClientId(auth2Configuration.getClientId())
                        .setClientSecret(auth2Configuration.getClientSecret())
                        .buildBodyMessage();


        return client.accessToken(request,
                OAuth.HttpMethod.POST,
                OAuthJSONAccessTokenResponse.class).getAccessToken();
    }
}
