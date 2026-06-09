import { readFileSync, existsSync } from 'fs';
import { join } from 'path';
import { fileURLToPath } from 'url';

const root = fileURLToPath(new URL('../..', import.meta.url));

const docs = [
  {
    path: 'docs/superpowers/specs/2026-06-09-admin-figma-prototype.md',
    checks: ['Figma Prototype', 'AdminPageShell', 'Dashboard', 'CRUD Pages', 'Frames'],
  },
  {
    path: 'docs/superpowers/specs/2026-06-09-admin-backend-interface-adaptation.md',
    checks: ['Backend Interface Adaptation', 'Pagination Contract', 'Status Enum', 'RBAC', 'AuthConstant.PERMIT_ALL_URLS'],
  },
  {
    path: 'docs/superpowers/plans/2026-06-09-admin-frontend-rollout-summary.md',
    checks: ['Frontend Rollout Summary', 'Verification', 'Remaining Work', 'Files Changed'],
  },
];

function assert(condition, message) {
  if (!condition) {
    throw new Error(message);
  }
}

for (const doc of docs) {
  const fullPath = join(root, doc.path);
  assert(existsSync(fullPath), `${doc.path} should exist`);
  const source = readFileSync(fullPath, 'utf8');
  for (const copy of doc.checks) {
    assert(source.includes(copy), `${doc.path} should include ${copy}`);
  }
}

console.log('Admin handoff document structure checks passed');
