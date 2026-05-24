import { APIRequestContext, APIResponse, test, expect as playwrightExpect } from '@playwright/test'
import path from 'path'
import fs from 'fs'

export interface ApiResult<T = any> {
  success: boolean
  status: number
  body: T
  headers: Record<string, string>
}

export class ApiClient {
  private adminToken: string | null = null
  private userToken: string | null = null
  private screenshotDir: string

  constructor(
    private request: APIRequestContext,
    private baseURL: string,
  ) {
    this.screenshotDir = path.resolve(process.cwd(), 'api-e2e-report', 'screenshots')
    fs.mkdirSync(this.screenshotDir, { recursive: true })
  }

  async loginAsAdmin(username = 'admin', password = 'admin123'): Promise<string> {
    const res = await this.request.post(`${this.baseURL}/system/umsAdmin/login`, {
      data: { username, password },
      headers: { 'Content-Type': 'application/json' },
    })
    const body = await res.json()
    if (res.status() !== 200 || body.code !== 200) {
      throw new Error(`Admin login failed: ${JSON.stringify(body)}`)
    }
    this.adminToken = body.data.accessToken
    return this.adminToken!
  }

  async loginAsUser(username = 'zhangwei', password = '123456'): Promise<string> {
    const res = await this.request.post(`${this.baseURL}/user/login`, {
      data: { username, password },
      headers: { 'Content-Type': 'application/json' },
    })
    const body = await res.json()
    if (res.status() !== 200 || body.code !== 200) {
      throw new Error(`User login failed: ${JSON.stringify(body)}`)
    }
    this.userToken = body.data.token
    return this.userToken!
  }

  getAdminToken(): string | null {
    return this.adminToken
  }

  getUserToken(): string | null {
    return this.userToken
  }

  async get(path: string, token?: string | null): Promise<ApiResult> {
    const headers: Record<string, string> = { 'Content-Type': 'application/json' }
    const t = token !== undefined ? token : this.adminToken
    if (t) headers['Authorization'] = `Bearer ${t}`
    const res = await this.request.get(`${this.baseURL}${path}`, { headers })
    return this.parseResponse(res)
  }

  async post(path: string, data?: any, token?: string | null): Promise<ApiResult> {
    const headers: Record<string, string> = { 'Content-Type': 'application/json' }
    const t = token !== undefined ? token : this.adminToken
    if (t) headers['Authorization'] = `Bearer ${t}`
    const res = await this.request.post(`${this.baseURL}${path}`, { data, headers })
    return this.parseResponse(res)
  }

  async put(path: string, data?: any, token?: string | null): Promise<ApiResult> {
    const headers: Record<string, string> = { 'Content-Type': 'application/json' }
    const t = token !== undefined ? token : this.adminToken
    if (t) headers['Authorization'] = `Bearer ${t}`
    const res = await this.request.put(`${this.baseURL}${path}`, { data, headers })
    return this.parseResponse(res)
  }

  async delete(path: string, body?: any, token?: string | null): Promise<ApiResult> {
    const headers: Record<string, string> = { 'Content-Type': 'application/json' }
    const t = token !== undefined ? token : this.adminToken
    if (t) headers['Authorization'] = `Bearer ${t}`
    const options: any = { headers }
    if (body !== undefined) options.data = body
    const res = await this.request.delete(`${this.baseURL}${path}`, options)
    return this.parseResponse(res)
  }

  private async parseResponse(res: APIResponse): Promise<ApiResult> {
    const status = res.status()
    let body: any
    try {
      body = await res.json()
    } catch {
      body = { raw: await res.text() }
    }
    return {
      success: status >= 200 && status < 300,
      status,
      body,
      headers: res.headers(),
    }
  }

  async assertOk(result: ApiResult, message?: string) {
    playwrightExpect(result.status, `${message || 'status should be 2xx'}: ${JSON.stringify(result.body)}`).toBeGreaterThanOrEqual(200)
    playwrightExpect(result.status, `${message || 'status should be < 300'}: ${JSON.stringify(result.body)}`).toBeLessThan(300)
    if (result.body && typeof result.body === 'object' && 'code' in result.body) {
      playwrightExpect(result.body.code, `${message || 'business code should be 200'}: ${JSON.stringify(result.body)}`).toBe(200)
    }
  }

  async assertBusinessFail(result: ApiResult, expectedCode?: number) {
    const code = expectedCode || 400
    playwrightExpect(result.body.code, `Business code should be ${code}`).toBe(code)
  }

  async assertUnauthorized(result: ApiResult) {
    playwrightExpect(result.status).toBe(401)
  }
}
