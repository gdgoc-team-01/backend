package com.gdg.backend.infra;

import com.gdg.backend.application.GoogleAccountService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;

@Configuration(proxyBeanMethods = false)
class GoogleLoginConfiguration {

    @Bean
    SecurityFilterChain googleLoginFilterChain(HttpSecurity http,
                                               ObjectProvider<ClientRegistrationRepository> clients,
                                               GoogleAccountService accounts) throws Exception {
        var paths = PathPatternRequestMatcher.withDefaults();
        var updateUser = paths.matcher(HttpMethod.PATCH, "/users/{id}");
        var deleteUser = paths.matcher(HttpMethod.DELETE, "/users/{id}");
        http.securityMatcher(new OrRequestMatcher(
                        paths.matcher("/oauth2/authorization/google"), paths.matcher("/login/oauth2/code/google"),
                        paths.matcher("/login/success"), paths.matcher("/login/failed"),
                        paths.matcher("/me"), paths.matcher("/csrf"), paths.matcher("/logout"),
                        updateUser, deleteUser))
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers("/login/success", "/me", "/csrf").authenticated()
                        .requestMatchers(updateUser, deleteUser).authenticated()
                        .requestMatchers("/actuator/health").permitAll()
                        .anyRequest().permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .deleteCookies("JSESSIONID")
                        .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler(HttpStatus.NO_CONTENT)))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)));

        ClientRegistrationRepository registrations = clients.getIfAvailable();
        if (registrations != null) {
            OidcUserService oidcUsers = new OidcUserService();
            var authorizationRequests = new DefaultOAuth2AuthorizationRequestResolver(
                    registrations, "/oauth2/authorization");
            authorizationRequests.setAuthorizationRequestCustomizer(request -> request
                    .additionalParameters(parameters -> parameters.put("prompt", "select_account")));
            http.oauth2Login(login -> login
                    .defaultSuccessUrl("/login/success", true)
                    .failureUrl("/login/failed")
                    .authorizationEndpoint(endpoint -> endpoint.authorizationRequestResolver(authorizationRequests))
                    .userInfoEndpoint(endpoint -> endpoint.oidcUserService(request -> {
                        var principal = oidcUsers.loadUser(request);
                        accounts.registerIfNeeded(request.getIdToken());
                        return principal;
                    })));
        }

        return http.build();
    }
}
