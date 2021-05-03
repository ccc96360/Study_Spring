package com.devminj.config.auth;

import com.devminj.config.auth.dto.OAuthAttributes;
import com.devminj.config.auth.dto.SessionUser;
import com.devminj.domain.user.User;
import com.devminj.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.util.Collections;

@RequiredArgsConstructor
@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;
    private final HttpSession httpSession;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        // 현재 로그인 진행중인 서비스를 구분하느 코드이다.
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        // OAuth2 로그인 진행 시 키가되는 필드 값을 이야기한다. Primary Key와 같은 의미이다.
        // 구글은 기본적으로 코드를 지원하지만 네이버 카카오 등은 기본 지원하지 않는다.
        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();
        // OAuth2UserService를 통해 가져온 OAuth2User의 Attribute를 담는 클래스이다.
        OAuthAttributes attributes = OAuthAttributes.of(registrationId, userNameAttributeName, oAuth2User.getAttributes());

        User user = saveOrUpdate(attributes);

        httpSession.setAttribute("user", new SessionUser(user));// SessionUser는 세션에 사용자 정보를 저장하기 위한 DTO이다.

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(user.getRoleKey())),
                attributes.getAttributes(),
                attributes.getNameAttributeKey());
    }

    private User saveOrUpdate(OAuthAttributes attributes){
        User user = userRepository.findByEmail(attributes.getEmail())
                    .map(entity -> entity.update(attributes.getName(), attributes.getPicture()))
                    .orElse(attributes.toEntity());
        return userRepository.save(user);

    }
}
