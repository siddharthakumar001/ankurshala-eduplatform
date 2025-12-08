/**
 * Authentication Type Definitions
 * Matches backend DTOs for type safety
 */

export interface StudentSignupRequest {
  // Personal Information
  name: string
  email: string
  password: string
  
  // Academic Information
  board: string
  grade: string
  language: string
  school: string
  dob: string // ISO date string
  pincode: string
  
  // Guardian Information
  guardianName: string
  guardianContact: string
  
  // Learning Goals
  goals: string[]
  
  // Optional Fields
  fatherName?: string
  motherName?: string
  mobileNumber?: string
  emergencyContact?: string
}

export interface SubjectExpertiseDto {
  board: string
  grade: string
  subjectId: number
  language: string
}

export interface AvailabilitySlotDto {
  weekday: number // 0-6 (Sunday to Saturday)
  startTime: string // HH:MM format
  endTime: string // HH:MM format
  timezone: string
}

export interface TeacherSignupRequest {
  // Personal Information
  name: string
  email: string
  password: string
  
  // Professional Information
  bio: string
  yearsExperience: number
  languages: string[]
  categories: string[]
  hourlyRate: number
  
  // Subject Expertise
  subjectExpertise: SubjectExpertiseDto[]
  
  // Availability
  availability: AvailabilitySlotDto[]
  
  // Optional Fields
  phoneNumber?: string
  linkedinProfile?: string
}

export interface AuthResponse {
  accessToken: string
  refreshToken: string
  tokenType: string
  userId: number
  name: string
  email: string
  role: string
}

export interface SigninRequest {
  email: string
  password: string
}

export interface RefreshTokenRequest {
  refreshToken: string
}
