'use client'

import { useState, useEffect } from 'react'
import AdminLayoutSimple from '@/components/admin-layout-simple'
import { Card } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Badge } from '@/components/ui/badge'
import { 
  CreditCard, 
  Plus, 
  Download, 
  Filter, 
  Search,
  DollarSign,
  Calendar,
  User,
  CheckCircle,
  AlertCircle
} from 'lucide-react'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { Textarea } from '@/components/ui/textarea'

interface FeeWaiver {
  id: number
  bookingId?: number
  userId: number
  userEmail: string
  reason: string
  amount: number
  createdAt: string
}

interface User {
  id: number
  name: string
  email: string
}

interface FeeWaiverStats {
  totalWaivers: number
  waiversLast30Days: number
  totalAmountLast30Days: number
  waiversLast7Days: number
  totalAmountLast7Days: number
}

export default function AdminFeesPage() {
  const [feeWaivers, setFeeWaivers] = useState<FeeWaiver[]>([])
  const [users, setUsers] = useState<User[]>([])
  const [stats, setStats] = useState<FeeWaiverStats | null>(null)
  const [loading, setLoading] = useState(true)
  const [searchTerm, setSearchTerm] = useState('')
  const [showCreateForm, setShowCreateForm] = useState(false)
  const [creating, setCreating] = useState(false)
  
  // Form states
  const [formData, setFormData] = useState({
    userId: '',
    reason: '',
    amount: '',
    notes: ''
  })

  useEffect(() => {
    const checkAuthAndFetch = () => {
      const token = localStorage.getItem('accessToken')
      if (token) {
        fetchFeeWaivers()
        fetchUsers()
        fetchStats()
      } else {
        setTimeout(checkAuthAndFetch, 100)
      }
    }
    checkAuthAndFetch()
  }, [])

  const fetchFeeWaivers = async () => {
    try {
      const token = localStorage.getItem('accessToken')
      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/admin/fees/waivers`, {
        headers: { 'Authorization': `Bearer ${token}` }
      })
      if (response.ok) {
        const data = await response.json()
        setFeeWaivers(data.content || data)
      }
    } catch (error) {
      console.error('Error fetching fee waivers:', error)
    } finally {
      setLoading(false)
    }
  }

  const fetchUsers = async () => {
    try {
      const token = localStorage.getItem('accessToken')
      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/admin/users/students`, {
        headers: { 'Authorization': `Bearer ${token}` }
      })
      if (response.ok) {
        const data = await response.json()
        setUsers(data.content || data)
      }
    } catch (error) {
      console.error('Error fetching users:', error)
    }
  }

  const fetchStats = async () => {
    try {
      const token = localStorage.getItem('accessToken')
      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/admin/fees/waivers/stats`, {
        headers: { 'Authorization': `Bearer ${token}` }
      })
      if (response.ok) {
        const data = await response.json()
        setStats(data)
      }
    } catch (error) {
      console.error('Error fetching fee waiver stats:', error)
    }
  }

  const handleCreateWaiver = async () => {
    try {
      setCreating(true)
      const token = localStorage.getItem('accessToken')
      
      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/admin/fees/waive`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          userId: parseInt(formData.userId),
          reason: formData.reason,
          amount: parseFloat(formData.amount)
        })
      })

      if (response.ok) {
        setFormData({
          userId: '',
          reason: '',
          amount: '',
          notes: ''
        })
        setShowCreateForm(false)
        fetchFeeWaivers()
        fetchStats()
      }
    } catch (error) {
      console.error('Error creating fee waiver:', error)
    } finally {
      setCreating(false)
    }
  }

  const formatDate = (dateString: string) => {
    const date = new Date(dateString)
    const now = new Date()
    const diffInHours = Math.floor((now.getTime() - date.getTime()) / (1000 * 60 * 60))
    
    if (diffInHours < 1) {
      return 'Just now'
    } else if (diffInHours < 24) {
      return `${diffInHours} hour${diffInHours > 1 ? 's' : ''} ago`
    } else {
      const diffInDays = Math.floor(diffInHours / 24)
      return `${diffInDays} day${diffInDays > 1 ? 's' : ''} ago`
    }
  }

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR'
    }).format(amount)
  }

  const filteredWaivers = feeWaivers.filter(waiver => {
    const matchesSearch = waiver.userEmail.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         waiver.reason.toLowerCase().includes(searchTerm.toLowerCase())
    return matchesSearch
  })

  if (loading) {
    return (
      <AdminLayoutSimple>
        <div className="flex items-center justify-center h-64">
          <div className="text-center">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mx-auto mb-2"></div>
            <p className="text-gray-600 dark:text-gray-400">Loading fee waivers...</p>
          </div>
        </div>
      </AdminLayoutSimple>
    )
  }

  return (
    <AdminLayoutSimple>
      <div className="space-y-6">
        {/* Header */}
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold text-gray-900 dark:text-white">Fee Waivers</h1>
            <p className="text-gray-600 dark:text-gray-400">Manage fee waivers and exemptions</p>
          </div>
          <Button 
            onClick={() => setShowCreateForm(!showCreateForm)}
            className="flex items-center space-x-2"
          >
            <Plus className="h-4 w-4" />
            <span>Create Waiver</span>
          </Button>
        </div>

        {/* Stats Cards */}
        {stats && (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-6">
            <Card className="p-6">
              <div className="flex items-center">
                <div className="p-2 bg-blue-100 dark:bg-blue-900 rounded-lg">
                  <CreditCard className="h-6 w-6 text-blue-600 dark:text-blue-400" />
                </div>
                <div className="ml-4">
                  <p className="text-sm font-medium text-gray-600 dark:text-gray-400">Total Waivers</p>
                  <p className="text-2xl font-bold text-gray-900 dark:text-white">
                    {stats.totalWaivers}
                  </p>
                </div>
              </div>
            </Card>

            <Card className="p-6">
              <div className="flex items-center">
                <div className="p-2 bg-green-100 dark:bg-green-900 rounded-lg">
                  <DollarSign className="h-6 w-6 text-green-600 dark:text-green-400" />
                </div>
                <div className="ml-4">
                  <p className="text-sm font-medium text-gray-600 dark:text-gray-400">Last 30 Days</p>
                  <p className="text-2xl font-bold text-gray-900 dark:text-white">
                    {formatCurrency(stats.totalAmountLast30Days)}
                  </p>
                  <p className="text-xs text-gray-600 dark:text-gray-400">
                    {stats.waiversLast30Days} waivers
                  </p>
                </div>
              </div>
            </Card>

            <Card className="p-6">
              <div className="flex items-center">
                <div className="p-2 bg-yellow-100 dark:bg-yellow-900 rounded-lg">
                  <Calendar className="h-6 w-6 text-yellow-600 dark:text-yellow-400" />
                </div>
                <div className="ml-4">
                  <p className="text-sm font-medium text-gray-600 dark:text-gray-400">Last 7 Days</p>
                  <p className="text-2xl font-bold text-gray-900 dark:text-white">
                    {formatCurrency(stats.totalAmountLast7Days)}
                  </p>
                  <p className="text-xs text-gray-600 dark:text-gray-400">
                    {stats.waiversLast7Days} waivers
                  </p>
                </div>
              </div>
            </Card>
          </div>
        )}

        {/* Create Waiver Form */}
        {showCreateForm && (
          <Card className="p-6">
            <div className="space-y-4">
              <h3 className="text-lg font-semibold text-gray-900 dark:text-white">Create Fee Waiver</h3>
              
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <Label htmlFor="userId">Student</Label>
                  <Select value={formData.userId} onValueChange={(value) => setFormData({...formData, userId: value})}>
                    <SelectTrigger>
                      <SelectValue placeholder="Select a student..." />
                    </SelectTrigger>
                    <SelectContent>
                      {users.map((user) => (
                        <SelectItem key={user.id} value={user.id.toString()}>
                          {user.name} ({user.email})
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
                <div>
                  <Label htmlFor="amount">Waiver Amount (₹)</Label>
                  <Input
                    id="amount"
                    type="number"
                    placeholder="Enter amount..."
                    value={formData.amount}
                    onChange={(e) => setFormData({...formData, amount: e.target.value})}
                  />
                </div>
              </div>

              <div>
                <Label htmlFor="reason">Reason</Label>
                <Select value={formData.reason} onValueChange={(value) => setFormData({...formData, reason: value})}>
                  <SelectTrigger>
                    <SelectValue placeholder="Select reason..." />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="Financial Hardship">Financial Hardship</SelectItem>
                    <SelectItem value="Merit Scholarship">Merit Scholarship</SelectItem>
                    <SelectItem value="Referral Bonus">Referral Bonus</SelectItem>
                    <SelectItem value="Promotional Offer">Promotional Offer</SelectItem>
                    <SelectItem value="Other">Other</SelectItem>
                  </SelectContent>
                </Select>
              </div>

              <div>
                <Label htmlFor="notes">Additional Notes</Label>
                <Textarea
                  id="notes"
                  rows={3}
                  placeholder="Enter additional notes..."
                  value={formData.notes}
                  onChange={(e) => setFormData({...formData, notes: e.target.value})}
                />
              </div>

              <div className="flex justify-end space-x-3">
                <Button variant="outline" onClick={() => setShowCreateForm(false)}>
                  Cancel
                </Button>
                <Button 
                  onClick={handleCreateWaiver}
                  disabled={!formData.userId || !formData.reason || !formData.amount || creating}
                  className="flex items-center space-x-2"
                >
                  {creating ? (
                    <>
                      <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2"></div>
                      Creating...
                    </>
                  ) : (
                    <>
                      <CreditCard className="h-4 w-4" />
                      <span>Create Waiver</span>
                    </>
                  )}
                </Button>
              </div>
            </div>
          </Card>
        )}

        {/* Search and Filters */}
        <Card className="p-6">
          <div className="flex items-center space-x-4">
            <div className="flex-1">
              <div className="relative">
                <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 h-4 w-4" />
                <Input
                  placeholder="Search by student email or reason..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  className="pl-10"
                />
              </div>
            </div>
            <Button variant="outline" className="flex items-center space-x-2">
              <Download className="h-4 w-4" />
              <span>Export</span>
            </Button>
          </div>
        </Card>

        {/* Fee Waivers Table */}
        <Card className="p-6">
          <div className="space-y-4">
            <div className="flex items-center justify-between">
              <h3 className="text-lg font-semibold text-gray-900 dark:text-white">Fee Waivers</h3>
              <Badge variant="secondary" className="flex items-center space-x-1">
                <CheckCircle className="h-3 w-3" />
                <span>{filteredWaivers.length} waivers</span>
              </Badge>
            </div>

            {filteredWaivers.length === 0 ? (
              <div className="text-center py-8">
                <CreditCard className="h-12 w-12 text-gray-400 mx-auto mb-4" />
                <h3 className="text-lg font-medium text-gray-900 dark:text-white mb-2">No fee waivers found</h3>
                <p className="text-gray-600 dark:text-gray-400 mb-4">
                  {searchTerm ? 'No waivers match your search criteria.' : 'Get started by creating your first fee waiver.'}
                </p>
                {!searchTerm && (
                  <Button onClick={() => setShowCreateForm(true)} className="flex items-center space-x-2">
                    <Plus className="h-4 w-4" />
                    <span>Create First Waiver</span>
                  </Button>
                )}
              </div>
            ) : (
              <div className="overflow-hidden border border-gray-200 dark:border-gray-700 rounded-lg">
                <table className="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
                  <thead className="bg-gray-50 dark:bg-gray-800">
                    <tr>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                        Student
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                        Amount
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                        Reason
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                        Created
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                        Status
                      </th>
                    </tr>
                  </thead>
                  <tbody className="bg-white dark:bg-gray-900 divide-y divide-gray-200 dark:divide-gray-700">
                    {filteredWaivers.map((waiver) => (
                      <tr key={waiver.id} className="hover:bg-gray-50 dark:hover:bg-gray-800">
                        <td className="px-6 py-4 whitespace-nowrap">
                          <div className="flex items-center">
                            <div className="flex-shrink-0 h-8 w-8">
                              <div className="h-8 w-8 rounded-full bg-blue-100 dark:bg-blue-900 flex items-center justify-center">
                                <User className="h-4 w-4 text-blue-600 dark:text-blue-400" />
                              </div>
                            </div>
                            <div className="ml-4">
                              <div className="text-sm font-medium text-gray-900 dark:text-white">
                                {waiver.userEmail}
                              </div>
                              <div className="text-sm text-gray-500 dark:text-gray-400">
                                ID: {waiver.userId}
                              </div>
                            </div>
                          </div>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          <div className="text-sm font-medium text-gray-900 dark:text-white">
                            {formatCurrency(waiver.amount)}
                          </div>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          <div className="text-sm text-gray-900 dark:text-white">
                            {waiver.reason}
                          </div>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          <div className="text-sm text-gray-900 dark:text-white">
                            {formatDate(waiver.createdAt)}
                          </div>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          <Badge variant="default" className="flex items-center space-x-1">
                            <CheckCircle className="h-3 w-3" />
                            <span>Active</span>
                          </Badge>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </Card>
      </div>
    </AdminLayoutSimple>
  )
}
