package com.example.tradingbot.mapping;

import com.example.tradingbot.client.common.ClientCandle;
import com.example.tradingbot.client.common.ExchangeSymbol;
import com.example.tradingbot.domain.model.Candle;
import com.example.tradingbot.domain.model.ExchangeInstrument;
import com.example.tradingbot.domain.model.HistoryGroup;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR, nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface ClientToDomainMapper {

    default Candle toCandle(ClientCandle source, HistoryGroup historyGroup) {
        return Candle.create(
            historyGroup,
            source.getTimestamp(),
            source.getOpen(),
            source.getHigh(),
            source.getLow(),
            source.getClose(),
            source.getVolumeBase(),
            source.getVolumeQuote()
        );
    }

    default ExchangeInstrument toExchangeInstrument(ExchangeSymbol symbol, ExchangeInstrument template) {
        return ExchangeInstrument.create(
            template.exchange(),
            template.instrument(),
            symbol.getClientSymbol(),
            symbol.getContractType(),
            symbol.getMarginMode(),
            symbol.getLeverageMax()
        ).withId(template.id());
    }
}
