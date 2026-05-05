package org.example.backend.user.repository;

import org.example.backend.user.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByOauthProviderAndOauthProviderUserId(User.OAuthProvider oauthProvider, String oauthProviderUserId);

    boolean existsByOauthProviderAndOauthProviderUserId(User.OAuthProvider oauthProvider, String oauthProviderUserId);
}
