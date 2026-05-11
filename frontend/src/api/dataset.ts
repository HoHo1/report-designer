import request from '@/utils/request'
import type { PageResult } from './types'

export interface Dataset {
  id?: string
  datasetName: string
  datasetType: 'SQL' | 'API' | 'FILE'
  config: string
  status: number
  createUser?: string
  createTime?: string
}

export interface DatasetField {
  name: string
  type: string
}

export const getDatasetList = (params: {
  page?: number
  size?: number
  type?: string
  keyword?: string
}) => {
  return request.get<any, { data: PageResult<Dataset> }>('/dataset', { params })
}

export const getDataset = (id: string) => {
  return request.get<any, { data: Dataset }>(`/dataset/${id}`)
}

export const createDataset = (data: Dataset) => {
  return request.post<any, { data: Dataset }>('/dataset', data)
}

export const updateDataset = (id: string, data: Dataset) => {
  return request.put(`/dataset/${id}`, data)
}

export const deleteDataset = (id: string) => {
  return request.delete(`/dataset/${id}`)
}

export const testDataset = (id: string) => {
  return request.post(`/dataset/${id}/test`)
}

export const getDatasetFields = (id: string) => {
  return request.get<any, { data: DatasetField[] }>(`/dataset/${id}/fields`)
}
