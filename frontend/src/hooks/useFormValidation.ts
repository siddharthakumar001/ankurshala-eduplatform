'use client';

import { useState, useCallback } from 'react';

interface ValidationRule {
  required?: boolean;
  minLength?: number;
  maxLength?: number;
  pattern?: RegExp;
  custom?: (value: any) => string | null;
}

interface ValidationRules {
  [key: string]: ValidationRule;
}

interface FormData {
  [key: string]: any;
}

interface ValidationErrors {
  [key: string]: string | null;
}

export const useFormValidation = (rules: ValidationRules) => {
  const [errors, setErrors] = useState<ValidationErrors>({});
  const [touched, setTouched] = useState<{ [key: string]: boolean }>({});

  const validateField = useCallback((name: string, value: any): string | null => {
    const rule = rules[name];
    if (!rule) return null;

    // Required validation
    if (rule.required && (!value || value.toString().trim() === '')) {
      return `${name} is required`;
    }

    // Skip other validations if value is empty and not required
    if (!value || value.toString().trim() === '') return null;

    // Min length validation
    if (rule.minLength && value.toString().length < rule.minLength) {
      return `${name} must be at least ${rule.minLength} characters`;
    }

    // Max length validation
    if (rule.maxLength && value.toString().length > rule.maxLength) {
      return `${name} must not exceed ${rule.maxLength} characters`;
    }

    // Pattern validation
    if (rule.pattern && !rule.pattern.test(value.toString())) {
      return `${name} format is invalid`;
    }

    // Custom validation
    if (rule.custom) {
      return rule.custom(value);
    }

    return null;
  }, [rules]);

  const validateForm = useCallback((data: FormData): boolean => {
    const newErrors: ValidationErrors = {};
    let isValid = true;

    Object.keys(rules).forEach((field) => {
      const error = validateField(field, data[field]);
      if (error) {
        newErrors[field] = error;
        isValid = false;
      }
    });

    setErrors(newErrors);
    return isValid;
  }, [rules, validateField]);

  const handleFieldChange = useCallback((name: string, value: any) => {
    const error = validateField(name, value);
    setErrors(prev => ({ ...prev, [name]: error }));
  }, [validateField]);

  const handleFieldBlur = useCallback((name: string) => {
    setTouched(prev => ({ ...prev, [name]: true }));
  }, []);

  const getFieldError = useCallback((name: string): string | null => {
    return touched[name] ? errors[name] || null : null;
  }, [errors, touched]);

  const clearErrors = useCallback(() => {
    setErrors({});
    setTouched({});
  }, []);

  const setFieldError = useCallback((name: string, error: string | null) => {
    setErrors(prev => ({ ...prev, [name]: error }));
  }, []);

  return {
    errors,
    touched,
    validateForm,
    handleFieldChange,
    handleFieldBlur,
    getFieldError,
    clearErrors,
    setFieldError
  };
};
