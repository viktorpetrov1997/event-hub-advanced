package com.eventhub.user;

import com.eventhub.security.AuthenticatedUser;
import com.eventhub.user.dto.UserDto;
import com.eventhub.user.dto.UserRegisterRequest;
import com.eventhub.user.exception.UserAlreadyExistsException;
import com.eventhub.user.exception.UserNotFoundException;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserService implements UserDetailsService
{
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder)
    {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserDto register(UserRegisterRequest userRegisterRequest)
    {
        if(userRepository.existsByUsername(userRegisterRequest.getUsername())
                || userRepository.existsByEmail(userRegisterRequest.getEmail()))
        {
            throw new UserAlreadyExistsException("User with this username or email already exists!");
        }

        String encodedPassword = passwordEncoder.encode(userRegisterRequest.getPassword());

        User user = UserMapper.toUserEntity(userRegisterRequest, encodedPassword);

        User savedUser = userRepository.save(user);

        return UserMapper.toUserDto(savedUser);
    }

    public UserDto getById(UUID id)
    {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException
    {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid email or password!"));

        return AuthenticatedUser.builder()
                .id(user.getId())
                .displayName(user.getUsername())
                .email(user.getEmail())
                .password(user.getPassword())
                .role(user.getRole())
                .build();
    }
}
