package com.victor.user_service.service;

import com.victor.user_service.client.FileStorageClient;
import com.victor.user_service.client.WalletServiceClient;
import com.victor.user_service.dto.CreateUserResponse;
import com.victor.user_service.dto.CreateWalletRequest;
import com.victor.user_service.dto.WalletResponse;
import com.victor.user_service.enums.Active;
import com.victor.user_service.enums.Role;
import com.victor.user_service.exception.EmailAlreadyExistException;
import com.victor.user_service.exception.NotFoundException;
import com.victor.user_service.exception.UsernameAlreadyExistException;
import com.victor.user_service.model.User;
import com.victor.user_service.model.UserDetails;
import com.victor.user_service.repository.UserRepository;
import com.victor.user_service.request.RegisterRequest;
import com.victor.user_service.request.UserUpdateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageClient fileStorageClient;
    private final WalletServiceClient walletServiceClient;
    private final ModelMapper modelMapper;

    public CreateUserResponse saveUser(RegisterRequest request) {

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new EmailAlreadyExistException("User with email address: " + request.getEmail() + " already exists");
        }

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new UsernameAlreadyExistException("Username: " + request.getUsername() + " already taken");
        }

        User toSave = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .role(Role.USER)
                .active(Active.ACTIVE).build();
        userRepository.save(toSave);

        CreateWalletRequest createWalletRequest = new CreateWalletRequest();
        createWalletRequest.setUserId(toSave.getId());

        WalletResponse walletInfo = walletServiceClient.createWallet(createWalletRequest);

        return CreateUserResponse.builder()
                .id(toSave.getId())
                .username(toSave.getUsername())
                .email(toSave.getEmail())
                .userDetails(toSave.getUserDetails())
                .walletInfo(walletInfo)
                .build();
    }

    public List<User> getAll() {
        return userRepository.findAllByActive(Active.ACTIVE);
    }

    public User getUserById(String id) {
        return findUserById(id);
    }

    public User getUserByEmail(String email) {
        return findUserByEmail(email);
    }

    public User getUserByUsername(String username) {
        return findUserByUsername(username);
    }

//    @Transactional
    public User updateUserById(UserUpdateRequest request, MultipartFile file) {
        User toUpdate = findUserById(request.getId());

        // Initialize UserDetails if null
        if (toUpdate.getUserDetails() == null) {
            toUpdate.setUserDetails(new UserDetails());
        }

        // Store old picture reference before update
        String oldProfilePicture = toUpdate.getUserDetails().getProfilePicture();

        log.info("oldProfilePicture: {}", oldProfilePicture);

        // Update user details
        updateUserDetails(toUpdate.getUserDetails(), request.getUserDetails(), file);

        // Update password if provided
        if (request.getPassword() != null) {
            toUpdate.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        User savedUser = userRepository.save(toUpdate);

        // Delete old picture after successful save (using Feign client)
        if (file != null && oldProfilePicture != null) {
            try {
                // Extract just the file ID from the URL
                String oldFileId = extractFileIdFromUrl(oldProfilePicture);
                fileStorageClient.deleteImageFromFileSystem(oldFileId);
            } catch (Exception e) {
                log.error("Failed to delete old profile picture: {}", oldProfilePicture, e);
            }
        }

        return savedUser;
    }

    private String extractFileIdFromUrl(String url) {
        // Example URL: https://storage.googleapis.com/bucket-name/uuid.ext
        String fileName = url.substring(url.lastIndexOf("/") + 1);
        return fileName.split("\\.")[0]; // Return just the UUID part
    }

    public void deleteUserById(String id) {
        User toDelete = findUserById(id);
        toDelete.setActive(Active.INACTIVE);
        userRepository.save(toDelete);
    }

    protected User findUserById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    protected User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    protected User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private void updateUserDetails(UserDetails toUpdate, UserDetails request, MultipartFile file) {
        if (file != null) {
            log.info("About to upload file to gcp");
            String profilePicture = fileStorageClient.uploadImageToFIleSystem(file).getBody();
            log.info("Successfully uploaded file to gcp. Profile Picture: {}", profilePicture);
            if (profilePicture != null) {
                // Only delete old picture if it exists
                if (toUpdate.getProfilePicture() != null) {
                    log.info("Old profile picture exists: {}", toUpdate.getProfilePicture());
                    fileStorageClient.deleteImageFromFileSystem(extractFileIdFromUrl(toUpdate.getProfilePicture()));
                    log.info("Successfully deleted old profile picture");
                }
                toUpdate.setProfilePicture(profilePicture);
            }
        }

        // Map non-null fields
        if (request != null) {
            if (request.getFirstName() != null) toUpdate.setFirstName(request.getFirstName());
            if (request.getLastName() != null) toUpdate.setLastName(request.getLastName());
            if (request.getPhoneNumber() != null) toUpdate.setPhoneNumber(request.getPhoneNumber());
            if (request.getCountry() != null) toUpdate.setCountry(request.getCountry());
            if (request.getCity() != null) toUpdate.setCity(request.getCity());
            if (request.getAddress() != null) toUpdate.setAddress(request.getAddress());
            if (request.getPostalCode() != null) toUpdate.setPostalCode(request.getPostalCode());
            if (request.getAboutMe() != null) toUpdate.setAboutMe(request.getAboutMe());
        }
    }
}
