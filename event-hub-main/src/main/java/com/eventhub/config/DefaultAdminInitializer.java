package com.eventhub.config;

import com.eventhub.user.UserService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class DefaultAdminInitializer implements ApplicationRunner
{
    private final UserService userService;

    @Value("${eventhub.admin.username}")
    private String adminUsername;

    @Value("${eventhub.admin.email}")
    private String adminEmail;

    @Value("${eventhub.admin.password}")
    private String adminPassword;

    public DefaultAdminInitializer(UserService userService)
    {
        this.userService = userService;
    }

    @Override
    public void run(ApplicationArguments args)
    {
        userService.createDefaultAdminIfMissing(adminUsername, adminEmail, adminPassword);
    }
}
