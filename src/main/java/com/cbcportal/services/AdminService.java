package com.cbcportal.services;

import com.cbcportal.models.EducatorProfile;
import com.cbcportal.models.Notification;
import com.cbcportal.models.SchoolProfile;
import com.cbcportal.models.User;
import com.cbcportal.repositories.EducatorProfileRepository;
import com.cbcportal.repositories.NotificationRepository;
import com.cbcportal.repositories.SchoolProfileRepository;
import com.cbcportal.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EducatorProfileRepository profileRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private SchoolProfileRepository schoolProfileRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<EducatorProfile> getUnvettedEducators() {
        return profileRepository.findByIsVettedFalse();
    }

    public EducatorProfile verifyEducator(Long profileId) {
        Optional<EducatorProfile> profileOpt = profileRepository.findById(profileId);
        if (profileOpt.isPresent()) {
            EducatorProfile profile = profileOpt.get();
            profile.setVetted(true);
            EducatorProfile savedProfile = profileRepository.save(profile);

            // NEW FIX: Also approve the base User account so they can actually log in!
            User user = savedProfile.getUser();
            user.setApproved(true);
            userRepository.save(user);

            // Send the notification
            Notification notif = new Notification();
            notif.setRecipient(user);
            notif.setMessage("Congratulations! Your TSC Registration has been verified and your account is approved. You can now log in and apply for teaching vacancies.");
            notificationRepository.save(notif);

            return savedProfile;
        }
        throw new RuntimeException("Educator Profile not found.");
    }

    public List<SchoolProfile> getUnapprovedSchools() {
        return schoolProfileRepository.findByUserIsApprovedFalse();
    }

    public User approveUserAccount(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setApproved(true);
            User savedUser = userRepository.save(user);

            Notification notif = new Notification();
            notif.setRecipient(savedUser);
            notif.setMessage("Your account has been verified by the System Administrator. You can now log in to the portal.");
            notificationRepository.save(notif);

            return savedUser;
        }
        throw new RuntimeException("User not found.");
    }
}