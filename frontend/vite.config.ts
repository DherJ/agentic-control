import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
export default defineConfig({plugins:[react()],base:'/agent-workspace/',server:{port:5173,proxy:{'/agent-workspace/api':{target:'http://localhost:8080',rewrite:(p)=>p.replace(/^\/agent-workspace/,'')},'/agent-workspace/login':{target:'http://localhost:8080',rewrite:(p)=>p.replace(/^\/agent-workspace/,'')},'/agent-workspace/logout':{target:'http://localhost:8080',rewrite:(p)=>p.replace(/^\/agent-workspace/,'')}}}});
