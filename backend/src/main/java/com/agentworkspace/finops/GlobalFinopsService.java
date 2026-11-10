package com.agentworkspace.finops;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class GlobalFinopsService {
 private final JdbcTemplate db;
 public GlobalFinopsService(JdbcTemplate db){this.db=db;}
 public record Settings(BigDecimal monthlyBudgetUsd,int alertPercent,boolean enforceLimit){}
 public record ProjectSpend(UUID projectId,String projectName,BigDecimal spentUsd,BigDecimal reservedUsd){}
 public record Summary(Settings settings,String month,BigDecimal spentUsd,BigDecimal reservedUsd,BigDecimal remainingUsd,boolean alert,List<ProjectSpend> projects){}
 // The singleton row is a serialization lock for reservations across ALL projects.
 public void lock(){db.queryForObject("SELECT id FROM agent_workspace.ai_billing_settings WHERE id=1 FOR UPDATE",Integer.class);}
 private String periodSql(){return "date_trunc('month',NOW() AT TIME ZONE 'Europe/Paris')";}
 private String usageMonth(){return "date_trunc('month',created_at AT TIME ZONE 'Europe/Paris') = "+periodSql();}
 public BigDecimal spent(){return db.queryForObject("SELECT COALESCE(SUM(cost_usd),0) FROM agent_workspace.ai_usage WHERE "+usageMonth(),BigDecimal.class);}
 public BigDecimal reserved(){return db.queryForObject("SELECT COALESCE(SUM(reserved_usd),0) FROM agent_workspace.ai_usage WHERE status='RESERVED' AND "+usageMonth(),BigDecimal.class);}
 public Settings settings(){return db.queryForObject("SELECT monthly_budget_usd,alert_percent,enforce_limit FROM agent_workspace.ai_billing_settings WHERE id=1",(r,n)->new Settings(r.getBigDecimal(1),r.getInt(2),r.getBoolean(3)));}
 public void assertAvailable(BigDecimal estimate){
  Settings s=settings();if(s.enforceLimit()&&spent().add(reserved()).add(estimate).compareTo(s.monthlyBudgetUsd())>0)
   throw new ResponseStatusException(HttpStatus.CONFLICT,"Plafond mensuel global IA atteint ou réservation insuffisante");
 }
 public Summary summary(){
  Settings s=settings();BigDecimal used=spent(),held=reserved();
  String month=db.queryForObject("SELECT to_char(NOW() AT TIME ZONE 'Europe/Paris','YYYY-MM')",String.class);
  List<ProjectSpend> projects=db.query("SELECT p.id,p.name,COALESCE(SUM(u.cost_usd),0) spent,COALESCE(SUM(CASE WHEN u.status='RESERVED' THEN u.reserved_usd ELSE 0 END),0) reserved FROM agent_workspace.project p LEFT JOIN agent_workspace.ai_usage u ON u.project_id=p.id AND "+usageMonth().replace("created_at","u.created_at")+" GROUP BY p.id,p.name ORDER BY spent DESC",(r,n)->new ProjectSpend((UUID)r.getObject(1),r.getString(2),r.getBigDecimal(3),r.getBigDecimal(4)));
  boolean alert=used.add(held).multiply(BigDecimal.valueOf(100)).compareTo(s.monthlyBudgetUsd().multiply(BigDecimal.valueOf(s.alertPercent())))>=0;
  return new Summary(s,month,used,held,s.monthlyBudgetUsd().subtract(used).subtract(held),alert,projects);
 }
 @Transactional public Settings update(BigDecimal budget,Integer alert,Boolean enforce){
  if(budget==null||budget.signum()<=0||alert==null||alert<1||alert>100||enforce==null)
   throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Paramètres FinOps invalides");
  lock();
  if(enforce&&budget.compareTo(spent().add(reserved()))<0)
   throw new ResponseStatusException(HttpStatus.CONFLICT,"Le nouveau plafond est inférieur aux dépenses et réservations du mois");
  db.update("UPDATE agent_workspace.ai_billing_settings SET monthly_budget_usd=?,alert_percent=?,enforce_limit=?,updated_at=NOW() WHERE id=1",budget,alert,enforce);
  return settings();
 }
}
