package com.parkandride.service;

import com.parkandride.model.User;
import com.parkandride.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {
    
    private final UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    
    // Reward costs
    private static final Map<String, Integer> REWARD_POINT_COSTS = new HashMap<>();
    
    static {
        // Initialize reward costs
        REWARD_POINT_COSTS.put("FREE_PARKING", 200);
        REWARD_POINT_COSTS.put("PREMIUM_SPOT", 300);
        REWARD_POINT_COSTS.put("EXTENDED_HOURS", 150);
    }
    
    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    @Autowired
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }
    
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                new ArrayList<>()
        );
    }
    
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    @Transactional
    public User registerNewUser(User user) {
        // Check if username or email already exists
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        
        
        // Encode password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        // Set initial credit balance for new users instead of reward points
        user.setCreditBalance(50.0); // $50 welcome credit
        user.setRewardPoints(0);      // Start with 0 points
        
        return userRepository.save(user);
    }
    
    @Transactional
    public User updateUser(User user) {
        return userRepository.save(user);
    }
    
    @Transactional(readOnly = true)
    public Optional<User> findById(Long userId) {
        return userRepository.findById(userId);
    }
    
    @Transactional
    public User addSubscription(User user, String subscriptionTypeStr) {
        try {
            User.SubscriptionType subscriptionType = User.SubscriptionType.fromString(subscriptionTypeStr);
            
            // Set subscription details
            user.setSubscriptionType(subscriptionType);
            user.setSubscriptionStartDate(LocalDateTime.now());
            
            // Set expiration based on subscription type
            LocalDateTime expirationDate;
            switch (subscriptionType) {
                case BASIC:
                    expirationDate = LocalDateTime.now().plusMonths(1);
                    break;
                case STANDARD:
                    expirationDate = LocalDateTime.now().plusMonths(3);
                    break;
                case PREMIUM:
                    expirationDate = LocalDateTime.now().plusMonths(6);
                    break;
                case MONTHLY:
                    expirationDate = LocalDateTime.now().plusMonths(1);
                    break;
                case QUARTERLY:
                    expirationDate = LocalDateTime.now().plusMonths(3);
                    break;
                case ANNUAL:
                    expirationDate = LocalDateTime.now().plusMonths(12);
                    break;
                default:
                    expirationDate = LocalDateTime.now().plusMonths(1);
            }
            user.setSubscriptionExpiryDate(expirationDate);
            
            return userRepository.save(user);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid subscription type: " + subscriptionTypeStr);
        }
    }
    
    @Transactional
    public boolean redeemReward(User user, String rewardType) {
        if (!REWARD_POINT_COSTS.containsKey(rewardType)) {
            throw new IllegalArgumentException("Invalid reward type: " + rewardType);
        }
        
        int pointCost = REWARD_POINT_COSTS.get(rewardType);
        
        // Check if user has enough points
        if (user.getRewardPoints() < pointCost) {
            return false; // Not enough points
        }
        
        // Deduct points
        user.setRewardPoints(user.getRewardPoints() - pointCost);
        
        // Process reward (implementation would depend on the specific reward)
        switch (rewardType) {
            case "FREE_PARKING":
                // Generate a free parking coupon or add it to user's account
                user.addRewardCoupon("FREE_PARKING");
                break;
            case "PREMIUM_SPOT":
                // Add premium spot access to user's account
                user.addRewardCoupon("PREMIUM_SPOT");
                break;
            case "EXTENDED_HOURS":
                // Add extended hours coupon to user's account
                user.addRewardCoupon("EXTENDED_HOURS");
                break;
        }
        
        userRepository.save(user);
        return true;
    }
    
    @Transactional
    public double convertPointsToCredit(User user, int points) {
        double convertedCredit = user.convertPointsToCredit(points);
        if (convertedCredit > 0) {
            userRepository.save(user);
        }
        return convertedCredit;
    }
    
    @Transactional
    public void initializeSampleData() {
        // Create test users if they don't exist
        createUserIfNotExists(
            "john.doe", 
            "password", 
            "John", 
            "Doe", 
            "john.doe@example.com", 
            "1234567890", 
            "MH-01-AB-1234",
            250,
            50.0,
            null
        );
        
        createUserIfNotExists(
            "jane.smith", 
            "password", 
            "Jane", 
            "Smith", 
            "jane.smith@example.com", 
            "0987654321", 
            "MH-02-CD-5678",
            100,
            25.0,
            "BASIC"
        );
        
        createUserIfNotExists(
            "testuser",
            "test123",
            "Test",
            "User",
            "test@example.com",
            "5555555555",
            "DL-05-EF-9012",
            100,
            50.0,
            null
        );
    }
    
    private void createUserIfNotExists(String username, String password, String firstName, 
                                     String lastName, String email, String phoneNumber, 
                                     String vehicleNumber, int rewardPoints, double creditBalance, 
                                     String subscriptionType) {
        // Check if user already exists
        if (userRepository.findByUsername(username).isPresent()) {
            return; // User already exists, skip creation
        }
        
        try {
            User user = new User(
                username,
                passwordEncoder.encode(password),
                firstName,
                lastName,
                email,
                phoneNumber,
                vehicleNumber
            );
            
            user.setRewardPoints(rewardPoints);
            user.setCreditBalance(creditBalance);
            
            // Add subscription if specified
            if (subscriptionType != null) {
                user = addSubscription(user, subscriptionType);
            }
            
            userRepository.save(user);
        } catch (Exception e) {
            // Log the error but don't fail the entire application
            System.err.println("Error creating user " + username + ": " + e.getMessage());
        }
    }
} 