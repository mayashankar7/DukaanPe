package com.dukaanpe.gsttax.service;

import com.dukaanpe.gsttax.dto.GstReturnResponse;
import com.dukaanpe.gsttax.dto.PrepareGstReturnRequest;

public interface GstReturnService {

    GstReturnResponse prepare(PrepareGstReturnRequest request);

    GstReturnResponse getById(Long id);
}

