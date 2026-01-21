'use client'

import { useState, useEffect } from 'react'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Label } from '@/components/ui/label'
import { Switch } from '@/components/ui/switch'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog'
import { 
  Calendar, 
  Plus, 
  Edit, 
  Trash2, 
  Save, 
  X,
  Check,
  Loader2,
  Settings
} from 'lucide-react'
import { toast } from 'sonner'
import { api } from '@/utils/api'
import { TeacherRoute } from '@/components/route-guard'

interface WeeklyAvailability {
  id?: number
  dayOfWeek: number
  startTime: string
  endTime: string
  isAvailable: boolean
}

interface BookingPreferences {
  id?: number
  autoAcceptBookings: boolean
  advanceBookingDays: number
  minimumSessionDuration: number
  maximumSessionDuration: number
  cancellationPolicyHours: number
}

const DAYS_OF_WEEK = [
  { value: 0, label: 'Sunday' },
  { value: 1, label: 'Monday' },
  { value: 2, label: 'Tuesday' },
  { value: 3, label: 'Wednesday' },
  { value: 4, label: 'Thursday' },
  { value: 5, label: 'Friday' },
  { value: 6, label: 'Saturday' }
]

const TIME_SLOTS = [
  '06:00', '06:30', '07:00', '07:30', '08:00', '08:30', '09:00', '09:30',
  '10:00', '10:30', '11:00', '11:30', '12:00', '12:30', '13:00', '13:30',
  '14:00', '14:30', '15:00', '15:30', '16:00', '16:30', '17:00', '17:30',
  '18:00', '18:30', '19:00', '19:30', '20:00', '20:30', '21:00', '21:30',
  '22:00', '22:30', '23:00', '23:30'
]

export default function TeacherAvailabilityPage() {
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [weeklyAvailability, setWeeklyAvailability] = useState<WeeklyAvailability[]>([])
  const [bookingPreferences, setBookingPreferences] = useState<BookingPreferences>({
    autoAcceptBookings: false,
    advanceBookingDays: 7,
    minimumSessionDuration: 30,
    maximumSessionDuration: 120,
    cancellationPolicyHours: 24
  })
  const [editingSlot, setEditingSlot] = useState<WeeklyAvailability | null>(null)
  const [addDay, setAddDay] = useState<number | null>(null)

  useEffect(() => {
    fetchAvailabilityData()
  }, [])

  const fetchAvailabilityData = async () => {
    try {
      setLoading(true)
      
      // Fetch weekly availability
      const availabilityResponse = await api.get('/teacher/availability/weekly')
      setWeeklyAvailability((availabilityResponse.data as any) || [])
      
      // Fetch booking preferences
      const preferencesResponse = await api.get('/teacher/preferences/booking')
      setBookingPreferences((preferencesResponse.data as any) || bookingPreferences)
      
    } catch (error) {
      console.error('Error fetching availability data:', error)
      toast.error('Failed to load availability data')
    } finally {
      setLoading(false)
    }
  }

  const handleSaveAvailability = async () => {
    try {
      setSaving(true)
      await api.put('/teacher/availability/weekly', weeklyAvailability)
      toast.success('Availability updated successfully')
    } catch (error) {
      console.error('Error saving availability:', error)
      toast.error('Failed to save availability')
    } finally {
      setSaving(false)
    }
  }

  const handleSavePreferences = async () => {
    try {
      setSaving(true)
      await api.put('/teacher/preferences/booking', bookingPreferences)
      toast.success('Booking preferences updated successfully')
    } catch (error) {
      console.error('Error saving preferences:', error)
      toast.error('Failed to save preferences')
    } finally {
      setSaving(false)
    }
  }

  const addAvailabilitySlot = (slot: WeeklyAvailability) => {
    setWeeklyAvailability([...weeklyAvailability, slot])
    setAddDay(null)
  }

  const updateAvailabilitySlot = (updatedSlot: WeeklyAvailability) => {
    setWeeklyAvailability(weeklyAvailability.map(slot => 
      slot.id === updatedSlot.id ? updatedSlot : slot
    ))
    setEditingSlot(null)
  }

  const removeAvailabilitySlot = (slotId: number) => {
    setWeeklyAvailability(weeklyAvailability.filter(slot => slot.id !== slotId))
  }

  const toggleAvailability = (slotId: number) => {
    setWeeklyAvailability(weeklyAvailability.map(slot => 
      slot.id === slotId ? { ...slot, isAvailable: !slot.isAvailable } : slot
    ))
  }

  const getAvailabilityForDay = (dayOfWeek: number) => {
    return weeklyAvailability.filter(slot => slot.dayOfWeek === dayOfWeek)
  }

  if (loading) {
    return (
      <TeacherRoute>
        <div className="space-y-6">
          <div className="glass-panel p-6 flex items-center justify-center">
            <Loader2 className="h-6 w-6 animate-spin text-emerald-500" />
            <span className="ml-2 text-slate-600 dark:text-slate-200">Loading availability...</span>
          </div>
        </div>
      </TeacherRoute>
    )
  }

  return (
    <TeacherRoute>
      <div className="space-y-6">
        <div className="page-header">
          <h1 className="text-2xl md:text-3xl font-bold text-white">Availability & Preferences</h1>
          <p className="text-white/80">Manage your teaching schedule and booking preferences</p>
        </div>

          <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
            {/* Weekly Availability */}
            <Card className="glass-panel border border-white/40">
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <Calendar className="h-5 w-5 text-emerald-500" />
                  Weekly Availability
                </CardTitle>
                <CardDescription>Set your available time slots for each day of the week</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="space-y-6">
                  {DAYS_OF_WEEK.map((day) => {
                    const daySlots = getAvailabilityForDay(day.value)
                    return (
                      <div key={day.value} className="glass rounded-2xl p-4 border border-white/30">
                        <div className="flex justify-between items-center mb-4">
                          <h3 className="font-medium text-slate-900 dark:text-white">{day.label}</h3>
                          <Dialog open={addDay === day.value} onOpenChange={(open) => setAddDay(open ? day.value : null)}>
                            <DialogTrigger asChild>
                              <Button size="sm" variant="outline" className="btn-outline">
                                <Plus className="h-4 w-4 mr-1" />
                                Add Slot
                              </Button>
                            </DialogTrigger>
                            <DialogContent className="modal-content">
                              <DialogHeader>
                                <DialogTitle>Add Availability Slot</DialogTitle>
                              </DialogHeader>
                              <AddAvailabilitySlotForm
                                dayOfWeek={day.value}
                                onAdd={addAvailabilitySlot}
                                onCancel={() => setAddDay(null)}
                              />
                            </DialogContent>
                          </Dialog>
                        </div>
                        
                        {daySlots.length === 0 ? (
                          <p className="text-slate-500 dark:text-slate-300 text-center py-4">No availability set</p>
                        ) : (
                          <div className="space-y-2">
                            {daySlots.map((slot) => (
                              <div key={slot.id} className="flex items-center justify-between p-3 glass rounded-xl border border-white/30">
                                <div className="flex items-center gap-4">
                                  <Switch
                                    checked={slot.isAvailable}
                                    onCheckedChange={() => slot.id && toggleAvailability(slot.id)}
                                  />
                                  <div>
                                    <span className="font-medium text-slate-900 dark:text-white">
                                      {slot.startTime} - {slot.endTime}
                                    </span>
                                    <span className={`ml-2 text-sm ${slot.isAvailable ? 'text-emerald-600' : 'text-red-600'}`}>
                                      {slot.isAvailable ? 'Available' : 'Unavailable'}
                                    </span>
                                  </div>
                                </div>
                                <div className="flex gap-2">
                                  <Button
                                    size="sm"
                                    variant="outline"
                                    className="btn-outline"
                                    onClick={() => setEditingSlot(slot)}
                                  >
                                    <Edit className="h-4 w-4" />
                                  </Button>
                                  <Button
                                    size="sm"
                                    variant="outline"
                                    onClick={() => slot.id && removeAvailabilitySlot(slot.id)}
                                    className="btn-outline text-red-600 border-red-200 hover:border-red-300 hover:text-red-700"
                                  >
                                    <Trash2 className="h-4 w-4" />
                                  </Button>
                                </div>
                              </div>
                            ))}
                          </div>
                        )}
                      </div>
                    )
                  })}
                </div>
                
                <div className="mt-6">
                  <Button 
                    onClick={handleSaveAvailability}
                    disabled={saving}
                    className="btn-primary w-full"
                  >
                    {saving ? (
                      <>
                        <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                        Saving...
                      </>
                    ) : (
                      <>
                        <Save className="h-4 w-4 mr-2" />
                        Save Availability
                      </>
                    )}
                  </Button>
                </div>
              </CardContent>
            </Card>

            {/* Booking Preferences */}
            <Card className="glass-panel border border-white/40">
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <Settings className="h-5 w-5 text-emerald-500" />
                  Booking Preferences
                </CardTitle>
                <CardDescription>Configure how students can book sessions with you</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="space-y-6">
                  <div className="flex items-center justify-between">
                    <div>
                      <Label htmlFor="auto-accept">Auto-accept bookings</Label>
                      <p className="text-sm text-slate-500 dark:text-slate-300">Automatically accept booking requests</p>
                    </div>
                    <Switch
                      id="auto-accept"
                      checked={bookingPreferences.autoAcceptBookings}
                      onCheckedChange={(checked) => 
                        setBookingPreferences({ ...bookingPreferences, autoAcceptBookings: checked })
                      }
                    />
                  </div>

                  <div>
                    <Label htmlFor="advance-booking">Advance booking days</Label>
                    <Select
                      value={bookingPreferences.advanceBookingDays.toString()}
                      onValueChange={(value) => 
                        setBookingPreferences({ ...bookingPreferences, advanceBookingDays: parseInt(value) })
                      }
                    >
                      <SelectTrigger className="input-modern">
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="1">1 day</SelectItem>
                        <SelectItem value="3">3 days</SelectItem>
                        <SelectItem value="7">7 days</SelectItem>
                        <SelectItem value="14">14 days</SelectItem>
                        <SelectItem value="30">30 days</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>

                  <div>
                    <Label htmlFor="min-duration">Minimum session duration (minutes)</Label>
                    <Select
                      value={bookingPreferences.minimumSessionDuration.toString()}
                      onValueChange={(value) => 
                        setBookingPreferences({ ...bookingPreferences, minimumSessionDuration: parseInt(value) })
                      }
                    >
                      <SelectTrigger className="input-modern">
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="30">30 minutes</SelectItem>
                        <SelectItem value="45">45 minutes</SelectItem>
                        <SelectItem value="60">60 minutes</SelectItem>
                        <SelectItem value="90">90 minutes</SelectItem>
                        <SelectItem value="120">120 minutes</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>

                  <div>
                    <Label htmlFor="max-duration">Maximum session duration (minutes)</Label>
                    <Select
                      value={bookingPreferences.maximumSessionDuration.toString()}
                      onValueChange={(value) => 
                        setBookingPreferences({ ...bookingPreferences, maximumSessionDuration: parseInt(value) })
                      }
                    >
                      <SelectTrigger className="input-modern">
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="60">60 minutes</SelectItem>
                        <SelectItem value="90">90 minutes</SelectItem>
                        <SelectItem value="120">120 minutes</SelectItem>
                        <SelectItem value="180">180 minutes</SelectItem>
                        <SelectItem value="240">240 minutes</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>

                  <div>
                    <Label htmlFor="cancellation-policy">Cancellation policy (hours)</Label>
                    <Select
                      value={bookingPreferences.cancellationPolicyHours.toString()}
                      onValueChange={(value) => 
                        setBookingPreferences({ ...bookingPreferences, cancellationPolicyHours: parseInt(value) })
                      }
                    >
                      <SelectTrigger className="input-modern">
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="1">1 hour</SelectItem>
                        <SelectItem value="6">6 hours</SelectItem>
                        <SelectItem value="12">12 hours</SelectItem>
                        <SelectItem value="24">24 hours</SelectItem>
                        <SelectItem value="48">48 hours</SelectItem>
                        <SelectItem value="72">72 hours</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>
                </div>
                
                <div className="mt-6">
                  <Button 
                    onClick={handleSavePreferences}
                    disabled={saving}
                    className="btn-primary w-full"
                  >
                    {saving ? (
                      <>
                        <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                        Saving...
                      </>
                    ) : (
                      <>
                        <Save className="h-4 w-4 mr-2" />
                        Save Preferences
                      </>
                    )}
                  </Button>
                </div>
              </CardContent>
            </Card>
          </div>

          {/* Edit Slot Dialog */}
          {editingSlot && (
            <Dialog open={!!editingSlot} onOpenChange={() => setEditingSlot(null)}>
              <DialogContent className="modal-content">
                <DialogHeader>
                  <DialogTitle>Edit Availability Slot</DialogTitle>
                </DialogHeader>
                <EditAvailabilitySlotForm
                  slot={editingSlot}
                  onUpdate={updateAvailabilitySlot}
                  onCancel={() => setEditingSlot(null)}
                />
              </DialogContent>
            </Dialog>
          )}
      </div>
    </TeacherRoute>
  )
}

// Add Availability Slot Form Component
function AddAvailabilitySlotForm({ 
  dayOfWeek, 
  onAdd, 
  onCancel 
}: { 
  dayOfWeek: number
  onAdd: (slot: WeeklyAvailability) => void
  onCancel: () => void
}) {
  const [startTime, setStartTime] = useState('09:00')
  const [endTime, setEndTime] = useState('17:00')
  const [isAvailable, setIsAvailable] = useState(true)

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    onAdd({
      dayOfWeek,
      startTime,
      endTime,
      isAvailable
    })
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <div>
        <Label htmlFor="start-time">Start Time</Label>
        <Select value={startTime} onValueChange={setStartTime}>
        <SelectTrigger className="input-modern">
          <SelectValue />
        </SelectTrigger>
          <SelectContent>
            {TIME_SLOTS.map((time) => (
              <SelectItem key={time} value={time}>{time}</SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>

      <div>
        <Label htmlFor="end-time">End Time</Label>
        <Select value={endTime} onValueChange={setEndTime}>
        <SelectTrigger className="input-modern">
          <SelectValue />
        </SelectTrigger>
          <SelectContent>
            {TIME_SLOTS.map((time) => (
              <SelectItem key={time} value={time}>{time}</SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>

      <div className="flex items-center space-x-2">
        <Switch
          id="available"
          checked={isAvailable}
          onCheckedChange={setIsAvailable}
        />
        <Label htmlFor="available">Available</Label>
      </div>

      <div className="flex gap-2">
        <Button type="submit" className="btn-primary flex-1">
          <Check className="h-4 w-4 mr-2" />
          Add Slot
        </Button>
        <Button type="button" variant="outline" onClick={onCancel} className="btn-outline flex-1">
          <X className="h-4 w-4 mr-2" />
          Cancel
        </Button>
      </div>
    </form>
  )
}

// Edit Availability Slot Form Component
function EditAvailabilitySlotForm({ 
  slot, 
  onUpdate, 
  onCancel 
}: { 
  slot: WeeklyAvailability
  onUpdate: (slot: WeeklyAvailability) => void
  onCancel: () => void
}) {
  const [startTime, setStartTime] = useState(slot.startTime)
  const [endTime, setEndTime] = useState(slot.endTime)
  const [isAvailable, setIsAvailable] = useState(slot.isAvailable)

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    onUpdate({
      ...slot,
      startTime,
      endTime,
      isAvailable
    })
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <div>
        <Label htmlFor="start-time">Start Time</Label>
        <Select value={startTime} onValueChange={setStartTime}>
        <SelectTrigger className="input-modern">
          <SelectValue />
        </SelectTrigger>
          <SelectContent>
            {TIME_SLOTS.map((time) => (
              <SelectItem key={time} value={time}>{time}</SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>

      <div>
        <Label htmlFor="end-time">End Time</Label>
        <Select value={endTime} onValueChange={setEndTime}>
        <SelectTrigger className="input-modern">
          <SelectValue />
        </SelectTrigger>
          <SelectContent>
            {TIME_SLOTS.map((time) => (
              <SelectItem key={time} value={time}>{time}</SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>

      <div className="flex items-center space-x-2">
        <Switch
          id="available"
          checked={isAvailable}
          onCheckedChange={setIsAvailable}
        />
        <Label htmlFor="available">Available</Label>
      </div>

      <div className="flex gap-2">
        <Button type="submit" className="btn-primary flex-1">
          <Check className="h-4 w-4 mr-2" />
          Update Slot
        </Button>
        <Button type="button" variant="outline" onClick={onCancel} className="btn-outline flex-1">
          <X className="h-4 w-4 mr-2" />
          Cancel
        </Button>
      </div>
    </form>
  )
}
