package com.agentworkspace.service;
import com.agentworkspace.domain.WorkflowStatus;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class WorkflowTransitionPolicyTest {
    private final WorkflowTransitionPolicy policy = new WorkflowTransitionPolicy();
    @Test void draftCanStart() { assertTrue(policy.canTransition(WorkflowStatus.DRAFT, WorkflowStatus.RUNNING)); }
    @Test void approvalCannotBeBypassedWithCompletion() { assertFalse(policy.canTransition(WorkflowStatus.WAITING_APPROVAL, WorkflowStatus.COMPLETED)); }
    @Test void rejectionMayResumeExecution() { assertTrue(policy.canTransition(WorkflowStatus.WAITING_APPROVAL, WorkflowStatus.RUNNING)); }
    @Test void completedCannotBeCancelled() { assertFalse(policy.canTransition(WorkflowStatus.COMPLETED, WorkflowStatus.CANCELLED)); }
    @Test void cancelledCannotRestart() { assertFalse(policy.canTransition(WorkflowStatus.CANCELLED, WorkflowStatus.RUNNING)); }
    @Test void cannotTransitionToSameState() { assertFalse(policy.canTransition(WorkflowStatus.RUNNING, WorkflowStatus.RUNNING)); }
}
