'use client'

import { useState, useEffect } from 'react'
import { useRouter } from 'next/navigation'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { toast } from 'sonner'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { useAuthStore } from '@/store/auth'
import { teacherAPI } from '@/lib/apiClient'
import { TeacherRoute } from '@/components/route-guard'

// Stage-1 FE complete: Comprehensive teacher profile schemas
const profileSchema = z.object({
  firstName: z.string().min(1, 'First name is required'),
  middleName: z.string().optional(),
  lastName: z.string().min(1, 'Last name is required'),
  mobileNumber: z.string().min(10, 'Valid mobile number required'),
  alternateMobileNumber: z.string().optional(),
  contactEmail: z.string().email('Valid email required'),
  bio: z.string().optional(),
  specialization: z.string().optional(),
  yearsOfExperience: z.number().min(0).optional(),
  hourlyRate: z.number().min(0).optional(),
})

const qualificationSchema = z.object({
  degree: z.string().min(1, 'Degree is required'),
  specialization: z.string().optional(),
  university: z.string().optional(),
  year: z.number().min(1900).max(2030).optional(),
})

const experienceSchema = z.object({
  institution: z.string().min(1, 'Institution is required'),
  role: z.string().min(1, 'Role is required'),
  subjectsTaught: z.string().optional(),
  fromDate: z.string().min(1, 'From date is required'),
  toDate: z.string().optional(),
  currentlyWorking: z.boolean().optional(),
})

const certificationSchema = z.object({
  certificationName: z.string().min(1, 'Certification name is required'),
  issuingAuthority: z.string().optional(),
  certificationId: z.string().optional(),
  issueYear: z.number().min(1900).max(2030).optional(),
  expiryDate: z.string().optional(),
})

const availabilitySchema = z.object({
  availableFrom: z.string().optional(),
  availableTo: z.string().optional(),
  preferredStudentLevels: z.string().optional(),
  languagesSpoken: z.string().optional(),
})

const addressSchema = z.object({
  addressLine1: z.string().min(1, 'Address line 1 is required'),
  addressLine2: z.string().optional(),
  city: z.string().min(1, 'City is required'),
  state: z.string().min(1, 'State is required'),
  zipCode: z.string().optional(),
  country: z.string().min(1, 'Country is required'),
  addressType: z.enum(['PERMANENT', 'CURRENT']),
})

const bankDetailsSchema = z.object({
  panNumber: z.string().optional(),
  accountHolderName: z.string().min(1, 'Account holder name is required'),
  bankName: z.string().min(1, 'Bank name is required'),
  branchAddress: z.string().optional(),
  accountNumber: z.string().min(1, 'Account number is required'),
  ifscCode: z.string().min(1, 'IFSC code is required'),
  accountType: z.enum(['SAVINGS', 'CURRENT', 'CHECKING']),
  micrCode: z.string().optional(),
  mobileNumber: z.string().optional(),
  email: z.string().email().optional().or(z.literal('')),
  termsAccepted: z.boolean(),
})

const documentSchema = z.object({
  documentName: z.string().min(1, 'Document name is required'),
  documentUrl: z.string().url('Valid URL required'),
  documentType: z.string().optional(),
})

type ProfileForm = z.infer<typeof profileSchema>
type QualificationForm = z.infer<typeof qualificationSchema>
type ExperienceForm = z.infer<typeof experienceSchema>
type CertificationForm = z.infer<typeof certificationSchema>
type AvailabilityForm = z.infer<typeof availabilitySchema>
type AddressForm = z.infer<typeof addressSchema>
type BankDetailsForm = z.infer<typeof bankDetailsSchema>
type DocumentForm = z.infer<typeof documentSchema>

interface TeacherProfile {
  id: number;
  firstName: string;
  middleName?: string;
  lastName: string;
  mobileNumber?: string;
  alternateMobileNumber?: string;
  contactEmail?: string;
  highestEducation?: string;
  postalAddress?: string;
  city?: string;
  state?: string;
  country: string;
  secondaryAddress?: string;
  profilePhotoUrl?: string;
  govtIdProofUrl?: string;
  bio?: string;
  qualifications?: string;
  hourlyRate?: number;
  yearsOfExperience?: number;
  specialization?: string;
  verified: boolean;
  rating: number;
  totalReviews: number;
}

interface TeacherQualification {
  id?: number;
  degree: string;
  specialization?: string;
  university?: string;
  year?: number;
}

interface TeacherExperience {
  id?: number;
  institution?: string;
  role?: string;
  subjectsTaught?: string;
  fromDate?: string;
  toDate?: string;
  currentlyWorking?: boolean;
}

interface TeacherCertification {
  id?: number;
  certificationName: string;
  issuingAuthority?: string;
  certificationId?: string;
  issueYear?: number;
  expiryDate?: string;
}

interface TeacherDocument {
  id?: number;
  documentUrl: string;
  documentName?: string;
  documentType?: string;
  uploadDate?: string;
}

interface TeacherAvailability {
  id?: number;
  availableFrom?: string;
  availableTo?: string;
  preferredStudentLevels?: string;
  languagesSpoken?: string;
}

interface TeacherAddress {
  id?: number;
  addressLine1: string;
  addressLine2?: string;
  city: string;
  state: string;
  zipCode?: string;
  country: string;
  addressType: 'PERMANENT' | 'CURRENT';
}

interface TeacherBankDetails {
  id?: number;
  panNumber?: string;
  accountHolderName: string;
  bankName: string;
  branchAddress?: string;
  accountNumber: string; // Will be masked on GET
  ifscCode: string;
  accountType: 'SAVINGS' | 'CURRENT' | 'CHECKING';
  micrCode?: string;
  mobileNumber?: string;
  email?: string;
  termsAccepted?: boolean;
  verified?: boolean;
}

export default function TeacherProfilePage() {
  return (
    <TeacherRoute>
      <TeacherProfileContent />
    </TeacherRoute>
  )
}

function TeacherProfileContent() {
  const router = useRouter();
  const { user, logout } = useAuthStore();
  const [activeTab, setActiveTab] = useState('profile');
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState('');

  // State for all profile sections
  const [profile, setProfile] = useState<TeacherProfile | null>(null);
  const [qualifications, setQualifications] = useState<TeacherQualification[]>([]);
  const [experiences, setExperiences] = useState<TeacherExperience[]>([]);
  const [certifications, setCertifications] = useState<TeacherCertification[]>([]);
  const [documents, setDocuments] = useState<TeacherDocument[]>([]);
  const [availability, setAvailability] = useState<TeacherAvailability | null>(null);
  const [addresses, setAddresses] = useState<TeacherAddress[]>([]);
  const [bankDetails, setBankDetails] = useState<TeacherBankDetails | null>(null);

  useEffect(() => {
    console.log('useEffect triggered with user:', user);
    
    if (!user || user.role !== 'TEACHER') {
      console.log('User not authorized, redirecting to login');
      router.push('/login');
      return;
    }
    console.log('User authorized, calling fetchProfile');
    fetchProfile();
  }, [user, router]);

  const fetchProfile = async () => {
    console.log('fetchProfile called');
    
    try {
      setLoading(true);
      console.log('Making API call to teacher profile...');
      
      const data = await teacherAPI.getProfile();
      console.log('Profile data received:', data);
      setProfile(data);
    } catch (error) {
      console.log('Fetch error:', error);
      console.log('Error details:', error.message);
      console.log('Error stack:', error.stack);
      setMessage('Error loading profile: ' + error.message);
    } finally {
      console.log('Setting loading to false');
      setLoading(false);
    }
  };

  const updateProfile = async (updatedProfile: Partial<TeacherProfile>) => {
    try {
      setSaving(true);
      const response = await fetch('http://localhost:8080/api/teacher/profile', {
        method: 'PUT',
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('accessToken')}`,
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(updatedProfile),
      });

      if (response.ok) {
        const data = await response.json();
        setProfile(data);
        setMessage('Profile updated successfully!');
      } else if (response.status === 401) {
        logout();
        router.push('/login');
      } else {
        setMessage('Failed to update profile');
      }
    } catch (error) {
      setMessage('Error updating profile');
    } finally {
      setSaving(false);
    }
  };

  const fetchQualifications = async () => {
    if (!localStorage.getItem('accessToken')) return;

    try {
      const response = await fetch('http://localhost:8080/api/teacher/profile/qualifications', {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('accessToken')}`,
          'Content-Type': 'application/json',
        },
      });

      if (response.ok) {
        const data = await response.json();
        setQualifications(data);
      }
    } catch (error) {
      console.error('Error fetching qualifications:', error);
    }
  };

  const addQualification = async (qualification: TeacherQualification) => {
    if (!localStorage.getItem('accessToken')) return;

    try {
      const response = await fetch('http://localhost:8080/api/teacher/profile/qualifications', {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('accessToken')}`,
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(qualification),
      });

      if (response.ok) {
        fetchQualifications();
        setMessage('Qualification added successfully!');
      }
    } catch (error) {
      setMessage('Error adding qualification');
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-xl">Loading profile...</div>
      </div>
    );
  }

  if (!profile) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-xl text-red-600">{message || 'Failed to load profile'}</div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-7xl mx-auto px-4 py-8">
        <div className="bg-white rounded-lg shadow-md">
          <div className="px-6 py-4 border-b border-gray-200">
            <h1 className="text-2xl font-bold text-gray-900">Teacher Profile</h1>
            <p className="text-gray-600 mt-1">Manage your teaching profile and information</p>
          </div>

          {message && (
            <div className="mx-6 mt-4 p-4 bg-blue-50 border border-blue-200 rounded-md">
              <p className="text-blue-800">{message}</p>
            </div>
          )}

          {/* Tab Navigation */}
          <div className="px-6 py-4 border-b border-gray-200">
            <nav className="flex space-x-8">
              {[
                { key: 'profile', label: 'Profile' },
                { key: 'qualifications', label: 'Qualifications' },
                { key: 'experience', label: 'Experience' },
                { key: 'certifications', label: 'Certifications' },
                { key: 'availability', label: 'Availability' },
                { key: 'addresses', label: 'Addresses' },
                { key: 'bank-details', label: 'Bank Details' },
                { key: 'documents', label: 'Documents' },
              ].map((tab) => (
                <button
                  key={tab.key}
                  onClick={() => setActiveTab(tab.key)}
                  className={`px-3 py-2 text-sm font-medium rounded-md ${
                    activeTab === tab.key
                      ? 'bg-blue-100 text-blue-700'
                      : 'text-gray-500 hover:text-gray-700'
                  }`}
                >
                  {tab.label}
                </button>
              ))}
            </nav>
          </div>

          <div className="p-6">
            {/* Profile Tab */}
            {activeTab === 'profile' && (
              <div className="space-y-6">
                <h2 className="text-lg font-semibold text-gray-900">Basic Information</h2>
                
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      First Name
                    </label>
                    <input
                      type="text"
                      value={profile.firstName || ''}
                      onChange={(e) => setProfile({ ...profile, firstName: e.target.value })}
                      className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Middle Name
                    </label>
                    <input
                      type="text"
                      value={profile.middleName || ''}
                      onChange={(e) => setProfile({ ...profile, middleName: e.target.value })}
                      className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Last Name
                    </label>
                    <input
                      type="text"
                      value={profile.lastName || ''}
                      onChange={(e) => setProfile({ ...profile, lastName: e.target.value })}
                      className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Mobile Number
                    </label>
                    <input
                      type="tel"
                      value={profile.mobileNumber || ''}
                      onChange={(e) => setProfile({ ...profile, mobileNumber: e.target.value })}
                      className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Contact Email
                    </label>
                    <input
                      type="email"
                      value={profile.contactEmail || ''}
                      onChange={(e) => setProfile({ ...profile, contactEmail: e.target.value })}
                      className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Years of Experience
                    </label>
                    <input
                      type="number"
                      value={profile.yearsOfExperience || ''}
                      onChange={(e) => setProfile({ ...profile, yearsOfExperience: parseInt(e.target.value) })}
                      className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Hourly Rate (₹)
                    </label>
                    <input
                      type="number"
                      value={profile.hourlyRate || ''}
                      onChange={(e) => setProfile({ ...profile, hourlyRate: parseFloat(e.target.value) })}
                      className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                    />
                  </div>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Bio
                  </label>
                  <textarea
                    rows={4}
                    value={profile.bio || ''}
                    onChange={(e) => setProfile({ ...profile, bio: e.target.value })}
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                    placeholder="Tell us about yourself and your teaching experience..."
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Specialization
                  </label>
                  <input
                    type="text"
                    value={profile.specialization || ''}
                    onChange={(e) => setProfile({ ...profile, specialization: e.target.value })}
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                    placeholder="e.g., Mathematics, Physics, Chemistry"
                  />
                </div>

                <div className="flex justify-end">
                  <button
                    onClick={() => updateProfile(profile)}
                    disabled={saving}
                    className="px-6 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:opacity-50"
                  >
                    {saving ? 'Saving...' : 'Save Profile'}
                  </button>
                </div>
              </div>
            )}

            {/* Qualifications Tab */}
            {activeTab === 'qualifications' && (
              <div className="space-y-6">
                <div className="flex justify-between items-center">
                  <h2 className="text-lg font-semibold text-gray-900">Qualifications</h2>
                  <button
                    onClick={fetchQualifications}
                    className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700"
                  >
                    Load Qualifications
                  </button>
                </div>

                {qualifications.length > 0 ? (
                  <div className="space-y-4">
                    {qualifications.map((qual, index) => (
                      <div key={qual.id || index} className="p-4 border border-gray-200 rounded-md">
                        <h3 className="font-medium text-gray-900">{qual.degree}</h3>
                        {qual.specialization && (
                          <p className="text-gray-600">Specialization: {qual.specialization}</p>
                        )}
                        {qual.university && (
                          <p className="text-gray-600">University: {qual.university}</p>
                        )}
                        {qual.year && (
                          <p className="text-gray-600">Year: {qual.year}</p>
                        )}
                      </div>
                    ))}
                  </div>
                ) : (
                  <div className="text-center py-8 text-gray-500">
                    No qualifications found. Click "Load Qualifications" to fetch your data.
                  </div>
                )}
              </div>
            )}

            {/* Other tabs would be implemented similarly */}
            {activeTab !== 'profile' && activeTab !== 'qualifications' && (
              <div className="text-center py-8 text-gray-500">
                <p>This section is under development.</p>
                <p className="text-sm mt-2">
                  The {activeTab} management functionality will be implemented soon.
                </p>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
