package com.agentworkspace.approval;
import com.agentworkspace.workflow.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;
@Service
public class ArtifactService {
 private final ArtifactRepository artifacts;private final WorkflowEngine engine;private final ObjectMapper mapper;
 public ArtifactService(ArtifactRepository artifacts,WorkflowEngine engine,ObjectMapper mapper){this.artifacts=artifacts;this.engine=engine;this.mapper=mapper;}
 @Transactional(readOnly=true) public List<Artifact> list(UUID workflowId,String username){engine.authorizedWorkflow(workflowId,username);return artifacts.findByWorkflowIdOrderByCreatedAtAsc(workflowId);}
 @Transactional(readOnly=true) public Artifact get(UUID id,String username){Artifact a=artifacts.findById(id).orElseThrow(()->new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND));engine.authorizedWorkflow(a.getWorkflowId(),username);return a;}
 @Transactional(readOnly=true) public List<Artifact> versions(UUID workflowId,String type,String username){engine.authorizedWorkflow(workflowId,username);return artifacts.findByWorkflowIdAndTypeOrderByVersionDesc(workflowId,type);}
 // Internal only: agent completion adapter in 1E calls this after validating step ownership and output schema.
 @Transactional public Artifact record(UUID workflowId,UUID stepExecutionId,String type,String json,String username){
 engine.authorizedWorkflow(workflowId,username);
 try {var tree=mapper.readTree(json);if(tree==null||!tree.isObject())throw new IllegalArgumentException("Artifact must be a JSON object");
 String canonical=mapper.writeValueAsString(tree);String hash=HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(canonical.getBytes(StandardCharsets.UTF_8)));
 int version=artifacts.findByWorkflowIdAndTypeOrderByVersionDesc(workflowId,type).stream().findFirst().map(a->a.getVersion()+1).orElse(1);
 return artifacts.save(new Artifact(workflowId,stepExecutionId,type,version,canonical,hash));
 }catch(java.security.NoSuchAlgorithmException e){throw new IllegalStateException(e);}}
}
