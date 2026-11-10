package com.agentworkspace.workflowdefinition;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import tools.jackson.databind.ObjectMapper;

@Service
public class DefinitionImporter {
    private final DefinitionValidator validator;
    private final WorkflowDefinitionRepository definitions;
    private final StepDefinitionRepository steps;
    private final ObjectMapper mapper;
    private final Resource source;
    public DefinitionImporter(DefinitionValidator validator,WorkflowDefinitionRepository definitions,
            StepDefinitionRepository steps,ObjectMapper mapper,
            @Value("classpath:workflow-definitions/bmad-standard-v1.yaml") Resource source) {
        this.validator=validator;this.definitions=definitions;this.steps=steps;this.mapper=mapper;this.source=source;
    }
    @Transactional
    public void importBundledDefinition() throws Exception {
        byte[] bytes;
        try(InputStream stream=source.getInputStream()){bytes=stream.readAllBytes();}
        LoaderOptions options=new LoaderOptions();options.setAllowDuplicateKeys(false);options.setMaxAliasesForCollections(0);
        Object raw=new Yaml(new SafeConstructor(options)).load(new String(bytes,StandardCharsets.UTF_8));
        if(!(raw instanceof Map<?,?> rawMap))throw new IllegalArgumentException("Workflow YAML must be a map");
        Map<String,Object> doc=new LinkedHashMap<>();
        for(var e:rawMap.entrySet()) {if(!(e.getKey() instanceof String))throw new IllegalArgumentException("Invalid YAML key");doc.put((String)e.getKey(),e.getValue());}
        var parsed=validator.validate(doc);
        byte[] digest=MessageDigest.getInstance("SHA-256").digest(bytes);
        String checksum=HexFormat.of().formatHex(digest);
        var existing=definitions.findByDefinitionKeyAndDefinitionVersion(parsed.key(),parsed.version());
        if(existing.isPresent()) {
            if(!existing.get().getChecksum().equals(checksum)) throw new IllegalStateException("Published definition checksum mismatch: "+parsed.key()+":"+parsed.version());
            return;
        }
        WorkflowDefinition definition=definitions.save(new WorkflowDefinition(parsed.key(),parsed.version(),parsed.name(),mapper.writeValueAsString(parsed.document()),checksum));
        for(var step:parsed.steps())steps.save(new StepDefinition(definition,step.key(),step.phase(),step.kind(),mapper.writeValueAsString(step.config())));
    }
}
