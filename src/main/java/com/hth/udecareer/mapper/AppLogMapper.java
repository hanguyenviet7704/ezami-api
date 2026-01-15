package com.hth.udecareer.mapper;

import com.hth.udecareer.entities.AppLogEntity;
import com.hth.udecareer.model.request.AppLogRequest;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AppLogMapper {
    AppLogEntity toAppLogEntity(AppLogRequest appLogRequest);
}
