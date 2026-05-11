import request from '@/utils/request'
import type { PageResult } from './types'

export interface Report {
  id?: string
  reportName: string
  designContent: string
  datasetIds?: string
  category?: string
  status: number
  isPublic: number
  createUser?: string
  createTime?: string
}

export interface ReportVersion {
  id: string
  versionNum: number
  createUser: string
  createTime: string
}

export const getReportList = (params: {
  page?: number
  size?: number
  category?: string
  keyword?: string
}) => {
  return request.get<any, { data: PageResult<Report> }>('/report', { params })
}

export const getReport = (id: string) => {
  return request.get<any, { data: Report }>(`/report/${id}`)
}

export const createReport = (data: Report) => {
  return request.post<any, { data: Report }>('/report', data)
}

export const updateReport = (id: string, data: Report) => {
  return request.put(`/report/${id}`, data)
}

export const deleteReport = (id: string) => {
  return request.delete(`/report/${id}`)
}

export const renderReport = (id: string, params: any) => {
  return request.post(`/report/${id}/render`, params)
}

export const exportReport = (id: string, format: string, params: any) => {
  return request.get(`/report/${id}/export`, { 
    params: { format, ...params },
    responseType: 'blob'
  })
}

export const copyReport = (id: string) => {
  return request.post<any, { data: Report }>(`/report/${id}/copy`)
}

export const getReportVersions = (id: string) => {
  return request.get<any, { data: ReportVersion[] }>(`/report/${id}/versions`)
}

export const rollbackReport = (id: string, versionId: string) => {
  return request.post(`/report/${id}/rollback/${versionId}`)
}
