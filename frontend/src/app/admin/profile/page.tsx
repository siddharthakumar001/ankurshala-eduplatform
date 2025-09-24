'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useAuthStore } from '@/store/auth';
import { AdminRoute } from '@/components/route-guard';
import { adminAPI } from '@/lib/apiClient';

interface AdminProfile {
  id: number;
  firstName?: string;
  middleName?: string;
  lastName?: string;
  mobileNumber?: string;
  alternateMobileNumber?: string;
  contactEmail?: string;
  department?: string;
  designation?: string;
  employeeId?: string;
  joiningDate?: string;
  reportingManager?: string;
  workLocation?: string;
  emergencyContact?: string;
  profilePhotoUrl?: string;
  permissions?: string; // JSONB stored as string
  lastLoginAt?: string;
  isActive: boolean;
}

export default function AdminProfilePage() {
  const router = useRouter();
  const { user, logout } = useAuthStore();
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState('');
  const [profile, setProfile] = useState<AdminProfile | null>(null);

  useEffect(() => {
    if (!user || user.role !== 'ADMIN') {
      router.push('/login');
      return;
    }
    fetchProfile();
  }, [user, router]);

  const fetchProfile = async () => {
    try {
      setLoading(true);
      const data = await adminAPI.getProfile();
      setProfile(data);
    } catch (error) {
      console.error('Error fetching admin profile:', error);
      if (error instanceof Error && error.message.includes('401')) {
        logout();
        router.push('/login');
      } else {
        setMessage('Error loading profile');
      }
    } finally {
      setLoading(false);
    }
  };

  const updateProfile = async () => {
    if (!profile) return;

    try {
      setSaving(true);
      const data = await adminAPI.updateProfile(profile);
      setProfile(data);
      setMessage('Profile updated successfully!');
      setTimeout(() => setMessage(''), 3000);
    } catch (error) {
      console.error('Error updating admin profile:', error);
      if (error instanceof Error && error.message.includes('401')) {
        logout();
        router.push('/login');
      } else {
        setMessage('Failed to update profile');
      }
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-xl">Loading admin profile...</div>
      </div>
    );
  }

  if (!profile) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-xl text-red-600">Failed to load admin profile</div>
      </div>
    );
  }

  return (
    <AdminRoute>
      <div className="min-h-screen bg-gray-50">
        <div className="max-w-4xl mx-auto px-4 py-8">
          <div className="bg-white rounded-lg shadow-md">
            <div className="px-6 py-4 border-b border-gray-200">
              <h1 className="text-2xl font-bold text-gray-900">Admin Profile</h1>
              <p className="text-gray-600 mt-1">Manage your administrative profile and information</p>
            </div>

          {message && (
            <div className={`mx-6 mt-4 p-4 rounded-md ${
              message.includes('successfully') 
                ? 'bg-green-50 border border-green-200 text-green-800' 
                : 'bg-red-50 border border-red-200 text-red-800'
            }`}>
              <p>{message}</p>
            </div>
          )}

          <div className="p-6">
            <div className="space-y-6">
              {/* Personal Information Section */}
              <div>
                <h2 className="text-lg font-semibold text-gray-900 mb-4">Personal Information</h2>
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
                      placeholder="Enter first name"
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
                      placeholder="Enter middle name (optional)"
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
                      placeholder="Enter last name"
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
                      placeholder="Enter mobile number"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Alternate Mobile
                    </label>
                    <input
                      type="tel"
                      value={profile.alternateMobileNumber || ''}
                      onChange={(e) => setProfile({ ...profile, alternateMobileNumber: e.target.value })}
                      className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                      placeholder="Enter alternate mobile (optional)"
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
                      placeholder="Enter contact email"
                    />
                  </div>
                </div>
              </div>

              {/* Professional Information Section */}
              <div>
                <h2 className="text-lg font-semibold text-gray-900 mb-4">Professional Information</h2>
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Employee ID
                    </label>
                    <input
                      type="text"
                      value={profile.employeeId || ''}
                      onChange={(e) => setProfile({ ...profile, employeeId: e.target.value })}
                      className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                      placeholder="Enter employee ID"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Department
                    </label>
                    <input
                      type="text"
                      value={profile.department || ''}
                      onChange={(e) => setProfile({ ...profile, department: e.target.value })}
                      className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                      placeholder="Enter department"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Designation
                    </label>
                    <input
                      type="text"
                      value={profile.designation || ''}
                      onChange={(e) => setProfile({ ...profile, designation: e.target.value })}
                      className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                      placeholder="Enter designation"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Joining Date
                    </label>
                    <input
                      type="date"
                      value={profile.joiningDate || ''}
                      onChange={(e) => setProfile({ ...profile, joiningDate: e.target.value })}
                      className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Reporting Manager
                    </label>
                    <input
                      type="text"
                      value={profile.reportingManager || ''}
                      onChange={(e) => setProfile({ ...profile, reportingManager: e.target.value })}
                      className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                      placeholder="Enter reporting manager name"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Work Location
                    </label>
                    <input
                      type="text"
                      value={profile.workLocation || ''}
                      onChange={(e) => setProfile({ ...profile, workLocation: e.target.value })}
                      className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                      placeholder="Enter work location"
                    />
                  </div>
                </div>
              </div>

              {/* Emergency Contact Section */}
              <div>
                <h2 className="text-lg font-semibold text-gray-900 mb-4">Emergency Information</h2>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Emergency Contact
                    </label>
                    <input
                      type="text"
                      value={profile.emergencyContact || ''}
                      onChange={(e) => setProfile({ ...profile, emergencyContact: e.target.value })}
                      className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                      placeholder="Enter emergency contact details"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Profile Photo URL
                    </label>
                    <input
                      type="url"
                      value={profile.profilePhotoUrl || ''}
                      onChange={(e) => setProfile({ ...profile, profilePhotoUrl: e.target.value })}
                      className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                      placeholder="Enter profile photo URL"
                    />
                  </div>
                </div>
              </div>

              {/* Account Status Section */}
              <div>
                <h2 className="text-lg font-semibold text-gray-900 mb-4">Account Status</h2>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <div className="flex items-center">
                    <input
                      type="checkbox"
                      id="isActive"
                      checked={profile.isActive}
                      onChange={(e) => setProfile({ ...profile, isActive: e.target.checked })}
                      className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                    />
                    <label htmlFor="isActive" className="ml-2 block text-sm text-gray-900">
                      Active Account
                    </label>
                  </div>

                  {profile.lastLoginAt && (
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-2">
                        Last Login
                      </label>
                      <p className="text-sm text-gray-600">
                        {new Date(profile.lastLoginAt).toLocaleString()}
                      </p>
                    </div>
                  )}
                </div>
              </div>

              {/* Permissions Section (Read-only) */}
              {profile.permissions && (
                <div>
                  <h2 className="text-lg font-semibold text-gray-900 mb-4">Permissions</h2>
                  <div className="bg-gray-50 p-4 rounded-md">
                    <pre className="text-sm text-gray-700 whitespace-pre-wrap">
                      {JSON.stringify(JSON.parse(profile.permissions), null, 2)}
                    </pre>
                  </div>
                </div>
              )}

              {/* Save Button */}
              <div className="flex justify-end pt-6">
                <button
                  onClick={updateProfile}
                  disabled={saving}
                  className="px-6 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  {saving ? 'Saving...' : 'Save Profile'}
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    </AdminRoute>
  );
}
