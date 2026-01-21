'use client'

import { useEffect, useState } from 'react'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Loader2, TrendingUp, Calendar } from 'lucide-react'
import { api } from '@/utils/api'
import { toast } from 'sonner'
import { TeacherRoute } from '@/components/route-guard'

interface EarningsPeriod {
  period: string
  amount: number
  classCount: number
}

interface EarningsBySubject {
  subjectId: number
  subjectName: string
  amount: number
  classCount: number
  averagePerClass: number
}

interface TeacherEarningsResponse {
  totalEarnings: number
  thisMonthEarnings: number
  lastMonthEarnings: number
  averagePerClass: number
  totalClasses: number
  earningsByPeriod: EarningsPeriod[]
  earningsBySubject: EarningsBySubject[]
}

export default function TeacherEarningsPage() {
  const [loading, setLoading] = useState(true)
  const [data, setData] = useState<TeacherEarningsResponse | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    const fetchEarnings = async () => {
      try {
        setLoading(true)
        setError(null)
        const response = await api.get<TeacherEarningsResponse>('/dashboard/teacher/earnings')
        setData(response.data)
      } catch (err) {
        console.error('Failed to load earnings:', err)
        setError('Unable to load earnings. Please try again.')
        toast.error('Failed to load earnings')
      } finally {
        setLoading(false)
      }
    }

    fetchEarnings()
  }, [])

  const formatCurrency = (amount?: number | string | null) => {
    const safeAmount = typeof amount === 'number' ? amount : Number(amount || 0)
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
      maximumFractionDigits: 0
    }).format(safeAmount || 0)
  }

  if (loading) {
    return (
      <TeacherRoute>
        <div className="space-y-6">
          <div className="glass-panel p-6 flex items-center justify-center">
            <Loader2 className="h-6 w-6 animate-spin text-emerald-500" />
            <span className="ml-2 text-slate-600 dark:text-slate-200">Loading earnings...</span>
          </div>
        </div>
      </TeacherRoute>
    )
  }

  if (error || !data) {
    return (
      <TeacherRoute>
        <div className="space-y-6">
          <Card className="glass-panel border border-white/40">
            <CardContent className="p-6 flex flex-col items-center gap-4">
              <p className="text-slate-700 dark:text-slate-200">{error || 'No earnings data available.'}</p>
              <Button onClick={() => window.location.reload()} className="btn-primary">Retry</Button>
            </CardContent>
          </Card>
        </div>
      </TeacherRoute>
    )
  }

  return (
    <TeacherRoute>
      <div className="space-y-6">
        <div className="page-header">
          <h1 className="text-2xl md:text-3xl font-bold text-white">Earnings</h1>
          <p className="text-white/80">Track your income and teaching performance</p>
        </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
            <Card className="glass-panel border border-white/40">
              <CardHeader className="pb-2">
                <CardDescription>Total Earnings</CardDescription>
                <CardTitle className="text-2xl text-emerald-500">{formatCurrency(data.totalEarnings)}</CardTitle>
              </CardHeader>
              <CardContent className="text-sm text-slate-500 dark:text-slate-300">All time</CardContent>
            </Card>
            <Card className="glass-panel border border-white/40">
              <CardHeader className="pb-2">
                <CardDescription>This Month</CardDescription>
                <CardTitle className="text-2xl text-emerald-500">{formatCurrency(data.thisMonthEarnings)}</CardTitle>
              </CardHeader>
              <CardContent className="text-sm text-slate-500 dark:text-slate-300">Current month</CardContent>
            </Card>
            <Card className="glass-panel border border-white/40">
              <CardHeader className="pb-2">
                <CardDescription>Last Month</CardDescription>
                <CardTitle className="text-2xl text-emerald-500">{formatCurrency(data.lastMonthEarnings)}</CardTitle>
              </CardHeader>
              <CardContent className="text-sm text-slate-500 dark:text-slate-300">Previous month</CardContent>
            </Card>
            <Card className="glass-panel border border-white/40">
              <CardHeader className="pb-2">
                <CardDescription>Average per Class</CardDescription>
                <CardTitle className="text-2xl text-emerald-500">{formatCurrency(data.averagePerClass)}</CardTitle>
              </CardHeader>
              <CardContent className="text-sm text-slate-500 dark:text-slate-300">{data.totalClasses} classes</CardContent>
            </Card>
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            <Card className="glass-panel border border-white/40">
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <TrendingUp className="h-5 w-5 text-emerald-500" />
                  Earnings by Period
                </CardTitle>
                <CardDescription>Monthly earnings summary</CardDescription>
              </CardHeader>
              <CardContent>
                {data.earningsByPeriod?.length ? (
                  <div className="space-y-3">
                    {data.earningsByPeriod.map((period) => (
                      <div key={period.period} className="flex items-center justify-between">
                        <div>
                          <p className="font-medium text-slate-900 dark:text-white">{period.period}</p>
                          <p className="text-sm text-slate-500 dark:text-slate-300">{period.classCount} classes</p>
                        </div>
                        <p className="font-semibold text-emerald-500">{formatCurrency(period.amount)}</p>
                      </div>
                    ))}
                  </div>
                ) : (
                  <p className="text-sm text-slate-500 dark:text-slate-300">No earnings data for this period.</p>
                )}
              </CardContent>
            </Card>

            <Card className="glass-panel border border-white/40">
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <Calendar className="h-5 w-5 text-emerald-500" />
                  Earnings by Subject
                </CardTitle>
                <CardDescription>Performance across subjects</CardDescription>
              </CardHeader>
              <CardContent>
                {data.earningsBySubject?.length ? (
                  <div className="space-y-3">
                    {data.earningsBySubject.map((subject) => (
                      <div key={subject.subjectId} className="flex items-center justify-between">
                        <div>
                          <p className="font-medium text-slate-900 dark:text-white">{subject.subjectName}</p>
                          <p className="text-sm text-slate-500 dark:text-slate-300">{subject.classCount} classes</p>
                        </div>
                        <p className="font-semibold text-emerald-500">{formatCurrency(subject.amount)}</p>
                      </div>
                    ))}
                  </div>
                ) : (
                  <p className="text-sm text-slate-500 dark:text-slate-300">No subject breakdown available.</p>
                )}
              </CardContent>
            </Card>
          </div>
      </div>
    </TeacherRoute>
  )
}
