package com.gurula.mailXpert.oauth2;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface OAuthTokenRepository extends MongoRepository<OAuthToken, String> {
    Optional<OAuthToken> findByUserIdAndOauth2ClientName(String userId, String oauth2ClientName);
    Optional<OAuthToken> findByUserId(String email);
}
