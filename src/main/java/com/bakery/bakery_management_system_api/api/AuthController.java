package com.bakery.bakery_management_system_api.api;


import com.bakery.bakery_management_system_api.dto.request.UserRequestDto;
import com.bakery.bakery_management_system_api.serviceImpl.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/authController")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;

    public AuthController(AuthenticationManager authenticationManager, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
    }



    @PostMapping("/login")
    public ResponseEntity<String> loginUser (@RequestBody UserRequestDto userRequestDto){

        try{
            String token = userService.loginUser(userRequestDto);
            return ResponseEntity.ok(token);
        }

        catch (BadCredentialsException e){

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid User Name or Password");
        }


    }

@PostMapping("/saveUser")
    public void createUser (@RequestBody UserRequestDto userRequestDto){
        userService.saveUser(userRequestDto);

    }


}
