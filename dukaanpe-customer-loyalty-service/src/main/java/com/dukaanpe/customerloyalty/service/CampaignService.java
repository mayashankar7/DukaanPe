package com.dukaanpe.customerloyalty.service;

import com.dukaanpe.customerloyalty.dto.CampaignResponse;
import com.dukaanpe.customerloyalty.dto.CustomerResponse;
import com.dukaanpe.customerloyalty.dto.CreateCampaignRequest;
import com.dukaanpe.customerloyalty.dto.EligibleCustomerCountResponse;
import com.dukaanpe.customerloyalty.dto.PagedResponse;
import com.dukaanpe.customerloyalty.dto.UpdateCampaignRequest;
import java.util.List;

public interface CampaignService {

    CampaignResponse createCampaign(CreateCampaignRequest request);

    PagedResponse<CampaignResponse> listCampaigns(Long storeId, int page, int size);

    CampaignResponse getCampaign(Long id);

    CampaignResponse updateCampaign(Long id, UpdateCampaignRequest request);

    void deactivateCampaign(Long id);

    List<CampaignResponse> listActiveCampaigns(Long storeId);

    PagedResponse<CustomerResponse> previewEligibleCustomers(Long id, int page, int size);

    EligibleCustomerCountResponse countEligibleCustomers(Long id);
}

