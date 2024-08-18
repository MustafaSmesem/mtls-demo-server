package com.joumer.mtlsserver.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import jakarta.servlet.Filter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Configuration
public class WebConfig {

    private static final List<String> EXCLUDED_ENDPOINTS = Arrays.asList("/auth");

    @Bean
    public FilterRegistrationBean<ClientAuthExclusionFilter> clientAuthExclusionFilter() {
        FilterRegistrationBean<ClientAuthExclusionFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new ClientAuthExclusionFilter(EXCLUDED_ENDPOINTS));
        registrationBean.setOrder(1); // Set the order of the filter
        registrationBean.addUrlPatterns("/*");
        return registrationBean;
    }
}

class ClientAuthExclusionFilter implements Filter {

    private final List<String> excludedEndpoints;

    public ClientAuthExclusionFilter(List<String> excludedEndpoints) {
        this.excludedEndpoints = excludedEndpoints;
    }

    @Override
    public void doFilter(jakarta.servlet.ServletRequest request, jakarta.servlet.ServletResponse response, jakarta.servlet.FilterChain chain)
        throws IOException, jakarta.servlet.ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String requestUri = httpRequest.getRequestURI();
        if (excludedEndpoints.stream().anyMatch(requestUri::startsWith)) {
            // The request matches an excluded endpoint, skip client authentication
            chain.doFilter(request, response);
        } else {
            // Perform client authentication for the request
            performClientAuthentication(httpRequest, httpResponse);
            chain.doFilter(request, response);
        }
    }

    private void performClientAuthentication(HttpServletRequest request, HttpServletResponse response) {
        // Implement your client authentication logic here
        // For example, you can use the ServletRequest.getAttribute() method to access the client certificate
    }

    // Other filter lifecycle methods (init, destroy) can be implemented as needed
}