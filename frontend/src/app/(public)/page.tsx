'use client'

import Link from 'next/link'
import Image from 'next/image'
import { Button } from '@/components/ui/button'
import { Card, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { BookOpen, Clock, GraduationCap, Moon, ShieldCheck, Sparkles, Star, Sun, Users } from 'lucide-react'
import { useTheme } from '@/components/theme-provider'

export default function PublicLandingPage() {
  const { theme, setTheme } = useTheme()
  const toggleTheme = () => setTheme(theme === 'dark' ? 'light' : 'dark')

  const highlights = [
    { label: 'Live Sessions', value: '1:1', icon: Users },
    { label: 'Teacher Rating', value: '4.9/5', icon: Star },
    { label: 'Lesson Booking', value: 'Instant', icon: Clock },
  ]

  const features = [
    {
      title: 'Verified Educators',
      description: 'Every teacher is vetted for credentials, pedagogy, and student feedback.',
      icon: ShieldCheck,
    },
    {
      title: 'AI Learning Companion',
      description: 'Personalized plans, practice prompts, and recap notes for every session.',
      icon: Sparkles,
    },
    {
      title: 'Goal Focused Paths',
      description: 'Custom study journeys mapped to boards, exams, and milestones.',
      icon: GraduationCap,
    },
  ]

  return (
    <div className="min-h-screen bg-transparent text-foreground">
      <header className="sticky top-0 z-40 border-b border-white/30 bg-white/70 backdrop-blur-2xl dark:bg-slate-900/70 dark:border-white/10">
        <div className="container mx-auto px-6 py-4 flex items-center justify-between">
          <Link href="/" className="flex items-center gap-3">
            <Image src="/ankurshala-logo-small.png" alt="Ankurshala" width={40} height={40} className="rounded-lg" />
            <span className="text-xl font-display font-semibold text-ankur-secondary dark:text-white">Ankurshala</span>
          </Link>
          <nav className="hidden md:flex items-center gap-8 text-sm">
            <Link href="#features" className="text-gray-600 hover:text-ankur-primary dark:text-gray-200">Features</Link>
            <Link href="#paths" className="text-gray-600 hover:text-ankur-primary dark:text-gray-200">Learning Paths</Link>
            <Link href="#cta" className="text-gray-600 hover:text-ankur-primary dark:text-gray-200">Get Started</Link>
          </nav>
          <div className="flex items-center gap-3">
            <Button variant="ghost" size="icon" onClick={toggleTheme} className="text-gray-600 hover:text-ankur-primary dark:text-gray-200">
              {theme === 'dark' ? <Sun className="h-5 w-5" /> : <Moon className="h-5 w-5" />}
            </Button>
            <Button variant="ghost" asChild className="hidden sm:inline-flex text-gray-600 hover:text-ankur-primary dark:text-gray-200">
              <Link href="/login">Login</Link>
            </Button>
            <Button className="btn-primary" asChild>
              <Link href="/register-student">Get Started</Link>
            </Button>
          </div>
        </div>
      </header>

      <section className="relative overflow-hidden">
        <div className="absolute inset-0 brand-gradient" />
        <div className="absolute inset-0 bg-[url('/grid.svg')] opacity-10" />
        <div className="relative container mx-auto px-6 py-20">
          <div className="grid lg:grid-cols-2 gap-12 items-center">
            <div className="text-center lg:text-left text-white">
              <div className="inline-flex items-center gap-2 bg-white/10 rounded-full px-4 py-2 text-sm mb-6 backdrop-blur">
                <Sparkles className="h-4 w-4 text-ankur-accent" />
                <span>On demand learning for real outcomes</span>
              </div>
              <h1 className="text-4xl md:text-5xl font-display font-semibold leading-tight mb-4">
                Build confidence with
                <span className="block text-ankur-accent">personalized teacher sessions</span>
              </h1>
              <p className="text-lg text-white/80 mb-8 max-w-xl mx-auto lg:mx-0">
                Book classes in minutes, get AI-guided study plans, and track progress across every topic.
              </p>
              <div className="flex flex-col sm:flex-row gap-4 justify-center lg:justify-start">
                <Button size="lg" className="btn-primary text-base px-8" asChild>
                  <Link href="/register-student">Start as Student</Link>
                </Button>
                <Button size="lg" variant="outline" className="border-white/30 text-white hover:bg-white/10 text-base px-8" asChild>
                  <Link href="/register-teacher">Teach with Ankurshala</Link>
                </Button>
              </div>
            </div>
            <div className="relative">
              <div className="absolute -top-10 -left-8 w-72 h-72 bg-ankur-primary/30 rounded-full blur-3xl" />
              <div className="absolute -bottom-10 -right-8 w-72 h-72 bg-ankur-accent/30 rounded-full blur-3xl" />
              <div className="relative glass-panel rounded-3xl p-8 border border-white/40">
                <div className="grid gap-4">
                  {highlights.map((item) => (
                    <div key={item.label} className="flex items-center justify-between rounded-2xl bg-white/70 px-5 py-4 text-ankur-secondary backdrop-blur dark:bg-slate-900/70 dark:text-white">
                      <div className="flex items-center gap-3">
                        <div className="w-10 h-10 rounded-xl bg-ankur-primary/10 text-ankur-primary flex items-center justify-center">
                          <item.icon className="h-5 w-5" />
                        </div>
                        <div>
                          <p className="text-sm text-gray-500 dark:text-gray-300">{item.label}</p>
                          <p className="text-lg font-semibold">{item.value}</p>
                        </div>
                      </div>
                      <span className="text-xs text-ankur-secondary/70 dark:text-white/70">Live</span>
                    </div>
                  ))}
                </div>
                <div className="mt-6 flex items-center gap-3 text-sm text-gray-600 dark:text-gray-300">
                  <BookOpen className="h-5 w-5 text-ankur-primary" />
                  Track every lesson with notes, outcomes, and practice links.
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <section id="features" className="py-20 bg-transparent">
        <div className="container mx-auto px-6">
          <div className="text-center mb-12">
            <h2 className="text-3xl md:text-4xl font-display font-semibold text-ankur-secondary dark:text-white">
              Why learners choose Ankurshala
            </h2>
            <p className="text-gray-600 dark:text-gray-300 mt-3 max-w-2xl mx-auto">
              Modern classrooms, trusted teachers, and smart insights for every student.
            </p>
          </div>
          <div className="grid md:grid-cols-3 gap-6">
            {features.map((feature) => (
              <Card key={feature.title} className="glass-panel rounded-3xl border border-white/40 hover:border-ankur-primary/30 transition-all">
                <CardHeader className="gap-3">
                  <div className="w-12 h-12 rounded-xl bg-ankur-secondary/10 text-ankur-secondary flex items-center justify-center">
                    <feature.icon className="h-6 w-6" />
                  </div>
                  <CardTitle className="text-xl text-ankur-secondary dark:text-white">{feature.title}</CardTitle>
                  <CardDescription className="text-gray-600 dark:text-gray-300">
                    {feature.description}
                  </CardDescription>
                </CardHeader>
              </Card>
            ))}
          </div>
        </div>
      </section>

      <section id="paths" className="py-20 bg-transparent">
        <div className="container mx-auto px-6">
          <div className="grid lg:grid-cols-2 gap-10 items-center">
            <div className="glass-panel rounded-3xl p-8 border border-white/40">
              <p className="text-sm text-ankur-primary font-semibold mb-3">Learning Paths</p>
              <h3 className="text-2xl font-display font-semibold text-ankur-secondary dark:text-white mb-4">
                Plans tailored to your board, goals, and timeline
              </h3>
              <p className="text-gray-600 dark:text-gray-300 mb-6">
                Create a schedule that fits your rhythm. Teachers align every lesson to outcomes and track progress along the way.
              </p>
              <div className="flex flex-wrap gap-3">
                {['CBSE', 'ICSE', 'IB', 'State Boards', 'Cambridge'].map((board) => (
                  <span key={board} className="rounded-full bg-white/70 px-4 py-2 text-sm text-ankur-secondary border border-white/50 dark:bg-slate-900/70 dark:text-white dark:border-white/10">
                    {board}
                  </span>
                ))}
              </div>
            </div>
            <div className="grid gap-4">
              {[
                { title: 'Choose a Subject', detail: 'Pick a topic and set your availability.' },
                { title: 'Match with a Teacher', detail: 'Verified mentors accept your request quickly.' },
                { title: 'Learn, Review, Improve', detail: 'Get AI summaries and practice tasks.' },
              ].map((step, index) => (
                <div key={step.title} className="glass rounded-2xl p-5 border border-white/40 hover-lift">
                  <div className="flex items-start gap-4">
                    <div className="w-10 h-10 rounded-xl bg-ankur-primary/10 text-ankur-primary flex items-center justify-center font-semibold">
                      {index + 1}
                    </div>
                    <div>
                      <h4 className="text-lg font-semibold text-ankur-secondary dark:text-white">{step.title}</h4>
                      <p className="text-gray-600 dark:text-gray-300">{step.detail}</p>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </section>

      <section id="cta" className="py-20 bg-transparent">
        <div className="container mx-auto px-6">
          <div className="brand-gradient rounded-3xl p-10 text-center text-white shadow-xl">
            <h2 className="text-3xl md:text-4xl font-display font-semibold mb-3">Ready to start your journey?</h2>
            <p className="text-white/80 max-w-2xl mx-auto mb-8">
              Join learners and teachers building the future of education on Ankurshala.
            </p>
            <div className="flex flex-col sm:flex-row gap-4 justify-center">
              <Button size="lg" className="btn-primary text-base px-8" asChild>
                <Link href="/register-student">Create Student Account</Link>
              </Button>
              <Button size="lg" variant="outline" className="border-white/40 text-white hover:bg-white/10 text-base px-8" asChild>
                <Link href="/register-teacher">Become a Teacher</Link>
              </Button>
            </div>
          </div>
        </div>
      </section>

      <footer className="bg-ankur-secondary text-white py-12">
        <div className="container mx-auto px-6">
          <div className="grid md:grid-cols-4 gap-8">
            <div>
              <div className="flex items-center gap-3 mb-4">
                <Image src="/ankurshala-logo-small.png" alt="Ankurshala" width={40} height={40} className="rounded-lg" />
                <span className="text-xl font-semibold">Ankurshala</span>
              </div>
              <p className="text-white/70">
                Personalized 1-on-1 learning built for students, parents, and modern educators.
              </p>
            </div>
            <div>
              <h3 className="font-semibold mb-4">For Students</h3>
              <ul className="space-y-2 text-white/70">
                <li><Link href="/register-student" className="hover:text-white">Sign up</Link></li>
                <li><Link href="/login" className="hover:text-white">Login</Link></li>
                <li><Link href="/student/dashboard" className="hover:text-white">Dashboard</Link></li>
              </ul>
            </div>
            <div>
              <h3 className="font-semibold mb-4">For Teachers</h3>
              <ul className="space-y-2 text-white/70">
                <li><Link href="/register-teacher" className="hover:text-white">Teach with us</Link></li>
                <li><Link href="/teacher/dashboard" className="hover:text-white">Teacher dashboard</Link></li>
              </ul>
            </div>
            <div>
              <h3 className="font-semibold mb-4">Support</h3>
              <ul className="space-y-2 text-white/70">
                <li><Link href="#" className="hover:text-white">Help center</Link></li>
                <li><Link href="#" className="hover:text-white">Contact us</Link></li>
                <li><Link href="#" className="hover:text-white">Privacy policy</Link></li>
              </ul>
            </div>
          </div>
          <div className="border-t border-white/10 mt-8 pt-6 text-center text-white/60">
            Copyright {new Date().getFullYear()} Ankurshala. All rights reserved.
          </div>
        </div>
      </footer>
    </div>
  )
}
