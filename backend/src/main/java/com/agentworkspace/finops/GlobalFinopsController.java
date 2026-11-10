package com.agentworkspace.finops;

import java.math.BigDecimal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/finops")
public class GlobalFinopsController {
 private final GlobalFinopsService service;
 public GlobalFinopsController(GlobalFinopsService service){this.service=service;}
 @GetMapping("/monthly-summary") public GlobalFinopsService.Summary summary(){return service.summary();}
 @GetMapping("/settings") public GlobalFinopsService.Settings settings(){return service.settings();}
 public record Update(BigDecimal monthlyBudgetUsd,Integer alertPercent,Boolean enforceLimit){}
 @PutMapping("/settings") public GlobalFinopsService.Settings update(@RequestBody Update body){return service.update(body.monthlyBudgetUsd(),body.alertPercent(),body.enforceLimit());}
}
