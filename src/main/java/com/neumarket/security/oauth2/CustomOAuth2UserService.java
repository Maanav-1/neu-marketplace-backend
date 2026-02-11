package com.neumarket.security.oauth2;

import com.neumarket.enums.Role;
import com.neumarket.model.User;
import com.neumarket.repository.UserRepository;
import com.neumarket.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

  private final UserRepository userRepository;

  @Override
  public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
    OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);
    return processOAuth2User(oAuth2User);
  }

  private OAuth2User processOAuth2User(OAuth2User oAuth2User) {
    String email = oAuth2User.getAttribute("email");
    String name = oAuth2User.getAttribute("name");
    String picture = oAuth2User.getAttribute("picture");

    Optional<User> userOptional = userRepository.findByEmail(email);
    User user;
    if (userOptional.isPresent()) {
      user = userOptional.get();

      // FIX: Verify the user is not blocked before proceeding
      if (Boolean.TRUE.equals(user.getBlocked())) {
        throw new org.springframework.security.authentication.LockedException(
            "This account has been blocked: " + user.getBlockReason()
        );
      }

      user.setName(name);
      user.setProfilePicUrl(picture);
      userRepository.save(user);
    } else {
      user = User.builder()
          .email(email)
          .name(name)
          .profilePicUrl(picture)
          .passwordHash("OAUTH_users-password") // PLACEHOLDER PASSWORD FOR USERS WHO LOGIN THRU GOOGLE
          .role(Role.USER)
          .emailVerified(true) // Google emails are implicitly verified
          .blocked(false)
          .build();
      userRepository.save(user);
    }

    return UserPrincipal.create(user, oAuth2User.getAttributes());
  }
}