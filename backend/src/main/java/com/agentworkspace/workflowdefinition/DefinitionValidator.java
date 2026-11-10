package com.agentworkspace.workflowdefinition;

import java.util.*;
import org.springframework.stereotype.Component;

@Component
public class DefinitionValidator {
    public record Step(String key,String phase,String kind,Map<String,Object> config) {}
    public record Parsed(String key,String version,String name,Map<String,Object> document,List<Step> steps) {}
    private static final Set<String> PHASES=Set.of("analysis","planning","solutioning","implementation");
    private static final Set<String> KINDS=Set.of("AGENT","APPROVAL","HANDOFF");
    private static final Set<String> APPROVALS=Set.of("UX_DESIGN","FUNCTIONAL","TECHNICAL","READINESS");
    private static final Set<String> ARTIFACTS=Set.of("PROJECT_BRIEF","PRD","UX_MOCKUP_SET","ARCHITECTURE","EPICS_AND_STORIES","READINESS_REPORT");
    private static final Set<String> AGENTS=Set.of("analyst","product-manager","ux-designer","architect","scrum-master","reviewer");
    private static final Set<String> REQUIRED=Set.of("UX_DESIGN","FUNCTIONAL","TECHNICAL","READINESS");
    private static final List<String> PHASE_ORDER=List.of("analysis","planning","solutioning","implementation");

    public Parsed validate(Map<String,Object> document) {
        String key=required(document,"id"); String version=required(document,"version"); String name=required(document,"name");
        if(!key.matches("[a-z][a-z0-9-]{1,99}") || !version.matches("[0-9]+[.][0-9]+[.][0-9]+")) fail("Invalid id or semantic version");
        List<Map<String,Object>> phases=maps(document.get("phases"),"phases");
        if(phases.size()!=4) fail("Expected exactly four BMAD phases");
        List<Step> steps=new ArrayList<>(); Map<String,Integer> positions=new LinkedHashMap<>();
        Map<String,String> kinds=new HashMap<>(); Set<String> approvals=new HashSet<>();
        int index=0;
        for(int pi=0;pi<phases.size();pi++) {
            Map<String,Object> phase=phases.get(pi); String phaseKey=required(phase,"id");
            if(!PHASES.contains(phaseKey) || !PHASE_ORDER.get(pi).equals(phaseKey)) fail("Invalid phase order: "+phaseKey);
            for(Map<String,Object> step:maps(phase.get("steps"),"steps")) {
                String id=required(step,"id"); String kind=required(step,"kind");
                if(!id.matches("[a-z][a-z0-9-]{1,99}") || !KINDS.contains(kind)) fail("Invalid step: "+id);
                if(positions.putIfAbsent(id,index)!=null) fail("Duplicate step: "+id);
                kinds.put(id,kind); index++;
                if(kind.equals("AGENT")) {
                    if(!AGENTS.contains(required(step,"agent"))) fail("Unknown agent for "+id);
                    if(!ARTIFACTS.contains(required(step,"produces"))) fail("Unknown artifact for "+id);
                    if("UX_MOCKUP_SET".equals(step.get("produces"))) {
                        Object max=step.get("maxProposals");
                        if(!(max instanceof Number n) || n.intValue()<2 || n.intValue()>3 || n.doubleValue()!=n.intValue()) fail("UX proposals must be 2 or 3");
                    }
                } else if(kind.equals("APPROVAL")) {
                    String approval=required(step,"approvalType");
                    if(!APPROVALS.contains(approval) || !approvals.add(approval)) fail("Invalid or duplicate approval: "+approval);
                    if(!ARTIFACTS.contains(required(step,"artifact"))) fail("Invalid approval artifact: "+id);
                    required(step,"requires"); required(step,"onReject");
                }
                steps.add(new Step(id,phaseKey,kind,Map.copyOf(step)));
            }
        }
        if(!approvals.containsAll(REQUIRED)) fail("Missing mandatory approvals: "+REQUIRED);
        if(steps.isEmpty() || !steps.getLast().kind().equals("HANDOFF")) fail("Workflow must end with HANDOFF");
        for(Step step:steps) {
            String dep=optional(step.config(),"requires");
            if(dep!=null && (!positions.containsKey(dep) || positions.get(dep)>=positions.get(step.key()))) fail("Invalid or forward dependency: "+step.key());
            if(step.kind().equals("APPROVAL")) {
                String reject=required(step.config(),"onReject");
                if(!positions.containsKey(reject) || positions.get(reject)>=positions.get(step.key()) || !"AGENT".equals(kinds.get(reject))) fail("Invalid rejection target: "+step.key());
            }
        }
        checkOrder(steps,"approve-ux","approve-prd");
        checkOrder(steps,"approve-prd","approve-architecture");
        checkOrder(steps,"approve-architecture","approve-readiness");
        checkOrder(steps,"approve-readiness",steps.getLast().key());
        return new Parsed(key,version,name,document,List.copyOf(steps));
    }
    private static void checkOrder(List<Step> steps,String before,String after) {
        int a=-1,b=-1; for(int i=0;i<steps.size();i++){if(steps.get(i).key().equals(before))a=i;if(steps.get(i).key().equals(after))b=i;}
        if(a<0||b<0||a>=b) fail("Required approval order invalid: "+before+" -> "+after);
    }
    private static String required(Map<String,Object> m,String key) { Object v=m.get(key); if(!(v instanceof String s)||s.isBlank()) throw new IllegalArgumentException("Missing/invalid: "+key); return s; }
    private static String optional(Map<String,Object> m,String key) { Object v=m.get(key); if(v==null)return null; if(!(v instanceof String s)||s.isBlank())fail("Invalid: "+key); return (String)v; }
    private static List<Map<String,Object>> maps(Object v,String key) {
        if(!(v instanceof List<?> list)||list.isEmpty()) throw new IllegalArgumentException("Expected nonempty list: "+key);
        List<Map<String,Object>> out=new ArrayList<>();
        for(Object o:list) {if(!(o instanceof Map<?,?>))fail("Invalid object in "+key);
            Map<?,?> m=(Map<?,?>)o;
            Map<String,Object> converted=new LinkedHashMap<>(); for(var e:m.entrySet()){if(!(e.getKey() instanceof String))fail("Non-string key");converted.put((String)e.getKey(),e.getValue());}out.add(converted);}
        return out;
    }
    private static void fail(String message){throw new IllegalArgumentException(message);}
}
