package com.agentworkspace.workflowdefinition;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
@Component
public class DefinitionStartup implements ApplicationRunner {
    private final DefinitionImporter importer;
    public DefinitionStartup(DefinitionImporter importer){this.importer=importer;}
    @Override public void run(ApplicationArguments args) throws Exception { importer.importBundledDefinition(); }
}
