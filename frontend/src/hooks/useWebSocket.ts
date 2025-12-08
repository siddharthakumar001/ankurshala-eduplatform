'use client';

import { useState, useCallback, useRef, useEffect } from 'react';

interface WebSocketOptions {
  protocols?: string | string[];
  reconnectInterval?: number;
  maxReconnectAttempts?: number;
}

interface WebSocketResult {
  isConnected: boolean;
  isConnecting: boolean;
  error: string | null;
  connect: (url: string, options?: WebSocketOptions) => void;
  disconnect: () => void;
  sendMessage: (message: string | ArrayBuffer | Blob) => void;
  onMessage: (callback: (event: MessageEvent) => void) => void;
  onOpen: (callback: (event: Event) => void) => void;
  onClose: (callback: (event: CloseEvent) => void) => void;
  onError: (callback: (event: Event) => void) => void;
}

export const useWebSocket = (): WebSocketResult => {
  const [isConnected, setIsConnected] = useState(false);
  const [isConnecting, setIsConnecting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const wsRef = useRef<WebSocket | null>(null);
  const reconnectTimeoutRef = useRef<NodeJS.Timeout | null>(null);
  const reconnectAttemptsRef = useRef(0);

  const connect = useCallback((url: string, options: WebSocketOptions = {}) => {
    if (wsRef.current && wsRef.current.readyState === WebSocket.OPEN) {
      return;
    }

    setIsConnecting(true);
    setError(null);

    try {
      const ws = new WebSocket(url, options.protocols);
      wsRef.current = ws;

      ws.onopen = (event) => {
        setIsConnected(true);
        setIsConnecting(false);
        setError(null);
        reconnectAttemptsRef.current = 0;
      };

      ws.onclose = (event) => {
        setIsConnected(false);
        setIsConnecting(false);
        
        if (event.code !== 1000 && options.reconnectInterval && options.maxReconnectAttempts) {
          if (reconnectAttemptsRef.current < options.maxReconnectAttempts) {
            reconnectAttemptsRef.current++;
            reconnectTimeoutRef.current = setTimeout(() => {
              connect(url, options);
            }, options.reconnectInterval);
          }
        }
      };

      ws.onerror = (event) => {
        setError('WebSocket connection error');
        setIsConnecting(false);
      };

    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to create WebSocket');
      setIsConnecting(false);
    }
  }, []);

  const disconnect = useCallback(() => {
    if (reconnectTimeoutRef.current) {
      clearTimeout(reconnectTimeoutRef.current);
      reconnectTimeoutRef.current = null;
    }

    if (wsRef.current) {
      wsRef.current.close(1000, 'Manual disconnect');
      wsRef.current = null;
    }

    setIsConnected(false);
    setIsConnecting(false);
    reconnectAttemptsRef.current = 0;
  }, []);

  const sendMessage = useCallback((message: string | ArrayBuffer | Blob) => {
    if (wsRef.current && wsRef.current.readyState === WebSocket.OPEN) {
      wsRef.current.send(message);
    }
  }, []);

  const onMessage = useCallback((callback: (event: MessageEvent) => void) => {
    if (wsRef.current) {
      wsRef.current.onmessage = callback;
    }
  }, []);

  const onOpen = useCallback((callback: (event: Event) => void) => {
    if (wsRef.current) {
      wsRef.current.onopen = callback;
    }
  }, []);

  const onClose = useCallback((callback: (event: CloseEvent) => void) => {
    if (wsRef.current) {
      wsRef.current.onclose = callback;
    }
  }, []);

  const onError = useCallback((callback: (event: Event) => void) => {
    if (wsRef.current) {
      wsRef.current.onerror = callback;
    }
  }, []);

  useEffect(() => {
    return () => {
      disconnect();
    };
  }, [disconnect]);

  return {
    isConnected,
    isConnecting,
    error,
    connect,
    disconnect,
    sendMessage,
    onMessage,
    onOpen,
    onClose,
    onError
  };
};