package com.bakery.bakery_management_system_api.serviceImpl;


import com.bakery.bakery_management_system_api.dto.request.UserRequestDto;
import com.bakery.bakery_management_system_api.entity.Users;
import com.bakery.bakery_management_system_api.repositary.UserRepo;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;


    public UserService(UserRepo userRepo, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JwtService jwtService) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public void saveUser(UserRequestDto userRequestDto){


        Users users = new Users();


        users.setUserName(userRequestDto.getUserName());
        users.setPassword(passwordEncoder.encode(userRequestDto.getPassword()));
        users.setRole("USERS");
        userRepo.save(users);
    }


    public String loginUser(UserRequestDto userRequestDto) {


        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                userRequestDto.getUserName(),
                                userRequestDto.getPassword()
                        )
                );
        System.out.println("AUTHENTICATION SUCCESSFUL");

        UserDetails userDetails =
                (UserDetails) authentication.getPrincipal();
        
        System.out.println("AUTHENTICATED USER: " + userDetails.getUsername());

        return jwtService.generateToken(userDetails);
    }
}
