package com.agentworkspace.service;
import com.agentworkspace.domain.WorkflowStatus;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.Set;
@Component
public class WorkflowTransitionPolicy {
    private static final Map<WorkflowStatus, Set<WorkflowStatus>> ALLOWED = Map.of(
        WorkflowStatus.DRAFT, Set.of(WorkflowStatus.RUNNING),
        WorkflowStatus.RUNNING, Set.of(WorkflowStatus.WAITING_APPROVAL, WorkflowStatus.BLOCKED, WorkflowStatus.FAILED, WorkflowStatus.COMPLETED),
        WorkflowStatus.WAITING_APPROVAL, Set.of(WorkflowStatus.RUNNING, WorkflowStatus.BLOCKED),
        WorkflowStatus.BLOCKED, Set.of(WorkflowStatus.RUNNING, WorkflowStatus.WAITING_APPROVAL, WorkflowStatus.FAILED),
        WorkflowStatus.FAILED, Set.of(WorkflowStatus.RUNNING, WorkflowStatus.BLOCKED)
    );
    public boolean canTransition(WorkflowStatus from, WorkflowStatus to) {
        if (from == null || to == null || from == to) return false;
        if (to == WorkflowStatus.CANCELLED) return from != WorkflowStatus.COMPLETED && from != WorkflowStatus.CANCELLED;
        return ALLOWED.getOrDefault(from, Set.of()).contains(to);
    }
    public void assertAllowed(WorkflowStatus from, WorkflowStatus to) {
        if (!canTransition(from, to)) throw new IllegalStateException("Transition interdite: " + from + " -> " + to);
    }
}
