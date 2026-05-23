// k6 基础压力测试
// 使用方式：
//   docker run --rm -i grafana/k6 run - <k6/basic-test.js --vus 10 --duration 30s
//   或带环境变量: -e BASE_URL=https://rjfwealth.cn

import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
    { duration: '30s', target: 5 },   // 逐渐增加到 5 并发
    { duration: '1m', target: 20 },   // 拉升到 20 并发
    { duration: '30s', target: 20 },  // 保持
    { duration: '30s', target: 0 },   // 逐渐归零
  ],
  thresholds: {
    http_req_duration: ['p(95)<3000'], // 95% 请求应在 3s 内
    http_req_failed: ['rate<0.01'],    // 错误率低于 1%
  },
};

const BASE_URL = __ENV.BASE_URL || 'https://rjfwealth.cn';

export default function () {
  const responses = http.batch([
    ['GET', `${BASE_URL}/`, null, { tags: { name: 'home' } }],
    ['POST', `${BASE_URL}/api/v1/system/auth/login`, JSON.stringify({ username: 'admin', password: 'admin123' }), {
      tags: { name: 'login' },
      headers: { 'Content-Type': 'application/json' },
    }],
  ]);

  check(responses[0], {
    'home status 200': (r) => r.status === 200,
  });

  check(responses[1], {
    'login returns token': (r) => JSON.parse(r.body).data !== undefined,
  });

  sleep(1);
}
