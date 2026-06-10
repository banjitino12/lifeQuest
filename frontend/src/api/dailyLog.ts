import { request } from '@/api/http';
import type { DailyLogSummaryResponse, SettlementResponse, SubmitDailyLogRequest } from '@/types';

export function submitDailyLog(payload: SubmitDailyLogRequest): Promise<DailyLogSummaryResponse> {
  return request<DailyLogSummaryResponse>({
    method: 'POST',
    url: '/daily-logs',
    data: payload,
  });
}

export function submitSettlement(payload: SubmitDailyLogRequest): Promise<SettlementResponse> {
  return request<SettlementResponse>({
    method: 'POST',
    url: '/settlements',
    data: payload,
  });
}

export function getSettlementByDate(date: string): Promise<SettlementResponse> {
  return request<SettlementResponse>({
    method: 'GET',
    url: '/settlements/by-date',
    params: { date },
  });
}

