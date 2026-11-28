package com.eventhub.user;

import com.eventhub.booking.BookingService;
import com.eventhub.event.EventService;
import com.eventhub.security.AuthenticatedUser;
import com.eventhub.user.dto.UserDto;
import com.eventhub.user.dto.UserRegisterRequest;
import com.eventhub.user.exception.RoleChangeNotAllowedException;
import com.eventhub.user.exception.UserAlreadyExistsException;
import com.eventhub.user.exception.UserNotFoundException;

import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UserService implements UserDetailsService
{
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EventService eventService;
    private final BookingService bookingService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       EventService eventService, BookingService bookingService)
    {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.eventService = eventService;
        this.bookingService = bookingService;
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

    public void createDefaultAdminIfMissing(String username, String email, String rawPassword)
    {
        if(userRepository.existsByRole(UserRole.ADMIN)
                || userRepository.existsByUsername(username)
                || userRepository.existsByEmail(email))
        {
            return;
        }

        User admin = User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .role(UserRole.ADMIN)
                .build();

        userRepository.save(admin);
    }

    public UserDto getById(UUID id)
    {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return UserMapper.toUserDto(user);
    }

    public List<UserDto> getAllUsers()
    {
        return userRepository.findAll(Sort.by("username"))
                .stream()
                .map(UserMapper::toUserDto)
                .toList();
    }

    public UserDto changeRole(UUID userId, UserRole newRole, UUID currentAdminId)
    {
        if(userId.equals(currentAdminId))
        {
            throw new RoleChangeNotAllowedException("You cannot change your own role.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if(user.getRole() == newRole)
        {
            throw new RoleChangeNotAllowedException("This user already has this role.");
        }

        if(user.getRole() == UserRole.ORGANIZER && eventService.hasUpcomingOrOngoingEvents(userId))
        {
            throw new RoleChangeNotAllowedException("This organizer still has upcoming or ongoing events.");
        }

        if(user.getRole() == UserRole.ATTENDEE && bookingService.hasActiveBookingsForUser(userId))
        {
            throw new RoleChangeNotAllowedException("This attendee still has active bookings.");
        }

        user.setRole(newRole);

        User savedUser = userRepository.save(user);

        return UserMapper.toUserDto(savedUser);
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
