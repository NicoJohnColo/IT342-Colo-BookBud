package com.example.colo.bookbud.service;

import com.example.colo.bookbud.dto.user.UpdateUserRequest;
import com.example.colo.bookbud.dto.user.UserProfileDTO;
import com.example.colo.bookbud.entity.Transaction;
import com.example.colo.bookbud.entity.User;
import com.example.colo.bookbud.exception.BusinessException;
import com.example.colo.bookbud.exception.ResourceNotFoundException;
import com.example.colo.bookbud.repository.TransactionRepository;
import com.example.colo.bookbud.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    @Transactional(readOnly = true)
    public UserProfileDTO getUserProfile(String userId, String requestingUserId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("DB-001", "User not found"));

        boolean hasSharedTransaction = hasSharedTransaction(userId, requestingUserId);

        return UserProfileDTO.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .rating(user.getRating())
                .createdAt(user.getCreatedAt().toString())
                .facebookUrl(hasSharedTransaction ? user.getFacebookUrl() : null)
                .messenger(hasSharedTransaction ? user.getMessenger() : null)
                .mobileNumber(hasSharedTransaction ? user.getMobileNumber() : null)
                .build();
    }

    @Transactional
    public UserProfileDTO updateUserProfile(String userId, String requestingUserId, UpdateUserRequest request) {
        if (!userId.equals(requestingUserId)) {
            throw new BusinessException("AUTH-003", "Insufficient permissions");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("DB-001", "User not found"));

        if (request.getUsername() != null) {
            if (!user.getUsername().equals(request.getUsername()) && 
                userRepository.existsByUsername(request.getUsername())) {
                throw new BusinessException("DB-002", "Username already exists");
            }
            user.setUsername(request.getUsername());
        }

        user.setFacebookUrl(request.getFacebookUrl());
        user.setMessenger(request.getMessenger());
        user.setMobileNumber(request.getMobileNumber());

        user = userRepository.save(user);

        return UserProfileDTO.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .rating(user.getRating())
                .createdAt(user.getCreatedAt().toString())
                .facebookUrl(user.getFacebookUrl())
                .messenger(user.getMessenger())
                .mobileNumber(user.getMobileNumber())
                .build();
    }

    private boolean hasSharedTransaction(String userId, String requestingUserId) {
        if (userId.equals(requestingUserId)) {
            return true;
        }
        
        return transactionRepository.findByUserUserId(requestingUserId).stream()
                .anyMatch(t -> t.getOwner().getUserId().equals(userId) && 
                    t.getStatus() == Transaction.Status.Completed) ||
               transactionRepository.findByOwnerUserId(requestingUserId).stream()
                .anyMatch(t -> t.getUser().getUserId().equals(userId) && 
                    t.getStatus() == Transaction.Status.Completed);
    }
}
