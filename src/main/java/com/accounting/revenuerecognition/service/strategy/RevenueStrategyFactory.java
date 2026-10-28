package com.accounting.revenuerecognition.service.strategy;

import com.accounting.revenuerecognition.domain.enums.PricingModel;
import com.accounting.revenuerecognition.exception.InvalidRevenueOperationException;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class RevenueStrategyFactory {

    private final Map<PricingModel, RevenueRecognitionStrategy> strategyMap = new EnumMap<>(PricingModel.class);

    public RevenueStrategyFactory(List<RevenueRecognitionStrategy> strategies) {
        for (RevenueRecognitionStrategy strategy : strategies) {
            strategyMap.put(strategy.getSupportedModel(), strategy);
        }
    }

    public RevenueRecognitionStrategy getStrategy(PricingModel pricingModel) {
        RevenueRecognitionStrategy strategy = strategyMap.get(pricingModel);
        if (strategy == null) {
            throw new InvalidRevenueOperationException("No revenue recognition strategy found for pricing model: " + pricingModel);
        }
        return strategy;
    }
}
