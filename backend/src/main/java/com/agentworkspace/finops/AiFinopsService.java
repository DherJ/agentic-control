package com.agentworkspace.finops;

import com.agentworkspace.agent.AgentExecutionResult;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AiFinopsService {
 private static final BigDecimal MILLION = new BigDecimal("1000000");
 private final JdbcTemplate db;
 private final GlobalFinopsService global;
 private final String model;
 private final BigDecimal inputRate, outputRate;
 public AiFinopsService(JdbcTemplate db, GlobalFinopsService global, @Value("${app.openai.model:gpt-4.1-mini}") String model,
  @Value("${app.openai.price-input-usd-per-million:0}") BigDecimal inputRate,
  @Value("${app.openai.price-output-usd-per-million:0}") BigDecimal outputRate) {
  this.db=db;this.global=global;this.model=model;this.inputRate=inputRate;this.outputRate=outputRate;
 }
 public String model(){return model;}
 public record Budget(BigDecimal budgetUsd,BigDecimal requestLimitUsd,int alertPercent,BigDecimal spentUsd,BigDecimal reservedUsd,BigDecimal remainingUsd,boolean alert){}
 public record Usage(UUID id,UUID projectId,UUID workflowId,String requestText,String agentType,String model,long inputTokens,long cachedInputTokens,long outputTokens,BigDecimal costUsd,String status,String createdAt){}
 public record Summary(Budget budget,List<Usage> usage,List<Usage> topFive,long totalInputTokens,long totalOutputTokens){}
 private void ensure(UUID projectId){db.update("INSERT INTO agent_workspace.ai_project_budget(project_id) VALUES (?) ON CONFLICT DO NOTHING",projectId);}
 private BigDecimal spent(UUID id){return db.queryForObject("SELECT COALESCE(SUM(cost_usd),0) FROM agent_workspace.ai_usage WHERE project_id=?",BigDecimal.class,id);}
 private BigDecimal round(BigDecimal n){return n.setScale(6,RoundingMode.HALF_UP);}
 private BigDecimal cost(long in,long out){return round(inputRate.multiply(BigDecimal.valueOf(in)).add(outputRate.multiply(BigDecimal.valueOf(out))).divide(MILLION,12,RoundingMode.HALF_UP));}
 @Transactional public UUID reserve(UUID projectId,UUID workflowId,UUID stepId,String request){
  if(inputRate.signum()<=0||outputRate.signum()<=0)throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"Tarifs du modèle non configurés");
  global.lock(); // lock order: global, then project; serializes all reservations
  ensure(projectId);
  var rows=db.queryForList("SELECT budget_usd,request_limit_usd,reserved_usd FROM agent_workspace.ai_project_budget WHERE project_id=? FOR UPDATE",projectId);
  var row=rows.getFirst();
  // Conservative upper bound for the configured analyst: max 20k chars input, 1600 output tokens.
  // Pricing is still an estimate; providers may count extra prompt or reasoning tokens.
  long estimatedInput=Math.max(2000,request.length()*2L+3000L);
  BigDecimal estimate=cost(estimatedInput,1600);
  global.assertAvailable(estimate);
  BigDecimal limit=(BigDecimal)row.get("request_limit_usd");
  BigDecimal remaining=((BigDecimal)row.get("budget_usd")).subtract(spent(projectId)).subtract((BigDecimal)row.get("reserved_usd"));
  if(estimate.compareTo(limit)>0||estimate.compareTo(remaining)>0)
   throw new ResponseStatusException(HttpStatus.CONFLICT,"Budget insuffisant ou plafond par demande dépassé (réservation estimée "+estimate+" USD)");
  UUID id=UUID.randomUUID();
  try {
   db.update("INSERT INTO agent_workspace.ai_usage(id,project_id,workflow_id,step_id,request_text,agent_type,model,input_usd_per_million,output_usd_per_million,reserved_usd,status) VALUES (?,?,?,?,?,'ANALYST',?,?,?,?,'RESERVED')",id,projectId,workflowId,stepId,request,model,inputRate,outputRate,estimate);
  }catch(DuplicateKeyException e){throw new ResponseStatusException(HttpStatus.CONFLICT,"Un appel IA est déjà en cours sur cette étape");}
  db.update("UPDATE agent_workspace.ai_project_budget SET reserved_usd=reserved_usd+?,updated_at=NOW() WHERE project_id=?",estimate,projectId);
  return id;
 }
 @Transactional public void finish(UUID id,AgentExecutionResult result){
  var rows=db.queryForList("SELECT project_id,reserved_usd FROM agent_workspace.ai_usage WHERE id=? AND status='RESERVED' FOR UPDATE",id);
  if(rows.isEmpty())return;
  var row=rows.getFirst();UUID projectId=(UUID)row.get("project_id");BigDecimal reserved=(BigDecimal)row.get("reserved_usd");
  long in=Math.max(0,result.inputTokens()),out=Math.max(0,result.outputTokens());
  BigDecimal actual=cost(in,out);
  db.update("UPDATE agent_workspace.ai_usage SET input_tokens=?,cached_input_tokens=?,output_tokens=?,cost_usd=?,reserved_usd=0,status=?,error_message=?,completed_at=NOW() WHERE id=?",in,Math.max(0,result.cachedInputTokens()),out,actual,result.success()?"COMPLETED":"FAILED",result.errorMessage(),id);
  db.update("UPDATE agent_workspace.ai_project_budget SET reserved_usd=GREATEST(0,reserved_usd-?),updated_at=NOW() WHERE project_id=?",reserved,projectId);
 }
 @Transactional public Budget budget(UUID id){ensure(id);var row=db.queryForMap("SELECT budget_usd,request_limit_usd,alert_percent,reserved_usd FROM agent_workspace.ai_project_budget WHERE project_id=?",id);
  BigDecimal total=(BigDecimal)row.get("budget_usd"),used=spent(id),reserved=(BigDecimal)row.get("reserved_usd");int threshold=((Number)row.get("alert_percent")).intValue();
  return new Budget(total,(BigDecimal)row.get("request_limit_usd"),threshold,used,reserved,total.subtract(used).subtract(reserved),used.add(reserved).multiply(BigDecimal.valueOf(100)).compareTo(total.multiply(BigDecimal.valueOf(threshold)))>=0);
 }
 @Transactional public Budget update(UUID id,BigDecimal total,BigDecimal perRequest,int alert){
  if(total==null||perRequest==null||total.signum()<=0||perRequest.signum()<=0||perRequest.compareTo(total)>0||alert<1||alert>100)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Budget ou seuil invalide");
  ensure(id);db.update("UPDATE agent_workspace.ai_project_budget SET budget_usd=?,request_limit_usd=?,alert_percent=?,updated_at=NOW() WHERE project_id=?",total,perRequest,alert,id);return budget(id);
 }
 public Summary summary(UUID id,String modelFilter,String statusFilter,String search){
  var b=budget(id);StringBuilder sql=new StringBuilder("SELECT id,project_id,workflow_id,request_text,agent_type,model,input_tokens,cached_input_tokens,output_tokens,cost_usd,status,created_at::text AS created_at FROM agent_workspace.ai_usage WHERE project_id=?");
  List<Object> params=new ArrayList<>();params.add(id);
  if(modelFilter!=null&&!modelFilter.isBlank()){sql.append(" AND model=?");params.add(modelFilter);}
  if(statusFilter!=null&&!statusFilter.isBlank()){sql.append(" AND status=?");params.add(statusFilter);}
  if(search!=null&&!search.isBlank()){sql.append(" AND request_text ILIKE ?");params.add("%"+search+"%");}
  var all=db.query(sql+" ORDER BY created_at DESC LIMIT 500",(rs,n)->new Usage((UUID)rs.getObject("id"),(UUID)rs.getObject("project_id"),(UUID)rs.getObject("workflow_id"),rs.getString("request_text"),rs.getString("agent_type"),rs.getString("model"),rs.getLong("input_tokens"),rs.getLong("cached_input_tokens"),rs.getLong("output_tokens"),rs.getBigDecimal("cost_usd"),rs.getString("status"),rs.getString("created_at")),params.toArray());
  var top=db.query(sql+" ORDER BY cost_usd DESC, created_at DESC LIMIT 5",(rs,n)->new Usage((UUID)rs.getObject("id"),(UUID)rs.getObject("project_id"),(UUID)rs.getObject("workflow_id"),rs.getString("request_text"),rs.getString("agent_type"),rs.getString("model"),rs.getLong("input_tokens"),rs.getLong("cached_input_tokens"),rs.getLong("output_tokens"),rs.getBigDecimal("cost_usd"),rs.getString("status"),rs.getString("created_at")),params.toArray());
  // Totals cover all matching rows, not only the paginated list.
  var sums=db.queryForMap("SELECT COALESCE(SUM(input_tokens),0) AS input_tokens,COALESCE(SUM(output_tokens),0) AS output_tokens FROM ("+sql+") q",params.toArray());
  return new Summary(b,all,top,((Number)sums.get("input_tokens")).longValue(),((Number)sums.get("output_tokens")).longValue());
 }
}
