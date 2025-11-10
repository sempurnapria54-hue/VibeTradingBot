package com.example.tradingbot.mapping;

import com.example.tradingbot.api.v1.dto.ResolveInstrumentRequest;
import com.example.tradingbot.domain.model.Instrument;

import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR, nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface ApiToDomainMapper {

    default Instrument toInstrument(ResolveInstrumentRequest request) {
        return Instrument.create(
            request.name(),
            request.base(),
            request.quote(),
            request.priceStep(),
            request.qtyStep(),
            request.minNotional(),
            request.perpetual()
        );
    }
}
