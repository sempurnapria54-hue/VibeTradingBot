package com.example.vibetradingbot.exchange.connector;

import java.util.List;
import java.util.Optional;

import com.example.vibetradingbot.domain.model.CancelAck;
import com.example.vibetradingbot.domain.model.CancelOrderCommand;
import com.example.vibetradingbot.domain.model.ModifyAck;
import com.example.vibetradingbot.domain.model.ModifyOrderCommand;
import com.example.vibetradingbot.domain.model.NewOrder;
import com.example.vibetradingbot.domain.model.OrderAck;
import com.example.vibetradingbot.domain.model.OrderSnapshot;
import com.example.vibetradingbot.domain.model.PositionSnapshot;

/**
 * Унифицированный адаптер исполнения заявок.
 */
public interface ExecutionAdapter {

    OrderAck createOrder(NewOrder newOrder);

    CancelAck cancelOrder(CancelOrderCommand cancelOrderCommand);

    ModifyAck modifyOrder(ModifyOrderCommand modifyOrderCommand);

    Optional<OrderSnapshot> getOrder(String clientOrderId, String exchangeOrderId);

    List<OrderSnapshot> listOpenOrders(Long exchangeInstrumentId);

    List<PositionSnapshot> getPositions();
}
