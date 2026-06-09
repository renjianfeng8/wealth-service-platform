import { readFileSync, existsSync } from 'fs';
import { join } from 'path';
import { fileURLToPath } from 'url';

const root = fileURLToPath(new URL('../..', import.meta.url));

function assert(condition, message) {
  if (!condition) {
    throw new Error(message);
  }
}

function read(path) {
  const fullPath = join(root, path);
  assert(existsSync(fullPath), `${path} should exist`);
  return readFileSync(fullPath, 'utf8');
}

const docsIndex = read('docs/README.md');
const governance = read('docs/DOCUMENTATION-GOVERNANCE.md');
const readme = read('README.md');
const changelog = read('docs/CHANGELOG.md');

for (const copy of [
  '文档总索引',
  '核心规范',
  '启动与部署',
  '架构与数据',
  'Superpowers 交付物',
  '文档治理规则',
]) {
  assert(docsIndex.includes(copy), `docs/README.md should include ${copy}`);
}

for (const copy of [
  '文档治理规则',
  '权威来源',
  '文档分类',
  '命名规则',
  '归档规则',
  '更新检查清单',
]) {
  assert(governance.includes(copy), `DOCUMENTATION-GOVERNANCE.md should include ${copy}`);
}

assert(readme.includes('docs/README.md'), 'README should link to docs index');
assert(readme.includes('DOCUMENTATION-GOVERNANCE.md'), 'README should link to documentation governance');
assert(changelog.includes('2026-06-09'), 'CHANGELOG should record the documentation governance date');
assert(changelog.includes('文档治理'), 'CHANGELOG should record documentation governance');

console.log('Documentation governance structure checks passed');
