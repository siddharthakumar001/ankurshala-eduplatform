'use client'

import { useState, useEffect } from 'react'
import { useRouter } from 'next/navigation'
import { Button } from '@/components/ui/button'
import { useAuthStore } from '@/store/auth'
import { teacherAPI } from '@/lib/apiClient'
import { TeacherRoute } from '@/components/route-guard'

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
  documentType: string;
  documentUrl: string;
  documentName?: string;
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
  bankName?: string;
  branchAddress?: string;
  accountNumber?: string;
  ifscCode?: string;
  accountHolderName?: string;
  accountType?: 'SAVINGS' | 'CURRENT' | 'CHECKING';
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
  const [loadedTabs, setLoadedTabs] = useState<Record<string, boolean>>({});

  // State for all profile sections
  const [profile, setProfile] = useState<TeacherProfile | null>(null);
  const [qualifications, setQualifications] = useState<TeacherQualification[]>([]);
  const [experiences, setExperiences] = useState<TeacherExperience[]>([]);
  const [certifications, setCertifications] = useState<TeacherCertification[]>([]);
  const [documents, setDocuments] = useState<TeacherDocument[]>([]);
  const [availability, setAvailability] = useState<TeacherAvailability | null>(null);
  const [addresses, setAddresses] = useState<TeacherAddress[]>([]);
  const [bankDetails, setBankDetails] = useState<TeacherBankDetails | null>(null);
  const [newQualification, setNewQualification] = useState<TeacherQualification>({
    degree: '',
    specialization: '',
    university: '',
    year: undefined
  });
  const [newExperience, setNewExperience] = useState<TeacherExperience>({
    institution: '',
    role: '',
    subjectsTaught: '',
    fromDate: '',
    toDate: '',
    currentlyWorking: false
  });
  const [newCertification, setNewCertification] = useState<TeacherCertification>({
    certificationName: '',
    issuingAuthority: '',
    certificationId: '',
    issueYear: undefined,
    expiryDate: ''
  });
  const [newDocument, setNewDocument] = useState<TeacherDocument>({
    documentType: '',
    documentUrl: '',
    documentName: ''
  });
  const [newAddress, setNewAddress] = useState<TeacherAddress>({
    addressLine1: '',
    addressLine2: '',
    city: '',
    state: '',
    zipCode: '',
    country: 'India',
    addressType: 'CURRENT'
  });

  useEffect(() => {
    if (!user || user.role !== 'TEACHER') {
      router.push('/login');
      return;
    }
    fetchProfile();
  }, [user, router]);

  useEffect(() => {
    const loadTabData = async () => {
      if (loadedTabs[activeTab]) {
        return;
      }
      if (activeTab === 'qualifications') {
        await fetchQualifications();
      }
      if (activeTab === 'experience') {
        await fetchExperiences();
      }
      if (activeTab === 'certifications') {
        await fetchCertifications();
      }
      if (activeTab === 'availability') {
        await fetchAvailability();
      }
      if (activeTab === 'addresses') {
        await fetchAddresses();
      }
      if (activeTab === 'bank-details') {
        await fetchBankDetails();
      }
      if (activeTab === 'documents') {
        await fetchDocuments();
      }
      setLoadedTabs((prev) => ({ ...prev, [activeTab]: true }));
    };

    loadTabData();
  }, [activeTab, loadedTabs]);

  const fetchProfile = async () => {
    try {
      setLoading(true);
      const data = await teacherAPI.getProfile();
      setProfile(data);
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : 'Unknown error occurred';
      setMessage('Error loading profile: ' + errorMessage);
    } finally {
      setLoading(false);
    }
  };

  const updateProfile = async (updatedProfile: Partial<TeacherProfile>) => {
    try {
      setSaving(true);
      const data = await teacherAPI.updateProfile(updatedProfile);
      setProfile(data);
      setMessage('Profile updated successfully!');
    } catch (error) {
      if (error instanceof Error && error.message === 'Unauthorized') {
        logout();
        router.push('/login');
        return;
      }
      setMessage('Failed to update profile');
    } finally {
      setSaving(false);
    }
  };

  const fetchQualifications = async () => {
    try {
      const data = await teacherAPI.getQualifications();
      setQualifications(data);
    } catch (error) {
      console.error('Error fetching qualifications:', error);
    }
  };

  const addQualification = async () => {
    try {
      await teacherAPI.addQualification(newQualification);
      setNewQualification({
        degree: '',
        specialization: '',
        university: '',
        year: undefined
      });
      fetchQualifications();
      setMessage('Qualification added successfully!');
    } catch (error) {
      setMessage('Error adding qualification');
    }
  };

  const deleteQualification = async (id?: number) => {
    if (!id) return;
    try {
      await teacherAPI.deleteQualification(id);
      fetchQualifications();
      setMessage('Qualification deleted successfully!');
    } catch (error) {
      setMessage('Error deleting qualification');
    }
  };

  const fetchExperiences = async () => {
    try {
      const data = await teacherAPI.getExperiences();
      setExperiences(data);
    } catch (error) {
      console.error('Error fetching experiences:', error);
    }
  };

  const fetchCertifications = async () => {
    try {
      const data = await teacherAPI.getCertifications();
      setCertifications(data);
    } catch (error) {
      console.error('Error fetching certifications:', error);
    }
  };

  const fetchDocuments = async () => {
    try {
      const data = await teacherAPI.getDocuments();
      setDocuments(data);
    } catch (error) {
      console.error('Error fetching documents:', error);
    }
  };

  const fetchAvailability = async () => {
    try {
      const data = await teacherAPI.getAvailability();
      setAvailability(data);
    } catch (error) {
      console.error('Error fetching availability:', error);
    }
  };

  const fetchAddresses = async () => {
    try {
      const data = await teacherAPI.getAddresses();
      setAddresses(data);
    } catch (error) {
      console.error('Error fetching addresses:', error);
    }
  };

  const fetchBankDetails = async () => {
    try {
      const data = await teacherAPI.getBankDetails();
      setBankDetails(data);
    } catch (error) {
      console.error('Error fetching bank details:', error);
    }
  };

  const addExperience = async () => {
    try {
      await teacherAPI.addExperience(newExperience);
      setNewExperience({
        institution: '',
        role: '',
        subjectsTaught: '',
        fromDate: '',
        toDate: '',
        currentlyWorking: false
      });
      fetchExperiences();
      setMessage('Experience added successfully!');
    } catch (error) {
      setMessage('Error adding experience');
    }
  };

  const deleteExperience = async (id?: number) => {
    if (!id) return;
    try {
      await teacherAPI.deleteExperience(id);
      fetchExperiences();
      setMessage('Experience deleted successfully!');
    } catch (error) {
      setMessage('Error deleting experience');
    }
  };

  const addCertification = async () => {
    try {
      await teacherAPI.addCertification(newCertification);
      setNewCertification({
        certificationName: '',
        issuingAuthority: '',
        certificationId: '',
        issueYear: undefined,
        expiryDate: ''
      });
      fetchCertifications();
      setMessage('Certification added successfully!');
    } catch (error) {
      setMessage('Error adding certification');
    }
  };

  const deleteCertification = async (id?: number) => {
    if (!id) return;
    try {
      await teacherAPI.deleteCertification(id);
      fetchCertifications();
      setMessage('Certification deleted successfully!');
    } catch (error) {
      setMessage('Error deleting certification');
    }
  };

  const addDocument = async () => {
    try {
      await teacherAPI.addDocument(newDocument);
      setNewDocument({
        documentType: '',
        documentUrl: '',
        documentName: ''
      });
      fetchDocuments();
      setMessage('Document added successfully!');
    } catch (error) {
      setMessage('Error adding document');
    }
  };

  const deleteDocument = async (id?: number) => {
    if (!id) return;
    try {
      await teacherAPI.deleteDocument(id);
      fetchDocuments();
      setMessage('Document deleted successfully!');
    } catch (error) {
      setMessage('Error deleting document');
    }
  };

  const saveAvailability = async () => {
    try {
      if (!availability) return;
      const data = await teacherAPI.updateAvailability(availability);
      setAvailability(data);
      setMessage('Availability updated successfully!');
    } catch (error) {
      setMessage('Error updating availability');
    }
  };

  const addAddress = async () => {
    try {
      await teacherAPI.addAddress(newAddress);
      setNewAddress({
        addressLine1: '',
        addressLine2: '',
        city: '',
        state: '',
        zipCode: '',
        country: 'India',
        addressType: 'CURRENT'
      });
      fetchAddresses();
      setMessage('Address added successfully!');
    } catch (error) {
      setMessage('Error adding address');
    }
  };

  const deleteAddress = async (id?: number) => {
    if (!id) return;
    try {
      await teacherAPI.deleteAddress(id);
      fetchAddresses();
      setMessage('Address deleted successfully!');
    } catch (error) {
      setMessage('Error deleting address');
    }
  };

  const saveBankDetails = async () => {
    try {
      if (!bankDetails) return;
      const data = await teacherAPI.updateBankDetails(bankDetails);
      setBankDetails(data);
      setMessage('Bank details updated successfully!');
    } catch (error) {
      setMessage('Error updating bank details');
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[240px]">
        <div className="glass-panel px-6 py-4 text-slate-600 dark:text-slate-200">
          Loading profile...
        </div>
      </div>
    );
  }

  if (!profile) {
    return (
      <div className="flex items-center justify-center min-h-[240px]">
        <div className="glass-panel px-6 py-4 text-red-600">
          {message || 'Failed to load profile'}
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="page-header">
        <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
          <div>
            <h1 className="text-2xl md:text-3xl font-bold text-white">Teacher Profile</h1>
            <p className="text-white/80">Manage your teaching profile and information</p>
          </div>
          <div className="flex flex-wrap gap-2">
            <Button variant="outline" className="btn-outline" onClick={() => router.push('/teacher/dashboard')}>
              Dashboard
            </Button>
            <Button variant="outline" className="btn-outline" onClick={() => router.push('/teacher/availability')}>
              Availability
            </Button>
            <Button variant="outline" className="btn-outline" onClick={() => router.push('/teacher/bookings')}>
              Bookings
            </Button>
            <Button variant="outline" className="btn-outline" onClick={() => router.push('/teacher/sessions')}>
              Sessions
            </Button>
          </div>
        </div>
      </div>

      {message && (
        <div className="glass-panel border border-white/40 p-4">
          <p className="text-slate-700 dark:text-slate-200">{message}</p>
        </div>
      )}

      <div className="glass-panel border border-white/40 p-6">
        {/* Tab Navigation */}
        <div className="flex flex-wrap gap-2 border-b border-white/30 pb-4">
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
              className={`px-4 py-2 text-sm font-semibold rounded-xl border transition-colors ${
                activeTab === tab.key
                  ? 'bg-white/70 text-emerald-600 border-white/60'
                  : 'text-slate-600 dark:text-slate-300 border-transparent hover:bg-white/40 hover:text-emerald-600'
              }`}
            >
              {tab.label}
            </button>
          ))}
        </div>

        <div className="pt-6">
            {/* Profile Tab */}
            {activeTab === 'profile' && (
              <div className="space-y-6">
                <h2 className="text-lg font-semibold text-slate-900 dark:text-white">Basic Information</h2>
                
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                  <div>
                    <label className="block text-sm font-medium text-slate-700 dark:text-slate-200 mb-2">
                      First Name
                    </label>
                    <input
                      type="text"
                      value={profile.firstName || ''}
                      onChange={(e) => setProfile({ ...profile, firstName: e.target.value })}
                      className="input-modern"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-slate-700 dark:text-slate-200 mb-2">
                      Middle Name
                    </label>
                    <input
                      type="text"
                      value={profile.middleName || ''}
                      onChange={(e) => setProfile({ ...profile, middleName: e.target.value })}
                      className="input-modern"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-slate-700 dark:text-slate-200 mb-2">
                      Last Name
                    </label>
                    <input
                      type="text"
                      value={profile.lastName || ''}
                      onChange={(e) => setProfile({ ...profile, lastName: e.target.value })}
                      className="input-modern"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-slate-700 dark:text-slate-200 mb-2">
                      Mobile Number
                    </label>
                    <input
                      type="tel"
                      value={profile.mobileNumber || ''}
                      onChange={(e) => setProfile({ ...profile, mobileNumber: e.target.value })}
                      className="input-modern"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-slate-700 dark:text-slate-200 mb-2">
                      Contact Email
                    </label>
                    <input
                      type="email"
                      value={profile.contactEmail || ''}
                      onChange={(e) => setProfile({ ...profile, contactEmail: e.target.value })}
                      className="input-modern"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-slate-700 dark:text-slate-200 mb-2">
                      Years of Experience
                    </label>
                    <input
                      type="number"
                      value={profile.yearsOfExperience || ''}
                      onChange={(e) => setProfile({ ...profile, yearsOfExperience: parseInt(e.target.value) })}
                      className="input-modern"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-slate-700 dark:text-slate-200 mb-2">
                      Hourly Rate (INR)
                    </label>
                    <input
                      type="number"
                      value={profile.hourlyRate || ''}
                      onChange={(e) => setProfile({ ...profile, hourlyRate: parseFloat(e.target.value) })}
                      className="input-modern"
                    />
                  </div>
                </div>

                <div>
                  <label className="block text-sm font-medium text-slate-700 dark:text-slate-200 mb-2">
                    Bio
                  </label>
                  <textarea
                    rows={4}
                    value={profile.bio || ''}
                    onChange={(e) => setProfile({ ...profile, bio: e.target.value })}
                    className="input-modern"
                    placeholder="Tell us about yourself and your teaching experience..."
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-slate-700 dark:text-slate-200 mb-2">
                    Specialization
                  </label>
                  <input
                    type="text"
                    value={profile.specialization || ''}
                    onChange={(e) => setProfile({ ...profile, specialization: e.target.value })}
                    className="input-modern"
                    placeholder="e.g., Mathematics, Physics, Chemistry"
                  />
                </div>

                <div className="flex justify-end">
                  <Button
                    onClick={() => updateProfile(profile)}
                    disabled={saving}
                    className="btn-primary"
                  >
                    {saving ? 'Saving...' : 'Save Profile'}
                  </Button>
                </div>
              </div>
            )}

            {/* Qualifications Tab */}
            {activeTab === 'qualifications' && (
              <div className="space-y-6">
                <div className="flex justify-between items-center">
                  <h2 className="text-lg font-semibold text-slate-900 dark:text-white">Qualifications</h2>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <input
                    type="text"
                    placeholder="Degree"
                    value={newQualification.degree}
                    onChange={(e) => setNewQualification({ ...newQualification, degree: e.target.value })}
                    className="input-modern"
                  />
                  <input
                    type="text"
                    placeholder="Specialization"
                    value={newQualification.specialization || ''}
                    onChange={(e) => setNewQualification({ ...newQualification, specialization: e.target.value })}
                    className="input-modern"
                  />
                  <input
                    type="text"
                    placeholder="University"
                    value={newQualification.university || ''}
                    onChange={(e) => setNewQualification({ ...newQualification, university: e.target.value })}
                    className="input-modern"
                  />
                  <input
                    type="number"
                    placeholder="Year"
                    value={newQualification.year || ''}
                    onChange={(e) => setNewQualification({ ...newQualification, year: Number(e.target.value) || undefined })}
                    className="input-modern"
                  />
                </div>
                <Button onClick={addQualification} disabled={saving} className="btn-primary">
                  Add Qualification
                </Button>

                {qualifications.length > 0 ? (
                  <div className="space-y-4">
                    {qualifications.map((qual, index) => (
                      <div key={qual.id || index} className="glass rounded-2xl p-4 border border-white/30 flex items-start justify-between">
                        <div>
                          <h3 className="font-medium text-slate-900 dark:text-white">{qual.degree}</h3>
                          {qual.specialization && (
                            <p className="text-slate-600 dark:text-slate-300">Specialization: {qual.specialization}</p>
                          )}
                          {qual.university && (
                            <p className="text-slate-600 dark:text-slate-300">University: {qual.university}</p>
                          )}
                          {qual.year && (
                            <p className="text-slate-600 dark:text-slate-300">Year: {qual.year}</p>
                          )}
                        </div>
                        <Button
                          variant="outline"
                          size="sm"
                          className="btn-outline text-red-600 border-red-200 hover:border-red-300 hover:text-red-700"
                          onClick={() => deleteQualification(qual.id)}
                        >
                          Delete
                        </Button>
                      </div>
                    ))}
                  </div>
                ) : (
                  <div className="text-center py-8 text-slate-500 dark:text-slate-300">
                    No qualifications found.
                  </div>
                )}
              </div>
            )}

            {activeTab === 'experience' && (
              <div className="space-y-6">
                <h2 className="text-lg font-semibold text-slate-900 dark:text-white">Experience</h2>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <input
                    type="text"
                    placeholder="Institution"
                    value={newExperience.institution || ''}
                    onChange={(e) => setNewExperience({ ...newExperience, institution: e.target.value })}
                    className="input-modern"
                  />
                  <input
                    type="text"
                    placeholder="Role"
                    value={newExperience.role || ''}
                    onChange={(e) => setNewExperience({ ...newExperience, role: e.target.value })}
                    className="input-modern"
                  />
                  <input
                    type="text"
                    placeholder="Subjects taught"
                    value={newExperience.subjectsTaught || ''}
                    onChange={(e) => setNewExperience({ ...newExperience, subjectsTaught: e.target.value })}
                    className="input-modern"
                  />
                  <div className="flex gap-2">
                    <input
                      type="date"
                      value={newExperience.fromDate || ''}
                      onChange={(e) => setNewExperience({ ...newExperience, fromDate: e.target.value })}
                      className="input-modern"
                    />
                    <input
                      type="date"
                      value={newExperience.toDate || ''}
                      onChange={(e) => setNewExperience({ ...newExperience, toDate: e.target.value })}
                      className="input-modern"
                    />
                  </div>
                  <label className="flex items-center gap-2 text-sm text-slate-700 dark:text-slate-200">
                    <input
                      type="checkbox"
                      checked={!!newExperience.currentlyWorking}
                      onChange={(e) => setNewExperience({ ...newExperience, currentlyWorking: e.target.checked })}
                    />
                    Currently working here
                  </label>
                </div>
                <Button onClick={addExperience} disabled={saving} className="btn-primary">
                  Add Experience
                </Button>

                <div className="space-y-3">
                  {experiences.length === 0 ? (
                    <p className="text-slate-500 dark:text-slate-300">No experiences added yet.</p>
                  ) : (
                    experiences.map((exp) => (
                      <div key={exp.id} className="glass rounded-2xl p-4 border border-white/30 flex items-start justify-between">
                        <div>
                          <p className="font-medium text-slate-900 dark:text-white">{exp.role || 'Role'}</p>
                          <p className="text-sm text-slate-600 dark:text-slate-300">{exp.institution}</p>
                          <p className="text-sm text-slate-500 dark:text-slate-300">
                            {exp.fromDate || 'Start'} - {exp.currentlyWorking ? 'Present' : (exp.toDate || 'End')}
                          </p>
                          {exp.subjectsTaught && (
                            <p className="text-sm text-slate-600 dark:text-slate-300">Subjects: {exp.subjectsTaught}</p>
                          )}
                        </div>
                        <Button
                          variant="outline"
                          size="sm"
                          className="btn-outline text-red-600 border-red-200 hover:border-red-300 hover:text-red-700"
                          onClick={() => deleteExperience(exp.id)}
                        >
                          Delete
                        </Button>
                      </div>
                    ))
                  )}
                </div>
              </div>
            )}

            {activeTab === 'certifications' && (
              <div className="space-y-6">
                <h2 className="text-lg font-semibold text-slate-900 dark:text-white">Certifications</h2>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <input
                    type="text"
                    placeholder="Certification name"
                    value={newCertification.certificationName || ''}
                    onChange={(e) => setNewCertification({ ...newCertification, certificationName: e.target.value })}
                    className="input-modern"
                  />
                  <input
                    type="text"
                    placeholder="Issuing authority"
                    value={newCertification.issuingAuthority || ''}
                    onChange={(e) => setNewCertification({ ...newCertification, issuingAuthority: e.target.value })}
                    className="input-modern"
                  />
                  <input
                    type="text"
                    placeholder="Certification ID"
                    value={newCertification.certificationId || ''}
                    onChange={(e) => setNewCertification({ ...newCertification, certificationId: e.target.value })}
                    className="input-modern"
                  />
                  <input
                    type="number"
                    placeholder="Issue year"
                    value={newCertification.issueYear || ''}
                    onChange={(e) => setNewCertification({ ...newCertification, issueYear: Number(e.target.value) || undefined })}
                    className="input-modern"
                  />
                  <input
                    type="date"
                    placeholder="Expiry date"
                    value={newCertification.expiryDate || ''}
                    onChange={(e) => setNewCertification({ ...newCertification, expiryDate: e.target.value })}
                    className="input-modern"
                  />
                </div>
                <Button onClick={addCertification} disabled={saving} className="btn-primary">
                  Add Certification
                </Button>

                <div className="space-y-3">
                  {certifications.length === 0 ? (
                    <p className="text-slate-500 dark:text-slate-300">No certifications added yet.</p>
                  ) : (
                    certifications.map((cert) => (
                      <div key={cert.id} className="glass rounded-2xl p-4 border border-white/30 flex items-start justify-between">
                        <div>
                          <p className="font-medium text-slate-900 dark:text-white">{cert.certificationName}</p>
                          <p className="text-sm text-slate-600 dark:text-slate-300">{cert.issuingAuthority || 'Issuing authority'}</p>
                          <p className="text-sm text-slate-500 dark:text-slate-300">
                            {cert.issueYear ? `Issued ${cert.issueYear}` : 'Issue year not set'}
                          </p>
                        </div>
                        <Button
                          variant="outline"
                          size="sm"
                          className="btn-outline text-red-600 border-red-200 hover:border-red-300 hover:text-red-700"
                          onClick={() => deleteCertification(cert.id)}
                        >
                          Delete
                        </Button>
                      </div>
                    ))
                  )}
                </div>
              </div>
            )}

            {activeTab === 'availability' && (
              <div className="space-y-6">
                <h2 className="text-lg font-semibold text-slate-900 dark:text-white">Availability</h2>
                <p className="text-sm text-slate-600 dark:text-slate-300">
                  Update your general availability details here. For weekly slots, use the Availability page.
                </p>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <input
                    type="time"
                    value={availability?.availableFrom || ''}
                    onChange={(e) => setAvailability({ ...(availability || {}), availableFrom: e.target.value })}
                    className="input-modern"
                  />
                  <input
                    type="time"
                    value={availability?.availableTo || ''}
                    onChange={(e) => setAvailability({ ...(availability || {}), availableTo: e.target.value })}
                    className="input-modern"
                  />
                  <input
                    type="text"
                    placeholder="Preferred student levels"
                    value={availability?.preferredStudentLevels || ''}
                    onChange={(e) => setAvailability({ ...(availability || {}), preferredStudentLevels: e.target.value })}
                    className="input-modern"
                  />
                  <input
                    type="text"
                    placeholder="Languages spoken"
                    value={availability?.languagesSpoken || ''}
                    onChange={(e) => setAvailability({ ...(availability || {}), languagesSpoken: e.target.value })}
                    className="input-modern"
                  />
                </div>
                <div className="flex items-center gap-3">
                  <Button onClick={saveAvailability} disabled={saving} className="btn-primary">
                    Save Availability
                  </Button>
                  <Button variant="outline" className="btn-outline" onClick={() => router.push('/teacher/availability')}>
                    Manage Weekly Slots
                  </Button>
                </div>
              </div>
            )}

            {activeTab === 'addresses' && (
              <div className="space-y-6">
                <h2 className="text-lg font-semibold text-slate-900 dark:text-white">Addresses</h2>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <input
                    type="text"
                    placeholder="Address line 1"
                    value={newAddress.addressLine1}
                    onChange={(e) => setNewAddress({ ...newAddress, addressLine1: e.target.value })}
                    className="input-modern"
                  />
                  <input
                    type="text"
                    placeholder="Address line 2"
                    value={newAddress.addressLine2 || ''}
                    onChange={(e) => setNewAddress({ ...newAddress, addressLine2: e.target.value })}
                    className="input-modern"
                  />
                  <input
                    type="text"
                    placeholder="City"
                    value={newAddress.city}
                    onChange={(e) => setNewAddress({ ...newAddress, city: e.target.value })}
                    className="input-modern"
                  />
                  <input
                    type="text"
                    placeholder="State"
                    value={newAddress.state}
                    onChange={(e) => setNewAddress({ ...newAddress, state: e.target.value })}
                    className="input-modern"
                  />
                  <input
                    type="text"
                    placeholder="ZIP code"
                    value={newAddress.zipCode || ''}
                    onChange={(e) => setNewAddress({ ...newAddress, zipCode: e.target.value })}
                    className="input-modern"
                  />
                  <input
                    type="text"
                    placeholder="Country"
                    value={newAddress.country}
                    onChange={(e) => setNewAddress({ ...newAddress, country: e.target.value })}
                    className="input-modern"
                  />
                  <select
                    value={newAddress.addressType}
                    onChange={(e) => setNewAddress({ ...newAddress, addressType: e.target.value as TeacherAddress['addressType'] })}
                    className="input-modern"
                  >
                    <option value="CURRENT">Current</option>
                    <option value="PERMANENT">Permanent</option>
                  </select>
                </div>
                <Button onClick={addAddress} disabled={saving} className="btn-primary">
                  Add Address
                </Button>

                <div className="space-y-3">
                  {addresses.length === 0 ? (
                    <p className="text-slate-500 dark:text-slate-300">No addresses added yet.</p>
                  ) : (
                    addresses.map((addr) => (
                      <div key={addr.id} className="glass rounded-2xl p-4 border border-white/30 flex items-start justify-between">
                        <div>
                          <p className="font-medium text-slate-900 dark:text-white">{addr.addressType} Address</p>
                          <p className="text-sm text-slate-600 dark:text-slate-300">
                            {addr.addressLine1}{addr.addressLine2 ? `, ${addr.addressLine2}` : ''}
                          </p>
                          <p className="text-sm text-slate-600 dark:text-slate-300">
                            {addr.city}, {addr.state} {addr.zipCode}
                          </p>
                          <p className="text-sm text-slate-600 dark:text-slate-300">{addr.country}</p>
                        </div>
                        <Button
                          variant="outline"
                          size="sm"
                          className="btn-outline text-red-600 border-red-200 hover:border-red-300 hover:text-red-700"
                          onClick={() => deleteAddress(addr.id)}
                        >
                          Delete
                        </Button>
                      </div>
                    ))
                  )}
                </div>
              </div>
            )}

            {activeTab === 'bank-details' && (
              <div className="space-y-6">
                <h2 className="text-lg font-semibold text-slate-900 dark:text-white">Bank Details</h2>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <input
                    type="text"
                    placeholder="Account holder name"
                    value={bankDetails?.accountHolderName || ''}
                    onChange={(e) => setBankDetails({ ...(bankDetails || { accountType: 'SAVINGS' }), accountHolderName: e.target.value })}
                    className="input-modern"
                  />
                  <input
                    type="text"
                    placeholder="Bank name"
                    value={bankDetails?.bankName || ''}
                    onChange={(e) => setBankDetails({ ...(bankDetails || { accountType: 'SAVINGS' }), bankName: e.target.value })}
                    className="input-modern"
                  />
                  <input
                    type="text"
                    placeholder="Branch address"
                    value={bankDetails?.branchAddress || ''}
                    onChange={(e) => setBankDetails({ ...(bankDetails || { accountType: 'SAVINGS' }), branchAddress: e.target.value })}
                    className="input-modern"
                  />
                  <input
                    type="text"
                    placeholder="Account number"
                    value={bankDetails?.accountNumber || ''}
                    onChange={(e) => setBankDetails({ ...(bankDetails || { accountType: 'SAVINGS' }), accountNumber: e.target.value })}
                    className="input-modern"
                  />
                  <input
                    type="text"
                    placeholder="IFSC code"
                    value={bankDetails?.ifscCode || ''}
                    onChange={(e) => setBankDetails({ ...(bankDetails || { accountType: 'SAVINGS' }), ifscCode: e.target.value })}
                    className="input-modern"
                  />
                  <select
                    value={bankDetails?.accountType || 'SAVINGS'}
                    onChange={(e) => setBankDetails({ ...(bankDetails || { accountType: 'SAVINGS' }), accountType: e.target.value as TeacherBankDetails['accountType'] })}
                    className="input-modern"
                  >
                    <option value="SAVINGS">Savings</option>
                    <option value="CURRENT">Current</option>
                    <option value="CHECKING">Checking</option>
                  </select>
                </div>
                <Button onClick={saveBankDetails} disabled={saving} className="btn-primary">
                  Save Bank Details
                </Button>
              </div>
            )}

            {activeTab === 'documents' && (
              <div className="space-y-6">
                <h2 className="text-lg font-semibold text-slate-900 dark:text-white">Documents</h2>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <input
                    type="text"
                    placeholder="Document type"
                    value={newDocument.documentType}
                    onChange={(e) => setNewDocument({ ...newDocument, documentType: e.target.value })}
                    className="input-modern"
                  />
                  <input
                    type="text"
                    placeholder="Document name"
                    value={newDocument.documentName || ''}
                    onChange={(e) => setNewDocument({ ...newDocument, documentName: e.target.value })}
                    className="input-modern"
                  />
                  <input
                    type="url"
                    placeholder="Document URL"
                    value={newDocument.documentUrl}
                    onChange={(e) => setNewDocument({ ...newDocument, documentUrl: e.target.value })}
                    className="input-modern"
                  />
                </div>
                <Button onClick={addDocument} disabled={saving} className="btn-primary">
                  Add Document
                </Button>

                <div className="space-y-3">
                  {documents.length === 0 ? (
                    <p className="text-slate-500 dark:text-slate-300">No documents added yet.</p>
                  ) : (
                    documents.map((doc) => (
                      <div key={doc.id} className="glass rounded-2xl p-4 border border-white/30 flex items-start justify-between">
                        <div>
                          <p className="font-medium text-slate-900 dark:text-white">{doc.documentName || doc.documentType}</p>
                          <p className="text-sm text-slate-600 dark:text-slate-300">{doc.documentType}</p>
                          <a
                            className="text-sm text-emerald-600 hover:text-emerald-700 hover:underline"
                            href={doc.documentUrl}
                            target="_blank"
                            rel="noreferrer"
                          >
                            View document
                          </a>
                        </div>
                        <Button
                          variant="outline"
                          size="sm"
                          className="btn-outline text-red-600 border-red-200 hover:border-red-300 hover:text-red-700"
                          onClick={() => deleteDocument(doc.id)}
                        >
                          Delete
                        </Button>
                      </div>
                    ))
                  )}
                </div>
              </div>
            )}
      </div>
    </div>
  </div>
  );
}
