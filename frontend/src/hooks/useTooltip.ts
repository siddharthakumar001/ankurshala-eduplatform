'use client';

import { useState, useCallback } from 'react';

interface TooltipState {
  isVisible: boolean;
  content: string;
  position: { x: number; y: number };
}

interface TooltipResult {
  isVisible: boolean;
  content: string;
  position: { x: number; y: number };
  showTooltip: (content: string, event: React.MouseEvent) => void;
  hideTooltip: () => void;
}

export const useTooltip = (): TooltipResult => {
  const [tooltipState, setTooltipState] = useState<TooltipState>({
    isVisible: false,
    content: '',
    position: { x: 0, y: 0 }
  });

  const showTooltip = useCallback((content: string, event: React.MouseEvent) => {
    const rect = event.currentTarget.getBoundingClientRect();
    setTooltipState({
      isVisible: true,
      content,
      position: {
        x: rect.left + rect.width / 2,
        y: rect.top - 10
      }
    });
  }, []);

  const hideTooltip = useCallback(() => {
    setTooltipState(prev => ({
      ...prev,
      isVisible: false
    }));
  }, []);

  return {
    isVisible: tooltipState.isVisible,
    content: tooltipState.content,
    position: tooltipState.position,
    showTooltip,
    hideTooltip
  };
};
