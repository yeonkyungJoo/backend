package com.project.mentoridge.modules.account.service;

import com.project.mentoridge.config.exception.EntityNotFoundException;
import com.project.mentoridge.config.exception.UnauthorizedException;
import com.project.mentoridge.modules.account.controller.request.CareerCreateRequest;
import com.project.mentoridge.modules.account.controller.request.CareerUpdateRequest;
import com.project.mentoridge.modules.account.controller.response.CareerResponse;
import com.project.mentoridge.modules.account.repository.CareerRepository;
import com.project.mentoridge.modules.account.repository.MentorRepository;
import com.project.mentoridge.modules.account.vo.Career;
import com.project.mentoridge.modules.account.vo.Mentor;
import com.project.mentoridge.modules.account.vo.User;
import com.project.mentoridge.modules.log.component.CareerLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static com.project.mentoridge.config.exception.EntityNotFoundException.EntityType.CAREER;
import static com.project.mentoridge.modules.account.enums.RoleType.MENTOR;

@Transactional
@RequiredArgsConstructor
@Service
public class CareerService {

    private final CareerRepository careerRepository;
    private final MentorRepository mentorRepository;
    private final CareerLogService careerLogService;

    private Career getCareer(User user, Long careerId) {

        Mentor mentor = Optional.ofNullable(mentorRepository.findByUser(user))
                .orElseThrow(() -> new UnauthorizedException(MENTOR));

        return careerRepository.findByMentorAndId(mentor, careerId)
                .orElseThrow(() -> new EntityNotFoundException(CAREER));
    }

    @Transactional(readOnly = true)
    public CareerResponse getCareerResponse(User user, Long careerId) {
        return new CareerResponse(getCareer(user, careerId));
    }

    public Career createCareer(User user, CareerCreateRequest careerCreateRequest) {

        Mentor mentor = Optional.ofNullable(mentorRepository.findByUser(user))
                .orElseThrow(() -> new UnauthorizedException(MENTOR));

        Career career = careerCreateRequest.toEntity(mentor);
        mentor.addCareer(career);

        Career saved = careerRepository.save(career);
        careerLogService.insert(user, saved);
        return saved;
    }

    public void updateCareer(User user, Long careerId, CareerUpdateRequest careerUpdateRequest) {

        Mentor mentor = Optional.ofNullable(mentorRepository.findByUser(user))
                .orElseThrow(() -> new UnauthorizedException(MENTOR));

        Career career = careerRepository.findByMentorAndId(mentor, careerId)
                .orElseThrow(() -> new EntityNotFoundException(CAREER));
        Career before = career.copy();
        career.update(careerUpdateRequest);
        careerLogService.update(user, before, career);
    }

    public void deleteCareer(User user, Long careerId) {

        Mentor mentor = Optional.ofNullable(mentorRepository.findByUser(user))
                .orElseThrow(() -> new UnauthorizedException(MENTOR));

        Career career = careerRepository.findByMentorAndId(mentor, careerId)
                .orElseThrow(() -> new EntityNotFoundException(CAREER));

        career.delete();
        // TODO - CHECK
        careerRepository.delete(career);
        careerLogService.delete(user, career);
    }
}
