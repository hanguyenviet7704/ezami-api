package com.hth.udecareer.mapper;

import com.hth.udecareer.entities.SupportLog;
import com.hth.udecareer.model.request.SupportLogRequest;
import com.hth.udecareer.model.response.SupportResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SupportMapper {

    SupportLog toSupportLog(SupportLogRequest request);

    @Mapping(target = "imageUrls", ignore = true)
    SupportResponse toSupportResponse(SupportLog supportLog);
}
