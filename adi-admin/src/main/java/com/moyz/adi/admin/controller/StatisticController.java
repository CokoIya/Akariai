package com.moyz.adi.admin.controller;

import com.moyz.adi.common.dto.StatisticDto;
import com.moyz.adi.common.service.StatisticService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/statistic")
@Validated
@RequiredArgsConstructor
public class StatisticController {

    private final StatisticService statisticService;

    @GetMapping("/info")
    public StatisticDto statistic() {
        StatisticDto result = new StatisticDto();
        result.setKbStatistic(statisticService.calKbStat());
        result.setUserStatistic(statisticService.calUserStat());
        result.setTokenCostStatistic(statisticService.calTokenCostStat());
        result.setConvStatistic(statisticService.calConvStatistic());
        result.setImageCostStatistic(statisticService.calImageCostStat());
        return result;
    }
}
