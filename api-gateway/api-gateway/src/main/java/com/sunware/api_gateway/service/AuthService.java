package com.sunware.api_gateway.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.sunware.api_gateway.model.Employee;
import com.sunware.api_gateway.model.Permission;
import com.sunware.api_gateway.repository.EmployeeRepository;
import com.sunware.api_gateway.util.JwtUtil;

import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.HashSet;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private EmployeeRepository employeeRepository;

    
    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private JwtUtil jwtUtil; // Util class to generate JWT

    // Generate OTP and send email
    public String generateOtp(String email) {
        try {
            Employee employee = employeeRepository.findByEmail(email);

            if (employee == null) {
                logger.error("Employee not found for email: {}", email);
                throw new RuntimeException("Invalid email");
            }

            // Generate 6-digit alphanumeric OTP
            String otp = RandomStringUtils.randomAlphanumeric(6);
            employee.setTemporaryOtp(otp);
            employeeRepository.save(employee);

            // Send OTP to email
            sendOtpEmail(email, otp);

            logger.info("Generated OTP for email: {}", email);
            return otp;
        } catch (Exception e) {
            logger.error("Error generating OTP for email: {}. Exception: {}", email, e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }
    }

    // Send OTP to employee's email
    public void sendOtpEmail(String to, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("Your OTP Code");
            message.setText("Your OTP code is: " + otp);

            mailSender.send(message);
            logger.info("OTP email sent to: {}", to);
        } catch (Exception e) {
            logger.error("Failed to send OTP email to: {}. Exception: {}", to, e.getMessage(), e);
            throw new RuntimeException("Failed to send OTP email. Please try again later.");
        }
    }

    // Validate the OTP
    public String validateOtpAndGenerateToken(String email, String otp) {
        try {
            Employee employee = employeeRepository.findByEmail(email);

            if (employee == null) {
                logger.error("Employee not found for email: {}", email);
                throw new RuntimeException("Invalid email");
            }

            String storedOtp = employee.getTemporaryOtp();

            logger.debug("Retrieved OTP from DB for email {}: {}", email, storedOtp);
            logger.debug("Provided OTP: {}", otp);

            if (otp.equals(storedOtp)) {
                // OTP is valid, clear it from the employee record to prevent reuse
                employee.setTemporaryOtp(null);
                employeeRepository.save(employee);
                // Fetch permissions and generate JWT
                Set<Permission> permissions = new HashSet<>(employee.getPermissions());
                String jwt = jwtUtil.generateTokenWithPermissions(email, permissions);

                logger.info("OTP successfully validated for email: {}", email);
                return jwt;
            } else {
                logger.warn("Invalid OTP provided for email: {}", email);
                throw new RuntimeException("Invalid OTP");
            }
        } catch (Exception e) {
            logger.error("Error validating OTP for email: {}. Exception: {}", email, e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }
    }
}
