package com.victor.user_service.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.victor.user_service.dto.AuthUserDto;
import com.victor.user_service.dto.CreateUserResponse;
import com.victor.user_service.dto.UserDto;
import com.victor.user_service.model.User;
import com.victor.user_service.request.RegisterRequest;
import com.victor.user_service.request.UserUpdateRequest;
import com.victor.user_service.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/v1/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;
    private final ModelMapper modelMapper;

    @PostMapping("/save")
    public ResponseEntity<CreateUserResponse> save(@Valid @RequestBody RegisterRequest request) {
        log.info("Inside UserController.save with request body {}", request);
        return ResponseEntity.ok(userService.saveUser(request));
    }

    @GetMapping("/getAll")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDto>> getAll() {
        log.info("Inside UserController.getAll");
        return ResponseEntity.ok(userService.getAll().stream()
                .map(user -> modelMapper.map(user, UserDto.class)).toList());
    }

    @GetMapping("/getUserById/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable String id) {
        log.info("Inside UserController.getUserById with id:: {}", id);
        return ResponseEntity.ok(modelMapper.map(userService.getUserById(id), UserDto.class));
    }

    @GetMapping("/getUserByEmail/{email}")
    public ResponseEntity<UserDto> getUserByEmail(@PathVariable String email) {
        log.info("Inside UserController.getUserByEmail with email:: {}", email);
        return ResponseEntity.ok(modelMapper.map(userService.getUserByEmail(email), UserDto.class));
    }

    @GetMapping("/getUserByUsername/{username}")
    public ResponseEntity<AuthUserDto> getUserByUsername(@PathVariable String username) {
        log.info("Inside UserController.getUserByUsername with username:: {}", username);
        return ResponseEntity.ok(modelMapper.map(userService.getUserByUsername(username), AuthUserDto.class));
    }

    @PutMapping(value = "/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN') or @userService.getUserById(#request.id).username == principal")
    public ResponseEntity<UserDto> updateUserById(
            @RequestPart("request") String requestJson,
            @RequestPart(value = "file", required = false) MultipartFile file) throws JsonProcessingException {

        log.info("Inside UserController.updateUserById");
        // Convert JSON string to Object
        UserUpdateRequest request = new ObjectMapper().readValue(requestJson, UserUpdateRequest.class);
        User updatedUser = userService.updateUserById(request, file);

        if (updatedUser == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        return ResponseEntity.ok(modelMapper.map(updatedUser, UserDto.class));
    }


    @DeleteMapping("/deleteUserById/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userService.getUserById(#id).username == principal")
    public ResponseEntity<Void> deleteUserById(@PathVariable String id) {
        log.info("Inside UserController.deleteUserById with id:: {}", id);
        userService.deleteUserById(id);
        return ResponseEntity.ok().build();
    }
}
