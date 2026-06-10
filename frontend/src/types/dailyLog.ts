export type DailyLogSourceType = 'FORM' | 'NATURAL_LANGUAGE' | 'MIXED';

export interface SubmitDailyLogRequest {
  logDate: string;
  rawText?: string;
  studyHours?: number;
  workHours?: number;
  sleepHours?: number;
  exerciseMinutes?: number;
  entertainmentMinutes?: number;
  moodTag?: string;
  taskCompletionRate?: number;
  completedContent?: string;
  problemText?: string;
  reflectionText?: string;
  sourceType?: DailyLogSourceType;
}

export interface DailyLogSummaryResponse {
  dailyLogId: number;
  logDate: string;
  sourceType: DailyLogSourceType;
  llmStatus?: string;
  updated: boolean;
}

export interface SettlementScoreBlock {
  dailyScore: number;
  rating: string;
  growthScore: number;
  executionScore: number;
  energyScore: number;
  moodScore: number;
  distractionScore: number;
  reflectionScore: number;
  reasons: Record<string, string>;
}

export interface SettlementAttributeChangeBlock {
  focusDelta: number;
  disciplineDelta: number;
  knowledgeDelta: number;
  energyDelta: number;
  moodDelta: number;
  executionDelta: number;
  balanceDelta: number;
  expDelta: number;
  reasons: Record<string, string>;
}

export interface SettlementGameEventBlock {
  eventType: string;
  eventCode: string;
  eventName: string;
  eventLevel: number;
  eventDescription: string;
  effectJson?: string;
}

export interface SettlementLlmBlock {
  status: string;
  fallbackUsed: boolean;
  feedback: string;
  storyNarration: string;
}

export interface TomorrowTaskBlock {
  taskType: string;
  title: string;
  generatedBy: string;
}

export interface SettlementResponse {
  dailyLogId: number;
  logDate: string;
  sourceType: DailyLogSourceType;
  dailyLogUpdated: boolean;
  score: SettlementScoreBlock;
  attributeChange: SettlementAttributeChangeBlock;
  events: SettlementGameEventBlock[];
  llm: SettlementLlmBlock;
  tomorrowTasks: TomorrowTaskBlock[];
  basicSuggestion: string;
}

