package roomescape.service;

import java.time.Clock;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationTimeRepository;
import roomescape.domain.Theme;
import roomescape.domain.ThemeRepository;
import roomescape.domain.Waiting;
import roomescape.domain.WaitingRepository;
import roomescape.exception.reservation.DuplicatedReservationException;
import roomescape.exception.reservation.InvalidDateTimeReservationException;
import roomescape.exception.reservation.NotFoundReservationException;
import roomescape.exception.theme.NotFoundThemeException;
import roomescape.exception.time.NotFoundTimeException;
import roomescape.service.dto.ReservationRequest;
import roomescape.service.dto.WaitingRequest;
import roomescape.service.dto.WaitingResponse;

@Service
@Transactional(readOnly = true)
public class WaitingService {

    private final WaitingRepository waitingRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final Clock clock;

    public WaitingService(WaitingRepository waitingRepository, ReservationTimeRepository reservationTimeRepository,
                          ThemeRepository themeRepository, Clock clock) {
        this.waitingRepository = waitingRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.clock = clock;
    }

    @Transactional
    public WaitingResponse save(WaitingRequest waitingRequest, Member member) {
        ReservationTime reservationTime = findReservationTimeById(waitingRequest.getTimeId());
        Theme theme = findThemeById(waitingRequest.getThemeId());
        Waiting waiting = waitingRequest.toWaiting(member, reservationTime, theme);

        validateDuplicateWaiting(waitingRequest);
        validateDateTimeWaiting(waitingRequest, reservationTime);

        Waiting savedWaiting = waitingRepository.save(waiting);
        return new WaitingResponse(savedWaiting);
    }

    @Transactional
    public void delete(Long waitingId) {
        Waiting waiting = findWaitingById(waitingId);
        waitingRepository.delete(waiting);
    }

    private void validateDuplicateWaiting(WaitingRequest request) {
        if (waitingRepository.existsByDateAndTimeIdAndThemeId(
                request.getDate(), request.getTimeId(), request.getThemeId())) {
            throw new DuplicatedReservationException();
        }
    }

    private void validateDateTimeWaiting(WaitingRequest request, ReservationTime time) {
        LocalDateTime localDateTime = request.getDate().atTime(time.getStartAt());
        if (localDateTime.isBefore(LocalDateTime.now(clock))) {
            throw new InvalidDateTimeReservationException();
        }
    }

    private Waiting findWaitingById(long id) {
        return waitingRepository.findById(id)
                .orElseThrow(NotFoundReservationException::new);
    }

    private ReservationTime findReservationTimeById(long id) {
        return reservationTimeRepository.findById(id)
                .orElseThrow(NotFoundTimeException::new);
    }

    private Theme findThemeById(long id) {
        return themeRepository.findById(id)
                .orElseThrow(NotFoundThemeException::new);
    }
}
