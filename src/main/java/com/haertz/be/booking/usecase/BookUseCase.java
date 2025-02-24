package com.haertz.be.booking.usecase;

import com.haertz.be.booking.adaptor.BookingAdaptor;
import com.haertz.be.booking.adaptor.DesignerScheduleAdaptor;
import com.haertz.be.booking.converter.BookingConverter;
import com.haertz.be.booking.dto.request.BookingInfoRequest;
import com.haertz.be.booking.dto.response.BookingResponse;
import com.haertz.be.booking.entity.Booking;
import com.haertz.be.booking.entity.DesignerSchedule;
import com.haertz.be.booking.entity.MeetingType;
import com.haertz.be.booking.exception.BookingErrorCode;
import com.haertz.be.booking.service.BookingDomainService;
import com.haertz.be.common.annotation.UseCase;
import com.haertz.be.common.exception.base.BaseException;
import com.haertz.be.common.utils.AuthenticatedUserUtils;
import com.haertz.be.designer.adaptor.DesignerAdaptor;
import com.haertz.be.designer.entity.Designer;
import com.haertz.be.designer.entity.MeetingMode;
import com.haertz.be.designer.exception.DesignerErrorCode;
import lombok.RequiredArgsConstructor;


@UseCase
@RequiredArgsConstructor
public class BookUseCase {
    private final AuthenticatedUserUtils userUtils;
    private final BookingDomainService bookingDomainService;
    private final BookingAdaptor bookingAdaptor;
    private final DesignerAdaptor designerAdaptor;
    private final DesignerScheduleAdaptor designerScheduleAdaptor;

    public BookingResponse execute(BookingInfoRequest bookingInfo) {
        Long currentUserId = userUtils.getCurrentUserId();

         // DesignerSchedule에 등록 여부 검증
        if (!designerScheduleAdaptor.hasUserBookedSchedule(currentUserId, bookingInfo.designerScheduleId())) {
            throw new BaseException(BookingErrorCode.DESIGNER_SCHEDULE_NOT_FOUND);
        }

        // 결제 상태 검증
        DesignerSchedule designerSchedule = designerScheduleAdaptor.isPaymentStatusValid(bookingInfo.designerScheduleId());

        // 이미 예약된 스케쥴인지 검증
        if (bookingAdaptor.existsByDesignerScheduleId(bookingInfo.designerScheduleId())) {
            throw new BaseException(BookingErrorCode.BOOKING_ALREADY_REGISTERED);
        }

        Designer designer = designerAdaptor.findByDesignerId(designerSchedule.getDesignerId());

        Booking booking = bookingDomainService.book(bookingInfo, designer, currentUserId, designerSchedule);

        return BookingConverter.toBookingResponse(booking, designer.getDesignerName());
    }
}