export type Project={id:string;name:string};
export type Definition={id:string;key:string;version:string;name:string};
export type Workflow={id:string;projectId:string;workflowDefinitionId:string;title:string;status:string;version:number};
export type Step={id:string;key:string;phase:string;kind:string;status:string;iteration:number;version:number};
export type Artifact={id:string;workflowId:string;type:string;version:number;content:string;contentHash:string};
export type Approval={id:string;workflowId:string;artifactId:string;type:string;status:string;version:number;comment:string|null};
export type Event={id:string;type:string;payload:string};
export type AiBudget={budgetUsd:number;requestLimitUsd:number;alertPercent:number;spentUsd:number;reservedUsd:number;remainingUsd:number;alert:boolean};
export type AiUsage={id:string;projectId:string;workflowId:string;requestText:string;agentType:string;model:string;inputTokens:number;cachedInputTokens:number;outputTokens:number;costUsd:number;status:string;createdAt:string};
export type AiCosts={budget:AiBudget;usage:AiUsage[];topFive:AiUsage[];totalInputTokens:number;totalOutputTokens:number};
export type GlobalSettings={monthlyBudgetUsd:number;alertPercent:number;enforceLimit:boolean};
export type GlobalMonthlySummary={settings:GlobalSettings;month:string;spentUsd:number;reservedUsd:number;remainingUsd:number;alert:boolean;projects:{projectId:string;projectName:string;spentUsd:number;reservedUsd:number}[]};
export type Session={username:string;authorities:string[]};
const BASE='/agent-workspace/api/v1';
let csrf: {token:string;headerName:string}|null=null;
export class ApiError extends Error {constructor(public status:number,message:string){super(message);}}
async function request<T>(path:string,options:RequestInit={}):Promise<T>{
 const method=(options.method||'GET').toUpperCase();
 if(!['GET','HEAD','OPTIONS'].includes(method)&&!csrf)csrf=await request('/auth/csrf');
 const response=await fetch(BASE+path,{...options,credentials:'include',headers:{...(options.body?{'Content-Type':'application/json'}:{}),...(!['GET','HEAD','OPTIONS'].includes(method)&&csrf?{[csrf.headerName]:csrf.token}:{}),...options.headers}});
 if(response.status===401)throw new ApiError(401,'Session expirée ou non authentifiée');
 if(!response.ok){let message=`Erreur HTTP ${response.status}`;try{const payload=await response.json();message=payload.detail||payload.message||payload.title||message;}catch{/* no JSON */}throw new ApiError(response.status,message);}
 if(response.status===204)return undefined as T;
 return response.json() as Promise<T>;
}
export const api={
 login:async (username:string,password:string):Promise<void>=>{
   if(!csrf)csrf=await request('/auth/csrf');
   const response=await fetch(BASE+'/auth/login',{method:'POST',credentials:'include',headers:{'Content-Type':'application/x-www-form-urlencoded',[csrf.headerName]:csrf.token},body:new URLSearchParams({username,password})});
   if(!response.ok)throw new ApiError(response.status,'Identifiants invalides ou connexion impossible');
   csrf=null;
 },
 session:()=>request<Session>('/auth/me'),
 logout:async()=>{await request<void>('/auth/logout',{method:'POST'});csrf=null;},
 globalFinops:()=>request<GlobalMonthlySummary>('/finops/monthly-summary'),
 globalFinopsSettings:()=>request<GlobalSettings>('/finops/settings'),
 updateGlobalFinops:(body:GlobalSettings)=>request<GlobalSettings>('/finops/settings',{method:'PUT',body:JSON.stringify(body)}),
 projects:()=>request<Project[]>('/projects'),
 aiCosts:(projectId:string,filters:{model?:string;status?:string;search?:string}={})=>request<AiCosts>(`/projects/${projectId}/ai-costs?${new URLSearchParams(Object.entries(filters).filter(([,v])=>!!v) as [string,string][]).toString()}`),
 updateAiBudget:(projectId:string,body:{budgetUsd:number;requestLimitUsd:number;alertPercent:number})=>request<AiBudget>(`/projects/${projectId}/ai-costs/budget`,{method:'PUT',body:JSON.stringify(body)}),
 definitions:()=>request<Definition[]>('/workflow-definitions'),
 workflows:()=>request<Workflow[]>('/workflows'),
 steps:(id:string)=>request<Step[]>(`/workflows/${id}/steps`),
 artifacts:(id:string)=>request<Artifact[]>(`/workflows/${id}/artifacts`),
 approvals:(id:string)=>request<Approval[]>(`/workflows/${id}/approvals`),
 events:(id:string)=>request<Event[]>(`/workflows/${id}/events`),
 createProject:(body:{name:string;description:string;githubRepository:string})=>request<Project>('/projects',{method:'POST',body:JSON.stringify(body)}),
 createWorkflow:(body:{projectId:string;workflowDefinitionKey:string;workflowDefinitionVersion:string;title:string;rawRequest:string})=>request<Workflow>('/workflows',{method:'POST',body:JSON.stringify(body)}),
 start:(id:string)=>request<Workflow>(`/workflows/${id}/start`,{method:'POST'}),
 cancel:(id:string)=>request<Workflow>(`/workflows/${id}/cancel`,{method:'POST'}),
 decide:(id:string,body:{decision:'APPROVED'|'REJECTED';approvalVersion:number;artifactId:string;comment:string;selectedProposalId?:string})=>request<Approval>(`/approvals/${id}/decision`,{method:'POST',body:JSON.stringify(body)}),
 runAnalyst:(workflowId:string,stepId:string)=>request<{artifactId:string;artifactType:string;version:number}>(`/workflows/${workflowId}/steps/${stepId}/run-analyst`,{method:'POST'}),
 simulate:(workflowId:string,stepId:string,content:Record<string,unknown>)=>request<{artifactId:string}>(`/dev/workflows/${workflowId}/steps/${stepId}/complete`,{method:'POST',body:JSON.stringify({content})})
};
