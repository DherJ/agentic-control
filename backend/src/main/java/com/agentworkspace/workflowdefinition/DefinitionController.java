package com.agentworkspace.workflowdefinition;

import java.time.OffsetDateTime;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/v1/workflow-definitions")
public class DefinitionController {
    private final WorkflowDefinitionRepository definitions;
    private final StepDefinitionRepository steps;
    private final ObjectMapper mapper;
    public DefinitionController(WorkflowDefinitionRepository definitions,StepDefinitionRepository steps,ObjectMapper mapper){this.definitions=definitions;this.steps=steps;this.mapper=mapper;}
    public record Summary(UUID id,String key,String version,String name,String checksum,OffsetDateTime publishedAt) {}
    public record Detail(Summary definition,Object document) {}
    public record StepView(UUID id,String key,String phase,String kind,Object config) {}
    private Summary summary(WorkflowDefinition d){return new Summary(d.getId(),d.getDefinitionKey(),d.getDefinitionVersion(),d.getName(),d.getChecksum(),d.getPublishedAt());}
    private WorkflowDefinition find(UUID id){return definitions.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Workflow definition not found"));}
    @GetMapping public List<Summary> list(){return definitions.findAll().stream().map(this::summary).sorted(Comparator.comparing(Summary::key).thenComparing(Summary::version)).toList();}
    @GetMapping("/{id}") public Detail get(@PathVariable UUID id){var d=find(id);return new Detail(summary(d),mapper.readTree(d.getDefinitionJson()));}
    @GetMapping("/{id}/steps") public List<StepView> steps(@PathVariable UUID id){find(id);return steps.findByWorkflowDefinitionIdOrderByStepKey(id).stream().map(s->new StepView(s.getId(),s.getStepKey(),s.getPhaseKey(),s.getKind(),mapper.readTree(s.getConfigJson()))).toList();}
}
