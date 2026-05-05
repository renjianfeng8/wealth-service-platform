import request from './index'

// ES 产品搜索
export function searchProduct(params: { keyword: string; page?: number; size?: number }) {
  return request.get('/search/product/search', { params })
}

export function saveProductDocument(data: any) {
  return request.post('/search/product', data)
}

export function getProductDocumentById(id: string) {
  return request.get(`/search/product/${id}`)
}

export function deleteProductDocument(id: string) {
  return request.delete(`/search/product/${id}`)
}
