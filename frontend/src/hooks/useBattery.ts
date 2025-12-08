'use client';

import { useState, useEffect } from 'react';

interface BatteryStatus {
  level: number;
  charging: boolean;
  chargingTime: number;
  dischargingTime: number;
}

interface BatteryResult {
  battery: BatteryStatus | null;
  error: string | null;
  isSupported: boolean;
}

export const useBattery = (): BatteryResult => {
  const [battery, setBattery] = useState<BatteryStatus | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [isSupported, setIsSupported] = useState(false);

  useEffect(() => {
    if (!('getBattery' in navigator)) {
      setIsSupported(false);
      setError('Battery API not supported');
      return;
    }

    setIsSupported(true);

    (navigator as any).getBattery().then((battery: any) => {
      const updateBatteryInfo = () => {
        setBattery({
          level: battery.level,
          charging: battery.charging,
          chargingTime: battery.chargingTime,
          dischargingTime: battery.dischargingTime
        });
      };

      updateBatteryInfo();

      battery.addEventListener('levelchange', updateBatteryInfo);
      battery.addEventListener('chargingchange', updateBatteryInfo);
      battery.addEventListener('chargingtimechange', updateBatteryInfo);
      battery.addEventListener('dischargingtimechange', updateBatteryInfo);

      return () => {
        battery.removeEventListener('levelchange', updateBatteryInfo);
        battery.removeEventListener('chargingchange', updateBatteryInfo);
        battery.removeEventListener('chargingtimechange', updateBatteryInfo);
        battery.removeEventListener('dischargingtimechange', updateBatteryInfo);
      };
    }).catch((err: Error) => {
      setError(err.message);
    });
  }, []);

  return {
    battery,
    error,
    isSupported
  };
};
