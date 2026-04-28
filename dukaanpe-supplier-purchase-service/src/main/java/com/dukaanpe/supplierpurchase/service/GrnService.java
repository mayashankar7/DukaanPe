package com.dukaanpe.supplierpurchase.service;

import com.dukaanpe.supplierpurchase.dto.GrnRequest;
import com.dukaanpe.supplierpurchase.dto.GrnResponse;
import java.util.List;

public interface GrnService {

    GrnResponse createGrn(GrnRequest request);

    List<GrnResponse> listGrns(Long storeId);

    GrnResponse getGrn(Long id);

    GrnResponse verifyGrn(Long id);

    GrnResponse approveGrn(Long id);
}

