'use client';

import { useState, useEffect } from 'react';

export const useResponsive = () => {
  const [screenSize, setScreenSize] = useState<'sm' | 'md' | 'lg' | 'xl' | '2xl'>('lg');
  const [isMobile, setIsMobile] = useState(false);
  const [isTablet, setIsTablet] = useState(false);
  const [isDesktop, setIsDesktop] = useState(false);

  useEffect(() => {
    const updateScreenSize = () => {
      const width = window.innerWidth;
      
      if (width < 640) {
        setScreenSize('sm');
        setIsMobile(true);
        setIsTablet(false);
        setIsDesktop(false);
      } else if (width < 768) {
        setScreenSize('sm');
        setIsMobile(true);
        setIsTablet(false);
        setIsDesktop(false);
      } else if (width < 1024) {
        setScreenSize('md');
        setIsMobile(false);
        setIsTablet(true);
        setIsDesktop(false);
      } else if (width < 1280) {
        setScreenSize('lg');
        setIsMobile(false);
        setIsTablet(false);
        setIsDesktop(true);
      } else if (width < 1536) {
        setScreenSize('xl');
        setIsMobile(false);
        setIsTablet(false);
        setIsDesktop(true);
      } else {
        setScreenSize('2xl');
        setIsMobile(false);
        setIsTablet(false);
        setIsDesktop(true);
      }
    };

    updateScreenSize();
    window.addEventListener('resize', updateScreenSize);
    
    return () => window.removeEventListener('resize', updateScreenSize);
  }, []);

  return {
    screenSize,
    isMobile,
    isTablet,
    isDesktop
  };
};
