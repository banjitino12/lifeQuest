export type GameEventType = 'BUFF' | 'DEBUFF' | 'ENEMY' | 'DAILY_SETTLEMENT' | string;

export interface GameEventViewModel {
  id?: number | string;
  title: string;
  description?: string;
  type: GameEventType;
  effectText?: string;
  remainingText?: string;
}

export interface AttributeViewModel {
  key: string;
  label: string;
  value: number;
  change?: number;
}

