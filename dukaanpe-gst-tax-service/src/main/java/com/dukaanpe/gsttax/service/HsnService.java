package com.dukaanpe.gsttax.service;

import com.dukaanpe.gsttax.dto.CreateHsnRequest;
import com.dukaanpe.gsttax.dto.HsnResponse;
import com.dukaanpe.gsttax.dto.PagedResponse;
import com.dukaanpe.gsttax.dto.UpdateHsnRequest;

public interface HsnService {

    HsnResponse create(CreateHsnRequest request);

    HsnResponse getById(Long id);

    PagedResponse<HsnResponse> list(String hsnCode, String description, int page, int size);

    HsnResponse update(Long id, UpdateHsnRequest request);

    void delete(Long id);
}

