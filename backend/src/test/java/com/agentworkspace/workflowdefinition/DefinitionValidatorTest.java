package com.agentworkspace.workflowdefinition;
import static org.junit.jupiter.api.Assertions.*;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
class DefinitionValidatorTest {
    @SuppressWarnings("unchecked")
    private Map<String,Object> definition() {
        var in=getClass().getResourceAsStream("/workflow-definitions/bmad-standard-v1.yaml");
        return (Map<String,Object>)new Yaml(new SafeConstructor(new LoaderOptions())).load(new InputStreamReader(Objects.requireNonNull(in),StandardCharsets.UTF_8));
    }
    @Test void validBmadWithUxApproval() {
        var parsed=new DefinitionValidator().validate(definition());
        assertEquals(11,parsed.steps().size());
        assertEquals("approve-ux",parsed.steps().get(3).key());
    }
    @Test void rejectsMissingUxApproval() {
        var d=definition();
        var phases=(List<Map<String,Object>>)d.get("phases");
        var planning=(List<Map<String,Object>>)phases.get(1).get("steps");
        planning.removeIf(s->"approve-ux".equals(s.get("id")));
        assertThrows(IllegalArgumentException.class,()->new DefinitionValidator().validate(d));
    }
    @Test void rejectsMoreThanThreeMockups() {
        var d=definition();
        var phases=(List<Map<String,Object>>)d.get("phases");
        var planning=(List<Map<String,Object>>)phases.get(1).get("steps");
        planning.stream().filter(s->"create-ux-mockups".equals(s.get("id"))).findFirst().orElseThrow().put("maxProposals",4);
        assertThrows(IllegalArgumentException.class,()->new DefinitionValidator().validate(d));
    }
    @Test void rejectsDuplicateSteps() {
        var d=definition();var phases=(List<Map<String,Object>>)d.get("phases");
        var planning=(List<Map<String,Object>>)phases.get(1).get("steps");planning.add(new LinkedHashMap<>(planning.get(0)));
        assertThrows(IllegalArgumentException.class,()->new DefinitionValidator().validate(d));
    }
}
