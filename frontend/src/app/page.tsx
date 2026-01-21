'use client'

import { useEffect } from 'react'
import { useRouter } from 'next/navigation'
import Image from 'next/image'
import Link from 'next/link'
import { Button } from '@/components/ui/button'
import { 
  CalendarDays, 
  ShieldCheck, 
  Brain, 
  GraduationCap, 
  Users, 
  Clock,
  Star,
  ArrowRight,
  BookOpen,
  Trophy,
  Target,
  Sparkles,
  Moon,
  Sun
} from 'lucide-react'
import { authManager } from '@/utils/auth'
import { api } from '@/utils/api'
import { useTheme } from '@/components/theme-provider'

const USER_DASHBOARD_ROUTES = {
  ADMIN: '/admin/dashboard',
  TEACHER: '/teacher/profile',
  STUDENT: '/student/dashboard'
} as const

export default function Home() {
  const router = useRouter()
  const { theme, setTheme } = useTheme()

  useEffect(() => {
    // Check if user is authenticated and redirect to appropriate dashboard
    const checkAuthAndRedirect = async () => {
      try {
        if (authManager.isAuthenticated()) {
          try {
            const response = await api.get('/user/me')
            const userRole = (response.data as any).role
            
            // Redirect to appropriate dashboard based on role
            const redirectTo = USER_DASHBOARD_ROUTES[userRole as keyof typeof USER_DASHBOARD_ROUTES]
            if (redirectTo) {
              router.replace(redirectTo)
            }
          } catch (error) {
            // Token is invalid, clear it and stay on homepage
            console.log('Token validation failed, clearing auth state')
            authManager.logout()
          }
        }
      } catch (error) {
        console.error('Auth check error:', error)
        authManager.logout()
      }
    }

    checkAuthAndRedirect()
  }, [router])

  const toggleTheme = () => {
    setTheme(theme === 'dark' ? 'light' : 'dark')
  }

  const stats = [
    { label: 'Active Students', value: '5,000+', icon: GraduationCap },
    { label: 'Expert Teachers', value: '500+', icon: Users },
    { label: 'Sessions Completed', value: '25,000+', icon: Clock },
    { label: 'Average Rating', value: '4.9/5', icon: Star },
  ]

  const features = [
    {
      icon: CalendarDays,
      title: 'Book by Topic & Time',
      description: 'Pick a subject, choose a topic, select your slot. Teachers accept in real time.',
      color: 'bg-ankur-primary/10 text-ankur-primary'
    },
    {
      icon: ShieldCheck,
      title: 'Verified Teachers',
      description: 'Background-checked experts across CBSE, ICSE, IB and Cambridge. Quality you can trust.',
      color: 'bg-ankur-secondary/10 text-ankur-secondary'
    },
    {
      icon: Brain,
      title: 'AI-Enabled Learning',
      description: 'Personalized prep lists, weak-area insights, and after-class summaries powered by AI.',
      color: 'bg-ankur-accent/10 text-ankur-accent'
    },
    {
      icon: Target,
      title: 'Goal-Oriented',
      description: 'Track progress with personalized learning paths designed to meet your academic goals.',
      color: 'bg-emerald-100 text-emerald-600'
    },
    {
      icon: Trophy,
      title: 'Proven Results',
      description: 'Students improve their grades by 25% on average within the first month.',
      color: 'bg-orange-100 text-orange-600'
    },
    {
      icon: Sparkles,
      title: 'Interactive Sessions',
      description: 'Engage with live whiteboard, screen sharing, and real-time problem solving.',
      color: 'bg-rose-100 text-rose-600'
    }
  ]

  const subjects = [
    { name: 'Mathematics', code: 'M' },
    { name: 'Physics', code: 'P' },
    { name: 'Chemistry', code: 'C' },
    { name: 'Biology', code: 'B' },
    { name: 'English', code: 'E' },
    { name: 'Computer Science', code: 'CS' },
    { name: 'Economics', code: 'Ec' },
    { name: 'Social Science', code: 'SS' },
  ]

  const boards = ['CBSE', 'ICSE', 'IB', 'Cambridge', 'State Boards']

  return (
    <main className="min-h-screen bg-transparent text-foreground">
      {/* TOP NAV */}
      <header className="sticky top-0 z-50 bg-white/70 backdrop-blur-2xl border-b border-white/40 dark:bg-slate-900/70 dark:border-white/10">
        <div className="container mx-auto flex items-center justify-between px-6 py-4">
          <Link href="/" className="flex items-center gap-3">
            <Image src="/ankurshala-logo-small.png" width={48} height={48} alt="Ankurshala" priority className="rounded-lg" />
            <span className="text-2xl font-display font-semibold text-ankur-secondary dark:text-white">Ankurshala</span>
          </Link>
          <nav className="hidden md:flex items-center gap-8">
            <Link href="#features" className="text-gray-600 hover:text-ankur-primary transition-colors dark:text-gray-200">Features</Link>
            <Link href="#subjects" className="text-gray-600 hover:text-ankur-primary transition-colors dark:text-gray-200">Subjects</Link>
            <Link href="#boards" className="text-gray-600 hover:text-ankur-primary transition-colors dark:text-gray-200">Boards</Link>
          </nav>
          <div className="hidden md:flex items-center gap-3">
            <Button variant="ghost" onClick={toggleTheme} className="text-gray-600 hover:text-ankur-primary dark:text-gray-200">
              {theme === 'dark' ? <Sun className="h-5 w-5" /> : <Moon className="h-5 w-5" />}
            </Button>
            <Button variant="ghost" asChild className="text-gray-600 hover:text-ankur-primary dark:text-gray-200">
              <Link href="/login">Login</Link>
            </Button>
            <Button className="btn-primary" asChild>
              <Link href="/register-student">Get Started</Link>
            </Button>
          </div>
          {/* Mobile menu button */}
          <Button variant="ghost" className="md:hidden" size="icon">
            <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" />
            </svg>
          </Button>
        </div>
      </header>

      {/* HERO SECTION */}
      <section className="relative overflow-hidden">
        {/* Background gradient */}
        <div className="absolute inset-0 brand-gradient" />
        <div className="absolute inset-0 bg-[url('/grid.svg')] opacity-10" />
        
        <div className="relative container mx-auto px-6 py-20 md:py-28">
          <div className="flex flex-col lg:flex-row items-center gap-12">
            {/* Left content */}
            <div className="lg:w-1/2 text-center lg:text-left">
              <div className="inline-flex items-center gap-2 bg-white/10 backdrop-blur px-4 py-2 rounded-full text-white/90 text-sm mb-6">
                <Sparkles className="h-4 w-4 text-ankur-accent" />
                <span>India's Most Trusted Learning Platform</span>
              </div>
              
              <h1 className="text-4xl md:text-5xl lg:text-6xl font-display font-semibold text-white leading-tight mb-6">
                On Demand Learning
                <br />
                <span className="bg-gradient-to-r from-ankur-primary to-ankur-accent bg-clip-text text-transparent">
                  for Real Results
                </span>
              </h1>
              
              <p className="text-lg md:text-xl text-white/80 max-w-lg mb-8">
                Connect with verified tutors for live, 1-on-1 sessions, powered by AI-driven insights and scheduled around your calendar.
              </p>
              
              <div className="flex flex-col sm:flex-row gap-4 justify-center lg:justify-start mb-12">
                <Button size="lg" className="btn-primary text-base px-8" asChild>
                  <Link href="/register-student">
                    Start Learning
                    <ArrowRight className="ml-2 h-5 w-5" />
                  </Link>
                </Button>
                <Button size="lg" variant="outline" className="border-white/30 text-white hover:bg-white/10 text-base px-8" asChild>
                  <Link href="/register-teacher">Become a Teacher</Link>
                </Button>
              </div>

              {/* Stats */}
              <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
                {stats.map((stat) => (
                  <div key={stat.label} className="bg-white/10 backdrop-blur rounded-2xl p-4 text-center border border-white/20">
                    <stat.icon className="h-6 w-6 text-ankur-accent mx-auto mb-2" />
                    <div className="text-2xl font-bold text-white">{stat.value}</div>
                    <div className="text-xs text-white/70">{stat.label}</div>
                  </div>
                ))}
              </div>
            </div>
            
            {/* Right content - Brand image */}
            <div className="lg:w-1/2 flex justify-center">
              <div className="relative">
                {/* Decorative circles */}
                <div className="absolute -top-8 -left-8 w-72 h-72 bg-ankur-primary/20 rounded-full blur-3xl" />
                <div className="absolute -bottom-8 -right-8 w-72 h-72 bg-ankur-accent/20 rounded-full blur-3xl" />
                
                {/* Main brand card */}
                <div className="relative bg-white/10 backdrop-blur-md rounded-3xl p-8 border border-white/20">
                  <div className="w-64 h-64 md:w-80 md:h-80 rounded-2xl bg-gradient-to-br from-white/20 to-white/5 flex items-center justify-center">
                    <Image 
                      src="/ankurshala-logo-large.png" 
                      width={200} 
                      height={200} 
                      alt="Ankurshala Brand" 
                      className="drop-shadow-2xl rounded-2xl"
                    />
                  </div>
                  <div className="mt-6 text-center">
                    <h3 className="text-2xl font-bold text-white">Ankurshala</h3>
                    <p className="text-white/70">On Demand Learning</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        {/* Wave divider */}
        <div className="absolute bottom-0 left-0 right-0">
          <svg viewBox="0 0 1440 120" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M0 120L60 105C120 90 240 60 360 45C480 30 600 30 720 37.5C840 45 960 60 1080 67.5C1200 75 1320 75 1380 75L1440 75V120H1380C1320 120 1200 120 1080 120C960 120 840 120 720 120C600 120 480 120 360 120C240 120 120 120 60 120H0Z" fill="white"/>
          </svg>
        </div>
      </section>

      {/* FEATURES SECTION */}
      <section id="features" className="py-20 bg-transparent">
        <div className="container mx-auto px-6">
          <div className="text-center mb-16">
            <h2 className="text-3xl md:text-4xl font-display font-semibold text-ankur-secondary dark:text-white mb-4">
              Why Choose Ankurshala?
            </h2>
            <p className="text-gray-600 text-lg max-w-2xl mx-auto dark:text-gray-300">
              Experience personalized learning with cutting-edge technology and expert educators
            </p>
          </div>
          
          <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-8">
            {features.map((feature) => (
              <div 
                key={feature.title}
                className="group p-6 rounded-3xl border border-white/40 hover:border-ankur-primary/20 hover:shadow-lg transition-all duration-300 bg-white/80 backdrop-blur dark:bg-slate-900/70 dark:border-white/10"
              >
                <div className={`w-14 h-14 rounded-xl ${feature.color} flex items-center justify-center mb-4 group-hover:scale-110 transition-transform`}>
                  <feature.icon className="h-7 w-7" />
                </div>
                <h3 className="text-xl font-semibold text-ankur-secondary dark:text-white mb-2">{feature.title}</h3>
                <p className="text-gray-600 dark:text-gray-300">{feature.description}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* SUBJECTS SECTION */}
      <section id="subjects" className="py-20 bg-transparent">
        <div className="container mx-auto px-6">
          <div className="text-center mb-16">
            <h2 className="text-3xl md:text-4xl font-display font-semibold text-ankur-secondary dark:text-white mb-4">
              Popular Subjects
            </h2>
            <p className="text-gray-600 text-lg max-w-2xl mx-auto dark:text-gray-300">
              Master any subject with expert guidance from our verified teachers
            </p>
          </div>
          
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            {subjects.map((subject) => (
              <div 
                key={subject.name}
                className="group bg-white/80 rounded-2xl p-6 text-center hover:shadow-lg hover:border-ankur-primary/20 border border-white/50 transition-all duration-300 cursor-pointer backdrop-blur dark:bg-slate-900/70 dark:border-white/10"
              >
                <span className="mx-auto mb-3 flex h-12 w-12 items-center justify-center rounded-2xl bg-ankur-primary/10 text-ankur-primary font-semibold group-hover:scale-110 transition-transform">
                  {subject.code}
                </span>
                <span className="font-medium text-ankur-secondary dark:text-white">{subject.name}</span>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* BOARDS SECTION */}
      <section id="boards" className="py-20 bg-transparent">
        <div className="container mx-auto px-6">
          <div className="brand-gradient rounded-3xl p-8 md:p-12 text-center">
            <BookOpen className="h-12 w-12 text-white/80 mx-auto mb-6" />
            <h2 className="text-3xl md:text-4xl font-bold text-white mb-4">
              All Major Boards Covered
            </h2>
            <p className="text-white/80 text-lg max-w-2xl mx-auto mb-8">
              Our expert teachers are certified to teach across all major educational boards in India and internationally
            </p>
            <div className="flex flex-wrap justify-center gap-4">
              {boards.map((board) => (
                <span 
                  key={board}
                  className="bg-white/10 backdrop-blur border border-white/20 px-6 py-3 rounded-full text-white font-medium"
                >
                  {board}
                </span>
              ))}
            </div>
          </div>
        </div>
      </section>

      {/* CTA SECTION */}
      <section className="py-20 bg-transparent">
        <div className="container mx-auto px-6">
          <div className="max-w-4xl mx-auto text-center">
            <h2 className="text-3xl md:text-4xl font-display font-semibold text-ankur-secondary dark:text-white mb-4">
              Ready to Transform Your Learning?
            </h2>
            <p className="text-gray-600 text-lg mb-8 dark:text-gray-300">
              Join thousands of students achieving their academic goals with Ankurshala
            </p>
            <div className="flex flex-col sm:flex-row gap-4 justify-center">
              <Button size="lg" className="btn-primary text-base px-8" asChild>
                <Link href="/register-student">
                  <GraduationCap className="mr-2 h-5 w-5" />
                  Start as Student
                </Link>
              </Button>
              <Button size="lg" variant="outline" className="border-ankur-secondary text-ankur-secondary hover:bg-ankur-secondary/5 text-base px-8 dark:text-white dark:border-white/20" asChild>
                <Link href="/register-teacher">
                  <Users className="mr-2 h-5 w-5" />
                  Become a Teacher
                </Link>
              </Button>
            </div>
          </div>
        </div>
      </section>

      {/* FOOTER */}
      <footer className="bg-ankur-secondary text-white py-12">
        <div className="container mx-auto px-6">
          <div className="flex flex-col md:flex-row items-center justify-between gap-6">
            <div className="flex items-center gap-3">
              <Image src="/ankurshala-logo-small.png" width={48} height={48} alt="Ankurshala" className="rounded-lg" />
              <div>
                <h3 className="font-bold text-xl">Ankurshala</h3>
                <p className="text-white/70 text-sm">On Demand Learning</p>
              </div>
            </div>
            <div className="flex gap-6 text-white/70">
              <Link href="/login" className="hover:text-white transition-colors">Login</Link>
              <Link href="/register-student" className="hover:text-white transition-colors">Student Signup</Link>
              <Link href="/register-teacher" className="hover:text-white transition-colors">Teacher Signup</Link>
            </div>
          </div>
          <div className="border-t border-white/10 mt-8 pt-8 text-center text-white/60 text-sm">
            Copyright {new Date().getFullYear()} Ankurshala. All rights reserved.
          </div>
        </div>
      </footer>

      {/* Mobile floating bar */}
      <div className="fixed inset-x-0 bottom-4 flex justify-center px-4 md:hidden z-50">
        <div className="flex gap-2 rounded-full bg-ankur-secondary/95 backdrop-blur shadow-lg px-4 py-3">
          <Link href="/login" className="rounded-full bg-white/10 px-4 py-2 text-sm text-white">Login</Link>
          <Link href="/register-student" className="rounded-full bg-ankur-primary px-4 py-2 text-sm text-white">Student</Link>
          <Link href="/register-teacher" className="rounded-full bg-white/10 px-4 py-2 text-sm text-white">Teacher</Link>
        </div>
      </div>
    </main>
  )
}
