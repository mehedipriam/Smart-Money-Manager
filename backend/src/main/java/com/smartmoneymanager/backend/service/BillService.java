package com.smartmoneymanager.backend.service;

import java.util.List;

import com.smartmoneymanager.backend.dto.request.CreateBillRequest;
import com.smartmoneymanager.backend.dto.request.UpdateBillRequest;
import com.smartmoneymanager.backend.dto.response.BillResponse;
import com.smartmoneymanager.backend.entity.enums.BillPaymentStatus;

public interface BillService {

    List<BillResponse> getBills(Long userId, BillPaymentStatus status);

    /** Unpaid bills (PENDING or OVERDUE), soonest due date first — used for the dashboard's upcoming-bills widget. */
    List<BillResponse> getUpcomingBills(Long userId, int limit);

    BillResponse getBill(Long userId, Long billId);

    BillResponse createBill(Long userId, CreateBillRequest request);

    BillResponse updateBill(Long userId, Long billId, UpdateBillRequest request);

    void deleteBill(Long userId, Long billId);

    BillResponse markAsPaid(Long userId, Long billId);
}
