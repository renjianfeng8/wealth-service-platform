import { readFileSync } from 'fs';
import { join } from 'path';
import { fileURLToPath } from 'url';

const root = fileURLToPath(new URL('..', import.meta.url));
const router = readFileSync(join(root, 'src/router/index.ts'), 'utf8');
const types = readFileSync(join(root, 'src/types/index.ts'), 'utf8');
const format = readFileSync(join(root, 'src/utils/format.ts'), 'utf8');

function assert(condition, message) {
  if (!condition) {
    throw new Error(message);
  }
}

const expectedRouterCopy = [
  '\u9996\u9875',
  '\u4ea7\u54c1\u4e2d\u5fc3',
  '\u5b9e\u65f6\u884c\u60c5',
  '\u8d22\u7ecf\u8d44\u8baf',
  '\u63a7\u5236\u53f0',
  '\u7528\u6237\u7ba1\u7406',
  '\u4ea7\u54c1\u7ba1\u7406',
  '\u4ea4\u6613\u7ba1\u7406',
  '\u7406\u8d22\u670d\u52a1\u5e73\u53f0',
];

const expectedSharedCopy = [
  '\u6b63\u5e38',
  '\u7981\u7528',
  '\u4e70\u5165',
  '\u5356\u51fa',
  '\u5f85\u6210\u4ea4',
  '\u5df2\u6210\u4ea4',
  '\u5df2\u64a4\u9500',
  '\u8d35\u91d1\u5c5e',
  '\u7406\u8d22\u4ea7\u54c1',
  '\u884c\u4e1a\u52a8\u6001',
  '\u7cfb\u7edf\u901a\u77e5',
];

for (const copy of expectedRouterCopy) {
  assert(router.includes(copy), `router should include readable copy: ${copy}`);
}

for (const copy of expectedSharedCopy) {
  assert(types.includes(copy), `types should include readable option copy: ${copy}`);
  assert(format.includes(copy), `format should include readable formatter copy: ${copy}`);
}

assert(format.includes('\u521a\u521a'), 'formatRelativeTime should return readable just-now copy');
assert(format.includes('\u5206\u949f\u524d'), 'formatRelativeTime should return readable minute copy');
assert(format.includes('\u5c0f\u65f6\u524d'), 'formatRelativeTime should return readable hour copy');

console.log('Global readable copy structure checks passed');
