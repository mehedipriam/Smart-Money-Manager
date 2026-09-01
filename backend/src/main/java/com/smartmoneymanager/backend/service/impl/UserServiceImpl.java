package com.smartmoneymanager.backend.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartmoneymanager.backend.dto.request.UpdateProfileRequest;
import com.smartmoneymanager.backend.dto.response.UserProfileResponse;
import com.smartmoneymanager.backend.entity.User;
import com.smartmoneymanager.backend.exception.ResourceNotFoundException;
import com.smartmoneymanager.backend.mapper.UserMapper;
import com.smartmoneymanager.backend.repository.UserRepository;
import com.smartmoneymanager.backend.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(Long userId) {
        return userMapper.toProfileResponse(findUser(userId));
    }

    @Override
    public UserProfileResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = findUser(userId);
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setProfileImageUrl(request.getProfileImageUrl());
        user.setDefaultCurrency(request.getDefaultCurrency());
        user.setPreferredLanguage(request.getPreferredLanguage());
        return userMapper.toProfileResponse(userRepository.save(user));
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
