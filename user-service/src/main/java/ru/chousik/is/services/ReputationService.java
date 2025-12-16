package ru.chousik.is.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.chousik.is.entity.Review;
import ru.chousik.is.entity.ReviewAuthorRole;
import ru.chousik.is.entity.User;
import ru.chousik.is.repository.ReviewRepository;
import ru.chousik.is.repository.UserRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReputationService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    @Transactional
    public void updateReputationForLessor(UUID lessorId) {
        updateRating(lessorId, reviewRepository.findAllByLessor_Id(lessorId), ReviewAuthorRole.LESSEE);
    }

    @Transactional
    public void updateReputationForLessee(UUID lesseeId) {
        updateRating(lesseeId, reviewRepository.findAllByLessee_Id(lesseeId), ReviewAuthorRole.LESSOR);
    }

    private void updateRating(UUID userId, List<Review> reviews, ReviewAuthorRole expectedReviewerRole) {
        if (userId == null) {
            return;
        }
        double average = reviews.stream()
                .filter(review -> review.getAuthorRole() == expectedReviewerRole)
                .mapToInt(Review::getRating)
                .average()
                .orElse(Double.NaN);
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return;
        }
        if (Double.isNaN(average)) {
            user.setRating(null);
        } else {
            user.setRating(BigDecimal.valueOf(average).setScale(1, RoundingMode.HALF_UP));
        }
        userRepository.save(user);
    }
}
