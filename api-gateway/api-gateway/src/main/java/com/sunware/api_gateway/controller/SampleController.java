package com.sunware.api_gateway.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class SampleController {

    @GetMapping("/onboard")
    @PreAuthorize("hasAuthority('onboard_employee')")
    public String onboardEmployee() {
        return "This is a protected resource for onboarding employees!";
    }

    @GetMapping("/update")
    @PreAuthorize("hasAuthority('update_employee')")
    public String updateEmployee() {
        return "This is a protected resource for updating employees!";
    }
    
    @GetMapping("/read")
    public String readEmployee() {
        return "This is a protected resource for reading employee data!";
    }
    
    @GetMapping("/delete")
    @PreAuthorize("hasAuthority('delete_employee')")
    public String deleteEmployee() {
        return "This is a protected resource for deleting employees!";
    }
}
